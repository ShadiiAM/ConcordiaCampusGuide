package com.example.campusguide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance Test for US-1.8: Accessibility search bar
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT9AccessibilityButtonUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)


    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun accessibleSearchButtonUITest() {

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))

        Thread.sleep(5000)

        // Click the accessibility search button
        composeTestRule.onNodeWithContentDescription("Bottom search button").performClick()

        Thread.sleep(3000)


        // Type into the focused field
        composeTestRule
            .onNodeWithTag("searchBar")
            .assertIsFocused()
            .performTextInput("Accessible Button Worked!")

        composeTestRule.onNodeWithTag("searchBar")
            .assertTextEquals("Accessible Button Worked!")

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))

        Thread.sleep(2000)

        composeTestRule.onNodeWithTag("mapControls").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        Thread.sleep(2000)

        composeTestRule.onNodeWithTag("mapControls").assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        Thread.sleep(2000)

        composeTestRule.onNodeWithTag("mapControls").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Zoom In").performClick()
        composeTestRule.onNodeWithContentDescription("Zoom In").performClick()

        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Zoom Out").performClick()
        composeTestRule.onNodeWithContentDescription("Zoom Out").performClick()

        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Right").performClick()
        composeTestRule.onNodeWithContentDescription("Right").performClick()

        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Left").performClick()
        composeTestRule.onNodeWithContentDescription("Left").performClick()

        Thread.sleep(2000)


    }
}