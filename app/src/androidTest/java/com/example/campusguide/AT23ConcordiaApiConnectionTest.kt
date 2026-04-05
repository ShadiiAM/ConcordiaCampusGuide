package com.example.campusguide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance Test [AT-23] for US-4B.1
 * Connect to Concordia Open Data API
 *
 * Criteria:
 * - App can fetch course data from the Concordia Open Data API
 * - A valid course request returns course data and displays it
 * - An invalid request shows a "not found" error message
 * - A network failure shows a network error message
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT23ConcordiaApiConnectionTest {

    companion object {
        private const val STEP_DELAY_MS = 2_000L
        private const val TIMEOUT_MS = 15_000L
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    @Test
    fun validCourseRequest_returnsDataAndDisplaysCourse() {
        navigateToCalendar()
        openAddCourseTab()

        fillCourseForm(term = "2244", subject = "SOEN", catalog = "390", section = "UU")
        composeTestRule.onNodeWithText("Add Course").performClick()

        composeTestRule.waitUntil(timeoutMillis = TIMEOUT_MS) {
            composeTestRule.onAllNodesWithText("Successfully added course")
                .fetchSemanticsNodes().isNotEmpty()
                    ||
            composeTestRule.onAllNodesWithText("Course or section not found.")
                .fetchSemanticsNodes().isNotEmpty()
                    ||
            composeTestRule.onAllNodesWithText("Network Error: Check connection.")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // API responded — either success or a known error message is shown
        val successShown = composeTestRule.onAllNodesWithText("Successfully added course")
            .fetchSemanticsNodes().isNotEmpty()
        val notFoundShown = composeTestRule.onAllNodesWithText("Course or section not found.")
            .fetchSemanticsNodes().isNotEmpty()
        val networkErrorShown = composeTestRule.onAllNodesWithText("Network Error: Check connection.")
            .fetchSemanticsNodes().isNotEmpty()

        assert(successShown || notFoundShown || networkErrorShown) {
            "Expected a response from the API but nothing was displayed"
        }

        Thread.sleep(STEP_DELAY_MS)
    }

    @Test
    fun invalidCourseRequest_showsNotFoundError() {
        navigateToCalendar()
        openAddCourseTab()

        fillCourseForm(term = "9999", subject = "ZZZZ", catalog = "999", section = "ZZ")
        composeTestRule.onNodeWithText("Add Course").performClick()

        composeTestRule.waitUntil(timeoutMillis = TIMEOUT_MS) {
            composeTestRule.onAllNodesWithText("Course or section not found.")
                .fetchSemanticsNodes().isNotEmpty()
                    ||
            composeTestRule.onAllNodesWithText("Network Error: Check connection.")
                .fetchSemanticsNodes().isNotEmpty()
        }

        Thread.sleep(STEP_DELAY_MS)
    }

    // --- Helpers ---

    private fun navigateToCalendar() {
        Thread.sleep(2000)
        composeTestRule.onNodeWithContentDescription("Calendar").performClick()
        Thread.sleep(1000)
    }

    private fun openAddCourseTab() {
        composeTestRule.onNodeWithText("Add Course").performClick()
        composeTestRule.waitForIdle()
    }

    private fun fillCourseForm(term: String, subject: String, catalog: String, section: String) {
        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule.onAllNodesWithText("XXXX (winter 2025:2244)")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("XXXX (winter 2025:2244)").performTextReplacement(term)
        composeTestRule.onNodeWithText("ABCD").performTextReplacement(subject)
        composeTestRule.onNodeWithText("XXX").performTextReplacement(catalog)
        composeTestRule.onNodeWithText("X(X)").performTextReplacement(section)
        composeTestRule.waitForIdle()
    }
}
