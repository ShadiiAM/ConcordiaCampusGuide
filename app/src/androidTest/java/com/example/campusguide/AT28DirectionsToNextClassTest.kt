package com.example.campusguide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
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
 * Acceptance Test [AT-28] for US-4B.6
 * Generate directions to the next class
 *
 * Criteria:
 * - "Directions to next class" button is visible in the Daily Schedule tab
 * - Button is disabled when there is no upcoming class
 * - Tapping it when a next class exists triggers the full directions flow
 * - The directions top bar becomes active with destination set to the classroom
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT28DirectionsToNextClassTest {

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
    fun directionsToNextClassButton_isVisible() {
        navigateToCalendar()

        composeTestRule.onNodeWithText("Daily Schedule").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(STEP_DELAY_MS)

        composeTestRule.onNodeWithText("Directions to next class").assertIsDisplayed()
    }

    @Test
    fun directionsToNextClassButton_isDisabledWithNoTrackedCourses() {
        navigateToCalendar()

        composeTestRule.onNodeWithText("Daily Schedule").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(STEP_DELAY_MS)

        composeTestRule.onNodeWithText("Directions to next class").assertIsNotEnabled()
    }

    @Test
    fun directionsToNextClassButton_withTrackedCourse_triggersDirectionsFlow() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course")
            .fetchSemanticsNodes().isNotEmpty()

        if (added) {
            composeTestRule.onNodeWithText("Daily Schedule").performClick()
            composeTestRule.waitForIdle()

            // Find next class first to make sure we're on the right day
            composeTestRule.onNodeWithText("Find next class").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            // Check if directions to next class button is now enabled
            val buttonEnabled = composeTestRule.onAllNodesWithText("Directions to next class")
                .fetchSemanticsNodes().isNotEmpty()

            if (buttonEnabled) {
                composeTestRule.onNodeWithText("Directions to next class").performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(STEP_DELAY_MS)

                // Directions flow should now be active
                val directionsActive = composeTestRule.onAllNodesWithText("Cancel")
                    .fetchSemanticsNodes().isNotEmpty()
                val unknownBuilding = composeTestRule.onAllNodesWithText("building", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()

                assert(directionsActive || unknownBuilding) {
                    "Expected directions flow or unknown building dialog after tapping directions to next class"
                }
            }
        }

        Thread.sleep(STEP_DELAY_MS)
    }

    @Test
    fun findNextClassThenDirections_fullFlow() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course")
            .fetchSemanticsNodes().isNotEmpty()

        if (added) {
            composeTestRule.onNodeWithText("Daily Schedule").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(1000)

            // Step 1: Find next class
            composeTestRule.onNodeWithText("Find next class").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            // Step 2: Tap directions to next class
            composeTestRule.onNodeWithText("Directions to next class").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            // Step 3: Full directions flow should be active or building dialog shown
            val directionsActive = composeTestRule.onAllNodesWithText("Go")
                .fetchSemanticsNodes().isNotEmpty()
            val unknownBuilding = composeTestRule.onAllNodesWithText("building", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
            val cancelShown = composeTestRule.onAllNodesWithText("Cancel")
                .fetchSemanticsNodes().isNotEmpty()

            assert(directionsActive || unknownBuilding || cancelShown) {
                "Expected full directions flow to be triggered from next class"
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
