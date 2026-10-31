package com.phoenix.remotedbclient.driver

import java.sql.Connection
import java.sql.SQLException

/**
 * 职责：定义一种数据库方言的全部差异：连接 URL、驱动类、元数据查询、
 * 标识符引用、分页与增删改实现。
 * 位置：driver-api；每个数据库驱动模块实现该接口。
 */
interface DatabaseDialect {

    /** 稳定的方言 id，保存到 ConnectionConfigEntity.dbType 中，例如 "MySQL"。 */
    val id: String

    /** 展示给用户的名称。 */
    val displayName: String

    /** 未填写端口时使用的默认端口。 */
    val defaultPort: Int

    /** JDBC 驱动类名，连接前通过 Class.forName 加载。 */
    val driverClassName: String

    /** 构造该数据库的 JDBC URL。 */
    fun buildJdbcUrl(host: String, port: Int, database: String): String

    /**
     * 按方言引用并转义标识符（表名、列名）。
     * 例如 MySQL 的 `a`b` -> `a``b`，PostgreSQL 的 "a"b" -> "a""b"。
     */
    fun quoteIdentifier(name: String): String

    /** 查询当前数据库中的表与视图。 */
    @Throws(SQLException::class)
    fun listTables(connection: Connection, database: String): List<TableInfo>

    /** 查询指定表的列元数据。 */
    @Throws(SQLException::class)
    fun listColumns(connection: Connection, database: String, table: String): List<ColumnInfo>

    /** 执行分页查询；[orderBy] 为排序列，[ascending] 控制升/降序。 */
    @Throws(SQLException::class)
    fun selectRows(
        connection: Connection,
        table: String,
        columns: List<String>,
        offset: Int,
        limit: Int,
        orderBy: String? = null,
        ascending: Boolean = true
    ): QueryResult

    /** 统计表总行数。 */
    @Throws(SQLException::class)
    fun countRows(connection: Connection, table: String): Int

    /** 按主键更新单个单元格。 */
    @Throws(SQLException::class)
    fun updateCell(
        connection: Connection,
        table: String,
        column: String,
        newValue: String?,
        primaryKeyColumns: List<String>,
        primaryKeyValues: List<String?>
    ): Int

    /** 按主键删除一行。 */
    @Throws(SQLException::class)
    fun deleteRow(
        connection: Connection,
        table: String,
        primaryKeyColumns: List<String>,
        primaryKeyValues: List<String?>
    ): Int

    /** 插入一行。 */
    @Throws(SQLException::class)
    fun insertRow(connection: Connection, table: String, values: Map<String, String?>): Int
}
