package com.ajaib.github.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.ajaib.github.domain.model.User
import com.ajaib.github.domain.repository.UserRepository
import com.ajaib.github.domain.usecase.GetUsersUseCase
import com.ajaib.github.domain.usecase.SearchUsersUseCase
import com.ajaib.github.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class UserListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: UserRepository

    private lateinit var getUsersUseCase: GetUsersUseCase
    private lateinit var searchUsersUseCase: SearchUsersUseCase
    private lateinit var viewModel: UserListViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testUser = User(
        id = 1,
        login = "testuser",
        avatarUrl = "https://example.com/avatar.png",
        htmlUrl = "https://github.com/testuser",
        type = "User",
        name = "Test User",
        company = "Test Company",
        blog = "https://test.com",
        location = "Test City",
        email = null,
        bio = "Test bio",
        publicRepos = 10,
        publicGists = 5,
        followers = 100,
        following = 50,
        createdAt = "2020-01-01T00:00:00Z"
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        whenever(repository.getUsers())
            .thenReturn(flowOf(Resource.Success(listOf(testUser))))

        getUsersUseCase = GetUsersUseCase(repository)
        searchUsersUseCase = SearchUsersUseCase(repository)

        viewModel = UserListViewModel(getUsersUseCase, searchUsersUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel_should_load_users_on_initialization`() = runTest {
        viewModel.users.test {
            // 1️⃣ First emission should be loading
            val loadingState = awaitItem()
            println("First emission: $loadingState")
            assertTrue(loadingState is Resource.Loading)

            // 2️⃣ Then should be success
            val successState = awaitItem()
            println("Second emission: $successState")
            assertTrue(successState is Resource.Success)
            assertEquals(1, successState.data?.size)
            assertEquals("testuser", successState.data?.first()?.login)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `updateSearchQuery_should_trigger_search_users_use_case`() = runTest {
        val searchResult = Resource.Success(listOf(testUser.copy(login = "octocat")))

        whenever(repository.searchUsers(any()))
            .thenReturn(flowOf(searchResult))

        viewModel.updateSearchQuery("octocat")

        viewModel.searchQuery.test {
            assertEquals("octocat", awaitItem())
        }

        viewModel.users.test {
            // First emission should be loading
            val loadingState = awaitItem()
            assertTrue(loadingState is Resource.Loading)

            // Second emission should be success
            val successState = awaitItem()
            assertTrue(successState is Resource.Success)
            assertEquals("octocat", successState.data?.first()?.login)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `clearSearch_should_reset_search_query_to_empty`() = runTest {
        viewModel.updateSearchQuery("test")
        viewModel.clearSearch()

        viewModel.searchQuery.test {
            assertEquals("", awaitItem())
        }

        // Optional: also check that clearing triggers Loading -> Success of full list
        viewModel.users.test {
            val loadingState = awaitItem()
            assertTrue(loadingState is Resource.Loading)

            val successState = awaitItem()
            assertTrue(successState is Resource.Success)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `empty_search_query_should_trigger_get_users_use_case`() = runTest {
        viewModel.updateSearchQuery("test")

        // Stub repository to return default user again for empty query
        whenever(repository.getUsers())
            .thenReturn(flowOf(Resource.Success(listOf(testUser))))

        viewModel.updateSearchQuery("")

        viewModel.users.test {
            val loadingState = awaitItem()
            assertTrue(loadingState is Resource.Loading)

            val successState = awaitItem()
            assertTrue(successState is Resource.Success)
            assertEquals("testuser", successState.data?.first()?.login)

            cancelAndConsumeRemainingEvents()
        }
    }

}
