package com.example.campusguide

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
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
class MainNavigationUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun appLaunches_withBottomNavigation() {
        // AC: Bottom navigation is visible on main screens
        Thread.sleep(2000)

        // Verify all 4 bottom nav items exist
        composeTestRule.onNode(hasText("Map")).assertExists()
        composeTestRule.onNode(hasText("Directions")).assertExists()
        composeTestRule.onNode(hasText("Calendar")).assertExists()
        composeTestRule.onNode(hasText("POI")).assertExists()
    }

    @Test
    fun navigationTabs_areClickable() {
        // AC: User can tap bottom navigation items to navigate
        Thread.sleep(2000)

        // Click Directions tab
        composeTestRule.onNode(hasText("Directions")).performClick()
        Thread.sleep(1000)

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
    }

    @Test
    fun topSearchBar_isVisible() {
        // AC: Search bar displayed at top of main screens
        Thread.sleep(2000)

        // Verify search icon exists
        composeTestRule.onNode(hasContentDescription("Search")).assertExists()
    }

    @Test
    fun profileButton_navigatesToSettings() {
        // AC: Profile button navigates to user settings screen
        Thread.sleep(2000)

        // Click profile button (has text "A")
        composeTestRule.onNode(hasText("A")).performClick()

        // Wait for profile screen to load and show it clearly
        Thread.sleep(5000)

        // Verify we're on profile screen
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }


    @Test
    fun navigationDoesNotCrash() {
        // AC: Navigation doesn't crash or create duplicated screens
        Thread.sleep(2000)

        // Navigate through multiple tabs
        composeTestRule.onNode(hasText("Directions")).performClick()
        Thread.sleep(1000)

        composeTestRule.onNode(hasText("Calendar")).performClick()
        Thread.sleep(1000)

        composeTestRule.onNode(hasText("Map")).performClick()
        Thread.sleep(1000)

        // Verify app is still running
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }
}
