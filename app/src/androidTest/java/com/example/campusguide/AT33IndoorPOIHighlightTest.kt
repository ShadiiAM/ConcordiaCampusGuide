package com.example.campusguide

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance Test [AT-33] for US-5.5: Highlight Indoor Points of Interest (POIs)
 * GitHub Issue: #44 (US), #274 (AT)
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT33IndoorPOIHighlightTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    /** Opens the LB building indoor map by searching for a room and selecting it. */
    private fun openLBIndoorMap() {
        onView(withId(android.R.id.content)).check(matches(isDisplayed()))
        Thread.sleep(4000)

        composeTestRule.onNodeWithTag("searchBar").performClick()
        Thread.sleep(1500)
        composeTestRule.onNodeWithTag("searchBar").performTextInput("LB")
        Thread.sleep(3000)


        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithContentDescription("LB floor", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(1500)
        composeTestRule
            .onAllNodesWithContentDescription("LB floor", substring = true)[0]
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onNodeWithContentDescription("Floor map overlay", substring = true)
                .isDisplayed()
        }
        Thread.sleep(1500)
    }

    // ── AC1 ───────────────────────────────────────────────────────────────────

    /**
     * AC1: POI types include washrooms and water fountains on LB floor 3.
     */
    @Test
    fun ac1_supportedPoiTypesShownOnFloor() {
        openLBIndoorMap()

        composeTestRule.onNodeWithText("3").performClick()
        Thread.sleep(2000)

        composeTestRule
            .onNodeWithContentDescription("Washroom", substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Water Fountain", substring = true)
            .assertIsDisplayed()

        Thread.sleep(4000)
    }

    // ── AC2 ───────────────────────────────────────────────────────────────────

    /**
     * AC2: POI icons remain stable when switching between floors.
     * Switches from the default floor to floor 3 and back, verifying the
     * floor map canvas renders correctly on each floor without crashing.
     */
    @Test
    fun ac2_poiIconsStableWhenSwitchingFloors() {
        openLBIndoorMap()

        // Switch to floor 3 and verify canvas is shown
        composeTestRule.onNodeWithText("3").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onNodeWithContentDescription("Floor map overlay", substring = true)
                .isDisplayed()
        }
        Thread.sleep(2000)

        composeTestRule
            .onNodeWithContentDescription("Floor map overlay", substring = true)
            .assertIsDisplayed()

        // Switch back to floor 2 and verify canvas is still shown
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onNodeWithContentDescription("Floor map overlay", substring = true)
                .isDisplayed()
        }
        Thread.sleep(2000)

        composeTestRule
            .onNodeWithContentDescription("Floor map overlay", substring = true)
            .assertIsDisplayed()

        Thread.sleep(4000)
    }

    // ── AC3 ───────────────────────────────────────────────────────────────────

    /**
     * AC3: Tapping a POI icon opens the purple info popup with the POI name and details.
     *
     * Clicks the invisible tappable overlay on top of the Water Fountain icon on
     * LB floor 3, then verifies the purple dialog appears with title and Close button.
     */
    @Test
    fun ac3_tapPoiIconShowsInfoPopup() {
        openLBIndoorMap()

        composeTestRule.onNodeWithText("3").performClick()
        Thread.sleep(2000)

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithContentDescription("POI: Water Fountain", substring = false)
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(1500) // pause so GIF shows the map before tapping

        composeTestRule
            .onAllNodesWithContentDescription("POI: Water Fountain")[0]
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onNodeWithTag("poiInfoPopup").isDisplayed()
        }

        composeTestRule
            .onNodeWithText("Water Fountain")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Close")
            .assertIsDisplayed()

        Thread.sleep(4000)

        composeTestRule.onNodeWithText("Close").performClick()
        Thread.sleep(1500)

        composeTestRule
            .onAllNodesWithContentDescription("POI: Water Fountain", substring = false)
            .fetchSemanticsNodes()
            .also { assert(it.isNotEmpty()) }
    }
}
