package com.ajaib.github.data.local.dao

import androidx.room.*
import com.ajaib.github.data.local.entities.RepositoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao {

    @Query("SELECT * FROM repositories WHERE owner_login = :ownerLogin ORDER BY stargazers_count DESC")
    fun getRepositoriesByOwner(ownerLogin: String): Flow<List<RepositoryEntity>>

    @Query("SELECT * FROM repositories WHERE owner_login = :ownerLogin ORDER BY stargazers_count DESC")
    suspend fun getRepositoriesByOwnerSync(ownerLogin: String): List<RepositoryEntity>

    @Query("SELECT * FROM repositories WHERE id = :repositoryId")
    suspend fun getRepositoryById(repositoryId: Int): RepositoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepository(repository: RepositoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositories(repositories: List<RepositoryEntity>)

    @Update
    suspend fun updateRepository(repository: RepositoryEntity)

    @Delete
    suspend fun deleteRepository(repository: RepositoryEntity)

    @Query("DELETE FROM repositories WHERE owner_login = :ownerLogin")
    suspend fun deleteRepositoriesByOwner(ownerLogin: String)

    @Query("DELETE FROM repositories")
    suspend fun deleteAllRepositories()

    @Query("DELETE FROM repositories WHERE cached_at < :timestamp")
    suspend fun deleteOldRepositories(timestamp: Long)

    @Query("SELECT COUNT(*) FROM repositories WHERE owner_login = :ownerLogin")
    suspend fun getRepositoryCountForOwner(ownerLogin: String): Int
}
