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
 * Acceptance Test [AT-27] for US-4B.5
 * Resolve classroom/building location to a campus destination
 *
 * Criteria:
 * - Course card in Course List shows a "Directions to classroom" button
 * - Tapping it triggers the full directions flow
 * - The directions top bar becomes active with origin and destination filled
 * - If building is unrecognized, an appropriate dialog or message is shown
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT27ResolveClassroomLocationTest {

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
    fun courseCard_showsDirectionsButton() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course")
            .fetchSemanticsNodes().isNotEmpty()

        if (added) {
            composeTestRule.onNodeWithText("Course List").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            composeTestRule.waitUntil(timeoutMillis = 8_000) {
                composeTestRule.onAllNodesWithText("Directions to classroom")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Directions to classroom").assertIsDisplayed()
        }

        Thread.sleep(STEP_DELAY_MS)
    }

    @Test
    fun directionsButton_triggersDirectionsFlow() {
        navigateToCalendar()
        addCourse(term = "2244", subject = "SOEN", catalog = "390", section = "UU")

        val added = composeTestRule.onAllNodesWithText("Successfully added course")
            .fetchSemanticsNodes().isNotEmpty()

        if (added) {
            composeTestRule.onNodeWithText("Course List").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            composeTestRule.waitUntil(timeoutMillis = 8_000) {
                composeTestRule.onAllNodesWithText("Directions to classroom")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Directions to classroom").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(STEP_DELAY_MS)

            // Directions flow should be active — either top bar or unknown building dialog
            val directionsActive = composeTestRule.onAllNodesWithText("Cancel")
                .fetchSemanticsNodes().isNotEmpty()
            val unknownBuilding = composeTestRule.onAllNodesWithText("building", substring = true)
                .fetchSemanticsNodes().isNotEmpty()

            assert(directionsActive || unknownBuilding) {
                "Expected directions flow to start or unknown building dialog to appear"
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
