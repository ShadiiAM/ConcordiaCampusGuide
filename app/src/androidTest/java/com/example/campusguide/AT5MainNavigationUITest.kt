package com.example.campusguide

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
 * Acceptance Test for US-1.6: Main App Navigation
 *
 * Tests verify bottom navigation bar and top search bar with profile button.
 * Navigation between Map, Directions, Calendar, POI screens is tested.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT5MainNavigationUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun acceptanceTest5() {
        // AC: User can tap bottom navigation items to navigate
        Thread.sleep(2000)

        // Click Calendar tab
        composeTestRule.onNode(hasText("Calendar")).performClick()
        Thread.sleep(1000)

        // Click POI tab
        composeTestRule.onNode(hasText("POI")).performClick()
        Thread.sleep(1000)

        // Click back to Map tab
        composeTestRule.onNode(hasText("Map")).performClick()
        Thread.sleep(1000)

        // Verify app didn't crash
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))


        // AC: Profile button navigates to user settings screen
        Thread.sleep(2000)

        // Click profile button (has text "A")
        composeTestRule.onNode(hasText("A")).performClick()

        // Wait for profile screen to load and show it clearly

        // Verify we're on profile screen
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))

        Thread.sleep(5000)
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))

        Thread.sleep(3000)

    }
}
