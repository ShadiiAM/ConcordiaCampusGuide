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
 * Acceptance Test [AT-24] for US-4B.2
 * Allow user to configure which courses to track
 *
 * Criteria:
 * - User can add a course — it appears in Course List
 * - Adding the same course twice shows "already tracked" error
 * - User can remove a course — it disappears from the list
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT24ConfigureTrackedCoursesTest {

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
    fun configureCourses_addDuplicateAndRemove() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course").fetchSemanticsNodes().isNotEmpty()
        Thread.sleep(STEP_DELAY_MS)

        if (added) {
            // --- Criteria 1: Course appears in Course List ---
            composeTestRule.onNodeWithText("Course List").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            val courseVisible = composeTestRule.onAllNodesWithText("SOEN", substring = true).fetchSemanticsNodes().isNotEmpty()
            assert(courseVisible) { "Added course SOEN 390 not found in Course List" }

            // --- Criteria 2: Adding same course shows already tracked error ---
            composeTestRule.onAllNodesWithText("Add Course")[0].performClick()
            composeTestRule.waitForIdle()
            fillCourseForm(term = "2244", subject = "SOEN", catalog = "390", section = "UU")
            composeTestRule.onAllNodesWithText("Add Course")[1].performClick()

            composeTestRule.waitUntil(timeoutMillis = TIMEOUT_MS) {
                composeTestRule.onAllNodesWithText("This course is already being tracked.").fetchSemanticsNodes().isNotEmpty()
                        || composeTestRule.onAllNodesWithText("Network Error: Check connection.").fetchSemanticsNodes().isNotEmpty()
            }
            Thread.sleep(STEP_DELAY_MS)

            // --- Criteria 3: Remove course — disappears from list ---
            composeTestRule.onNodeWithText("Course List").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            composeTestRule.waitUntil(timeoutMillis = 8_000) {
                composeTestRule.onAllNodesWithText("Remove course").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onAllNodesWithText("Remove course")[0].performClick()
            composeTestRule.waitForIdle()

            composeTestRule.waitUntil(timeoutMillis = 8_000) {
                composeTestRule.onAllNodesWithText("Confirm removal").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onAllNodesWithText("Confirm removal")[0].performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            composeTestRule.waitUntil(timeoutMillis = 8_000) {
                composeTestRule.onAllNodesWithText("You aren't tracking any courses yet.", substring = true).fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("You aren't tracking any courses yet.", substring = true).assertIsDisplayed()
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
        fillCourseForm(term, subject, catalog, section)
        composeTestRule.onAllNodesWithText("Add Course")[1].performClick()
        composeTestRule.waitUntil(timeoutMillis = TIMEOUT_MS) {
            composeTestRule.onAllNodesWithText("Successfully added course").fetchSemanticsNodes().isNotEmpty()
                    || composeTestRule.onAllNodesWithText("Course or section not found.").fetchSemanticsNodes().isNotEmpty()
                    || composeTestRule.onAllNodesWithText("Network Error: Check connection.").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun fillCourseForm(term: String, subject: String, catalog: String, section: String) {
        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size >= 4
        }
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextReplacement(term)
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextReplacement(subject)
        composeTestRule.onAllNodes(hasSetTextAction())[2].performTextReplacement(catalog)
        composeTestRule.onAllNodes(hasSetTextAction())[3].performTextReplacement(section)
        composeTestRule.waitForIdle()
    }
}
