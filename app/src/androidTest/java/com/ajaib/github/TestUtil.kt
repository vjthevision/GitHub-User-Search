package com.ajaib.github

import com.ajaib.github.data.remote.dto.UserDto
import com.ajaib.github.domain.model.User

object TestUtil {

    fun createTestUserDto(
        id: Int = 1,
        login: String = "testuser",
        avatarUrl: String = "https://example.com/avatar.png",
        htmlUrl: String = "https://github.com/testuser",
        type: String = "User",
        name: String? = "Test User",
        company: String? = "Test Company",
        blog: String? = "https://test.com",
        location: String? = "Test City",
        email: String? = null,
        bio: String? = "Test bio",
        publicRepos: Int? = 10,
        publicGists: Int? = 5,
        followers: Int? = 100,
        following: Int? = 50,
        createdAt: String? = "2020-01-01T00:00:00Z"
    ) = UserDto(
        id = id,
        login = login,
        avatarUrl = avatarUrl,
        htmlUrl = htmlUrl,
        type = type,
        name = name,
        company = company,
        blog = blog,
        location = location,
        email = email,
        bio = bio,
        publicRepos = publicRepos,
        publicGists = publicGists,
        followers = followers,
        following = following,
        createdAt = createdAt
    )

    fun createTestUser(
        id: Int = 1,
        login: String = "testuser",
        avatarUrl: String = "https://example.com/avatar.png",
        htmlUrl: String = "https://github.com/testuser",
        type: String = "User",
        name: String? = "Test User",
        company: String? = "Test Company",
        blog: String? = "https://test.com",
        location: String? = "Test City",
        email: String? = null,
        bio: String? = "Test bio",
        publicRepos: Int? = 10,
        publicGists: Int? = 5,
        followers: Int? = 100,
        following: Int? = 50,
        createdAt: String? = "2020-01-01T00:00:00Z"
    ) = User(
        id = id,
        login = login,
        avatarUrl = avatarUrl,
        htmlUrl = htmlUrl,
        type = type,
        name = name,
        company = company,
        blog = blog,
        location = location,
        email = email,
        bio = bio,
        publicRepos = publicRepos,
        publicGists = publicGists,
        followers = followers,
        following = following,
        createdAt = createdAt
    )
}