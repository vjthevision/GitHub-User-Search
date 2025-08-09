package com.ajaib.github.data.mappers

import com.ajaib.github.data.local.entities.RepositoryEntity
import com.ajaib.github.data.remote.dto.RepositoryDto
import com.ajaib.github.domain.model.Repository

// DTO to Domain Model
fun RepositoryDto.toRepository(): Repository {
    return Repository(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        htmlUrl = htmlUrl,
        language = language,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        watchersCount = watchersCount,
        size = size,
        defaultBranch = defaultBranch,
        openIssuesCount = openIssuesCount,
        isPrivate = isPrivate,
        isFork = isFork,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pushedAt = pushedAt
    )
}

// Domain Model to Entity
fun Repository.toRepositoryEntity(ownerLogin: String): RepositoryEntity {
    return RepositoryEntity(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        htmlUrl = htmlUrl,
        language = language,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        watchersCount = watchersCount,
        size = size,
        defaultBranch = defaultBranch,
        openIssuesCount = openIssuesCount,
        isPrivate = isPrivate,
        isFork = isFork,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pushedAt = pushedAt,
        ownerLogin = ownerLogin
    )
}

// Entity to Domain Model
fun RepositoryEntity.toRepository(): Repository {
    return Repository(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        htmlUrl = htmlUrl,
        language = language,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        watchersCount = watchersCount,
        size = size,
        defaultBranch = defaultBranch,
        openIssuesCount = openIssuesCount,
        isPrivate = isPrivate,
        isFork = isFork,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pushedAt = pushedAt
    )
}

// DTO to Entity directly
fun RepositoryDto.toRepositoryEntity(ownerLogin: String): RepositoryEntity {
    return RepositoryEntity(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        htmlUrl = htmlUrl,
        language = language,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        watchersCount = watchersCount,
        size = size,
        defaultBranch = defaultBranch,
        openIssuesCount = openIssuesCount,
        isPrivate = isPrivate,
        isFork = isFork,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pushedAt = pushedAt,
        ownerLogin = ownerLogin
    )
}
