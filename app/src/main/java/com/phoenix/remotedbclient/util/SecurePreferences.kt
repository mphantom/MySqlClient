package com.phoenix.remotedbclient.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps Android [EncryptedSharedPreferences] for secure storage of database passwords.
 *
 * Each password is keyed by its connection config ID so that password retrieval
 * for a specific config is straightforward. Passwords are AES-256 encrypted at rest
 * using the Android Keystore-backed [MasterKey].
 */
@Singleton
class SecurePreferences @Inject constructor(@ApplicationContext context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Stores an encrypted password for the given connection config ID.
     */
    fun savePassword(configId: Long, password: String) {
        prefs.edit().putString(keyFor(configId), password).apply()
    }

    /**
     * Retrieves the decrypted password for the given connection config ID.
     *
     * @return The plaintext password, or null if not stored.
     */
    fun getPassword(configId: Long): String? {
        return prefs.getString(keyFor(configId), null)
    }

    /**
     * Deletes the stored password for the given connection config ID.
     */
    fun deletePassword(configId: Long) {
        prefs.edit().remove(keyFor(configId)).apply()
    }

    /** Whether a password is stored for this config ID. */
    fun hasPassword(configId: Long): Boolean {
        return prefs.contains(keyFor(configId))
    }

    private fun keyFor(configId: Long): String = "db_password_$configId"

    companion object {
        // [改动前] 偏好文件名 remotedb_secure_prefs
        // [改动后] 改为 remotedb_client_prefs，加密方案与 key 规则（db_password_<id>）不变
        // [原因] 统一命名；applicationId 变更后旧偏好文件不可达，无需迁移
        private const val PREFS_FILE_NAME = "remotedb_client_prefs"
    }
}
