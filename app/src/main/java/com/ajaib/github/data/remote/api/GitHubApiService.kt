package com.ajaib.github.data.remote.api

import com.ajaib.github.data.remote.dto.RepositoryDto
import com.ajaib.github.data.remote.dto.SearchUsersResponse
import com.ajaib.github.data.remote.dto.UserDto
import com.ajaib.github.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException

interface GitHubApiService {

    @GET("users")
    @Throws(IOException::class)
    suspend fun getUsers(
        @Query("since") since: Int = 0,
        @Query("per_page") perPage: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<UserDto>

    @GET("search/users")
    @Throws(IOException::class)
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("page") page: Int = Constants.FIRST_PAGE,
        @Query("per_page") perPage: Int = Constants.DEFAULT_PAGE_SIZE
    ): SearchUsersResponse

    @GET("users/{username}")
    @Throws(IOException::class)
    suspend fun getUser(
        @Path("username") username: String
    ): UserDto

    @GET("users/{username}/repos")
    @Throws(IOException::class)
    suspend fun getUserRepositories(
        @Path("username") username: String,
        @Query("page") page: Int = Constants.FIRST_PAGE,
        @Query("per_page") perPage: Int = Constants.DEFAULT_PAGE_SIZE,
        @Query("sort") sort: String = "updated"
    ): List<RepositoryDto>
}