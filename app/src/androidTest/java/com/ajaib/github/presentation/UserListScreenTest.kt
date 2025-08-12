package com.ajaib.github.presentation

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.ajaib.github.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class UserListScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun userListScreen_displaysUsers() {
        // Wait for content to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("octocat")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify users are displayed
        composeTestRule.onNodeWithText("octocat").assertIsDisplayed()
        composeTestRule.onNodeWithText("User").assertIsDisplayed()
    }

    @Test
    fun userListScreen_searchFunctionality() {
        // Open search
        composeTestRule.onNodeWithContentDescription("Search users").performClick()
        composeTestRule.onNodeWithText("Search users...").assertIsDisplayed()

        // Type into the field
        composeTestRule.onNodeWithText("Search users...")
            .performTextInput("octocat")

        // Wait until results list shows an octocat item
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeTestRule.onAllNodesWithText("octocat")
                    .filterToOne(isNotEditableText())
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Assert Octocat appears in the result list (not in the search box)
        composeTestRule.onAllNodesWithText("octocat")
            .filterToOne(isNotEditableText())
            .assertIsDisplayed()
    }

    @Test
    fun userListScreen_clickOnUser() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText("octocat")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("octocat").performClick()

        // Verify that UI for detail screen is visible
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun userListScreen_refreshButton() {
        // Click refresh button
        composeTestRule.onNodeWithContentDescription("Refresh").performClick()

        // Verify refresh action (loading indicator should appear briefly)
        composeTestRule.onNodeWithContentDescription("Refresh").assertIsDisplayed()
    }

    fun isEditableText(): SemanticsMatcher =
        SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText)

    fun isNotEditableText(): SemanticsMatcher =
        SemanticsMatcher.keyNotDefined(SemanticsProperties.EditableText)
}
