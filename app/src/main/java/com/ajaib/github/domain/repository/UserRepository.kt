package com.ajaib.github.domain.repository

import com.ajaib.github.domain.model.Repository
import com.ajaib.github.domain.model.User
import com.ajaib.github.utils.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun getUsers(): Flow<Resource<List<User>>>

    fun searchUsers(query: String): Flow<Resource<List<User>>>

    fun getUserDetails(username: String): Flow<Resource<User>>

    fun getUserRepositories(username: String): Flow<Resource<List<Repository>>>

    suspend fun refreshUsers()

    suspend fun refreshUserDetails(username: String)

    suspend fun refreshUserRepositories(username: String)
}