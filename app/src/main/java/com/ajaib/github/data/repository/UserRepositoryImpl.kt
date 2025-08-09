package com.ajaib.github.data.repository

import com.ajaib.github.data.local.dao.RepositoryDao
import com.ajaib.github.data.local.dao.UserDao
import com.ajaib.github.data.mappers.*
import com.ajaib.github.data.remote.api.GitHubApiService
import com.ajaib.github.domain.model.Repository
import com.ajaib.github.domain.model.User
import com.ajaib.github.domain.repository.UserRepository
import com.ajaib.github.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: GitHubApiService,
    private val userDao: UserDao,
    private val repositoryDao: RepositoryDao
) : UserRepository {

    override fun getUsers(): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())

        // First, emit cached data if available
        val localUsers = userDao.getAllUsers().first()
        if (localUsers.isNotEmpty()) {
            emit(Resource.Success(localUsers.map { it.toUser() }))
        }

        try {
            val remoteUsers = api.getUsers()
            val userEntities = remoteUsers.map { it.toUserEntity() }
            userDao.insertUsers(userEntities)

            // Emit fresh data
            val updatedUsers = userDao.getAllUsers().first()
            emit(Resource.Success(updatedUsers.map { it.toUser() }))

        } catch (e: HttpException) {
            emit(Resource.Error(
                message = "HTTP Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                data = localUsers.map { it.toUser() }
            ))
        } catch (e: IOException) {
            emit(Resource.Error(
                message = "Network Error: ${e.localizedMessage ?: "Check your internet connection"}",
                data = localUsers.map { it.toUser() }
            ))
        } catch (e: Exception) {
            emit(Resource.Error(
                message = "Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                data = localUsers.map { it.toUser() }
            ))
        }
    }

    override fun searchUsers(query: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())

        // First, search in local database
        val localUsers = userDao.searchUsers(query).first()
        if (localUsers.isNotEmpty()) {
            emit(Resource.Success(localUsers.map { it.toUser() }))
        }

        if (query.isBlank()) {
            emit(Resource.Success(emptyList()))
            return@flow
        }

        try {
            val remoteUsers = api.searchUsers(query)
            val userEntities = remoteUsers.items.map { it.toUserEntity() }
            userDao.insertUsers(userEntities)

            // Emit fresh search results
            val updatedUsers = userDao.searchUsers(query).first()
            emit(Resource.Success(updatedUsers.map { it.toUser() }))

        } catch (e: HttpException) {
            when (e.code()) {
                403 -> emit(Resource.Error(
                    message = "Rate limit exceeded. Please try again later.",
                    data = localUsers.map { it.toUser() }
                ))
                422 -> emit(Resource.Error(
                    message = "Search query validation failed.",
                    data = localUsers.map { it.toUser() }
                ))
                else -> emit(Resource.Error(
                    message = "HTTP Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                    data = localUsers.map { it.toUser() }
                ))
            }
        } catch (e: IOException) {
            emit(Resource.Error(
                message = "Network Error: Check your internet connection",
                data = localUsers.map { it.toUser() }
            ))
        } catch (e: Exception) {
            emit(Resource.Error(
                message = "Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                data = localUsers.map { it.toUser() }
            ))
        }
    }

    override fun getUserDetails(username: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        // First, try to get from local database
        val localUser = userDao.getUserByLogin(username)
        localUser?.let {
            emit(Resource.Success(it.toUser()))
        }

        try {
            val remoteUser = api.getUser(username)
            val userEntity = remoteUser.toUserEntity()
            userDao.insertUser(userEntity)

            // Emit updated user details
            val updatedUser = userDao.getUserByLogin(username)
            updatedUser?.let {
                emit(Resource.Success(it.toUser()))
            }

        } catch (e: HttpException) {
            when (e.code()) {
                404 -> emit(Resource.Error(
                    message = "User not found",
                    data = localUser?.toUser()
                ))
                else -> emit(Resource.Error(
                    message = "HTTP Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                    data = localUser?.toUser()
                ))
            }
        } catch (e: IOException) {
            emit(Resource.Error(
                message = "Network Error: Check your internet connection",
                data = localUser?.toUser()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(
                message = "Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                data = localUser?.toUser()
            ))
        }
    }

    override fun getUserRepositories(username: String): Flow<Resource<List<Repository>>> = flow {
        emit(Resource.Loading())

        // First, get from local database
        val localRepos = repositoryDao.getRepositoriesByOwnerSync(username)
        if (localRepos.isNotEmpty()) {
            emit(Resource.Success(localRepos.map { it.toRepository() }))
        }

        try {
            val remoteRepos = api.getUserRepositories(username)
            val repoEntities = remoteRepos.map {
                it.toRepositoryEntity(username)
            }

            // Clear old repositories and insert new ones
            repositoryDao.deleteRepositoriesByOwner(username)
            repositoryDao.insertRepositories(repoEntities)

            // Emit fresh repositories
            val updatedRepos = repositoryDao.getRepositoriesByOwnerSync(username)
            emit(Resource.Success(updatedRepos.map { it.toRepository() }))

        } catch (e: HttpException) {
            when (e.code()) {
                404 -> emit(Resource.Error(
                    message = "User repositories not found",
                    data = localRepos.map { it.toRepository() }
                ))
                else -> emit(Resource.Error(
                    message = "HTTP Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                    data = localRepos.map { it.toRepository() }
                ))
            }
        } catch (e: IOException) {
            emit(Resource.Error(
                message = "Network Error: Check your internet connection",
                data = localRepos.map { it.toRepository() }
            ))
        } catch (e: Exception) {
            emit(Resource.Error(
                message = "Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                data = localRepos.map { it.toRepository() }
            ))
        }
    }

    override suspend fun refreshUsers() {
        try {
            val remoteUsers = api.getUsers()
            val userEntities = remoteUsers.map { it.toUserEntity() }
            userDao.deleteAllUsers()
            userDao.insertUsers(userEntities)
        } catch (e: Exception) {
            // Handle refresh error silently or emit to a separate error channel
        }
    }

    override suspend fun refreshUserDetails(username: String) {
        try {
            val remoteUser = api.getUser(username)
            val userEntity = remoteUser.toUserEntity()
            userDao.insertUser(userEntity)
        } catch (e: Exception) {
            // Handle refresh error silently or emit to a separate error channel
        }
    }

    override suspend fun refreshUserRepositories(username: String) {
        try {
            val remoteRepos = api.getUserRepositories(username)
            val repoEntities = remoteRepos.map {
                it.toRepositoryEntity(username)
            }
            repositoryDao.deleteRepositoriesByOwner(username)
            repositoryDao.insertRepositories(repoEntities)
        } catch (e: Exception) {
            // Handle refresh error silently or emit to a separate error channel
        }
    }
}