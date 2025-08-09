package com.ajaib.github.data.repository

import com.ajaib.github.data.remote.api.GitHubApiService
import com.ajaib.github.data.remote.dto.UserDto
import com.ajaib.github.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: GitHubApiService
) {

    fun getUsers(): Flow<Resource<List<UserDto>>> = flow {
        emit(Resource.Loading())

        try {
            val remoteUsers = api.getUsers()
            emit(Resource.Success(remoteUsers))

        } catch (e: HttpException) {
            emit(Resource.Error("HTTP Error: ${e.localizedMessage ?: "Unknown error occurred"}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network Error: ${e.localizedMessage ?: "Check your internet connection"}"))
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.localizedMessage ?: "Unknown error occurred"}"))
        }
    }
}