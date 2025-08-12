package com.ajaib.github.presentation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.ajaib.github.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun navigation_startsWithUserListScreen() {
        composeTestRule.onNodeWithText("GitHub Users").assertIsDisplayed()
        // Wait until our fake user appears and assert
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("The Octocat")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("The Octocat").assertIsDisplayed()
    }

    @Test
    fun navigation_toUserDetailScreen() {
        // Wait until fake user is on screen
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("The Octocat")
                .fetchSemanticsNodes().isNotEmpty()
        }
        // Click and check for back button on detail screen
        composeTestRule.onNodeWithText("The Octocat").performClick()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun navigation_backFromUserDetailScreen() {
        // Wait until fake user is on screen
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("The Octocat")
                .fetchSemanticsNodes().isNotEmpty()
        }
        // Navigate to detail
        composeTestRule.onNodeWithText("The Octocat").performClick()

        // Return
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // Check list screen title again
        composeTestRule.onNodeWithText("GitHub Users").assertIsDisplayed()
    }
}

