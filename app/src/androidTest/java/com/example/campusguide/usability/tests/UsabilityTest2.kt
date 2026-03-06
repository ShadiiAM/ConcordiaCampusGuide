package com.example.campusguide.usability.tests

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import com.example.campusguide.MainActivity
import com.example.campusguide.usability.SimulatedUser
import com.example.campusguide.usability.UserProfile
import org.junit.Rule
import org.junit.Test

class UsabilityTest2 {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @Test
    fun simulatedUserChangesAccessibilitySettings() {
        runSimulatedAccessibilityChangeTest2(SimulatedUser(UserProfile.EMILY_NGUYEN), composeTestRule)
    }

}