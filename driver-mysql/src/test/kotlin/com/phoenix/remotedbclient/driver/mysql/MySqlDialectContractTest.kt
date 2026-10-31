package com.phoenix.remotedbclient.driver.mysql

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test
import org.testcontainers.containers.MySQLContainer
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * 职责：用真实 MySQL 容器验证 MySqlDialect 的元数据、保留字标识符、
 * 分页、增删改与注入防护。
 * 位置：driver-mysql 的 JVM 契约测试；需要 Docker。
 */
class MySqlDialectContractTest {

    companion object {
        @ClassRule
        @JvmField
        val MYSQL: MySQLContainer<Nothing> = MySQLContainer<Nothing>("mysql:8.0").apply {
            withDatabaseName("driver_test")
            withUsername("test")
            withPassword("test")
        }
    }

    private lateinit var connection: Connection
    private val dialect = MySqlDialect()

    @After
    fun tearDown() {
        if (::connection.isInitialized) {
            connection.close()
        }
    }

    @Test
    fun `lists reserved table and view`() {
        prepareSchema()
        val tables = dialect.listTables(connection, "driver_test")

        val table = tables.firstOrNull { it.name == "order" }
        val view = tables.firstOrNull { it.name == "order_view" }
        assertNotNull(table)
        assertNotNull(view)
        assertEquals("VIEW", view?.type)
    }

    @Test
    fun `reads columns with reserved names and primary key`() {
        prepareSchema()
        val columns = dialect.listColumns(connection, "driver_test", "order")

        assertEquals(listOf("id", "select", "group", "note"), columns.map { it.name })
        val id = columns.first { it.name == "id" }
        assertTrue(id.isPrimaryKey)
        assertFalse(id.isNullable)
        assertEquals(1, id.ordinalPosition)
    }

    @Test
    fun `selects reserved columns with pagination`() {
        prepareSchema()
        val firstPage = dialect.selectRows(connection, "order", listOf("select", "group"), 0, 10)
        val lastPage = dialect.selectRows(connection, "order", listOf("select", "group"), 50, 10)

        assertEquals(listOf("select", "group"), firstPage.columns)
        assertEquals(10, firstPage.rows?.size)
        assertEquals(5, lastPage.rows?.size)
    }

    @Test
    fun `orders rows by requested column`() {
        prepareSchema()
        val ascending = dialect.selectRows(connection, "order", listOf("id"), 0, 1, "id", true)
        val descending = dialect.selectRows(connection, "order", listOf("id"), 0, 1, "id", false)
        val lastPage = dialect.selectRows(connection, "order", listOf("id"), 50, 10, "id", true)

        assertEquals("1", ascending.rows?.first()?.first())
        assertEquals("55", descending.rows?.first()?.first())
        assertEquals(5, lastPage.rows?.size)
        assertEquals("51", lastPage.rows?.first()?.first())
    }

    @Test
    fun `inserts updates and deletes rows`() {
        prepareSchema()
        val inserted = dialect.insertRow(
            connection,
            "order",
            mapOf("id" to "100", "select" to "before")
        )
        assertEquals(1, inserted)
        assertEquals(56, dialect.countRows(connection, "order"))

        val updated = dialect.updateCell(
            connection,
            "order",
            "select",
            "after",
            listOf("id"),
            listOf("100")
        )
        assertEquals(1, updated)
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT `select` FROM `order` WHERE `id` = 100").use { resultSet ->
                assertTrue(resultSet.next())
                assertEquals("after", resultSet.getString(1))
            }
        }

        val deleted = dialect.deleteRow(connection, "order", listOf("id"), listOf("100"))
        assertEquals(1, deleted)
        assertEquals(55, dialect.countRows(connection, "order"))
    }

    @Test
    fun `malicious table name is treated as an identifier`() {
        prepareSchema()

        try {
            dialect.countRows(connection, "order; DROP TABLE `order`; --")
        } catch (_: SQLException) {
            // 期望：被引用符包住后只是不存在的表名
        }

        assertEquals(55, dialect.countRows(connection, "order"))
    }

    @Test
    fun `quotes and escapes backticks`() {
        assertEquals("`a``b`", dialect.quoteIdentifier("a`b"))
    }

    private fun prepareSchema() {
        connection = DriverManager.getConnection(MYSQL.jdbcUrl, MYSQL.username, MYSQL.password)
        connection.createStatement().use { statement ->
            statement.execute("DROP VIEW IF EXISTS `order_view`")
            statement.execute("DROP TABLE IF EXISTS `order`")
            statement.execute(
                """
                CREATE TABLE `order` (
                    `id` INT NOT NULL,
                    `select` VARCHAR(100),
                    `group` VARCHAR(100),
                    `note` VARCHAR(100),
                    PRIMARY KEY (`id`)
                )
                """.trimIndent()
            )
            statement.execute(
                "CREATE VIEW `order_view` AS SELECT `id`, `select` FROM `order`"
            )
        }
        connection.prepareStatement(
            "INSERT INTO `order` (`id`, `select`, `group`, `note`) VALUES (?, ?, ?, ?)"
        ).use { statement ->
            for (i in 1..55) {
                statement.setInt(1, i)
                statement.setString(2, "select-$i")
                statement.setString(3, "group-$i")
                statement.setString(4, "note-$i")
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }
}
