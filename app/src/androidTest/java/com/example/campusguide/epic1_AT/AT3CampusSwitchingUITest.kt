package com.example.campusguide.epic1_AT

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.example.campusguide.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for Campus Switching Feature (User Story 1.3)
 *
 * These tests verify the campus toggle functionality that allows users
 * to switch between SGW and Loyola campus views on the map.
 *
 * Acceptance Criteria Tested:
 * 1. Campus toggle switch is visible and accessible
 * 2. User can switch between SGW and Loyola campuses
 * 3. Map overlays update when switching campuses
 * 4. Campus selection persists across app restarts
 *
 * Note: These tests require a connected device or running emulator.
 * Run with: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT3CampusSwitchingUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun campusToggle_fullFlow_e2e() {


        Thread.sleep(2000)

        composeTestRule
            .onNodeWithContentDescription("SGW Campus")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Loyola Campus")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("SGW Campus")
            .assertIsSelected()

        composeTestRule
            .onNodeWithContentDescription("Loyola Campus")
            .assertIsNotSelected()

        Thread.sleep(2000)

        composeTestRule
            .onNodeWithContentDescription("Loyola Campus")
            .performClick()


        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        composeTestRule
            .onNodeWithContentDescription("Loyola Campus")
            .assertIsSelected()

        composeTestRule
            .onNodeWithContentDescription("SGW Campus")
            .assertIsNotSelected()

        composeTestRule
            .onNode(hasStateDescription("Loyola map shown"))
            .assertExists()

        Thread.sleep(3000)

        activityRule.scenario.recreate()
        composeTestRule.waitForIdle()

        Thread.sleep(2000)

        composeTestRule
            .onNodeWithContentDescription("Loyola Campus")
            .assertIsSelected()

        composeTestRule
            .onNodeWithContentDescription("SGW Campus")
            .assertIsNotSelected()
    }
}
