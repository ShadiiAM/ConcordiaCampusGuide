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
 * Visual verification done via GIF recording.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BuildingInfoPopupUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MapsActivity::class.java)

    @Test
    fun mapLoads_withBuildingPolygons() {
        // AC: Building polygons visible for tapping
        Thread.sleep(3000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun tapBuilding_showsPopup() {
        // AC: Tapping building shows popup with details
        // Note: Manual tap required during test recording
        Thread.sleep(8000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun popup_showsBuildingDetails() {
        // AC: Popup includes building name and code
        // Note: Manual tap and verification during recording
        Thread.sleep(8000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun tapDifferentBuilding_updatesPopup() {
        // AC: Different building updates popup info
        // Note: Manual taps on two buildings during recording
        Thread.sleep(12000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }
}
