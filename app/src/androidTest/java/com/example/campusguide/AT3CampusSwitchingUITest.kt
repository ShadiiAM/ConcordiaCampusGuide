package com.example.campusguide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

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

        composeTestRule.activityRule.scenario.recreate()
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
