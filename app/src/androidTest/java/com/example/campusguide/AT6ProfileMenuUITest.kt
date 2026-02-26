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
 * Acceptance Test for US-1.7: Access Profile Menu from Search Bar
 *
 * Tests verify profile icon in search bar opens user settings screen
 * with profile and accessibility items.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT6ProfileMenuUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun acceptanceTest6() {

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))

        // AC: Tapping profile icon navigates to User settings screen
        Thread.sleep(2000)

        // Click profile icon (has text "A")
        composeTestRule.onNode(hasText("A")).performClick()


        // Wait for profile screen to load and show it clearly
        // Verify we're on User settings screen
        composeTestRule.onNode(hasText("User settings")).assertExists()
        Thread.sleep(4000)

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))

        Thread.sleep(3000)

        composeTestRule.onNode(hasText("Calendar")).performClick()

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
        Thread.sleep(2000)

        // Click profile icon (has text "A")
        composeTestRule.onNode(hasText("A")).performClick()

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))

        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))

        Thread.sleep(2000)

    }
}