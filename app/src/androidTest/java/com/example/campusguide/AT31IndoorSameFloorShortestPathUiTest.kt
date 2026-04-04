package com.example.campusguide

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
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
 * Acceptance Test [AT-31] for US-5.3
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT31IndoorSameFloorShortestPathUiTest {

    companion object {
        private const val STEP_DELAY_MS = 2_000L
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    @Test
    fun uiFlow_selectDestination_thenStart_thenGo_withVisiblePauses() {
        pauseForDemo(3000)

        // Step 1: Pick destination first (VE.210)
        searchMainBarAndSetDestination(
            query = "VE-2-210",
            expectedSuggestion = "VE.210",
        )
        waitForText("Tap a room to set start")
        pauseForDemo()

        // Step 2: Pick start (VE.201)
        editIndoorEndpointFromTopBar(
            editContentDescription = "Change start position",
            query = "VE-2-201",
            expectedSuggestion = "VE.201",
        )
        waitForGoButton()
        pauseForDemo()

        // Step 3: Click Go and verify route found
        composeTestRule.onNodeWithContentDescription("Start navigation").performClick()
        waitForText("Route found")
        pauseForDemo()

        // Step 4: Change destination to room (VE.204), then Go
        editIndoorEndpointFromTopBar(
            editContentDescription = "Change destination",
            query = "VE-2-204",
            expectedSuggestion = "VE.204",
        )
        waitForGoButton()
        pauseForDemo()

        composeTestRule.onNodeWithContentDescription("Start navigation").performClick()
        waitForText("No path found")
        pauseForDemo()
    }

    private fun searchMainBarAndSetDestination(
        query: String,
        expectedSuggestion: String,
    ) {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag("searchBar").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("searchBar").performClick()
        composeTestRule.waitForIdle()
        pauseForDemo(800)

        composeTestRule.onNodeWithTag("searchBar").performTextReplacement(query)
        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule
                .onAllNodesWithText(expectedSuggestion, substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        pauseForDemo()

        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule
                .onAllNodesWithText("Set as destination", substring = false)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onAllNodesWithText("Set as destination", substring = false)[0].performClick()
        composeTestRule.waitForIdle()
        pauseForDemo()
    }

    private fun editIndoorEndpointFromTopBar(
        editContentDescription: String,
        query: String,
        expectedSuggestion: String,
    ) {
        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule
                .onAllNodesWithContentDescription(editContentDescription)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription(editContentDescription).performClick()
        composeTestRule.waitForIdle()
        pauseForDemo()

        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement(query)

        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule
                .onAllNodesWithText(expectedSuggestion, substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        pauseForDemo()

        composeTestRule.onNodeWithText(expectedSuggestion, substring = true).performClick()
        composeTestRule.waitForIdle()
        pauseForDemo()
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 12_000) {
            composeTestRule
                .onAllNodesWithText(text, substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun waitForGoButton() {
        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule
                .onAllNodesWithContentDescription("Start navigation")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun pauseForDemo(delayMs: Long = STEP_DELAY_MS) {
        composeTestRule.waitForIdle()
        Thread.sleep(delayMs)
    }
}
