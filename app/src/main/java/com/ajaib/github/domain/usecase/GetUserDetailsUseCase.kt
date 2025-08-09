package com.ajaib.github.domain.usecase

import com.ajaib.github.domain.model.User
import com.ajaib.github.domain.repository.UserRepository
import com.ajaib.github.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserDetailsUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(username: String): Flow<Resource<User>> {
        return repository.getUserDetails(username)
    }
}