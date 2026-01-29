package com.example.campusguide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.screens.ProfileScreen
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun profileScreen_displaysTitle() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                ProfileScreen()
            }
        }

        composeTestRule.onNodeWithText("User settings").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysUserName() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                ProfileScreen()
            }
        }

        composeTestRule.onNodeWithText("Jane Doe").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysStudentSubtitle() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                ProfileScreen()
            }
        }

        composeTestRule.onNodeWithText("Student").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysAccessibilityItem() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                ProfileScreen()
            }
        }

        composeTestRule.onNodeWithText("Accessibility").assertIsDisplayed()
    }

    @Test
    fun profileScreen_backButton_triggersCallback() {
        var backClicked = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                ProfileScreen(
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue("Back button should trigger callback", backClicked)
    }

    @Test
    fun profileScreen_profileItem_triggersCallback() {
        var profileClicked = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                ProfileScreen(
                    onProfileClick = { profileClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Jane Doe").performClick()
        assertTrue("Profile item should trigger callback", profileClicked)
    }

    @Test
    fun profileScreen_accessibilityItem_triggersCallback() {
        var accessibilityClicked = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                ProfileScreen(
                    onAccessibilityClick = { accessibilityClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Accessibility").performClick()
        assertTrue("Accessibility item should trigger callback", accessibilityClicked)
    }

    @Test
    fun profileScreen_displaysUserInitial() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                ProfileScreen()
            }
        }

        composeTestRule.onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun profileScreen_rendersWithoutErrors() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                ProfileScreen()
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun profileScreen_darkTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = true) {
                ProfileScreen()
            }
        }

        composeTestRule.onNodeWithText("User settings").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }
}
