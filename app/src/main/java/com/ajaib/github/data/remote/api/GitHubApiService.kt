package com.ajaib.github.data.remote.api

import com.ajaib.github.data.remote.dto.RepositoryDto
import com.ajaib.github.data.remote.dto.SearchUsersResponse
import com.ajaib.github.data.remote.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApiService {

    @GET("users")
    suspend fun getUsers(
        @Query("since") since: Int = 0,
        @Query("per_page") perPage: Int = 30
    ): List<UserDto>

    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): SearchUsersResponse

    @GET("users/{username}")
    suspend fun getUser(
        @Path("username") username: String
    ): UserDto

    @GET("users/{username}/repos")
    suspend fun getUserRepositories(
        @Path("username") username: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("sort") sort: String = "updated"
    ): List<RepositoryDto>
}