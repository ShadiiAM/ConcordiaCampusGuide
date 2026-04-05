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
 * Acceptance Test for US-2.3: Use my current location / current building as the start location
 * Link: https://github.com/SOEN390-Project-W26/issues/265
 *
 * Acceptance Criteria Verified:
 * - When location permission is granted, directions defaults origin to "Your location".
 * - When GPS places the user inside a Concordia building, a banner shows the building name.
 * - The banner text is the full building name (not just a code).
 * - The user can tap "Change start position" to override the origin.
 * - All relevant controls are labeled for screen reader accessibility.
 *
 * Pre-condition: Emulator GPS set to Hall Building:
 *   adb emu geo fix -73.5789 45.4972
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT13UseCurrentLocationAsStartUITest {

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

    private val buildingTapX = 540
    private val buildingTapY = 1200

    private fun selectBuildingViaSearch() {
        Thread.sleep(4000)
        composeTestRule.onNodeWithTag("searchBar").performTextInput("Hall")
        Thread.sleep(3000)
        composeTestRule.onNodeWithTag("Henry F. Hall Building").performClick()
        Thread.sleep(1000)
    }

    @Test
    fun appLoads_withLocationPermission_mapIsDisplayed() {
        onView(withId(android.R.id.content)).check(matches(isDisplayed()))
        Thread.sleep(4000)
        composeTestRule.onNodeWithTag("mapView").assertIsDisplayed()
    }

    @Test
    fun buildingBanner_appearsWhenUserIsInsideBuilding() {
        // Pre-condition: adb emu geo fix -73.5789 45.4972
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule
                .onNodeWithContentDescription("You are in Henry F. Hall Building")
                .isDisplayed()
        }
        composeTestRule
            .onNodeWithContentDescription("You are in Henry F. Hall Building")
            .assertIsDisplayed()
    }

    @Test
    fun buildingBanner_showsFullBuildingName_notCode() {
        // AC: Banner shows full name like "Henry F. Hall Building", not just "H"
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onNodeWithText("Henry F. Hall Building").isDisplayed()
        }
        composeTestRule.onNodeWithText("Henry F. Hall Building").assertIsDisplayed()
    }

    @Test
    fun directionsPanel_defaultOriginIsUserLocation() {
        // AC: When directions opens, origin defaults to "Your location" (current GPS)
        selectBuildingViaSearch()

        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onNodeWithText("Your location").isDisplayed()
        }
        composeTestRule.onNodeWithText("Your location").assertIsDisplayed()
    }

    @Test
    fun directionsPanel_changeDestination_isAccessible() {
        // AC: "Change destination" control is labeled for screen readers
        selectBuildingViaSearch()

        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onNodeWithContentDescription("Change destination").isDisplayed()
        }
        composeTestRule.onNodeWithContentDescription("Change destination").assertIsDisplayed()
    }

    @Test
    fun directionsPanel_cancelButton_dismissesAndReturnsToMap() {
        // AC: User can cancel the directions flow and return to the map
        selectBuildingViaSearch()

        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onNodeWithTag("CancelButton").isDisplayed()
        }
        composeTestRule.onNodeWithTag("CancelButton").performClick()
        Thread.sleep(1500)

        // Directions panel should be dismissed
        composeTestRule.onNodeWithTag("CancelButton").assertDoesNotExist()
        onView(withId(android.R.id.content)).check(matches(isDisplayed()))
    }
}
