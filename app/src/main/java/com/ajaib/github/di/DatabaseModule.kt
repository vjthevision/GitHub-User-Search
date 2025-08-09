package com.ajaib.github.di

import android.content.Context
import androidx.room.Room
import com.ajaib.github.data.local.database.GitHubDatabase
import com.ajaib.github.data.local.dao.RepositoryDao
import com.ajaib.github.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGitHubDatabase(
        @ApplicationContext context: Context
    ): GitHubDatabase {
        return Room.databaseBuilder(
            context,
            GitHubDatabase::class.java,
            GitHubDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: GitHubDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideRepositoryDao(database: GitHubDatabase): RepositoryDao {
        return database.repositoryDao()
    }
}