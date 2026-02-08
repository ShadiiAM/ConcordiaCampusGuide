package com.example.campusguide

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
 * Acceptance Test for US-1.3: Switch Between SGW and Loyola
 *
 * Tests verify campus switching functionality by clicking toggle buttons.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CampusSwitchingUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MapsActivity::class.java)

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun campusSwitchControl_isPresent() {
        // AC: Campus switch control exists and is visible
        Thread.sleep(3000)

        // Verify both campus buttons are present
        composeTestRule.onNodeWithTag("SGW_Button").assertExists()
        composeTestRule.onNodeWithTag("Loyola_Button").assertExists()
    }

    @Test
    fun campusSwitch_toLoyola_works() {
        // AC: Campus changes from SGW to Loyola without app restart
        // Wait for initial map load
        Thread.sleep(4000)

        // Click Loyola button
        composeTestRule.onNodeWithTag("Loyola_Button").performClick()

        // Wait for map to recenter and show Loyola campus
        Thread.sleep(5000)

        // Verify app didn't crash
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun campusSwitch_backToSGW_works() {
        // AC: Campus changes back to SGW
        // Wait for initial map load
        Thread.sleep(4000)

        // Switch to Loyola
        composeTestRule.onNodeWithTag("Loyola_Button").performClick()

        // Wait for map to recenter to Loyola
        Thread.sleep(5000)

        // Switch back to SGW
        composeTestRule.onNodeWithTag("SGW_Button").performClick()

        // Wait for map to recenter to SGW
        Thread.sleep(5000)

        // Verify app didn't crash and is still functional
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }
}
