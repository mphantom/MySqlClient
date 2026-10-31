package com.phoenix.remotedbclient.driver

import com.phoenix.remotedbclient.driver.mysql.MySqlDialect
import com.phoenix.remotedbclient.driver.postgresql.PostgreSqlDialect
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 职责：验证两种内置方言的元数据、默认端口、驱动类与 JDBC URL 拼接，
 * 防止新增驱动重构时改变既有连接参数。
 * 位置：driver 包的 JVM 单元测试。
 */
class DialectTest {

    @Test
    fun `mysql metadata and jdbc url`() {
        val dialect = MySqlDialect()

        assertEquals("MySQL", dialect.id)
        assertEquals("MySQL", dialect.displayName)
        assertEquals(3306, dialect.defaultPort)
        assertEquals("com.mysql.cj.jdbc.Driver", dialect.driverClassName)
        assertEquals(
            "jdbc:mysql://db.example.com:3307/app" +
                "?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=10000&socketTimeout=30000",
            dialect.buildJdbcUrl("db.example.com", 3307, "app")
        )
    }

    @Test
    fun `mysql quotes and escapes backticks`() {
        val dialect = MySqlDialect()

        assertEquals("`order`", dialect.quoteIdentifier("order"))
        assertEquals("`a``b`", dialect.quoteIdentifier("a`b"))
    }

    @Test
    fun `postgresql metadata and jdbc url`() {
        val dialect = PostgreSqlDialect()

        assertEquals("PostgreSQL", dialect.id)
        assertEquals("PostgreSQL", dialect.displayName)
        assertEquals(5432, dialect.defaultPort)
        assertEquals("org.postgresql.Driver", dialect.driverClassName)
        assertEquals(
            "jdbc:postgresql://db.example.com:5433/app?connectTimeout=10&socketTimeout=30",
            dialect.buildJdbcUrl("db.example.com", 5433, "app")
        )
    }

    @Test
    fun `postgresql quotes and escapes double quotes`() {
        val dialect = PostgreSqlDialect()

        assertEquals("\"order\"", dialect.quoteIdentifier("order"))
        assertEquals("\"a\"\"b\"", dialect.quoteIdentifier("a\"b"))
    }
}
