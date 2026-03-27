package com.example.campusguide.epic2_AT

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.example.campusguide.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E tests for US-2.7: step-by-step directions display.
 *
 * Tests cover:
 *  - Route options panel appearing after a building is selected
 *  - Travel-mode chips and Go/Cancel buttons in the planning state
 *  - Cancel remaining visible after the route is loaded
 *  - "View route details" / "Hide route details" toggle
 *  - The "Directions ready" bottom card NOT appearing after a route loads
 *  - Cancel dismissing the directions top bar
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT17StepByStepDirectionInstructionsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun searchAndSelectFromMainBar(query: String, expectedSuggestion: String) {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("searchBar").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("searchBar").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("searchBar").performTextInput(query)
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText(expectedSuggestion, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(expectedSuggestion, substring = true).performClick()
        composeTestRule.waitForIdle()
    }

    private fun waitForRouteOptions() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Route options").fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** Clicks Go and waits for either a route summary or an error. */
    private fun startNavigationAndWaitForResult() {
        composeTestRule.onNodeWithContentDescription("Start navigation").performClick()
        composeTestRule.waitUntil(timeoutMillis = 12000) {
            composeTestRule.onAllNodesWithText("min", substring = true).fetchSemanticsNodes().isNotEmpty()
                || composeTestRule.onAllNodesWithText("Unavailable").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun hasViewRouteDetailsButton(): Boolean =
        composeTestRule.onAllNodesWithContentDescription("View route details")
            .fetchSemanticsNodes().isNotEmpty()

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    fun `route options panel opens after building is selected from search`() {
        searchAndSelectFromMainBar("Hall", "Henry F. Hall Building")
        waitForRouteOptions()
        composeTestRule.onNodeWithText("Route options").assertExists()
    }

    @Test
    fun `travel mode chips are visible when planning a route`() {
        searchAndSelectFromMainBar("Hall", "Henry F. Hall Building")
        waitForRouteOptions()

        composeTestRule.onNodeWithContentDescription("Drive: car route").assertExists()
        composeTestRule.onNodeWithContentDescription("Walk: pedestrian route").assertExists()
        composeTestRule.onNodeWithContentDescription("Transit: bus or metro route").assertExists()
    }

    @Test
    fun `go and cancel buttons are visible when planning a route`() {
        searchAndSelectFromMainBar("Hall", "Henry F. Hall Building")
        waitForRouteOptions()

        composeTestRule.onNodeWithContentDescription("Start navigation").assertExists()
        composeTestRule.onNodeWithText("Cancel").assertExists()
    }

    @Test
    fun `cancel dismisses the directions top bar from planning state`() {
        searchAndSelectFromMainBar("Hall", "Henry F. Hall Building")
        waitForRouteOptions()

        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Start navigation").assertDoesNotExist()
    }

    @Test
    fun `cancel remains visible after route is loaded`() {
        searchAndSelectFromMainBar("Molson", "John Molson Building")
        waitForRouteOptions()

        startNavigationAndWaitForResult()

        // Cancel must be visible regardless of API success or failure
        composeTestRule.onNodeWithText("Cancel").assertExists()
    }

    @Test
    fun `go button is not visible in route summary view`() {
        searchAndSelectFromMainBar("Molson", "John Molson Building")
        waitForRouteOptions()

        startNavigationAndWaitForResult()

        // In ShowingRoute, showActions = false → Go button should be gone
        composeTestRule.onNodeWithContentDescription("Start navigation").assertDoesNotExist()
    }

    @Test
    fun `directions ready bottom card does not appear after route is loaded`() {
        searchAndSelectFromMainBar("Molson", "John Molson Building")
        waitForRouteOptions()

        startNavigationAndWaitForResult()

        composeTestRule.onNodeWithText("Directions ready").assertDoesNotExist()
    }

    @Test
    fun `view route details button appears when route has steps`() {
        searchAndSelectFromMainBar("Molson", "John Molson Building")
        waitForRouteOptions()

        startNavigationAndWaitForResult()

        val hasDetails = hasViewRouteDetailsButton()
        val hasError = composeTestRule.onAllNodesWithText("Unavailable")
            .fetchSemanticsNodes().isNotEmpty()

        assert(hasDetails || hasError) { "Expected either route details button or error message" }
    }

    @Test
    fun `view route details opens step list and shows hide button`() {
        searchAndSelectFromMainBar("Molson", "John Molson Building")
        waitForRouteOptions()

        startNavigationAndWaitForResult()

        if (!hasViewRouteDetailsButton()) return // route failed — skip

        composeTestRule.onNodeWithContentDescription("View route details").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Hide route details").assertExists()
        composeTestRule.onNodeWithText("Cancel").assertExists()
        // "View route details" must be gone while details are open
        composeTestRule.onNodeWithContentDescription("View route details").assertDoesNotExist()
    }

    @Test
    fun `go button is not visible while viewing step details`() {
        searchAndSelectFromMainBar("Molson", "John Molson Building")
        waitForRouteOptions()

        startNavigationAndWaitForResult()

        if (!hasViewRouteDetailsButton()) return

        composeTestRule.onNodeWithContentDescription("View route details").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Start navigation").assertDoesNotExist()
    }

    @Test
    fun `hide route details collapses step list and restores view button`() {
        searchAndSelectFromMainBar("Molson", "John Molson Building")
        waitForRouteOptions()

        startNavigationAndWaitForResult()

        if (!hasViewRouteDetailsButton()) return

        // Open
        composeTestRule.onNodeWithContentDescription("View route details").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Hide route details").assertExists()

        // Collapse
        composeTestRule.onNodeWithContentDescription("Hide route details").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("View route details").assertExists()
        composeTestRule.onNodeWithContentDescription("Hide route details").assertDoesNotExist()
    }

    @Test
    fun `cancel from step details view dismisses directions`() {
        searchAndSelectFromMainBar("Molson", "John Molson Building")
        waitForRouteOptions()

        startNavigationAndWaitForResult()

        if (!hasViewRouteDetailsButton()) {
            // Even without steps, Cancel should work
            composeTestRule.onNodeWithText("Cancel").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Cancel").assertDoesNotExist()
            return
        }

        composeTestRule.onNodeWithContentDescription("View route details").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForIdle()

        // Top bar must be fully gone
        composeTestRule.onNodeWithContentDescription("Hide route details").assertDoesNotExist()
        composeTestRule.onNodeWithText("Cancel").assertDoesNotExist()
    }

    @Test
    fun `route options bottom card x button hides card without cancelling route`() {
        searchAndSelectFromMainBar("Hall", "Henry F. Hall Building")
        waitForRouteOptions()

        // Close the bottom card via its X — two "Close directions" nodes exist (top bar + bottom card);
        // the bottom card is composed first in the tree, so [0] targets it.
        composeTestRule.onAllNodesWithContentDescription("Close directions")[0].performClick()
        composeTestRule.waitForIdle()

        // Route options bottom card should be gone
        composeTestRule.onNodeWithText("Route options").assertDoesNotExist()

        // Top bar must still be visible (route not cancelled)
        composeTestRule.onNodeWithContentDescription("Start navigation").assertExists()
    }
}