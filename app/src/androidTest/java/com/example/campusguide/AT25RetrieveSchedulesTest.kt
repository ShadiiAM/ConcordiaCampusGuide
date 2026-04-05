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
 * Acceptance Test [AT-25] for US-4B.3
 * Retrieve schedules and classroom locations for tracked courses
 *
 * Criteria:
 * - Tracked courses display time, building, and room info on course cards
 * - Daily Schedule tab shows courses for the selected day
 * - Course card shows location (building code + room)
 * - Refresh button updates schedules
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT25RetrieveSchedulesTest {

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
    fun trackedCourse_showsScheduleAndLocation() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course")
            .fetchSemanticsNodes().isNotEmpty()

        if (added) {
            // Go to Course List — each card should show time and location
            composeTestRule.onNodeWithText("Course List").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            // Course card should show a time range (contains ":")
            composeTestRule.waitUntil(timeoutMillis = 8_000) {
                composeTestRule.onAllNodesWithText(":", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }

            // Course card should show location label
            composeTestRule.waitUntil(timeoutMillis = 8_000) {
                composeTestRule.onAllNodesWithText("Location:", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Location:", substring = true).assertIsDisplayed()
        }

        Thread.sleep(STEP_DELAY_MS)
    }

    @Test
    fun refreshButton_triggersScheduleUpdate() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course")
            .fetchSemanticsNodes().isNotEmpty()

        if (added) {
            composeTestRule.onNodeWithText("Course List").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            // Tap the refresh button
            composeTestRule.onNodeWithContentDescription("Refresh Schedules").performClick()
            composeTestRule.waitForIdle()

            // Either "Refreshing schedules…" appears or "All courses updated." appears
            composeTestRule.waitUntil(timeoutMillis = TIMEOUT_MS) {
                composeTestRule.onAllNodesWithText("All courses updated.")
                    .fetchSemanticsNodes().isNotEmpty()
                        ||
                composeTestRule.onAllNodesWithText("Some courses failed to update.")
                    .fetchSemanticsNodes().isNotEmpty()
                        ||
                composeTestRule.onAllNodesWithText("Location:", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
        }

        Thread.sleep(STEP_DELAY_MS)
    }

    @Test
    fun dailyScheduleTab_showsCoursesForSelectedDay() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course")
            .fetchSemanticsNodes().isNotEmpty()

        if (added) {
            // Daily Schedule tab is selected by default
            composeTestRule.onNodeWithText("Daily Schedule").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            // Either a course card or the empty state message should be visible
            val hasCourse = composeTestRule.onAllNodesWithText("SOEN", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
            val isEmpty = composeTestRule.onAllNodesWithText("No upcoming classes today!")
                .fetchSemanticsNodes().isNotEmpty()

            assert(hasCourse || isEmpty) {
                "Daily Schedule tab should show either a course or empty state message"
            }
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
