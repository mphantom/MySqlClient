package com.phoenix.remotedbclient.driver

import java.sql.Connection
import java.sql.PreparedStatement

/**
 * 职责：复用各数据库相同的 JDBC CRUD 与分页逻辑，只把标识符引用、
 * 分页语法与元数据查询留给具体方言。
 * 位置：driver-api；MySqlDialect / PostgreSqlDialect 继承它。
 */
abstract class AbstractJdbcDialect : DatabaseDialect {

    /** 标识符引用符，例如 MySQL 的反引号、PostgreSQL 的双引号。 */
    protected abstract val identifierQuote: Char

    /** 转义标识符内部的引用符：连续写两次。 */
    protected open fun escapeIdentifier(name: String): String =
        name.replace(identifierQuote.toString(), "$identifierQuote$identifierQuote")

    /** 给表名/列名加引用符并转义内部引用符。 */
    override fun quoteIdentifier(name: String): String =
        "$identifierQuote${escapeIdentifier(name)}$identifierQuote"

    /** 内部使用的简写。 */
    protected fun quoted(name: String): String = quoteIdentifier(name)

    /** 生成 LIMIT/OFFSET 子句，例如 "LIMIT 0, 50" 或 "LIMIT 50 OFFSET 0"。 */
    protected abstract fun paginationClause(limit: Int, offset: Int): String

    /**
     * 绑定一个来自 UI 的字符串参数。
     * MySQL 可用 setString 由服务端隐式转换；PostgreSQL 需要覆盖此方法避免
     * varchar 与 integer 等严格类型不匹配。
     */
    protected open fun bindValue(statement: PreparedStatement, index: Int, value: String?) {
        statement.setString(index, value)
    }

    override fun selectRows(
        connection: Connection,
        table: String,
        columns: List<String>,
        offset: Int,
        limit: Int,
        orderBy: String?,
        ascending: Boolean
    ): QueryResult {
        val columnList = if (columns.isEmpty()) "*" else columns.joinToString(", ") { quoted(it) }
        // [改动前] 分页查询没有 ORDER BY，数据库返回顺序不保证，翻页可能重复/漏行
        // [改动后] 可按指定列排序；调用方通常传主键，保证分页稳定
        // [原因] 排序既是用户功能，也是分页正确性的前提
        val orderClause = orderBy
            ?.takeIf { it.isNotBlank() }
            ?.let { " ORDER BY ${quoted(it)} ${if (ascending) "ASC" else "DESC"}" }
            .orEmpty()
        val sql = "SELECT $columnList FROM ${quoted(table)}$orderClause " +
            paginationClause(limit, offset)

        connection.createStatement().use { statement ->
            statement.queryTimeout = 30
            statement.executeQuery(sql).use { resultSet ->
                val metaData = resultSet.metaData
                val columnCount = metaData.columnCount
                val columnNames = (1..columnCount).map { metaData.getColumnName(it) }
                val rows = mutableListOf<List<String?>>()
                while (resultSet.next()) {
                    rows.add((1..columnCount).map { resultSet.getString(it) })
                }
                return QueryResult.selectResult(columnNames, rows)
            }
        }
    }

    override fun countRows(connection: Connection, table: String): Int {
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT COUNT(*) FROM ${quoted(table)}").use { resultSet ->
                return if (resultSet.next()) resultSet.getInt(1) else 0
            }
        }
    }

    override fun updateCell(
        connection: Connection,
        table: String,
        column: String,
        newValue: String?,
        primaryKeyColumns: List<String>,
        primaryKeyValues: List<String?>
    ): Int {
        val whereClause = primaryKeyColumns.joinToString(" AND ") { "${quoted(it)} = ?" }
        val sql = "UPDATE ${quoted(table)} SET ${quoted(column)} = ? WHERE $whereClause"

        connection.prepareStatement(sql).use { statement ->
            bindValue(statement, 1, newValue)
            primaryKeyValues.forEachIndexed { index, value ->
                bindValue(statement, index + 2, value)
            }
            return statement.executeUpdate()
        }
    }

    override fun deleteRow(
        connection: Connection,
        table: String,
        primaryKeyColumns: List<String>,
        primaryKeyValues: List<String?>
    ): Int {
        val whereClause = primaryKeyColumns.joinToString(" AND ") { "${quoted(it)} = ?" }
        val sql = "DELETE FROM ${quoted(table)} WHERE $whereClause"

        connection.prepareStatement(sql).use { statement ->
            primaryKeyValues.forEachIndexed { index, value ->
                bindValue(statement, index + 1, value)
            }
            return statement.executeUpdate()
        }
    }

    override fun insertRow(
        connection: Connection,
        table: String,
        values: Map<String, String?>
    ): Int {
        val columns = values.keys.joinToString(", ") { quoted(it) }
        val placeholders = values.keys.joinToString(", ") { "?" }
        val sql = "INSERT INTO ${quoted(table)} ($columns) VALUES ($placeholders)"

        connection.prepareStatement(sql).use { statement ->
            values.values.forEachIndexed { index, value ->
                bindValue(statement, index + 1, value)
            }
            return statement.executeUpdate()
        }
    }
}
