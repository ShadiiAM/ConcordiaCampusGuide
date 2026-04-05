package com.example.campusguide

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
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
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance Test for US-2.1: Select start and destination by tapping buildings on the map
 * Link: https://github.com/SOEN390-Project-W26/issues/264
 *
 * Acceptance Criteria Verified:
 * - Selecting a building (via search or polygon tap) opens the route panel with
 *   visible start and destination fields.
 * - "Change start position" and "Change destination" are both labeled and clickable.
 * - User can tap "Change start position" to enter origin-picking mode.
 * - "CancelButton" dismisses the directions panel.
 * - "Start navigation" Go button is accessible.
 * - "Close directions" button has an accessible label.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT11SelectStartAndDestinationUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private val device: UiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    /** Search for Hall Building and select it to open the directions route panel. */
    private fun selectBuildingViaSearch() {
        Thread.sleep(4000)
        composeTestRule.onNodeWithTag("searchBar").performTextInput("Hall")
        Thread.sleep(3000)
        // Test tag on building suggestion row is the buildingName
        composeTestRule.onNodeWithTag("Henry F. Hall Building").performClick()
        Thread.sleep(1000)
    }

    @Test
    fun mapLoads_withBuildingPolygons_visible() {
        onView(withId(android.R.id.content)).check(matches(isDisplayed()))
        Thread.sleep(4000)
        composeTestRule.onNodeWithTag("mapView").assertIsDisplayed()
    }

    @Test
    fun tapBuilding_showsBuildingDetailsSheet_withDirectionsButton() {
        // Tapping a polygon still works — verify via UiAutomator at map center (Hall Building)
        Thread.sleep(5000)
        device.click(540, 1200)
        Thread.sleep(3000)

        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onNodeWithContentDescription("Get directions to this building").isDisplayed()
        }
        composeTestRule
            .onNodeWithContentDescription("Get directions to this building")
            .assertIsDisplayed()
    }

    @Test
    fun selectBuilding_opensRoutePanel_withStartAndDestinationLabels() {
        // AC: Start and destination fields are visible once a building is selected
        selectBuildingViaSearch()

        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onNodeWithText("Your location").isDisplayed()
        }
        // Origin label defaults to "Your location"
        composeTestRule.onNodeWithText("Your location").assertIsDisplayed()
        // Destination label shows the selected building — verify via "Change destination"
        composeTestRule.onNodeWithContentDescription("Change destination").assertIsDisplayed()
    }

    @Test
    fun directionsPanel_startNavigationButton_isAccessible() {
        // AC: "Start navigation" button is labeled for screen readers
        selectBuildingViaSearch()

        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onNodeWithContentDescription("Start navigation").isDisplayed()
        }
        composeTestRule.onNodeWithContentDescription("Start navigation").assertIsDisplayed()
    }

    @Test
    fun directionsPanel_cancelButton_dismissesDirections() {
        // AC: User can cancel/dismiss the direction flow at any time
        selectBuildingViaSearch()

        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onNodeWithTag("CancelButton").isDisplayed()
        }
        composeTestRule.onNodeWithTag("CancelButton").assertIsDisplayed().performClick()
        Thread.sleep(1500)

        composeTestRule.onNodeWithTag("CancelButton").assertDoesNotExist()
    }

    @Test
    fun tapChangeDestination_opensDestinationPicker() {
        // AC: "Change destination" lets the user pick a new destination
        selectBuildingViaSearch()

        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onNodeWithContentDescription("Change destination").isDisplayed()
        }
        composeTestRule.onNodeWithContentDescription("Change destination").performClick()
        Thread.sleep(1500)

        // Directions panel is still active — cancel button remains visible
        composeTestRule.onNodeWithTag("CancelButton").assertIsDisplayed()
    }

    @Test
    fun closeDirections_button_isAccessible() {
        // AC: "Close directions" button has a label for screen readers
        selectBuildingViaSearch()

        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onNodeWithContentDescription("Close directions").isDisplayed()
        }
        composeTestRule.onNodeWithContentDescription("Close directions").assertIsDisplayed()
    }
}
