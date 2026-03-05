package com.example.campusguide.usability.tests

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.campusguide.MainActivity
import com.example.campusguide.usability.SimulatedUser
import com.example.campusguide.usability.UserProfile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsabilityTest1{
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @Test
    fun simulatedUserStartsNavigation() {
        runSimulatedNavigationTest1(SimulatedUser(UserProfile.EMILY_NGUYEN), composeTestRule)
    }
}