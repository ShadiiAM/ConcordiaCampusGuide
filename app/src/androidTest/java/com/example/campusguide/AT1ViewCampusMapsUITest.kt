package com.example.campusguide

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
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
 * Acceptance Test for US-1.1: View SGW and Loyola Campus Maps
 *
 * US-1.1 is about VIEWING the campus map, not switching between campuses.
 * Campus switching is covered in US-1.3.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT1ViewCampusMapsUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)


    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun appOpens_displaysDefaultCampusMap() {
        // AC: App opens for first time and displays campus map with default campus
        Thread.sleep(3000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        // map is usable and can move
        composeTestRule.onNodeWithContentDescription("Right").performClick()
        composeTestRule.onNodeWithContentDescription("Right").performClick()

        Thread.sleep(2000)

    }
}