package com.ajaib.github.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.ajaib.github.data.local.dao.RepositoryDao
import com.ajaib.github.data.local.dao.UserDao
import com.ajaib.github.data.mappers.toUser
import com.ajaib.github.data.mappers.toUserEntity
import com.ajaib.github.data.remote.api.GitHubApiService
import com.ajaib.github.data.remote.dto.UserDto
import com.ajaib.github.domain.model.User
import com.ajaib.github.utils.Resource
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class UserRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var api: GitHubApiService

    @Mock
    private lateinit var userDao: UserDao

    @Mock
    private lateinit var repositoryDao: RepositoryDao

    private lateinit var repository: UserRepositoryImpl

    private val testUserDto = UserDto(
        id = 1,
        login = "octocat",
        avatarUrl = "https://github.com/images/error/octocat_happy.gif",
        htmlUrl = "https://github.com/octocat",
        type = "User",
        name = "The Octocat",
        company = "GitHub",
        blog = "https://github.blog",
        location = "San Francisco",
        email = "gg.com",
        bio = "GitHub mascot",
        publicRepos = 8,
        publicGists = 8,
        followers = 9000,
        following = 9,
        createdAt = "2011-01-25T18:44:36Z"
    )

    private val testUserEntity = testUserDto.toUserEntity()
    private val testUser = testUserDto.toUser()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = UserRepositoryImpl(api, userDao, repositoryDao)
    }

    @Test
    fun `getUsers_should_emit_loading_then_success_with_fresh_data`() = runTest {
        // Force cache expired to trigger API fetch
        repository.lastUserFetchTime = 0L

        // Arrange mocks with proper returns
        whenever(userDao.getAllUsers())
            .thenReturn(flowOf(emptyList(), listOf(testUserEntity)))
        whenever(api.getUsers())
            .thenReturn(listOf(testUserDto)) // Ensure this is a non-null list
        whenever(userDao.insertUsers(any()))
            .thenReturn(Unit)

        // Act & Assert
        repository.getUsers().test {
            val loading = awaitItem()
            assertTrue(loading is Resource.Loading)

            var success: Resource<List<User>>? = null
            do {
                val emission = awaitItem()
                if (emission is Resource.Success && !emission.data.isNullOrEmpty()) {
                    success = emission
                    break
                } else if (emission is Resource.Error) {
                    fail("Unexpected error emitted: ${emission.message}")
                }
            } while (true)

            assertNotNull(success)
            assertEquals(1, success!!.data?.size)
            assertEquals("octocat", success.data?.first()?.login)

            cancelAndConsumeRemainingEvents()
        }

        verify(api).getUsers()
        verify(userDao).insertUsers(any())
    }



    @Test
    fun `getUsers_should_emit_cached_data_when_API_fails`() = runTest {
        // Arrange
        val cachedUsers = listOf(testUserEntity)
        whenever(userDao.getAllUsers()).thenReturn(flowOf(cachedUsers))
        doThrow(IOException("Network error")).`when`(api).getUsers()

        // Set lastUserFetchTime to recent time to simulate valid cache
        repository.lastUserFetchTime = System.currentTimeMillis()

        // Act & Assert
        repository.getUsers().test {
            val loading = awaitItem()
            println("TestLog: Loading state: $loading")
            assertTrue(loading is Resource.Loading)

            val cachedSuccess = awaitItem()
            println("TestLog: CachedSuccess state: $cachedSuccess")
            assertTrue(cachedSuccess is Resource.Success)
            assertEquals(1, cachedSuccess.data?.size)

            awaitComplete()
        }
    }

    @Test
    fun `searchUsers_shouldHandleRateLimitErrorCorrectly`() = runTest {
        // Arrange
        val query = "octocat"
        val httpException = HttpException(Response.error<Any>(403, mock()))
        whenever(userDao.searchUsers(query)).thenReturn(flowOf(emptyList()))
        whenever(api.searchUsers(query)).thenThrow(httpException)

        // Act & Assert
        repository.searchUsers(query).test {
            val loading = awaitItem()
            assertTrue(loading is Resource.Loading)

            val error = awaitItem()
            assertTrue(error is Resource.Error)
            assertTrue(error.message?.contains("Rate limit exceeded") == true)

            awaitComplete()
        }
    }

    @Test
    fun `searchUsers_should_return_empty_list_for_blank_query`() = runTest {
        // Act & Assert
        repository.searchUsers("").test {
            val loading = awaitItem()
            assertTrue(loading is Resource.Loading)

            val success = awaitItem()
            assertTrue(success is Resource.Success)
            assertEquals(0, success.data?.size)

            awaitComplete()
        }

        verifyNoInteractions(api)
    }

    @Test
    fun `getUserDetails_should_emit_cached_data_then_fresh_data`() = runTest {
        // Arrange
        val username = "octocat"
        whenever(userDao.getUserByLogin(username))
            .thenReturn(null)
            .thenReturn(testUserEntity)
        whenever(api.getUser(username)).thenReturn(testUserDto)
        whenever(userDao.insertUser(any())).thenReturn(Unit)

        // Act & Assert
        repository.getUserDetails(username).test {
            val loading = awaitItem()
            assertTrue(loading is Resource.Loading)

            val success = awaitItem()
            assertTrue(success is Resource.Success)
            assertEquals("octocat", success.data?.login)

            awaitComplete()
        }

        verify(api).getUser(username)
        verify(userDao).insertUser(any())
    }

    @Test
    fun `getUserDetails_should_handle_user_not_found_error`() = runTest {
        // Arrange
        val username = "nonexistentuser"
        val httpException = HttpException(Response.error<Any>(404, mock()))
        whenever(userDao.getUserByLogin(username)).thenReturn(null)
        whenever(api.getUser(username)).thenThrow(httpException)

        // Act & Assert
        repository.getUserDetails(username).test {
            val loading = awaitItem()
            assertTrue(loading is Resource.Loading)

            val error = awaitItem()
            assertTrue(error is Resource.Error)
            assertTrue(error.message?.contains("User not found") == true)

            awaitComplete()
        }
    }
}
