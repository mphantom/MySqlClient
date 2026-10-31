package com.phoenix.remotedbclient.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 职责：验证 SessionManager 的会话缓存、密码内存表与关闭清理行为，
 * 这些行为直接影响多连接工作区的生命周期。
 * 位置：data/remote 的 JVM 单元测试。
 */
class SessionManagerTest {

    @Test
    fun `getOrCreate reuses the same manager`() {
        val sessionManager = SessionManager()

        val first = sessionManager.getOrCreate(1L)
        val second = sessionManager.getOrCreate(1L)

        assertSame(first, second)
        assertTrue(sessionManager.hasSession(1L))
    }

    @Test
    fun `get returns null for unknown session`() {
        assertNull(SessionManager().get(99L))
    }

    @Test
    fun `passwords are kept per config`() {
        val sessionManager = SessionManager()

        sessionManager.storePassword(1L, "secret-a")
        sessionManager.storePassword(2L, "secret-b")

        assertEquals("secret-a", sessionManager.getPassword(1L))
        assertEquals("secret-b", sessionManager.getPassword(2L))
        assertNull(sessionManager.getPassword(3L))
    }

    @Test
    fun `remove closes and forgets session`() {
        val sessionManager = SessionManager()
        val manager = sessionManager.getOrCreate(1L)

        sessionManager.remove(1L)

        assertFalse(sessionManager.hasSession(1L))
        assertFalse(manager.isConnected)
    }

    @Test
    fun `activeConfigIds tracks sessions`() {
        val sessionManager = SessionManager()
        sessionManager.getOrCreate(1L)
        sessionManager.getOrCreate(2L)

        assertEquals(setOf(1L, 2L), sessionManager.activeConfigIds())
    }

    @Test
    fun `clearAll removes sessions and passwords`() {
        val sessionManager = SessionManager()
        val manager = sessionManager.getOrCreate(1L)
        sessionManager.storePassword(1L, "secret")

        sessionManager.clearAll()

        assertFalse(sessionManager.hasSession(1L))
        assertNull(sessionManager.get(1L))
        assertNull(sessionManager.getPassword(1L))
        assertFalse(manager.isConnected)
    }

    @Test
    fun `manager is not connected before connect is called`() {
        val manager = SessionManager().getOrCreate(1L)

        assertNotNull(manager)
        assertFalse(manager.isConnected)
        assertNull(manager.connectedConfig)
    }
}
