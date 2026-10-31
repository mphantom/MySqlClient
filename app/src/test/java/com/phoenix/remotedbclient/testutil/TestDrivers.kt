package com.phoenix.remotedbclient.testutil

import com.phoenix.remotedbclient.driver.DriverRegistry
import com.phoenix.remotedbclient.driver.mysql.MySqlDialect
import com.phoenix.remotedbclient.driver.postgresql.PostgreSqlDialect

/**
 * 职责：为不依赖真实数据库的单元测试提供包含 MySQL/PostgreSQL 的注册表。
 * 位置：test 源的公共测试工具。
 */
fun testDriverRegistry(): DriverRegistry =
    DriverRegistry(listOf(MySqlDialect(), PostgreSqlDialect()))
