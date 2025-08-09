package com.ajaib.github.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.ajaib.github.data.local.dao.RepositoryDao
import com.ajaib.github.data.local.dao.UserDao
import com.ajaib.github.data.local.entities.RepositoryEntity
import com.ajaib.github.data.local.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        RepositoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GitHubDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun repositoryDao(): RepositoryDao

    companion object {
        const val DATABASE_NAME = "github_database"

        @Volatile
        private var INSTANCE: GitHubDatabase? = null

        fun getDatabase(context: Context): GitHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GitHubDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration example (for future versions)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migration logic here
            }
        }
    }
}

