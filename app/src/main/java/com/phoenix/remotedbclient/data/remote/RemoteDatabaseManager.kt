package com.phoenix.remotedbclient.data.remote

import com.phoenix.remotedbclient.data.db.ConnectionConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages JDBC connections to remote MySQL and PostgreSQL databases.
 *
 * Each [RemoteDatabaseManager] instance wraps a single [Connection].
 * All JDBC operations run on [Dispatchers.IO] to avoid blocking the main thread.
 * Call [close] when done to release the underlying connection.
 */
@Singleton
class RemoteDatabaseManager @Inject constructor() {

    /** The active JDBC connection, null if not connected. */
    @Volatile
    private var connection: Connection? = null

    /** Metadata about the currently connected database. */
    @Volatile
    var connectedConfig: ConnectionConfigEntity? = null
        private set

    /** Whether this manager currently holds an open connection. */
    val isConnected: Boolean
        get() = connection != null && !connection!!.isClosed

    /**
     * Opens a JDBC connection using the given [config] and [password].
     *
     * @throws SQLException if the connection cannot be established.
     */
    suspend fun connect(config: ConnectionConfigEntity, password: String) = withContext(Dispatchers.IO) {
        close() // ensure any prior connection is released

        val url = buildJdbcUrl(config)
        val props = Properties().apply {
            setProperty("user", config.username)
            setProperty("password", password)
            // Connection timeouts
            setProperty("connectTimeout", "10000")
            setProperty("socketTimeout", "30000")
        }

        // Load the appropriate JDBC driver class
        loadDriver(config.dbType)

        connection = DriverManager.getConnection(url, props)
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
                val url = buildJdbcUrl(config)
                val props = Properties().apply {
                    setProperty("user", config.username)
                    setProperty("password", password)
                    setProperty("connectTimeout", "10000")
                }
                loadDriver(config.dbType)
                val testConn = DriverManager.getConnection(url, props)
                if (testConn.isValid(5)) {
                    testConn.close()
                    null // success
                } else {
                    testConn.close()
                    "Connection validation failed"
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
            val conn = connection ?: return@withContext null
            val config = connectedConfig ?: return@withContext null
            val tables = mutableListOf<TableInfo>()

            when (config.dbType) {
                "MySQL" -> {
                    // MySQL: use SHOW TABLES or information_schema
                    val stmt = conn.createStatement()
                    val rs = stmt.executeQuery(
                        "SELECT TABLE_NAME, TABLE_TYPE FROM information_schema.TABLES " +
                            "WHERE TABLE_SCHEMA = '${escapeSql(config.defaultDatabase)}' " +
                            "ORDER BY TABLE_NAME"
                    )
                    while (rs.next()) {
                        tables.add(
                            TableInfo(
                                name = rs.getString("TABLE_NAME"),
                                type = rs.getString("TABLE_TYPE") // BASE TABLE or VIEW
                            )
                        )
                    }
                    rs.close()
                    stmt.close()
                }
                "PostgreSQL" -> {
                    val stmt = conn.createStatement()
                    val rs = stmt.executeQuery(
                        "SELECT table_name, table_type FROM information_schema.tables " +
                            "WHERE table_schema NOT IN ('pg_catalog', 'information_schema') " +
                            "ORDER BY table_name"
                    )
                    while (rs.next()) {
                        val tableType = rs.getString("table_type")
                        tables.add(
                            TableInfo(
                                name = rs.getString("table_name"),
                                type = if (tableType == "VIEW") "VIEW" else "BASE TABLE"
                            )
                        )
                    }
                    rs.close()
                    stmt.close()
                }
            }
            tables
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
            val conn = connection ?: return@withContext null
            val config = connectedConfig ?: return@withContext null
            val columns = mutableListOf<ColumnInfo>()

            when (config.dbType) {
                "MySQL" -> {
                    val stmt = conn.createStatement()
                    val rs = stmt.executeQuery(
                        "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_KEY, IS_NULLABLE, ORDINAL_POSITION " +
                            "FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = '${escapeSql(config.defaultDatabase)}' " +
                            "AND TABLE_NAME = '${escapeSql(tableName)}' " +
                            "ORDER BY ORDINAL_POSITION"
                    )
                    while (rs.next()) {
                        columns.add(
                            ColumnInfo(
                                name = rs.getString("COLUMN_NAME"),
                                dataType = rs.getString("DATA_TYPE"),
                                isPrimaryKey = "PRI" == rs.getString("COLUMN_KEY"),
                                isNullable = "YES" == rs.getString("IS_NULLABLE"),
                                ordinalPosition = rs.getInt("ORDINAL_POSITION")
                            )
                        )
                    }
                    rs.close()
                    stmt.close()
                }
                "PostgreSQL" -> {
                    val stmt = conn.createStatement()
                    val rs = stmt.executeQuery(
                        "SELECT c.column_name, c.data_type, " +
                            "CASE WHEN pk.column_name IS NOT NULL THEN true ELSE false END AS is_pk, " +
                            "c.is_nullable, c.ordinal_position " +
                            "FROM information_schema.columns c " +
                            "LEFT JOIN (SELECT ku.table_schema, ku.table_name, ku.column_name " +
                            "  FROM information_schema.table_constraints tc " +
                            "  JOIN information_schema.key_column_usage ku " +
                            "    ON tc.constraint_name = ku.constraint_name " +
                            "  WHERE tc.constraint_type = 'PRIMARY KEY') pk " +
                            "  ON c.table_schema = pk.table_schema " +
                            "  AND c.table_name = pk.table_name " +
                            "  AND c.column_name = pk.column_name " +
                            "WHERE c.table_schema NOT IN ('pg_catalog', 'information_schema') " +
                            "AND c.table_name = '${escapeSql(tableName)}' " +
                            "ORDER BY c.ordinal_position"
                    )
                    while (rs.next()) {
                        columns.add(
                            ColumnInfo(
                                name = rs.getString("column_name"),
                                dataType = rs.getString("data_type"),
                                isPrimaryKey = rs.getBoolean("is_pk"),
                                isNullable = "YES" == rs.getString("is_nullable"),
                                ordinalPosition = rs.getInt("ordinal_position")
                            )
                        )
                    }
                    rs.close()
                    stmt.close()
                }
            }
            columns
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
        limit: Int = 50
    ): QueryResult = withContext(Dispatchers.IO) {
        try {
            val conn = connection ?: return@withContext QueryResult.error("Not connected")
            val config = connectedConfig ?: return@withContext QueryResult.error("No config")
            val colList = if (columns.isEmpty()) "*" else columns.joinToString(", ") {
                "`$it`".takeIf { config.dbType == "MySQL" } ?: "\"$it\"".takeIf { config.dbType == "PostgreSQL" } ?: it
            }

            val tableRef = when (config.dbType) {
                "MySQL" -> "`${escapeSql(tableName)}`"
                "PostgreSQL" -> "\"${escapeSql(tableName)}\""
                else -> escapeSql(tableName)
            }

            val sql: String
            when (config.dbType) {
                "PostgreSQL" -> sql = "SELECT $colList FROM $tableRef LIMIT $limit OFFSET $offset"
                else -> sql = "SELECT $colList FROM $tableRef LIMIT $offset, $limit"
            }

            executeSelectInternal(conn, sql)
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
            val conn = connection ?: return@withContext QueryResult.error("Not connected")
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
                executeSelectInternal(conn, trimmed)
            } else {
                val stmt = conn.createStatement()
                val affected = stmt.executeUpdate(trimmed)
                stmt.close()
                QueryResult.updateResult(affected)
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
            val conn = connection ?: return@withContext 0
            val config = connectedConfig ?: return@withContext 0
            val tableRef = when (config.dbType) {
                "MySQL" -> "`${escapeSql(tableName)}`"
                "PostgreSQL" -> "\"${escapeSql(tableName)}\""
                else -> escapeSql(tableName)
            }
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery("SELECT COUNT(*) FROM $tableRef")
            rs.next()
            val count = rs.getInt(1)
            rs.close()
            stmt.close()
            count
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
            val conn = connection ?: return@withContext -1
            val config = connectedConfig ?: return@withContext -1

            val colQuote = when (config.dbType) {
                "MySQL" -> "`"
                "PostgreSQL" -> "\""
                else -> ""
            }
            val tableRef = "$colQuote${escapeSql(tableName)}$colQuote"
            val colRef = "$colQuote${escapeSql(columnName)}$colQuote"

            val whereClause = primaryKeyCols.joinToString(" AND ") {
                "$colQuote${escapeSql(it)}$colQuote = ?"
            }

            val sql = "UPDATE $tableRef SET $colRef = ? WHERE $whereClause"
            val pstmt = conn.prepareStatement(sql)
            pstmt.setString(1, newValue)
            primaryKeyVals.forEachIndexed { idx, value ->
                pstmt.setString(idx + 2, value)
            }
            val affected = pstmt.executeUpdate()
            pstmt.close()
            affected
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
            val conn = connection ?: return@withContext -1
            val config = connectedConfig ?: return@withContext -1

            val colQuote = when (config.dbType) {
                "MySQL" -> "`"
                "PostgreSQL" -> "\""
                else -> ""
            }
            val tableRef = "$colQuote${escapeSql(tableName)}$colQuote"

            val whereClause = primaryKeyCols.joinToString(" AND ") {
                "$colQuote${escapeSql(it)}$colQuote = ?"
            }

            val sql = "DELETE FROM $tableRef WHERE $whereClause"
            val pstmt = conn.prepareStatement(sql)
            primaryKeyVals.forEachIndexed { idx, value ->
                pstmt.setString(idx + 1, value)
            }
            val affected = pstmt.executeUpdate()
            pstmt.close()
            affected
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
            val conn = connection ?: return@withContext -1
            val config = connectedConfig ?: return@withContext -1

            val colQuote = when (config.dbType) {
                "MySQL" -> "`"
                "PostgreSQL" -> "\""
                else -> ""
            }
            val tableRef = "$colQuote${escapeSql(tableName)}$colQuote"

            val columns = columnValues.keys.joinToString(", ") { "$colQuote${escapeSql(it)}$colQuote" }
            val placeholders = columnValues.keys.joinToString(", ") { "?" }
            val sql = "INSERT INTO $tableRef ($columns) VALUES ($placeholders)"

            val pstmt = conn.prepareStatement(sql)
            columnValues.values.forEachIndexed { idx, value ->
                pstmt.setString(idx + 1, value)
            }
            val affected = pstmt.executeUpdate()
            pstmt.close()
            affected
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
        val stmt = conn.createStatement()
        stmt.queryTimeout = 30
        val rs = stmt.executeQuery(sql)
        val meta = rs.metaData
        val columnCount = meta.columnCount
        val columns = (1..columnCount).map { meta.getColumnName(it) }
        val rows = mutableListOf<List<String?>>()

        while (rs.next()) {
            val row = (1..columnCount).map { idx ->
                rs.getString(idx)
            }
            rows.add(row)
        }

        rs.close()
        stmt.close()
        return QueryResult.selectResult(columns, rows)
    }

    /** Builds the JDBC connection URL for the given config. */
    private fun buildJdbcUrl(config: ConnectionConfigEntity): String {
        val dbName = config.defaultDatabase.ifEmpty { "" }
        return when (config.dbType) {
            "MySQL" -> {
                val params = "?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=10000&socketTimeout=30000"
                "jdbc:mysql://${config.host}:${config.port}/${dbName}$params"
            }
            "PostgreSQL" -> {
                "jdbc:postgresql://${config.host}:${config.port}/${dbName}?connectTimeout=10&socketTimeout=30"
            }
            else -> throw IllegalArgumentException("Unsupported database type: ${config.dbType}")
        }
    }

    /** Ensures the JDBC driver class is loaded. */
    private fun loadDriver(dbType: String) {
        when (dbType) {
            "MySQL" -> Class.forName("com.mysql.cj.jdbc.Driver")
            "PostgreSQL" -> Class.forName("org.postgresql.Driver")
        }
    }

    /** Basic SQL identifier escaping to prevent injection in metadata queries. */
    private fun escapeSql(input: String): String {
        return input.replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("`", "\\`")
    }
}
