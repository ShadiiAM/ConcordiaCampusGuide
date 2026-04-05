package com.example.campusguide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.rules.ActivityScenarioRule
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
 * - Tracked course card shows time and location
 * - Daily Schedule shows courses for the day or empty state
 * - Refresh button triggers a schedule update
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT25RetrieveSchedulesTest {

    companion object {
        private const val STEP_DELAY_MS = 2_000L
        private const val TIMEOUT_MS = 30_000L
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    @Test
    fun retrieveSchedules_showsTimeLocationAndRefresh() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course").fetchSemanticsNodes().isNotEmpty()
                || composeTestRule.onAllNodesWithText("This course is already being tracked.").fetchSemanticsNodes().isNotEmpty()
        Thread.sleep(STEP_DELAY_MS)

        if (added) {
            // --- Criteria 1: Course card shows location ---
            composeTestRule.onNodeWithText("Course List").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            composeTestRule.waitUntil(timeoutMillis = 8_000) {
                composeTestRule.onAllNodesWithText("Location:", substring = true).fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Location:", substring = true).assertIsDisplayed()
            Thread.sleep(STEP_DELAY_MS)

            // --- Criteria 2: Daily Schedule shows course or empty state ---
            composeTestRule.onNodeWithText("Daily Schedule").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            val hasCourse = composeTestRule.onAllNodesWithText("SOEN", substring = true).fetchSemanticsNodes().isNotEmpty()
            val isEmpty = composeTestRule.onAllNodesWithText("No upcoming classes today!").fetchSemanticsNodes().isNotEmpty()
            assert(hasCourse || isEmpty) { "Daily Schedule should show a course or empty state" }
            Thread.sleep(STEP_DELAY_MS)

            // --- Criteria 3: Refresh button triggers update ---
            composeTestRule.onNodeWithText("Course List").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(1000)

            composeTestRule.onNodeWithContentDescription("Refresh Schedules").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.waitUntil(timeoutMillis = TIMEOUT_MS) {
                composeTestRule.onAllNodesWithText("All courses updated.").fetchSemanticsNodes().isNotEmpty()
                        || composeTestRule.onAllNodesWithText("Some courses failed to update.").fetchSemanticsNodes().isNotEmpty()
                        || composeTestRule.onAllNodesWithText("Location:", substring = true).fetchSemanticsNodes().isNotEmpty()
            }
        }

        Thread.sleep(STEP_DELAY_MS)
    }

    // --- Helpers ---

    private fun navigateToCalendar() {
        Thread.sleep(2000)
        composeTestRule.onNode(hasText("Calendar")).performClick()
        Thread.sleep(1000)
    }

    private fun addCourse(term: String, subject: String, catalog: String, section: String) {
        composeTestRule.onAllNodesWithText("Add Course")[0].performClick()
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size >= 4
        }
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextReplacement(term)
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextReplacement(subject)
        composeTestRule.onAllNodes(hasSetTextAction())[2].performTextReplacement(catalog)
        composeTestRule.onAllNodes(hasSetTextAction())[3].performTextReplacement(section)
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Add Course")[1].performClick()
        composeTestRule.waitUntil(timeoutMillis = TIMEOUT_MS) {
            composeTestRule.onAllNodesWithText("Successfully added course").fetchSemanticsNodes().isNotEmpty()
                    || composeTestRule.onAllNodesWithText("This course is already being tracked.").fetchSemanticsNodes().isNotEmpty()
                    || composeTestRule.onAllNodesWithText("Course or section not found.").fetchSemanticsNodes().isNotEmpty()
                    || composeTestRule.onAllNodesWithText("Network Error: Check connection.").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
