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
 * Acceptance Test for US-1.7: Access Profile Menu from Search Bar
 *
 * Tests verify profile icon in search bar opens user settings screen
 * with profile and accessibility items.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ProfileMenuUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun profileIcon_opensUserSettings() {
        // AC: Tapping profile icon navigates to User settings screen
        Thread.sleep(2000)

        // Click profile icon (has text "A")
        composeTestRule.onNode(hasText("A")).performClick()

        // Wait for profile screen to load and show it clearly
        Thread.sleep(4000)

        // Verify we're on User settings screen
        composeTestRule.onNode(hasText("User settings")).assertExists()
    }

    @Test
    fun userSettings_showsProfileItem() {
        // AC: User settings displays User profile item
        Thread.sleep(2000)

        // Navigate to profile
        composeTestRule.onNode(hasText("A")).performClick()
        Thread.sleep(4000)

        // Verify profile item shows placeholder name
        composeTestRule.onNode(hasText("Jane Doe")).assertExists()
        composeTestRule.onNode(hasText("Student")).assertExists()
    }

    @Test
    fun userSettings_showsAccessibilityItem() {
        // AC: User settings displays Accessibility item
        Thread.sleep(2000)

        // Navigate to profile
        composeTestRule.onNode(hasText("A")).performClick()
        Thread.sleep(4000)

        // Verify accessibility item exists
        composeTestRule.onNode(hasText("Accessibility")).assertExists()
    }

    @Test
    fun userSettings_backButton_returnsToMain() {
        // AC: Back arrow returns to previous screen
        Thread.sleep(2000)

        // Navigate to profile
        composeTestRule.onNode(hasText("A")).performClick()
        Thread.sleep(4000)

        // Click back button
        composeTestRule.onNode(hasContentDescription("Back")).performClick()
        Thread.sleep(3000)

        // Verify we're back on main screen with bottom nav
        composeTestRule.onNode(hasText("Map")).assertExists()
    }

    @Test
    fun profileIcon_worksFromDifferentTabs() {
        // AC: Profile navigation works from any main tab with search bar
        Thread.sleep(2000)

        // Navigate to Directions tab
        composeTestRule.onNode(hasText("Directions")).performClick()
        Thread.sleep(2000)

        // Click profile from Directions tab
        composeTestRule.onNode(hasText("A")).performClick()
        Thread.sleep(4000)

        // Verify User settings opened
        composeTestRule.onNode(hasText("User settings")).assertExists()

        // Go back
        composeTestRule.onNode(hasContentDescription("Back")).performClick()
        Thread.sleep(2000)

        // Navigate to Calendar tab
        composeTestRule.onNode(hasText("Calendar")).performClick()
        Thread.sleep(2000)

        // Click profile from Calendar tab
        composeTestRule.onNode(hasText("A")).performClick()
        Thread.sleep(4000)

        // Verify User settings opened again
        composeTestRule.onNode(hasText("User settings")).assertExists()
    }
}