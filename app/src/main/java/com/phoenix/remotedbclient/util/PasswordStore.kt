package com.phoenix.remotedbclient.util

/**
 * 职责：抽象密码存储，便于 ViewModel 单元测试替换掉 Android Keystore 实现。
 * 位置：util 层；SecurePreferences 是生产实现。
 */
interface PasswordStore {
    fun savePassword(configId: Long, password: String)
    fun getPassword(configId: Long): String?
    fun deletePassword(configId: Long)
    fun hasPassword(configId: Long): Boolean
}
