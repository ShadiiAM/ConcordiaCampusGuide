package com.example.campusguide.epic1_AT

import android.Manifest
import android.R
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.example.campusguide.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for Map Navigation Controls. US-1.9
 *
 * These tests verify the functionality of map navigation controls
 * such as zoom in and zoom out.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT9MapNavigationControlsUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)


    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun mapNavigationControlsTest() {

        onView(withId(R.id.content))
        .check(matches(isDisplayed()))

        Thread.sleep(5000)


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

        composeTestRule.onNodeWithContentDescription("Up").performClick()
        composeTestRule.onNodeWithContentDescription("Up").performClick()

        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Down").performClick()
        composeTestRule.onNodeWithContentDescription("Down").performClick()

        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Recenter").performClick()

        Thread.sleep(5000)

    }
}
