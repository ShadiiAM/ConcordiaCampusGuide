package com.example.campusguide.usability.tests

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.campusguide.MainActivity
import com.example.campusguide.usability.SimulatedUser
import com.example.campusguide.usability.UsabilityTracker
import com.example.campusguide.usability.UserProfile
import kotlin.random.Random

fun runSimulatedAccessibilityChangeTest2(
    user: SimulatedUser,
    composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    UsabilityTracker.start("accessibility_change")

    when (user.profile) {
        UserProfile.JORDAN_LEE -> {
            if (Random.nextFloat() < 0.3f) {

                composeTestRule
                .onNodeWithTag("searchBar")        //30 % chance of error misclick
                .performClick()
                user.pause()
            }
        }
        UserProfile.ALEXIA_MARTIN -> {
            user.pause()
        }
        else -> {
        }
    }

    composeTestRule.onNodeWithTag("UserProfile").performClick()
    user.pause()

    composeTestRule.onNodeWithTag("goToAccessibility").performClick()
    user.pause()


    composeTestRule.onNodeWithTag("increaseTextSize").performClick()
    composeTestRule.onNodeWithTag("increaseTextSize").performClick()

    user.pause()

    composeTestRule.onNodeWithTag("increaseTextSize").performClick()

    if (user.profile == UserProfile.SOFIA_LOPEZ || user.profile == UserProfile.JORDAN_LEE){
        if (Random.nextFloat() < 0.3f) {    // error prone /distracted user persona likely to  make extra button click

            composeTestRule.onNodeWithTag("increaseTextSize").performClick()
            user.pause()
            composeTestRule.onNodeWithTag("decreaseTextSize").performClick()

        }
    }

    user.pause()
    composeTestRule.onNodeWithTag("boldenText").performClick()

    user.pause()

    composeTestRule
        .onNodeWithContentDescription("Back")
        .performClick()

    user.pause()

    composeTestRule
        .onNodeWithContentDescription("Back")
        .performClick()

    composeTestRule
        .onNodeWithText("Map")
        .assertExists()
    UsabilityTracker.end("accessibility_change", user.profile)
}
