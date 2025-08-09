package com.ajaib.github.domain.usecase

import com.ajaib.github.domain.model.User
import com.ajaib.github.domain.repository.UserRepository
import com.ajaib.github.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(query: String): Flow<Resource<List<User>>> {
        return repository.searchUsers(query.trim())
    }
}