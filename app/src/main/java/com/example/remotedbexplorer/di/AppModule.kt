package com.example.remotedbexplorer.di

import android.content.Context
import androidx.room.Room
import com.example.remotedbexplorer.data.db.AppDatabase
import com.example.remotedbexplorer.data.db.ConnectionConfigDao
import com.example.remotedbexplorer.data.remote.RemoteDatabaseManager
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
            "remotedb_explorer.db"
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
