package com.example.campusguide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
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
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * Test: Zoom in button is displayed and clickable.
     */
    @Test
    fun zoomInButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Toggle Controls").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()

        // Find the zoom in button and check if it's displayed
        composeTestRule.onNodeWithContentDescription("Zoom In").assertIsDisplayed()

        // Perform a click on the zoom in button
        composeTestRule.onNodeWithContentDescription("Zoom In").performClick()
    }

    /**
     * Test: Zoom out button is displayed and clickable.
     */
    @Test
    fun zoomOutButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Toggle Controls").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()

        // Find the zoom out button and check if it's displayed
        composeTestRule.onNodeWithContentDescription("Zoom Out").assertIsDisplayed()

        // Perform a click on the zoom out button
        composeTestRule.onNodeWithContentDescription("Zoom Out").performClick()
    }

    /**
     * Test: Up button is displayed and clickable.
     */
    @Test
    fun upButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Toggle Controls").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        // Find the up button and check if it's displayed
        composeTestRule.onNodeWithContentDescription("Up").assertIsDisplayed()

        // Perform a click on the up button
        composeTestRule.onNodeWithContentDescription("Up").performClick()
    }

    /**
     * Test: Down button is displayed and clickable.
     */
    @Test
    fun downButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Toggle Controls").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        // Find the down button and check if it's displayed
        composeTestRule.onNodeWithContentDescription("Down").assertIsDisplayed()

        // Perform a click on the down button
        composeTestRule.onNodeWithContentDescription("Down").performClick()
    }

    /**
     * Test: Left button is displayed and clickable.
     */
    @Test
    fun leftButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Toggle Controls").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        // Find the left button and check if it's displayed
        composeTestRule.onNodeWithContentDescription("Left").assertIsDisplayed()

        // Perform a click on the left button
        composeTestRule.onNodeWithContentDescription("Left").performClick()
    }

    /**
     * Test: Right button is displayed and clickable.
     */
    @Test
    fun rightButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Toggle Controls").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        // Find the right button and check if it's displayed
        composeTestRule.onNodeWithContentDescription("Right").assertIsDisplayed()

        // Perform a click on the right button
        composeTestRule.onNodeWithContentDescription("Right").performClick()
    }

    /**
     * Test: Recenter button is displayed and clickable.
     */
    @Test
    fun recenterButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Toggle Controls").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        // Find the recenter button and check if it's displayed
        composeTestRule.onNodeWithContentDescription("Recenter").assertIsDisplayed()

        // Perform a click on the recenter button
        composeTestRule.onNodeWithContentDescription("Recenter").performClick()
    }

    /**
     * Test: Toggle controls button is displayed and clickable.
     */
    @Test
    fun toggleControlsButton_isDisplayedAndClickable() {
        // Wait for map to load
        Thread.sleep(2000)

        // Find the toggle controls button and check if it's displayed
        composeTestRule.onNodeWithContentDescription("Toggle Controls").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
    }
}
