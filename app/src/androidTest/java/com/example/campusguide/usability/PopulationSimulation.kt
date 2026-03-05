package com.example.campusguide.usability

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import com.example.campusguide.MainActivity
import com.example.campusguide.UsabilityTest1
import com.example.campusguide.runSimulatedNavigation
import org.junit.Rule
import org.junit.Test



class PopulationSimulationTest {


    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    val profiles = listOf(
        UserProfile.LIAM_DUBOIS,
        UserProfile.EMILY_NGUYEN,
        UserProfile.ALEXIA_MARTIN,
        UserProfile.JORDAN_LEE,
        UserProfile.SOFIA_LOPEZ

    )

    @Test
    fun runPopulationSimulation() {
        repeat(50) {

            runSimulatedNavigation(SimulatedUser(profiles.random()), composeTestRule)
            composeTestRule.activityRule.scenario.recreate()
            composeTestRule.waitForIdle()
            Thread.sleep(1000)
        }
        UsabilityTracker.dumpResults()
    }
}