package com.example.campusguide

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import org.junit.Before

import org.junit.Rule
import org.junit.Test


/**
 * UI Tests for Screen Switching
 *
 * These tests verify that the apps navigation bar is functional and working smoothly
 * to switch between different screens in the app
 *
 * Acceptance Criteria Tested:
 * 1. NavigationBar is visible and accessible
 * 2. User can switch between Map, Calendar, Directions, and POI screens
 * 3. The screen material updates when screen switch
 * 4. The home screen of the app is the Map screen and
 * will always open on it regardless of closing screen
 *
 * Note: These tests require a connected device or running emulator.
 * Run with: ./gradlew connectedAndroidTest
 */
class ScreenSwitchingUITest {

    private lateinit var navController: TestNavHostController

    val defaultState = AccessibilityState(
        initialOffsetSp = 16f
    )

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun navigationBar_isVisible() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    ConcordiaCampusGuideApp()
                }
            }
        }

        composeTestRule.onNodeWithText("Map").assertExists()
        composeTestRule.onNodeWithText("Directions").assertExists()
        composeTestRule.onNodeWithText("Calendar").assertExists()
        composeTestRule.onNodeWithText("POI").assertExists()
    }


    @Test
    fun navBarCanSwitchScreens() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    ConcordiaCampusGuideApp()
                }
            }
        }

        composeTestRule.onNodeWithText("Hello Android!").assertExists()
        composeTestRule.onNodeWithText("Directions").performClick()

        composeTestRule.onNodeWithText("Directions Screen").assertExists()
        composeTestRule.onNodeWithText("Calendar").performClick()

        composeTestRule.onNodeWithText("Daily").assertExists()
        composeTestRule.onNodeWithText("Weekly").assertExists()
        composeTestRule.onNodeWithText("Monthly").assertExists()

        composeTestRule.onNodeWithText("POI").performClick()
        composeTestRule.onNodeWithText("POI Screen").assertExists()

        composeTestRule.onNodeWithText("Map").performClick()
        composeTestRule.onNodeWithText("Hello Android!").assertExists()

    }

        @Test
        fun appStartsOnMapScreen() {
            composeTestRule.setContent {
                ConcordiaCampusGuideTheme {
                    CompositionLocalProvider(
                        LocalAccessibilityState provides defaultState
                    ) {
                        ConcordiaCampusGuideApp()
                    }
                }
            }

            composeTestRule.onNodeWithText("Hello Android!").assertExists()

        }

}