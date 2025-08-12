package com.ajaib.github.data.repository

import android.util.Log
import com.ajaib.github.data.local.dao.RepositoryDao
import com.ajaib.github.data.local.dao.UserDao
import com.ajaib.github.data.mappers.*
import com.ajaib.github.data.remote.api.GitHubApiService
import com.ajaib.github.domain.model.Repository
import com.ajaib.github.domain.model.User
import com.ajaib.github.domain.repository.UserRepository
import com.ajaib.github.utils.Constants.CACHE_TIMEOUT_MS
import com.ajaib.github.utils.Constants.DEFAULT_PAGE_SIZE
import com.ajaib.github.utils.Constants.FIRST_PAGE
import com.ajaib.github.utils.Constants.MAX_REQUESTS_PER_HOUR
import com.ajaib.github.utils.Constants.MIN_SEARCH_QUERY_LENGTH
import com.ajaib.github.utils.Resource
import com.ajaib.github.utils.Constants.RATE_LIMIT_RESET_TIME_MS
import kotlinx.coroutines.delay
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

    var lastUserFetchTime = 0L
    private var requestCount = 0
    private var requestWindowStart = System.currentTimeMillis()

    private suspend fun checkRateLimit() {
        val now = System.currentTimeMillis()
        if (now - requestWindowStart >= RATE_LIMIT_RESET_TIME_MS) {
            requestCount = 0
            requestWindowStart = now
        }
        if (requestCount >= MAX_REQUESTS_PER_HOUR) {
            val waitTime = RATE_LIMIT_RESET_TIME_MS - (now - requestWindowStart)
            delay(waitTime)
            requestCount = 0
            requestWindowStart = System.currentTimeMillis()
        }
        requestCount++
    }

    override fun getUsers(): Flow<Resource<List<User>>> = flow {
        Log.d("UserRepository", "DEBUG: Emitting Loading state...")
        emit(Resource.Loading())

        val localUsers = userDao.getAllUsers().first()
        Log.d("UserRepository", "DEBUG: Local DB returned ${localUsers.size} users")

        val now = System.currentTimeMillis()
        val cacheExpired = (now - lastUserFetchTime) > CACHE_TIMEOUT_MS
        Log.d(
            "UserRepository",
            "DEBUG: CacheExpired=$cacheExpired, lastFetch=$lastUserFetchTime, now=$now"
        )

        if (localUsers.isNotEmpty() && !cacheExpired) {
            Log.d("UserRepository", "DEBUG: Emitting SUCCESS from cache")
            emit(Resource.Success(localUsers.map { it.toUser() }))
            return@flow
        }

        try {
            Log.d("UserRepository", "DEBUG: Fetching from API...")
            checkRateLimit()
            val remoteUsers = api.getUsers(perPage = DEFAULT_PAGE_SIZE, since = 0)
            Log.d("UserRepository", "DEBUG: API returned ${remoteUsers.size} users")
            val userEntities = remoteUsers.map { it.toUserEntity() }
            userDao.insertUsers(userEntities)
            lastUserFetchTime = now
            Log.d("UserRepository", "DEBUG: Emitting SUCCESS from API data")
            emit(Resource.Success(userEntities.map { it.toUser() }))
        } catch (e: HttpException) {
            Log.e("UserRepository", "HTTP Error: ${e.localizedMessage}")
            emit(
                Resource.Error(
                    message = "HTTP Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                    data = localUsers.map { it.toUser() }
                )
            )
        } catch (e: IOException) {
            Log.e("UserRepository", "Network Error: ${e.localizedMessage}")
            emit(
                Resource.Error(
                    message = "Network Error: ${e.localizedMessage ?: "Check your internet connection"}",
                    data = localUsers.map { it.toUser() }
                )
            )
        } catch (e: Exception) {
            Log.e("UserRepository", "Error: ${e.localizedMessage}")
            emit(
                Resource.Error(
                    message = "Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                    data = localUsers.map { it.toUser() }
                )
            )
        }
    }

    override fun searchUsers(query: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())

        if (query.length < MIN_SEARCH_QUERY_LENGTH) {
            emit(Resource.Success(emptyList()))
            return@flow
        }

        val localUsers = userDao.searchUsers(query).first()
        if (localUsers.isNotEmpty()) {
            emit(Resource.Success(localUsers.map { it.toUser() }))
        }

        if (query.isBlank()) {
            emit(Resource.Success(emptyList()))
            return@flow
        }

        try {
            checkRateLimit()
            val remoteUsers = api.searchUsers(query, perPage = DEFAULT_PAGE_SIZE)
            val userEntities = remoteUsers.items.map { it.toUserEntity() }
            userDao.insertUsers(userEntities)

            // Emit fresh search results
            val updatedUsers = userDao.searchUsers(query).first()
            emit(Resource.Success(updatedUsers.map { it.toUser() }))

        } catch (e: HttpException) {
            when (e.code()) {
                403 -> emit(
                    Resource.Error(
                        message = "Rate limit exceeded. Please try again later.",
                        data = localUsers.map { it.toUser() }
                    )
                )
                422 -> emit(
                    Resource.Error(
                        message = "Search query validation failed.",
                        data = localUsers.map { it.toUser() }
                    )
                )
                else -> emit(
                    Resource.Error(
                        message = "HTTP Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                        data = localUsers.map { it.toUser() }
                    )
                )
            }
        } catch (e: IOException) {
            emit(
                Resource.Error(
                    message = "Network Error: Check your internet connection",
                    data = localUsers.map { it.toUser() }
                )
            )
        } catch (e: Exception) {
            emit(
                Resource.Error(
                    message = "Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                    data = localUsers.map { it.toUser() }
                )
            )
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
            checkRateLimit()
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
                404 -> emit(
                    Resource.Error(
                        message = "User not found",
                        data = localUser?.toUser()
                    )
                )
                else -> emit(
                    Resource.Error(
                        message = "HTTP Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                        data = localUser?.toUser()
                    )
                )
            }
        } catch (e: IOException) {
            emit(
                Resource.Error(
                    message = "Network Error: Check your internet connection",
                    data = localUser?.toUser()
                )
            )
        } catch (e: Exception) {
            emit(
                Resource.Error(
                    message = "Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                    data = localUser?.toUser()
                )
            )
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
            checkRateLimit()
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
                404 -> emit(
                    Resource.Error(
                        message = "User repositories not found",
                        data = localRepos.map { it.toRepository() }
                    )
                )
                else -> emit(
                    Resource.Error(
                        message = "HTTP Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                        data = localRepos.map { it.toRepository() }
                    )
                )
            }
        } catch (e: IOException) {
            emit(
                Resource.Error(
                    message = "Network Error: Check your internet connection",
                    data = localRepos.map { it.toRepository() }
                )
            )
        } catch (e: Exception) {
            emit(
                Resource.Error(
                    message = "Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                    data = localRepos.map { it.toRepository() }
                )
            )
        }
    }

    override suspend fun refreshUsers() {
        try {
            checkRateLimit()
            val remoteUsers = api.getUsers(perPage = DEFAULT_PAGE_SIZE, since = FIRST_PAGE)
            val userEntities = remoteUsers.map { it.toUserEntity() }
            userDao.deleteAllUsers()
            userDao.insertUsers(userEntities)
        } catch (e: Exception) {
            android.util.Log.e("UserRepositoryImpl","${e.localizedMessage}")
        }
    }

    override suspend fun refreshUserDetails(username: String) {
        try {
            checkRateLimit()
            val remoteUser = api.getUser(username)
            val userEntity = remoteUser.toUserEntity()
            userDao.insertUser(userEntity)
        } catch (e: Exception) {
            android.util.Log.e("UserRepositoryImpl","${e.localizedMessage}")
        }
    }

    override suspend fun refreshUserRepositories(username: String) {
        try {
            checkRateLimit()
            val remoteRepos = api.getUserRepositories(username)
            val repoEntities = remoteRepos.map {
                it.toRepositoryEntity(username)
            }
            repositoryDao.deleteRepositoriesByOwner(username)
            repositoryDao.insertRepositories(repoEntities)
        } catch (e: Exception) {
            android.util.Log.e("UserRepositoryImpl","${e.localizedMessage}")
        }
    }
}