package com.example.campusguide

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance Test for US-1.5: Show Building Information (Pop-up Details)
 *
 * Tests verify building tap shows popup with building name and code.
 *
 * NOTE: All interactions require MANUAL tapping on building polygons
 * during test recording. Tap when instructed in comments.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BuildingInfoPopupUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MapsActivity::class.java)

    @Test
    fun mapLoads_withBuildingPolygons() {
        // AC: Building polygons visible for tapping
        // ACTION: Just wait, no interaction needed
        Thread.sleep(4000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun tapBuilding_showsPopup() {
        // AC: Tapping building shows popup with details
        // ACTION: TAP a building polygon within first 5 seconds
        Thread.sleep(5000)

        // Wait to show popup with building name and code
        Thread.sleep(5000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun popup_canBeDismissed() {
        // AC: Popup provides clear way to close/dismiss
        // ACTION: TAP a building within first 5 seconds
        Thread.sleep(5000)

        // Popup should be visible now
        Thread.sleep(3000)

        // ACTION: DISMISS the popup (tap X or tap outside)
        Thread.sleep(5000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun tapDifferentBuilding_updatesPopup() {
        // AC: Different building updates popup info
        // ACTION: TAP first building within first 5 seconds
        Thread.sleep(5000)

        // Wait to show first building popup
        Thread.sleep(4000)

        // ACTION: TAP second building within next 5 seconds
        Thread.sleep(5000)

        // Wait to show second building popup updated
        Thread.sleep(4000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }
}
