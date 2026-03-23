package com.example.campusguide

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class US3_5_ShowShuttleRoute {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun showAllShuttleCustomRouteLooks() {

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))

        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("searchBar").performClick()
        Thread.sleep(2000)

        composeTestRule.onNodeWithTag("searchBar").performTextInput("H")
        Thread.sleep(2000)

        composeTestRule.onNodeWithTag("Henry F. Hall Building").performClick()
        Thread.sleep(3000)

        composeTestRule.onNodeWithContentDescription("Start navigation").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription("View route details")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(6000)

        composeTestRule.onNodeWithContentDescription("Transit: bus or metro route").performClick()
        Thread.sleep(5000)

        composeTestRule.onNodeWithContentDescription("Start navigation").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription("View route details")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(6000)

        composeTestRule.onNodeWithContentDescription("Walk: pedestrian route").performClick()
        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Start navigation").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription("View route details")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(6000)


    }

    @Test
    fun showShuttleTransitRoute() {
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))

        Thread.sleep(5000)

        composeTestRule.onNodeWithTag("searchBar").performClick()
        Thread.sleep(2000)

        composeTestRule.onNodeWithTag("searchBar").performTextInput("H")
        Thread.sleep(2000)

        composeTestRule.onNodeWithTag("Henry F. Hall Building").performClick()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription("Transit: bus or metro route")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(4000)


        composeTestRule.onNodeWithContentDescription("Transit: bus or metro route").performClick()
        Thread.sleep(5000)

        composeTestRule.onNodeWithContentDescription("Start navigation").performClick()

        composeTestRule.waitUntil(timeoutMillis = 20000) {
            composeTestRule.onAllNodesWithContentDescription("View route details")
                .fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(4000)

    }
}