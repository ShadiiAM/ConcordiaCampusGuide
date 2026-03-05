package com.example.campusguide

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.campusguide.usability.SimulatedUser
import com.example.campusguide.usability.UsabilityTracker
import com.example.campusguide.usability.UserProfile

fun runSimulatedNavigation(
    user: SimulatedUser,
    composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    UsabilityTracker.start("search_location")
    when (user.profile) {
        UserProfile.EMILY_NGUYEN -> {
            composeTestRule
                .onNodeWithContentDescription("Bottom search button")
                .performClick()
        }
        else -> {
            composeTestRule
                .onNodeWithTag("searchBar")
                .performClick()
        }
    }

    user.pause()

    val input = user.maybeMakeTypingError("Hall Bui")
    composeTestRule
        .onNodeWithTag("searchBar")
        .performTextInput(input)

    user.pause()

    if (input != "Hall Bui") {
        composeTestRule
            .onNodeWithTag("searchBar")
            .performTextClearance()
        composeTestRule
            .onNodeWithTag("searchBar")
            .performTextInput("Hall Bui")
        user.pause()
    }

    composeTestRule
        .onNodeWithTag("Henry F. Hall Building")
        .performClick()

    user.pause()

    composeTestRule
        .onNodeWithTag("DirectionsGo")
        .performClick()

    UsabilityTracker.end("search_location")
}
