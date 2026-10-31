package com.phoenix.remotedbclient.driver.mysql

import com.phoenix.remotedbclient.driver.AbstractJdbcDialect
import com.phoenix.remotedbclient.driver.ColumnInfo
import com.phoenix.remotedbclient.driver.TableInfo
import java.sql.Connection

/**
 * 职责：MySQL 方言——JDBC URL、驱动类、information_schema 元数据、
 * 反引号标识符与 LIMIT offset,limit 分页。
 * 位置：driver-mysql 模块。
 */
class MySqlDialect : AbstractJdbcDialect() {

    override val id: String = "MySQL"
    override val displayName: String = "MySQL"
    override val defaultPort: Int = 3306
    override val driverClassName: String = "com.mysql.cj.jdbc.Driver"

    override val identifierQuote: Char = '`'

    override fun buildJdbcUrl(host: String, port: Int, database: String): String {
        val params =
            "?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=10000&socketTimeout=30000"
        return "jdbc:mysql://$host:$port/$database$params"
    }

    override fun paginationClause(limit: Int, offset: Int): String = "LIMIT $offset, $limit"

    override fun listTables(connection: Connection, database: String): List<TableInfo> {
        val tables = mutableListOf<TableInfo>()
        connection.prepareStatement(
            "SELECT TABLE_NAME, TABLE_TYPE FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = ? ORDER BY TABLE_NAME"
        ).use { statement ->
            statement.setString(1, database)
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    tables.add(
                        TableInfo(
                            name = resultSet.getString("TABLE_NAME"),
                            type = resultSet.getString("TABLE_TYPE")
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
            "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_KEY, IS_NULLABLE, ORDINAL_POSITION " +
                "FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                "ORDER BY ORDINAL_POSITION"
        ).use { statement ->
            statement.setString(1, database)
            statement.setString(2, table)
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    columns.add(
                        ColumnInfo(
                            name = resultSet.getString("COLUMN_NAME"),
                            dataType = resultSet.getString("DATA_TYPE"),
                            isPrimaryKey = "PRI" == resultSet.getString("COLUMN_KEY"),
                            isNullable = "YES" == resultSet.getString("IS_NULLABLE"),
                            ordinalPosition = resultSet.getInt("ORDINAL_POSITION")
                        )
                    )
                }
            }
        }
        return columns
    }
}
