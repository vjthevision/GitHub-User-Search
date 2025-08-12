package com.ajaib.github.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.ajaib.github.data.local.dao.UserDao
import com.ajaib.github.data.local.database.GitHubDatabase
import com.ajaib.github.data.local.entities.UserEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: GitHubDatabase
    private lateinit var userDao: UserDao

    private val testUser = UserEntity(
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
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            GitHubDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        userDao = database.userDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertUser_and_getUserByLogin() = runTest {
        // Act
        userDao.insertUser(testUser)
        val retrievedUser = userDao.getUserByLogin("testuser")

        // Assert
        assertNotNull(retrievedUser)
        assertEquals("testuser", retrievedUser?.login)
        assertEquals("Test User", retrievedUser?.name)
        assertEquals("Test Company", retrievedUser?.company)
    }

    @Test
    fun insertUsers_and_getAllUsers() = runTest {
        // Arrange
        val users = listOf(
            testUser,
            testUser.copy(id = 2, login = "testuser2", name = "Test User 2")
        )

        // Act
        userDao.insertUsers(users)

        // Assert
        userDao.getAllUsers().test {
            val allUsers = awaitItem()
            assertEquals(2, allUsers.size)
            assertEquals("testuser", allUsers[0].login)
            assertEquals("testuser2", allUsers[1].login)
        }
    }

    @Test
    fun searchUsers_returnsMatchingUsers() = runTest {
        // Arrange
        val users = listOf(
            testUser,
            testUser.copy(id = 2, login = "octocat", name = "The Octocat"),
            testUser.copy(id = 3, login = "defunkt", name = "Chris Wanstrath")
        )
        userDao.insertUsers(users)

        // Act & Assert
        userDao.searchUsers("oct").test {
            val searchResults = awaitItem()
            assertEquals(1, searchResults.size)
            assertEquals("octocat", searchResults[0].login)
        }
    }

    @Test
    fun deleteAllUsers_removesAllUsers() = runTest {
        // Arrange
        userDao.insertUser(testUser)

        // Act
        userDao.deleteAllUsers()

        // Assert
        userDao.getAllUsers().test {
            val allUsers = awaitItem()
            assertEquals(0, allUsers.size)
        }
    }

    @Test
    fun getUserCount_returnsCorrectCount() = runTest {
        // Arrange
        val users = listOf(
            testUser,
            testUser.copy(id = 2, login = "testuser2")
        )
        userDao.insertUsers(users)

        // Act
        val count = userDao.getUserCount()

        // Assert
        assertEquals(2, count)
    }

    @Test
    fun updateUser_modifiesExistingUser() = runTest {
        // Arrange
        userDao.insertUser(testUser)

        // Act
        val updatedUser = testUser.copy(name = "Updated Name", company = "Updated Company")
        userDao.updateUser(updatedUser)

        // Assert
        val retrievedUser = userDao.getUserByLogin("testuser")
        assertEquals("Updated Name", retrievedUser?.name)
        assertEquals("Updated Company", retrievedUser?.company)
    }
}
