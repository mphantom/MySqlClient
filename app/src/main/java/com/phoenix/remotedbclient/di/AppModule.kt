package com.phoenix.remotedbclient.di

import android.content.Context
import androidx.room.Room
import com.phoenix.remotedbclient.data.db.AppDatabase
import com.phoenix.remotedbclient.data.db.ConnectionConfigDao
import com.phoenix.remotedbclient.data.remote.RemoteDatabaseManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-scoped dependencies:
 * Room database, DAO, and the [RemoteDatabaseManager].
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
    fun provideRemoteDatabaseManager(): RemoteDatabaseManager {
        return RemoteDatabaseManager()
    }
}
