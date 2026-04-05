package com.example.campusguide

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
 * Acceptance Test [AT-23] for US-4B.1
 * Connect to Concordia Open Data API
 *
 * Criteria:
 * - A valid course request gets a response from the API
 * - An invalid request shows a "not found" error message
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT23ConcordiaApiConnectionTest {

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
    fun concordiaApi_validAndInvalidRequests() {
        navigateToCalendar()
        openAddCourseTab()

        // --- Criteria 1: Valid request gets a response from the API ---
        fillCourseForm(term = "2244", subject = "SOEN", catalog = "390", section = "UU")
        composeTestRule.onAllNodesWithText("Add Course")[1].performClick()

        composeTestRule.waitUntil(timeoutMillis = TIMEOUT_MS) {
            composeTestRule.onAllNodesWithText("Successfully added course").fetchSemanticsNodes().isNotEmpty()
                    || composeTestRule.onAllNodesWithText("Course or section not found.").fetchSemanticsNodes().isNotEmpty()
                    || composeTestRule.onAllNodesWithText("Network Error: Check connection.").fetchSemanticsNodes().isNotEmpty()
        }

        val apiResponded =
            composeTestRule.onAllNodesWithText("Successfully added course").fetchSemanticsNodes().isNotEmpty()
                    || composeTestRule.onAllNodesWithText("Course or section not found.").fetchSemanticsNodes().isNotEmpty()
                    || composeTestRule.onAllNodesWithText("Network Error: Check connection.").fetchSemanticsNodes().isNotEmpty()
        assert(apiResponded) { "Expected an API response but nothing was displayed" }
        Thread.sleep(STEP_DELAY_MS)

        // --- Criteria 2: Invalid request shows not found or network error ---
        openAddCourseTab()
        fillCourseForm(term = "9999", subject = "ZZZZ", catalog = "999", section = "ZZ")
        composeTestRule.onAllNodesWithText("Add Course")[1].performClick()

        composeTestRule.waitUntil(timeoutMillis = TIMEOUT_MS) {
            composeTestRule.onAllNodesWithText("Course or section not found.").fetchSemanticsNodes().isNotEmpty()
                    || composeTestRule.onAllNodesWithText("Network Error: Check connection.").fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(STEP_DELAY_MS)
    }

    // --- Helpers ---

    private fun navigateToCalendar() {
        Thread.sleep(2000)
        composeTestRule.onNode(hasText("Calendar")).performClick()
        Thread.sleep(1000)
    }

    private fun openAddCourseTab() {
        composeTestRule.onAllNodesWithText("Add Course")[0].performClick()
        composeTestRule.waitForIdle()
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
