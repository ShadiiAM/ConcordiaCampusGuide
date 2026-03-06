package com.example.campusguide.usability.tests

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.campusguide.MainActivity
import com.example.campusguide.usability.SimulatedUser
import com.example.campusguide.usability.UsabilityTracker
import com.example.campusguide.usability.UserProfile

fun runSimulatedNavigationTest1(
    user: SimulatedUser,
    composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    UsabilityTracker.start("search_location")
    when (user.profile) {
        UserProfile.EMILY_NGUYEN -> {   //  user persona uses accessible search button
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
    val searchBar = composeTestRule.onNodeWithTag("searchBar")

    // do {    THIS IS IN CASE I want to simulate multiple fake typos not just once.
    var input = user.maybeMakeTypingError("Hall Bui")

    searchBar.performTextInput(input)

    user.pause()

    if (input != "Hall Bui") {
        // input = user.maybeMakeTypingError("Hall Bui")
        searchBar.performTextClearance()
        searchBar.performTextInput("Hall Bui")     //  .performTextInput(input)
        user.pause()
    }
    // }while(input != "Hall Bui")

    composeTestRule
        .onNodeWithTag("Henry F. Hall Building")
        .performClick()

    user.pause()

    composeTestRule
        .onNodeWithTag("DirectionsGo")
        .performClick()

    UsabilityTracker.end("search_location", user.profile)
}
