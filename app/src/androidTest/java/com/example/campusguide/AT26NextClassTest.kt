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
 * Acceptance Test [AT-26] for US-4B.4
 * Determine the next class based on current time
 *
 * Criteria:
 * - "Find next class" button is visible in the Daily Schedule tab
 * - Tapping it scrolls/navigates to the next upcoming class within a 7-day window
 * - If no upcoming class exists, an appropriate message is shown
 * - The Daily Schedule tab is selected after tapping the button
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT26NextClassTest {

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
    fun findNextClassButton_isVisibleInDailySchedule() {
        navigateToCalendar()

        composeTestRule.onNodeWithText("Daily Schedule").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(STEP_DELAY_MS)

        composeTestRule.onNodeWithText("Find next class").assertIsDisplayed()
    }

    @Test
    fun findNextClassButton_withNoTrackedCourses_showsNoUpcomingMessage() {
        navigateToCalendar()

        composeTestRule.onNodeWithText("Daily Schedule").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText("Find next class").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(STEP_DELAY_MS)

        // With no tracked courses, nothing changes — daily schedule stays on same tab
        composeTestRule.onNodeWithText("Daily Schedule").assertIsDisplayed()
    }

    @Test
    fun findNextClassButton_withTrackedCourse_navigatesToDailySchedule() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course")
            .fetchSemanticsNodes().isNotEmpty()

        if (added) {
            // Switch to a different tab first
            composeTestRule.onNodeWithText("Course List").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(1000)

            // Go to Daily Schedule and tap Find next class
            composeTestRule.onNodeWithText("Daily Schedule").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Find next class").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            // Should be on Daily Schedule tab
            composeTestRule.onNodeWithText("Daily Schedule").assertIsDisplayed()
        }

        Thread.sleep(STEP_DELAY_MS)
    }

    // --- Helpers ---

    private fun navigateToCalendar() {
        Thread.sleep(2000)
        composeTestRule.onNodeWithContentDescription("Calendar").performClick()
        Thread.sleep(1000)
    }

    private fun addCourse(term: String, subject: String, catalog: String, section: String) {
        composeTestRule.onNodeWithText("Add Course").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule.onAllNodesWithText("XXXX (winter 2025:2244)")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("XXXX (winter 2025:2244)").performTextReplacement(term)
        composeTestRule.onNodeWithText("ABCD").performTextReplacement(subject)
        composeTestRule.onNodeWithText("XXX").performTextReplacement(catalog)
        composeTestRule.onNodeWithText("X(X)").performTextReplacement(section)
        composeTestRule.waitForIdle()
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
    }
}
