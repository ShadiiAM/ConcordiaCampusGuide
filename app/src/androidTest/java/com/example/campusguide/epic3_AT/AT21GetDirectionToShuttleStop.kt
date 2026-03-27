package com.example.campusguide.epic3_AT

import android.Manifest
import android.R
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.example.campusguide.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance Test for US-3.4: Get directions to a shuttle stop
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT21GetDirectionToShuttleStop {


    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val composeTestRule = createEmptyComposeRule()



    @Test
    fun getDirectionsUsingShuttleMarker() {

        Espresso.onView(ViewMatchers.withId(R.id.content))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Thread.sleep(3000)

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val marker = device.findObject(UiSelector().description("sgw_shuttle1"))

        if (marker.waitForExists(5000)) {
            marker.click()
        } else {
            throw AssertionError("Marker 'sgw_shuttle1' not found on screen within 5 seconds")
        }

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onNodeWithContentDescription("ShuttleInfoCardDirections")
                .isDisplayed()
        }
        Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("ShuttleInfoCardDirections").performClick()
        Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("Start navigation").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription("View route details")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(6000)

    }


    @Test
    fun getDirectionsUsingShuttleSearch() {
        Espresso.onView(ViewMatchers.withId(R.id.content))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Thread.sleep(3000)

        composeTestRule.onNodeWithTag("searchBar").performTextInput("Shu")
        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("sgw_shuttle").performClick()
        Thread.sleep(5000)

        composeTestRule.onNodeWithContentDescription("Start navigation").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription("View route details")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithContentDescription("Loyola Campus").performClick()
        Thread.sleep(6000)
    }


}