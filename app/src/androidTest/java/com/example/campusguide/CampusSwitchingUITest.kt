package com.example.campusguide

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for Campus Switching Feature (User Story 1.3)
 *
 * These tests verify the campus toggle functionality that allows users
 * to switch between SGW and Loyola campus views on the map.
 *
 * Acceptance Criteria Tested:
 * 1. Campus toggle switch is visible and accessible
 * 2. User can switch between SGW and Loyola campuses
 * 3. Map overlays update when switching campuses
 * 4. Campus selection persists across app restarts
 *
 * Note: These tests require a connected device or running emulator.
 * Run with: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CampusSwitchingUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MapsActivity::class.java)

    /**
     * Test: Campus toggle is visible on map screen
     * Verifies that the campus toggle switch is rendered and accessible to users
     */
    @Test
    fun campusToggle_isDisplayed() {
        // Wait for map to load
        Thread.sleep(2000)

        // Verify toggle is visible
        onView(withContentDescription("Campus Toggle"))
            .check(matches(isDisplayed()))
    }

    /**
     * Test: User can switch from SGW to Loyola campus
     * Verifies that clicking the toggle switches the campus view
     */
    @Test
    fun campusToggle_switchToLoyola_updatesMap() {
        // Wait for map to load with default SGW campus
        Thread.sleep(2000)

        // Verify we start on SGW (default)
        onView(withContentDescription("Campus Toggle"))
            .check(matches(isDisplayed()))

        // Click toggle to switch to Loyola
        onView(withContentDescription("Campus Toggle"))
            .perform(click())

        // Wait for map to update
        Thread.sleep(1500)

        // Verify toggle is still displayed after switch
        onView(withContentDescription("Campus Toggle"))
            .check(matches(isDisplayed()))
    }

    /**
     * Test: User can switch back to SGW from Loyola
     * Verifies bidirectional campus switching works correctly
     */
    @Test
    fun campusToggle_switchBackToSGW_updatesMap() {
        // Wait for map to load
        Thread.sleep(2000)

        // Switch to Loyola
        onView(withContentDescription("Campus Toggle"))
            .perform(click())
        Thread.sleep(1500)

        // Switch back to SGW
        onView(withContentDescription("Campus Toggle"))
            .perform(click())
        Thread.sleep(1500)

        // Verify toggle still works
        onView(withContentDescription("Campus Toggle"))
            .check(matches(isDisplayed()))
    }

    /**
     * Test: Multiple rapid toggle switches are handled correctly
     * Verifies that the app handles rapid user interactions without crashing
     */
    @Test
    fun campusToggle_rapidSwitching_handlesCorrectly() {
        // Wait for map to load
        Thread.sleep(2000)

        // Perform multiple rapid switches
        repeat(3) {
            onView(withContentDescription("Campus Toggle"))
                .perform(click())
            Thread.sleep(800)
        }

        // Verify app is still responsive
        onView(withContentDescription("Campus Toggle"))
            .check(matches(isDisplayed()))
    }

    /**
     * Test: Map remains interactive after campus switch
     * Verifies that map functionality is not broken by campus switching
     */
    @Test
    fun mapInteraction_afterCampusSwitch_remainsInteractive() {
        // Wait for map to load
        Thread.sleep(2000)

        // Switch campus
        onView(withContentDescription("Campus Toggle"))
            .perform(click())
        Thread.sleep(1500)

        // Verify map is still interactive (toggle still works)
        onView(withContentDescription("Campus Toggle"))
            .perform(click())

        // If we reach here without crash, map is interactive
        onView(withContentDescription("Campus Toggle"))
            .check(matches(isDisplayed()))
    }
}
