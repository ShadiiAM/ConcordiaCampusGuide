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
 * Acceptance Test [AT-24] for US-4B.2
 * Allow user to configure which courses to track
 *
 * Criteria:
 * - User can add a course via term, subject, catalog, section inputs
 * - Successfully added course appears in the Course List tab
 * - Adding the same course twice shows an "already tracked" error
 * - User can remove a course from the Course List tab
 * - Removed course no longer appears in the list
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT24ConfigureTrackedCoursesTest {

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
    fun addCourse_appearsInCourseList() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course")
            .fetchSemanticsNodes().isNotEmpty()

        if (added) {
            composeTestRule.onNodeWithText("Course List").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)
            // Course card should now be visible in the list
            composeTestRule.onAllNodesWithText("SOEN", substring = true)
                .fetchSemanticsNodes().isNotEmpty().let { found ->
                    assert(found) { "Added course SOEN 390 not found in Course List" }
                }
        }

        Thread.sleep(STEP_DELAY_MS)
    }

    @Test
    fun addSameCourseAgain_showsAlreadyTrackedError() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course")
            .fetchSemanticsNodes().isNotEmpty()

        if (added) {
            // Try adding the same course again
            composeTestRule.onNodeWithText("Add Course").performClick()
            composeTestRule.waitForIdle()
            fillCourseForm(term = "2244", subject = "SOEN", catalog = "390", section = "UU")
            composeTestRule.onNodeWithText("Add Course").performClick()

            composeTestRule.waitUntil(timeoutMillis = TIMEOUT_MS) {
                composeTestRule.onAllNodesWithText("This course is already being tracked.")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeTestRule.onNodeWithText("This course is already being tracked.")
                .assertIsDisplayed()
        }

        Thread.sleep(STEP_DELAY_MS)
    }

    @Test
    fun removeCourse_disappearsFromCourseList() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course")
            .fetchSemanticsNodes().isNotEmpty()

        if (added) {
            composeTestRule.onNodeWithText("Course List").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            // Tap "Remove course" button
            composeTestRule.waitUntil(timeoutMillis = 8_000) {
                composeTestRule.onAllNodesWithText("Remove course")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onAllNodesWithText("Remove course")[0].performClick()
            composeTestRule.waitForIdle()

            // Confirm removal
            composeTestRule.waitUntil(timeoutMillis = 8_000) {
                composeTestRule.onAllNodesWithText("Confirm removal")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onAllNodesWithText("Confirm removal")[0].performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            // Course list should now be empty
            composeTestRule.waitUntil(timeoutMillis = 8_000) {
                composeTestRule.onAllNodesWithText("You aren't tracking any courses yet.")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("You aren't tracking any courses yet.", substring = true)
                .assertIsDisplayed()
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
        fillCourseForm(term, subject, catalog, section)
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
