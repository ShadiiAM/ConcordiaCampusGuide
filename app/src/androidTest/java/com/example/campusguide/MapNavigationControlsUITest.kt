package com.example.campusguide

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for Map Navigation Controls.
 *
 * These tests verify the functionality of map navigation controls
 * such as zoom in and zoom out.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MapNavigationControlsUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MapsActivity::class.java)

    /**
     * Test: Zoom in button is displayed and clickable.
     */
    @Test
    fun zoomInButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        // Find the zoom in button and check if it's displayed
        onView(withContentDescription("Zoom In")).check(matches(isDisplayed()))

        // Perform a click on the zoom in button
        onView(withContentDescription("Zoom In")).perform(click())
    }

    /**
     * Test: Zoom out button is displayed and clickable.
     */
    @Test
    fun zoomOutButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        // Find the zoom out button and check if it's displayed
        onView(withContentDescription("Zoom Out")).check(matches(isDisplayed()))

        // Perform a click on the zoom out button
        onView(withContentDescription("Zoom Out")).perform(click())
    }

    /**
     * Test: Up button is displayed and clickable.
     */
    @Test
    fun upButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        // Find the up button and check if it's displayed
        onView(withContentDescription("Up")).check(matches(isDisplayed()))

        // Perform a click on the up button
        onView(withContentDescription("Up")).perform(click())
    }

    /**
     * Test: Down button is displayed and clickable.
     */
    @Test
    fun downButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        // Find the down button and check if it's displayed
        onView(withContentDescription("Down")).check(matches(isDisplayed()))

        // Perform a click on the down button
        onView(withContentDescription("Down")).perform(click())
    }

    /**
     * Test: Left button is displayed and clickable.
     */
    @Test
    fun leftButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        // Find the left button and check if it's displayed
        onView(withContentDescription("Left")).check(matches(isDisplayed()))

        // Perform a click on the left button
        onView(withContentDescription("Left")).perform(click())
    }

    /**
     * Test: Right button is displayed and clickable.
     */
    @Test
    fun rightButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        // Find the right button and check if it's displayed
        onView(withContentDescription("Right")).check(matches(isDisplayed()))

        // Perform a click on the right button
        onView(withContentDescription("Right")).perform(click())
    }

    /**
     * Test: Recenter button is displayed and clickable.
     */
    @Test
    fun recenterButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        // Find the recenter button and check if it's displayed
        onView(withContentDescription("Recenter")).check(matches(isDisplayed()))

        // Perform a click on the recenter button
        onView(withContentDescription("Recenter")).perform(click())
    }

    /**
     * Test: Toggle controls button is displayed and clickable.
     */
    @Test
    fun toggleControlsButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        // Find the toggle controls button and check if it's displayed
        onView(withContentDescription("Toggle Controls")).check(matches(isDisplayed()))

        // Perform a click on the toggle controls button
        onView(withContentDescription("Toggle Controls")).perform(click())
    }
}
