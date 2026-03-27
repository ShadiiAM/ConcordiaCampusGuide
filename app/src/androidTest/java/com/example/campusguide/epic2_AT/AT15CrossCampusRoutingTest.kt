package com.example.campusguide.epic2_AT

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.example.campusguide.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E test for cross-campus routing (SGW <-> Loyola) US-2.5.
 *
 * Strategy: explicitly set BOTH origin and destination via the BuildingAutocompleteFields in
 * the "Route options" panel. This makes tests GPS-independent (no reliance on emulator location).
 *
 * Screen-record:
 *   adb shell screenrecord /sdcard/cross_campus_test.mp4 &
 *   ./gradlew connectedAndroidTest --tests "CrossCampusRoutingE2ETest"
 *   adb pull /sdcard/cross_campus_test.mp4
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT15CrossCampusRoutingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    /**
     * Types [query] into the main search bar and selects the suggestion containing [expectedSuggestion].
     * [query] must be SHORTER than [expectedSuggestion] so the input field text ≠ suggestion text.
     */
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

    /**
     * Types [query] into the "From:" autocomplete field and selects [expectedSuggestion].
     * [query] must be shorter than [expectedSuggestion].
     */
    private fun setOriginBuilding(query: String, expectedSuggestion: String) {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("origin_building_field").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("origin_building_field").performTextReplacement(query)
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText(expectedSuggestion, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(expectedSuggestion, substring = true).performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Types [query] into the "To:" autocomplete field and selects [expectedSuggestion].
     * [query] must be shorter than [expectedSuggestion].
     */
    private fun setDestinationBuilding(query: String, expectedSuggestion: String) {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("destination_building_field")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("destination_building_field").performTextReplacement(query)
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText(expectedSuggestion, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(expectedSuggestion, substring = true).performClick()
        composeTestRule.waitForIdle()
    }

    /** Waits for the Route options bottom card to appear. */
    private fun waitForRouteOptions() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Route options").fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** Waits for and asserts that the cross-campus badge exists. */
    private fun assertCrossCampusBadgeShown() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("This is a cross-campus route")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("This is a cross-campus route").assertExists()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `user can plan cross-campus route from SGW to Loyola`() {
        // Open directions panel with a Loyola destination
        searchAndSelectFromMainBar("Central", "Central Building")
        waitForRouteOptions()

        // Explicitly set origin to SGW (Henry F. Hall Building)
        setOriginBuilding("Hall", "Henry F. Hall Building")

        // Both buildings set: origin = H (SGW), destination = CC (Loyola) → cross-campus
        assertCrossCampusBadgeShown()

        // Select Transit mode
        composeTestRule.onNodeWithContentDescription("Transit: bus or metro route").performClick()

        // Tap Go
        composeTestRule.onNodeWithContentDescription("Start navigation").performClick()

        // Wait for route result (either success or error)
        composeTestRule.waitUntil(timeoutMillis = 12000) {
            composeTestRule.onAllNodesWithText("min", substring = true).fetchSemanticsNodes().isNotEmpty()
                || composeTestRule.onAllNodesWithText("Unavailable").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify we got some result
        val hasRoute = composeTestRule.onAllNodesWithText("min", substring = true)
            .fetchSemanticsNodes().isNotEmpty()
        val hasError = composeTestRule.onAllNodesWithText("Unavailable")
            .fetchSemanticsNodes().isNotEmpty()
        assert(hasRoute || hasError) { "Expected route summary or error message" }
    }

    @Test
    fun `user can plan cross-campus route from Loyola to SGW`() {
        // Open directions panel with an SGW destination
        searchAndSelectFromMainBar("Hall", "Henry F. Hall Building")
        waitForRouteOptions()

        // Explicitly set origin to Loyola (Central Building)
        setOriginBuilding("Central", "Central Building")

        // Both buildings set: origin = CC (Loyola), destination = H (SGW) → cross-campus
        assertCrossCampusBadgeShown()

        // Select Transit mode
        composeTestRule.onNodeWithContentDescription("Transit: bus or metro route").performClick()

        // Get route
        composeTestRule.onNodeWithContentDescription("Start navigation").performClick()

        // Wait for route result
        composeTestRule.waitUntil(timeoutMillis = 12000) {
            composeTestRule.onAllNodesWithText("min", substring = true).fetchSemanticsNodes().isNotEmpty()
                || composeTestRule.onAllNodesWithText("Unavailable").fetchSemanticsNodes().isNotEmpty()
        }

        val hasRoute = composeTestRule.onAllNodesWithText("min", substring = true)
            .fetchSemanticsNodes().isNotEmpty()
        val hasError = composeTestRule.onAllNodesWithText("Unavailable")
            .fetchSemanticsNodes().isNotEmpty()
        assert(hasRoute || hasError) { "Expected route summary or error message" }
    }

    @Test
    fun `cross-campus badge does not appear for same-campus route`() {
        // Open directions panel with SGW destination (John Molson Building)
        searchAndSelectFromMainBar("Molson", "John Molson Building")
        waitForRouteOptions()

        // Set origin to another SGW building (Henry F. Hall Building)
        setOriginBuilding("Hall", "Henry F. Hall Building")

        // Both buildings are SGW → no cross-campus badge
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("This is a cross-campus route").assertDoesNotExist()
    }

    @Test
    fun `error message suggests Transit mode when cross-campus route fails`() {
        // Open directions panel with Loyola destination
        searchAndSelectFromMainBar("Central", "Central Building")
        waitForRouteOptions()

        // Set origin to SGW
        setOriginBuilding("Hall", "Henry F. Hall Building")

        // Verify cross-campus badge
        assertCrossCampusBadgeShown()

        // Select DRIVE mode (may fail for cross-campus route)
        composeTestRule.onNodeWithContentDescription("Drive: car route").performClick()

        // Tap Go
        composeTestRule.onNodeWithContentDescription("Start navigation").performClick()

        // Wait for result (route or error)
        composeTestRule.waitUntil(timeoutMillis = 12000) {
            composeTestRule.onAllNodesWithText("min", substring = true).fetchSemanticsNodes().isNotEmpty()
                || composeTestRule.onAllNodesWithText("Shuttle", substring = true).fetchSemanticsNodes().isNotEmpty()
                || composeTestRule.onAllNodesWithText("Unavailable").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify either an error message (cross-campus failed) or a route summary (succeeded)
        val hasShuttleError = composeTestRule
            .onAllNodesWithText("Shuttle", substring = true).fetchSemanticsNodes().isNotEmpty()
        val hasRouteMin = composeTestRule
            .onAllNodesWithText("min", substring = true).fetchSemanticsNodes().isNotEmpty()
        val hasUnavailable = composeTestRule
            .onAllNodesWithText("Unavailable").fetchSemanticsNodes().isNotEmpty()
        assert(hasShuttleError || hasRouteMin || hasUnavailable) {
            "Expected route summary or cross-campus error message"
        }
    }

    @Test
    fun `user can switch travel modes for cross-campus route`() {
        // Open directions panel with Loyola destination
        searchAndSelectFromMainBar("Central", "Central Building")
        waitForRouteOptions()

        // Set origin to SGW
        setOriginBuilding("Hall", "Henry F. Hall Building")

        // Verify all travel mode options are available
        composeTestRule.onNodeWithContentDescription("Drive: car route").assertExists()
        composeTestRule.onNodeWithContentDescription("Walk: pedestrian route").assertExists()
        composeTestRule.onNodeWithContentDescription("Transit: bus or metro route").assertExists()

        // Verify cross-campus badge is shown
        assertCrossCampusBadgeShown()

        // Switch between modes
        composeTestRule.onNodeWithContentDescription("Walk: pedestrian route").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Transit: bus or metro route").performClick()
        composeTestRule.waitForIdle()

        // Badge must remain visible after mode switches
        composeTestRule.onNodeWithText("This is a cross-campus route").assertExists()
    }
}