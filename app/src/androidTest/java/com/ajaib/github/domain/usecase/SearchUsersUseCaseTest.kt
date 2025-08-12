package com.ajaib.github.domain.usecase

import app.cash.turbine.test
import com.ajaib.github.domain.model.User
import com.ajaib.github.domain.repository.UserRepository
import com.ajaib.github.utils.Resource
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SearchUsersUseCaseTest {

    @Mock
    private lateinit var repository: UserRepository

    private lateinit var useCase: SearchUsersUseCase

    private val testUser = User(
        id = 1,
        login = "testuser",
        avatarUrl = "https://example.com/avatar.png",
        htmlUrl = "https://github.com/testuser",
        type = "User"
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = SearchUsersUseCase(repository)
    }

    @Test
    fun `invoke_should_call_repository_searchUsers_with_trimmed_query`() = runTest {
        // Arrange
        val query = " octocat "
        val trimmedQuery = "octocat"
        val expectedResult = Resource.Success(listOf(testUser))
        whenever(repository.searchUsers(trimmedQuery)).thenReturn(flowOf(expectedResult))

        // Act & Assert
        useCase(query).test {
            val result = awaitItem()
            assertTrue(result is Resource.Success)
            assertEquals(1, result.data?.size)
            awaitComplete()
        }

        verify(repository).searchUsers(trimmedQuery)
    }

    @Test
    fun `invoke_should_handle_empty_query`() = runTest {
        // Arrange
        val query = ""
        val expectedResult = Resource.Success(emptyList<User>())
        whenever(repository.searchUsers(query)).thenReturn(flowOf(expectedResult))

        // Act & Assert
        useCase(query).test {
            val result = awaitItem()
            assertTrue(result is Resource.Success)
            assertEquals(0, result.data?.size)
            awaitComplete()
        }

        verify(repository).searchUsers(query)
    }
}
