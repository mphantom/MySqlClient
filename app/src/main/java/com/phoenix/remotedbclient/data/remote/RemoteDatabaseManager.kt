package com.phoenix.remotedbclient.data.remote

import com.phoenix.remotedbclient.data.db.ConnectionConfigEntity
import com.phoenix.remotedbclient.driver.ColumnInfo
import com.phoenix.remotedbclient.driver.DriverRegistry
import com.phoenix.remotedbclient.driver.QueryResult
import com.phoenix.remotedbclient.driver.TableInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Properties

/**
 * Manages JDBC connections to remote databases.
 *
 * Each [RemoteDatabaseManager] instance wraps a single [Connection].
 * All JDBC operations run on [Dispatchers.IO] to avoid blocking the main thread.
 * Call [close] when done to release the underlying connection.
 *
 * [改动前] 通过 when (dbType) 硬编码 MySQL/PostgreSQL 的 URL、驱动类、元数据 SQL、
 *          标识符引号与分页语法，新增数据库必须改这个类
 * [改动后] 从 [DriverRegistry] 取出 [com.phoenix.remotedbclient.driver.DatabaseDialect]，
 *          所有数据库差异委托给方言实现；本类只负责连接生命周期与异常映射
 * [原因] 让新增数据库只需新增驱动模块，不再修改核心连接与查询代码
 */
class RemoteDatabaseManager(private val driverRegistry: DriverRegistry) : DatabaseConnector {

    /** The active JDBC connection, null if not connected. */
    @Volatile
    private var connection: Connection? = null

    /** Metadata about the currently connected database. */
    @Volatile
    var connectedConfig: ConnectionConfigEntity? = null
        private set

    /** Whether this manager currently holds an open connection. */
    override val isConnected: Boolean
        get() = connection != null && !connection!!.isClosed

    /**
     * Opens a JDBC connection using the given [config] and [password].
     *
     * @throws SQLException if the connection cannot be established.
     */
    override suspend fun connect(config: ConnectionConfigEntity, password: String) = withContext(Dispatchers.IO) {
        close() // ensure any prior connection is released

        val dialect = driverRegistry.get(config.dbType)
        val props = Properties().apply {
            setProperty("user", config.username)
            setProperty("password", password)
            // Connection timeouts
            setProperty("connectTimeout", "10000")
            setProperty("socketTimeout", "30000")
        }

        // Load the driver declared by the dialect
        Class.forName(dialect.driverClassName)

        connection = DriverManager.getConnection(
            dialect.buildJdbcUrl(config.host, config.port, config.defaultDatabase),
            props
        )
        connectedConfig = config
    }

