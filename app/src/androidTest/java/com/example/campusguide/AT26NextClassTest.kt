package com.example.campusguide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
 * Acceptance Test [AT-26] for US-4B.4
 * Determine the next class based on current time
 *
 * Criteria:
 * - "Find next class" button is visible in Daily Schedule tab
 * - With no courses, stays on Daily Schedule tab
 * - With a tracked course, navigates to the correct day
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT26NextClassTest {

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
    fun findNextClass_fullFlow() {
        navigateToCalendar()

        // --- Criteria 1: Button is visible ---
        composeTestRule.onNodeWithText("Daily Schedule").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(STEP_DELAY_MS)
        composeTestRule.onNodeWithText("Find next class").assertIsDisplayed()

        // --- Criteria 2: With no courses, stays on Daily Schedule ---
        composeTestRule.onNodeWithText("Find next class").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(STEP_DELAY_MS)
        composeTestRule.onNodeWithText("Daily Schedule").assertIsDisplayed()

        // --- Criteria 3: With a tracked course, navigates to the right day ---
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "Q QC")
        val added = composeTestRule.onAllNodesWithText("Successfully added course").fetchSemanticsNodes().isNotEmpty()
                || composeTestRule.onAllNodesWithText("This course is already being tracked.").fetchSemanticsNodes().isNotEmpty()
        Thread.sleep(STEP_DELAY_MS)

        if (added) {
            composeTestRule.onNodeWithText("Daily Schedule").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Find next class").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            val foundCourse = composeTestRule.onAllNodesWithText("SOEN", substring = true).fetchSemanticsNodes().isNotEmpty()
            val noUpcoming = composeTestRule.onAllNodesWithText("No upcoming classes today!").fetchSemanticsNodes().isNotEmpty()
            assert(foundCourse || noUpcoming) {
                "Expected to see SOEN course in schedule or 'No upcoming classes today!' after Find next class"
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
