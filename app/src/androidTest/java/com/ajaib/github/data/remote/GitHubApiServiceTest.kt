package com.ajaib.github.data.remote

import com.ajaib.github.data.remote.api.GitHubApiService
import com.google.gson.GsonBuilder
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GitHubApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: GitHubApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()

        apiService = retrofit.create(GitHubApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getUsers_should_return_list_of_users`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                [
                    {
                        "id": 1,
                        "login": "octocat",
                        "avatar_url": "https://github.com/images/error/octocat_happy.gif",
                        "html_url": "https://github.com/octocat",
                        "type": "User"
                    }
                ]
            """.trimIndent())

        mockWebServer.enqueue(mockResponse)

        // Act
        val users = apiService.getUsers()

        // Assert
        assertEquals(1, users.size)
        assertEquals("octocat", users[0].login)
        assertEquals("User", users[0].type)
        assertEquals(1, users[0].id)

        val request = mockWebServer.takeRequest()
        assertEquals("/users?since=0&per_page=30", request.path)
        assertEquals("GET", request.method)
    }

    @Test
    fun `searchUsers_should_return_search_results`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "total_count": 1,
                    "incomplete_results": false,
                    "items": [
                        {
                            "id": 1,
                            "login": "octocat",
                            "avatar_url": "https://github.com/images/error/octocat_happy.gif",
                            "html_url": "https://github.com/octocat",
                            "type": "User"
                        }
                    ]
                }
            """.trimIndent())

        mockWebServer.enqueue(mockResponse)

        // Act
        val searchResult = apiService.searchUsers("octocat")

        // Assert
        assertEquals(1, searchResult.totalCount)
        assertEquals(false, searchResult.incompleteResults)
        assertEquals(1, searchResult.items.size)
        assertEquals("octocat", searchResult.items[0].login)

        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("/search/users") == true)
        assertTrue(request.path?.contains("q=octocat") == true)
    }

    @Test
    fun `getUser_should_return_user_details`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "id": 1,
                    "login": "octocat",
                    "avatar_url": "https://github.com/images/error/octocat_happy.gif",
                    "html_url": "https://github.com/octocat",
                    "type": "User",
                    "name": "The Octocat",
                    "company": "GitHub",
                    "blog": "https://github.blog",
                    "location": "San Francisco",
                    "email": null,
                    "bio": "GitHub mascot",
                    "public_repos": 8,
                    "public_gists": 8,
                    "followers": 9000,
                    "following": 9,
                    "created_at": "2011-01-25T18:44:36Z"
                }
            """.trimIndent())

        mockWebServer.enqueue(mockResponse)

        // Act
        val user = apiService.getUser("octocat")

        // Assert
        assertEquals("octocat", user.login)
        assertEquals("The Octocat", user.name)
        assertEquals("GitHub", user.company)
        assertEquals("San Francisco", user.location)
        assertEquals(8, user.publicRepos)
        assertEquals(9000, user.followers)

        val request = mockWebServer.takeRequest()
        assertEquals("/users/octocat", request.path)
        assertEquals("GET", request.method)
    }

    @Test
    fun `getUserRepositories_should_return_user_repositories`() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                [
                    {
                        "id": 1296269,
                        "name": "Hello-World",
                        "full_name": "octocat/Hello-World",
                        "description": "This your first repo!",
                        "html_url": "https://github.com/octocat/Hello-World",
                        "language": "C",
                        "stargazers_count": 80,
                        "forks_count": 9,
                        "watchers_count": 80,
                        "size": 108,
                        "default_branch": "master",
                        "open_issues_count": 0,
                        "private": false,
                        "fork": false,
                        "created_at": "2011-01-26T19:01:12Z",
                        "updated_at": "2011-01-26T19:14:43Z",
                        "pushed_at": "2011-01-26T19:06:43Z"
                    }
                ]
            """.trimIndent())

        mockWebServer.enqueue(mockResponse)

        // Act
        val repositories = apiService.getUserRepositories("octocat")

        // Assert
        assertEquals(1, repositories.size)
        val repo = repositories[0]
        assertEquals("Hello-World", repo.name)
        assertEquals("octocat/Hello-World", repo.fullName)
        assertEquals("This your first repo!", repo.description)
        assertEquals("C", repo.language)
        assertEquals(80, repo.stargazersCount)
        assertEquals(9, repo.forksCount)

        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("/users/octocat/repos") == true)
    }
}
