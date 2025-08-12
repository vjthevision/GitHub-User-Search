package com.ajaib.github.di

import com.ajaib.github.domain.model.Repository
import com.ajaib.github.domain.model.User
import com.ajaib.github.domain.repository.UserRepository
import com.ajaib.github.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeUserRepository @Inject constructor() : UserRepository {

    private val fakeUser = User(
        id = 1,
        login = "octocat",
        avatarUrl = "https://github.com/images/error/octocat_happy.gif",
        htmlUrl = "https://github.com/octocat",
        type = "User",
        name = "The Octocat",
        company = "GitHub",
        blog = "https://github.blog",
        location = "San Francisco",
        email = null,
        bio = "GitHub mascot",
        publicRepos = 8,
        publicGists = 8,
        followers = 9000,
        following = 9,
        createdAt = "2011-01-25T18:44:36Z"
    )

    override fun getUsers(): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        emit(Resource.Success(listOf(fakeUser)))
    }

    override fun searchUsers(query: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        emit(Resource.Success(listOf(fakeUser)))
    }

    override fun getUserDetails(username: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        emit(Resource.Success(fakeUser))
    }

    override fun getUserRepositories(username: String): Flow<Resource<List<Repository>>> = flow {
        emit(Resource.Loading())
        emit(Resource.Success(emptyList()))
    }

    override suspend fun refreshUsers() {
        TODO("Not yet implemented")
    }

    override suspend fun refreshUserDetails(username: String) {
        TODO("Not yet implemented")
    }

    override suspend fun refreshUserRepositories(username: String) {
        TODO("Not yet implemented")
    }
}