    /**
     * Tests whether a connection configuration is valid by attempting to connect
     * and immediately closing.
     *
     * @return null on success, or an error message string on failure.
     */
    suspend fun testConnection(config: ConnectionConfigEntity, password: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val dialect = driverRegistry.get(config.dbType)
                val props = Properties().apply {
                    setProperty("user", config.username)
                    setProperty("password", password)
                    setProperty("connectTimeout", "10000")
                }
                Class.forName(dialect.driverClassName)
                DriverManager.getConnection(
                    dialect.buildJdbcUrl(config.host, config.port, config.defaultDatabase),
                    props
                ).use { testConnection ->
                    if (testConnection.isValid(5)) {
                        null // success
                    } else {
                        "Connection validation failed"
                    }
                }
            } catch (e: SQLException) {
                e.message ?: "Unknown database error"
            } catch (e: Exception) {
                e.message ?: "Unknown error"
            }
        }

    /**
     * Retrieves all table and view names from the current database.
     *
     * @return List of [TableInfo] objects, or null if not connected/on error.
     */
    suspend fun getTables(): List<TableInfo>? = withContext(Dispatchers.IO) {
        try {
            val activeConnection = connection ?: return@withContext null
            val config = connectedConfig ?: return@withContext null
            driverRegistry.get(config.dbType)
                .listTables(activeConnection, config.defaultDatabase)
        } catch (e: SQLException) {
            null
        }
    }

    /**
     * Returns column metadata for the specified [tableName].
     *
     * @return List of [ColumnInfo], or null on error.
     */
    suspend fun getColumns(tableName: String): List<ColumnInfo>? = withContext(Dispatchers.IO) {
        try {
            val activeConnection = connection ?: return@withContext null
            val config = connectedConfig ?: return@withContext null
            driverRegistry.get(config.dbType)
                .listColumns(activeConnection, config.defaultDatabase, tableName)
        } catch (e: SQLException) {
            null
        }
    }

    /**
     * Executes a SELECT query with pagination support.
     *
     * @param tableName The table to query.
     * @param columns List of column names to select.
     * @param offset Zero-based row offset.
     * @param limit Maximum number of rows to return.
     * @return [QueryResult] with columns and rows, or error info.
     */
    suspend fun selectRows(
        tableName: String,
        columns: List<String>,
        offset: Int = 0,
        limit: Int = 50,
        orderBy: String? = null,
        ascending: Boolean = true
    ): QueryResult = withContext(Dispatchers.IO) {
        try {
            val activeConnection = connection ?: return@withContext QueryResult.error("Not connected")
            val config = connectedConfig ?: return@withContext QueryResult.error("No config")
            driverRegistry.get(config.dbType)
                .selectRows(activeConnection, tableName, columns, offset, limit, orderBy, ascending)
        } catch (e: SQLException) {
            QueryResult.error(e.message ?: "Query failed")
        }
    }

    /**
     * Executes a raw SQL statement (SELECT or DML/DDL).
     *
     * @param sql The SQL to execute.
     * @return [QueryResult] with results, affected rows, or error info.
     */
    suspend fun executeRawQuery(sql: String): QueryResult = withContext(Dispatchers.IO) {
        try {
            val activeConnection = connection ?: return@withContext QueryResult.error("Not connected")
            val trimmed = sql.trim()

            // Determine if it's a query (SELECT / WITH / SHOW / DESCRIBE / EXPLAIN) or a mutation
            val isQuery = trimmed.uppercase().let { upper ->
                upper.startsWith("SELECT") ||
                    upper.startsWith("WITH") ||
                    upper.startsWith("SHOW") ||
                    upper.startsWith("DESCRIBE") ||
                    upper.startsWith("EXPLAIN") ||
                    upper.startsWith("DESC ")
            }

            if (isQuery) {
                executeSelectInternal(activeConnection, trimmed)
            } else {
                activeConnection.createStatement().use { statement ->
                    QueryResult.updateResult(statement.executeUpdate(trimmed))
                }
            }
        } catch (e: SQLException) {
            QueryResult.error(e.message ?: "Query failed")
        }
    }

    /**
     * Counts total rows in a table (for pagination).
     */
    suspend fun countRows(tableName: String): Int = withContext(Dispatchers.IO) {
        try {
            val activeConnection = connection ?: return@withContext 0
            val config = connectedConfig ?: return@withContext 0
            driverRegistry.get(config.dbType).countRows(activeConnection, tableName)
        } catch (e: SQLException) {
            0
        }
    }

    /**
     * Generates and executes an UPDATE statement for a single cell change.
     *
     * @param tableName Target table.
     * @param columnName Column to update.
     * @param newValue New value (as string; will be set via PreparedStatement).
     * @param primaryKeyCols List of primary key column names for the WHERE clause.
     * @param primaryKeyVals Corresponding values for the primary key columns.
     * @return Affected row count (should be 1 on success), or -1 on error.
     */
    suspend fun updateCell(
        tableName: String,
        columnName: String,
        newValue: String?,
        primaryKeyCols: List<String>,
        primaryKeyVals: List<String?>
    ): Int = withContext(Dispatchers.IO) {
        try {
            val activeConnection = connection ?: return@withContext -1
            val config = connectedConfig ?: return@withContext -1
            driverRegistry.get(config.dbType).updateCell(
                activeConnection,
                tableName,
                columnName,
                newValue,
                primaryKeyCols,
                primaryKeyVals
            )
        } catch (e: SQLException) {
            -1
        }
    }

    /**
     * Deletes a row identified by its primary key values.
     *
     * @return Affected row count, or -1 on error.
     */
    suspend fun deleteRow(
        tableName: String,
        primaryKeyCols: List<String>,
        primaryKeyVals: List<String?>
    ): Int = withContext(Dispatchers.IO) {
        try {
            val activeConnection = connection ?: return@withContext -1
            val config = connectedConfig ?: return@withContext -1
            driverRegistry.get(config.dbType)
                .deleteRow(activeConnection, tableName, primaryKeyCols, primaryKeyVals)
        } catch (e: SQLException) {
            -1
        }
    }

    /**
     * Inserts a new row into the table.
     *
     * @return Affected row count, or -1 on error.
     */
    suspend fun insertRow(
        tableName: String,
        columnValues: Map<String, String?>
    ): Int = withContext(Dispatchers.IO) {
        try {
            val activeConnection = connection ?: return@withContext -1
            val config = connectedConfig ?: return@withContext -1
            driverRegistry.get(config.dbType)
                .insertRow(activeConnection, tableName, columnValues)
        } catch (e: SQLException) {
            -1
        }
    }

    /**
     * Closes the underlying JDBC connection and releases resources.
     * Safe to call multiple times; no-op if already closed.
     */
    fun close() {
        try {
            connection?.close()
        } catch (_: Exception) {
            // Ignore close errors
        } finally {
            connection = null
            connectedConfig = null
        }
    }

    // --- Private helpers ---

    /** Internal helper to execute a SELECT and map rows. */
    private fun executeSelectInternal(conn: Connection, sql: String): QueryResult {
        conn.createStatement().use { statement ->
            statement.queryTimeout = 30
            statement.executeQuery(sql).use { resultSet ->
                val metaData = resultSet.metaData
                val columnCount = metaData.columnCount
                val columns = (1..columnCount).map { metaData.getColumnName(it) }
                val rows = mutableListOf<List<String?>>()

                while (resultSet.next()) {
                    rows.add((1..columnCount).map { resultSet.getString(it) })
                }

                return QueryResult.selectResult(columns, rows)
            }
        }
    }
}
