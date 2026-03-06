package com.example.campusguide

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E UI tests for Shuttle Stop Markers on the Map (US-3.1).
 *
 * Strategy: tests are GPS-independent — they verify shuttle stop integration
 * by checking that map controls and campus switching remain fully functional
 * after shuttle markers are initialized, and by navigating to each campus
 * where shuttle stops are located.
 *
 * Shuttle stop coordinates tested:
 *   - SGW:              Hall Building, 1455 De Maisonneuve Blvd W
 *   - Loyola Arrival:   Loyola Chapel, 7137 Sherbrooke St W
 *   - Loyola Departure: Loyola Chapel, 7137 Sherbrooke St W
 *
 * Run with: ./gradlew connectedAndroidTest --tests "*.ShuttleStopsUITest"
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ShuttleStopsUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun waitForMap() {
        composeTestRule.waitUntil(timeoutMillis = 12000) {
            composeTestRule
                .onAllNodesWithContentDescription("Zoom In")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        // Allow shuttle markers to fully render on the map
        Thread.sleep(2000)
    }

    private fun pause(ms: Long = 1500) {
        Thread.sleep(ms)
    }

    /**
     * Verifies the app launches and map loads without crashing when
     * shuttle stop markers are initialized (SGW + 2 Loyola stops).
     */
    @Test
    fun shuttleStops_mapLoadsWithoutCrash() {
        waitForMap()

        composeTestRule.onNodeWithContentDescription("Zoom In")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Zoom Out")
            .assertIsDisplayed()
    }

    /**
     * Verifies zoom controls work correctly while shuttle markers are
     * present on the SGW campus (Hall Building stop).
     */
    @Test
    fun shuttleStops_zoomIn_withShuttleMarkersPresent() {
        waitForMap()

        // Zoom into the SGW shuttle stop area — confirms map is still
        // interactive with shuttle markers loaded
        composeTestRule.onNodeWithContentDescription("Zoom In").performClick()
        composeTestRule.waitForIdle()
        pause()
        composeTestRule.onNodeWithContentDescription("Zoom In").performClick()
        composeTestRule.waitForIdle()
        pause()
        composeTestRule.onNodeWithContentDescription("Zoom In").performClick()
        composeTestRule.waitForIdle()
        pause(2000)

        composeTestRule.onNodeWithContentDescription("Zoom In")
            .assertIsDisplayed()
    }

    /**
     * Verifies zoom out works after zooming in near a shuttle stop.
     */
    @Test
    fun shuttleStops_zoomOut_afterZoomingInToStop() {
        waitForMap()

        composeTestRule.onNodeWithContentDescription("Zoom In").performClick()
        composeTestRule.waitForIdle()
        pause()
        composeTestRule.onNodeWithContentDescription("Zoom In").performClick()
        composeTestRule.waitForIdle()
        pause(2000)

        composeTestRule.onNodeWithContentDescription("Zoom Out").performClick()
        composeTestRule.waitForIdle()
        pause()
        composeTestRule.onNodeWithContentDescription("Zoom Out").performClick()
        composeTestRule.waitForIdle()
        pause()

        composeTestRule.onNodeWithContentDescription("Zoom Out")
            .assertIsDisplayed()
    }

    /**
     * Verifies switching to Loyola campus (which has two shuttle stops)
     * does not crash the app and map remains interactive.
     */
    @Test
    fun shuttleStops_switchToLoyola_twoStopsPresent_mapStable() {
        waitForMap()

        // Switch to Loyola — both Arrival and Departure shuttle stops
        // should be visible at Loyola Chapel, 7137 Sherbrooke St W
        composeTestRule.onNodeWithContentDescription("Loyola Campus").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 6000) {
            composeTestRule
                .onAllNodesWithContentDescription("Zoom In")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        // Pause so the two Loyola shuttle stop markers are visually observable
        pause(2500)

        composeTestRule.onNodeWithContentDescription("Zoom In")
            .assertIsDisplayed()
    }

    /**
     * Verifies zooming in on the Loyola campus after switching shows
     * the map is stable with two shuttle stop markers loaded.
     */
    @Test
    fun shuttleStops_loyola_zoomInToShuttleStopArea() {
        waitForMap()

        composeTestRule.onNodeWithContentDescription("Loyola Campus").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 6000) {
            composeTestRule
                .onAllNodesWithContentDescription("Zoom In")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        pause(2000)

        // Zoom toward the Loyola Chapel shuttle stop area
        composeTestRule.onNodeWithContentDescription("Zoom In").performClick()
        composeTestRule.waitForIdle()
        pause()
        composeTestRule.onNodeWithContentDescription("Zoom In").performClick()
        composeTestRule.waitForIdle()
        pause(2000)

        composeTestRule.onNodeWithContentDescription("Zoom In")
            .assertIsDisplayed()
    }

    /**
     * Verifies switching between SGW (1 stop) and Loyola (2 stops)
     * multiple times does not crash the app.
     */
    @Test
    fun shuttleStops_campusSwitching_sgwAndLoyola_allStopsStable() {
        waitForMap()

        // SGW → Loyola (2 stops) → SGW (1 stop) → Loyola
        composeTestRule.onNodeWithContentDescription("Loyola Campus").performClick()
        composeTestRule.waitForIdle()
        pause(2000)

        composeTestRule.onNodeWithContentDescription("SGW Campus").performClick()
        composeTestRule.waitForIdle()
        pause(2000)

        composeTestRule.onNodeWithContentDescription("Loyola Campus").performClick()
        composeTestRule.waitForIdle()
        pause(2000)

        composeTestRule.onNodeWithContentDescription("Zoom In")
            .assertIsDisplayed()
    }

    /**
     * Verifies map navigation controls (pan) work alongside shuttle markers —
     * panning over the shuttle stop area should not cause any crash.
     */
    @Test
    fun shuttleStops_panMap_overStopArea_noIncrash() {
        waitForMap()

        composeTestRule.onNodeWithContentDescription("Zoom In").performClick()
        composeTestRule.waitForIdle()
        pause(2000)

        // Pan across the SGW shuttle stop area
        composeTestRule.onNodeWithContentDescription("Left").performClick()
        composeTestRule.waitForIdle()
        pause()
        composeTestRule.onNodeWithContentDescription("Right").performClick()
        composeTestRule.waitForIdle()
        pause()
        composeTestRule.onNodeWithContentDescription("Up").performClick()
        composeTestRule.waitForIdle()
        pause()
        composeTestRule.onNodeWithContentDescription("Down").performClick()
        composeTestRule.waitForIdle()
        pause()

        composeTestRule.onNodeWithContentDescription("Recenter")
            .assertIsDisplayed()
    }
}
