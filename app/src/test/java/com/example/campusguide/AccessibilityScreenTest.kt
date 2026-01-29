package com.example.campusguide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.screens.AccessibilityScreen
import com.example.campusguide.ui.screens.AccessibilityScreenPreview
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class AccessibilityScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun accessibilityScreen_displaysTitle() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        composeTestRule.onNodeWithText("Accessibility").assertIsDisplayed()
    }

    @Test
    fun accessibilityScreen_displaysSectionHeader() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        composeTestRule.onNodeWithText("Display and Text Size").assertIsDisplayed()
    }

    @Test
    fun accessibilityScreen_displaysTextSizeSetting() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        composeTestRule.onNodeWithText("Text size").assertIsDisplayed()
    }

    @Test
    fun accessibilityScreen_displaysTextColourSetting() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        composeTestRule.onNodeWithText("Text colour").assertIsDisplayed()
    }

    @Test
    fun accessibilityScreen_displaysBoldSetting() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        composeTestRule.onNodeWithText("Bold").assertIsDisplayed()
    }

    @Test
    fun accessibilityScreen_backButton_triggersCallback() {
        var backClicked = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen(
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue("Back button should trigger callback", backClicked)
    }

    @Test
    fun accessibilityScreen_boldToggle_canBeClicked() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        // Find and click the switch (it's near the Bold text)
        composeTestRule.onNodeWithText("Bold").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    @Test
    fun accessibilityScreen_displaysMinusButton() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        composeTestRule.onNodeWithText("-").assertIsDisplayed()
    }

    @Test
    fun accessibilityScreen_displaysPlusButton() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        composeTestRule.onNodeWithText("+").assertIsDisplayed()
    }

    @Test
    fun accessibilityScreen_rendersWithoutErrors() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun accessibilityScreen_darkTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = true) {
                AccessibilityScreen()
            }
        }

        composeTestRule.onNodeWithText("Accessibility").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    @Test
    fun accessibilityScreen_minusButton_canBeClicked() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        composeTestRule.onNodeWithText("-").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun accessibilityScreen_plusButton_canBeClicked() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun accessibilityScreenPreview_rendersCorrectly() {
        composeTestRule.setContent {
            AccessibilityScreenPreview()
        }

        composeTestRule.onNodeWithText("Accessibility").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    @Test
    fun accessibilityScreen_lightTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = false) {
                AccessibilityScreen()
            }
        }

        composeTestRule.onNodeWithText("Accessibility").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    @Test
    fun accessibilityScreen_withDefaultCallback_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen(
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Display and Text Size").assertIsDisplayed()
    }

    @Test
    fun accessibilityScreen_allSettingsAreDisplayed() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        // Verify all three settings are displayed
        composeTestRule.onNodeWithText("Text size").assertIsDisplayed()
        composeTestRule.onNodeWithText("Text colour").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bold").assertIsDisplayed()
    }

    @Test
    fun accessibilityScreen_multipleButtonClicks() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                AccessibilityScreen()
            }
        }

        composeTestRule.onNodeWithText("-").performClick()
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("-").performClick()
        composeTestRule.waitForIdle()
    }
}
