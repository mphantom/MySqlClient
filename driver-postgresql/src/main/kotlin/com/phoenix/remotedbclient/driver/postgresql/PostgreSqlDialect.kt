package com.phoenix.remotedbclient.driver.postgresql

import com.phoenix.remotedbclient.driver.AbstractJdbcDialect
import com.phoenix.remotedbclient.driver.ColumnInfo
import com.phoenix.remotedbclient.driver.TableInfo
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Types

/**
 * 职责：PostgreSQL 方言——JDBC URL、驱动类、information_schema 元数据、
 * 双引号标识符与 LIMIT ... OFFSET ... 分页。
 * 位置：driver-postgresql 模块。
 */
class PostgreSqlDialect : AbstractJdbcDialect() {

    override val id: String = "PostgreSQL"
    override val displayName: String = "PostgreSQL"
    override val defaultPort: Int = 5432
    override val driverClassName: String = "org.postgresql.Driver"

    override val identifierQuote: Char = '"'

    override fun buildJdbcUrl(host: String, port: Int, database: String): String =
        "jdbc:postgresql://$host:$port/$database?connectTimeout=10&socketTimeout=30"

    override fun paginationClause(limit: Int, offset: Int): String =
        "LIMIT $limit OFFSET $offset"

    /**
     * PostgreSQL 不会把 varchar 隐式转换为 integer 等类型。
     * Types.OTHER 让驱动发送未指定类型的参数，由服务端按目标列类型推断。
     */
    override fun bindValue(statement: PreparedStatement, index: Int, value: String?) {
        statement.setObject(index, value, Types.OTHER)
    }

    override fun listTables(connection: Connection, database: String): List<TableInfo> {
        val tables = mutableListOf<TableInfo>()
        connection.createStatement().use { statement ->
            statement.executeQuery(
                "SELECT table_name, table_type FROM information_schema.tables " +
                    "WHERE table_schema NOT IN ('pg_catalog', 'information_schema') " +
                    "ORDER BY table_name"
            ).use { resultSet ->
                while (resultSet.next()) {
                    val tableType = resultSet.getString("table_type")
                    tables.add(
                        TableInfo(
                            name = resultSet.getString("table_name"),
                            type = if (tableType == "VIEW") "VIEW" else "BASE TABLE"
                        )
                    )
                }
            }
        }
        return tables
    }

    override fun listColumns(
        connection: Connection,
        database: String,
        table: String
    ): List<ColumnInfo> {
        val columns = mutableListOf<ColumnInfo>()
        connection.prepareStatement(
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
                "AND c.table_name = ? " +
                "ORDER BY c.ordinal_position"
        ).use { statement ->
            statement.setString(1, table)
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    columns.add(
                        ColumnInfo(
                            name = resultSet.getString("column_name"),
                            dataType = resultSet.getString("data_type"),
                            isPrimaryKey = resultSet.getBoolean("is_pk"),
                            isNullable = "YES" == resultSet.getString("is_nullable"),
                            ordinalPosition = resultSet.getInt("ordinal_position")
                        )
                    )
                }
            }
        }
        return columns
    }
}
