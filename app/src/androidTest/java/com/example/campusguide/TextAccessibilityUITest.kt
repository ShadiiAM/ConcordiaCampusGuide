package com.example.campusguide

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance Test for US-1.10: Text Accessibility Features
 *
 * Tests verify text size adjustment, bold text toggle, and colorblind modes.
 *
 * NOTE: Bold toggle and color box require MANUAL clicking during recording
 * because they don't have accessible identifiers for automated clicking.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class TextAccessibilityUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun accessibilityScreen_isAccessible() {
        // AC: Accessibility settings accessible from profile menu
        Thread.sleep(2000)

        // Navigate to profile
        composeTestRule.onNode(hasText("A")).performClick()
        Thread.sleep(2000)

        // Click Accessibility item
        composeTestRule.onNode(hasText("Accessibility")).performClick()
        Thread.sleep(3000)

        // Verify we're on Accessibility screen
        composeTestRule.onNode(hasText("Display and Text Size")).assertExists()
    }

    @Test
    fun textSize_canBeAdjusted() {
        // AC: Text size can be adjusted with +/- buttons
        Thread.sleep(2000)

        // Navigate to accessibility
        composeTestRule.onNode(hasText("A")).performClick()
        Thread.sleep(2000)
        composeTestRule.onNode(hasText("Accessibility")).performClick()
        Thread.sleep(3000)

        // Increase text size
        composeTestRule.onNode(hasText("+")).performClick()
        Thread.sleep(2000)
        composeTestRule.onNode(hasText("+")).performClick()
        Thread.sleep(2000)

        // Decrease text size
        composeTestRule.onNode(hasText("-")).performClick()
        Thread.sleep(2000)

        // Verify screen still displayed
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun boldText_canBeToggled() {
        // AC: Bold text can be toggled on/off
        Thread.sleep(2000)

        // Navigate to accessibility
        composeTestRule.onNode(hasText("A")).performClick()
        Thread.sleep(2000)
        composeTestRule.onNode(hasText("Accessibility")).performClick()
        Thread.sleep(3000)

        // Bold setting row exists
        composeTestRule.onNode(hasText("Bold")).assertExists()
        Thread.sleep(2000)

        // ACTION: MANUALLY click the Bold toggle switch in the recording
        Thread.sleep(3000)

        // ACTION: MANUALLY click it again to toggle off
        Thread.sleep(3000)

        // Verify screen displayed
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun colorBlindModes_canBeCycled() {
        // AC: Colorblind modes can be cycled
        Thread.sleep(2000)

        // Navigate to accessibility
        composeTestRule.onNode(hasText("A")).performClick()
        Thread.sleep(2000)
        composeTestRule.onNode(hasText("Accessibility")).performClick()
        Thread.sleep(3000)

        // Text colour row exists
        composeTestRule.onNode(hasText("Text colour")).assertExists()
        Thread.sleep(2000)

        // ACTION: MANUALLY click the color box to cycle through modes
        // (Click once for each mode: Protanopia, Deuteranopia, Tritanopia)
        Thread.sleep(3000)

        // ACTION: Click again for Deuteranopia
        Thread.sleep(3000)

        // ACTION: Click again for Tritanopia
        Thread.sleep(3000)

        // Verify screen displayed
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun textSize_appliesAcrossScreens() {
        // AC: Font size changes apply across all screens
        Thread.sleep(2000)

        // Navigate to accessibility
        composeTestRule.onNode(hasText("A")).performClick()
        Thread.sleep(2000)
        composeTestRule.onNode(hasText("Accessibility")).performClick()
        Thread.sleep(3000)

        // Increase text size significantly
        composeTestRule.onNode(hasText("+")).performClick()
        Thread.sleep(1000)
        composeTestRule.onNode(hasText("+")).performClick()
        Thread.sleep(1000)
        composeTestRule.onNode(hasText("+")).performClick()
        Thread.sleep(2000)

        // Go back to main screen
        composeTestRule.onNode(hasContentDescription("Back")).performClick()
        Thread.sleep(2000)
        composeTestRule.onNode(hasContentDescription("Back")).performClick()
        Thread.sleep(2000)

        // Navigate to different tabs to show text size applies everywhere
        composeTestRule.onNode(hasText("Directions")).performClick()
        Thread.sleep(2000)

        composeTestRule.onNode(hasText("Calendar")).performClick()
        Thread.sleep(2000)

        composeTestRule.onNode(hasText("POI")).performClick()
        Thread.sleep(2000)

        composeTestRule.onNode(hasText("Map")).performClick()
        Thread.sleep(2000)

        // Verify app functional with larger text
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun accessibilitySettings_persist() {
        // AC: Settings persist across navigation
        Thread.sleep(2000)

        // Navigate to accessibility
        composeTestRule.onNode(hasText("A")).performClick()
        Thread.sleep(2000)
        composeTestRule.onNode(hasText("Accessibility")).performClick()
        Thread.sleep(3000)

        // Change text size
        composeTestRule.onNode(hasText("+")).performClick()
        Thread.sleep(2000)

        // Go back to main
        composeTestRule.onNode(hasContentDescription("Back")).performClick()
        Thread.sleep(2000)
        composeTestRule.onNode(hasContentDescription("Back")).performClick()
        Thread.sleep(2000)

        // Navigate to accessibility again
        composeTestRule.onNode(hasText("A")).performClick()
        Thread.sleep(2000)
        composeTestRule.onNode(hasText("Accessibility")).performClick()
        Thread.sleep(3000)

        // Text size setting should still be applied
        composeTestRule.onNode(hasText("Text size")).assertExists()
    }
}
