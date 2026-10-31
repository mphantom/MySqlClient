package com.phoenix.remotedbclient.di

import android.content.Context
import androidx.room.Room
import com.phoenix.remotedbclient.data.db.AppDatabase
import com.phoenix.remotedbclient.data.db.ConnectionConfigDao
import com.phoenix.remotedbclient.data.remote.RemoteDatabaseManager
import com.phoenix.remotedbclient.data.remote.SessionConnector
import com.phoenix.remotedbclient.data.remote.SessionManager
import com.phoenix.remotedbclient.driver.DatabaseDialect
import com.phoenix.remotedbclient.driver.DriverRegistry
import com.phoenix.remotedbclient.driver.mysql.MySqlDialect
import com.phoenix.remotedbclient.driver.postgresql.PostgreSqlDialect
import com.phoenix.remotedbclient.util.PasswordStore
import com.phoenix.remotedbclient.util.SecurePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

/**
 * Hilt module providing application-scoped dependencies:
 * Room database, DAO, driver registry, and the [RemoteDatabaseManager].
 *
 * [改动前] RemoteDatabaseManager 直接 new，数据库类型写死在核心类中
 * [改动后] 用 Hilt Set 聚合 DatabaseDialect，再构建 DriverRegistry 并注入连接管理器
 * [原因] 新增数据库只需增加一个方言绑定，核心连接代码不再修改
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            // [改动前] 数据库文件名为 remotedb_explorer.db
            // [改动后] 改为 remotedb_client.db
            // [原因] 统一命名；applicationId 已变更，新包本来就读不到旧应用的数据库，无需迁移
            "remotedb_client.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideConnectionConfigDao(db: AppDatabase): ConnectionConfigDao {
        return db.connectionConfigDao()
    }

    @Provides
    @Singleton
    fun providePasswordStore(securePreferences: SecurePreferences): PasswordStore {
        return securePreferences
    }

    @Provides
    @Singleton
    fun provideSessionConnector(sessionManager: SessionManager): SessionConnector {
        return sessionManager
    }

    @Provides
    @IntoSet
    fun provideMySqlDialect(): DatabaseDialect = MySqlDialect()

    @Provides
    @IntoSet
    fun providePostgreSqlDialect(): DatabaseDialect = PostgreSqlDialect()

    @Provides
    @Singleton
    fun provideDriverRegistry(
        dialects: Set<@JvmSuppressWildcards DatabaseDialect>
    ): DriverRegistry {
        return DriverRegistry(dialects)
    }

    @Provides
    @Singleton
    fun provideRemoteDatabaseManager(driverRegistry: DriverRegistry): RemoteDatabaseManager {
        return RemoteDatabaseManager(driverRegistry)
    }
}
