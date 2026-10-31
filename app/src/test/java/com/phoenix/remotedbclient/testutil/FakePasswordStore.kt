package com.phoenix.remotedbclient.testutil

import com.phoenix.remotedbclient.util.PasswordStore

/**
 * 职责：PasswordStore 的内存测试替身，避免单元测试依赖 Android Keystore。
 * 位置：test 源的公共测试工具。
 */
class FakePasswordStore : PasswordStore {

    val passwords = mutableMapOf<Long, String>()

    override fun savePassword(configId: Long, password: String) {
        passwords[configId] = password
    }

    override fun getPassword(configId: Long): String? = passwords[configId]

    override fun deletePassword(configId: Long) {
        passwords.remove(configId)
    }

    override fun hasPassword(configId: Long): Boolean = passwords.containsKey(configId)
}
