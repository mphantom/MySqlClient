package com.phoenix.remotedbclient.driver

/**
 * 职责：按 id 查找数据库方言，并向 UI 提供可用方言列表。
 * 位置：driver-api；AppModule 用 Hilt 的 Set 注入构建。
 */
class DriverRegistry(dialects: Collection<DatabaseDialect>) {

    private val byId: Map<String, DatabaseDialect> = dialects.associateBy { it.id }

    init {
        val duplicates = dialects.groupingBy { it.id }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        require(duplicates.isEmpty()) { "Duplicate database dialect id(s): $duplicates" }
    }

    /** 获取方言；不存在时抛出 IllegalArgumentException。 */
    fun get(id: String): DatabaseDialect =
        byId[id] ?: throw IllegalArgumentException("Unsupported database type: $id")

    /** 获取方言；不存在时返回 null，供 UI 判断使用。 */
    fun find(id: String): DatabaseDialect? = byId[id]

    /** 全部方言，按显示名排序，供数据库类型下拉框使用。 */
    fun all(): List<DatabaseDialect> =
        byId.values.sortedWith(compareBy({ it.displayName }, { it.id }))
}
