package com.example.campusguide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.components.SearchBarWithProfile
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SearchBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchBar_displaysPlaceholder() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                SearchBarWithProfile()
            }
        }

        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()
    }

    @Test
    fun searchBar_displaysSearchIcon() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                SearchBarWithProfile()
            }
        }

        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun searchBar_displaysProfileAvatar() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                SearchBarWithProfile()
            }
        }

        composeTestRule.onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun searchBar_profileClick_triggersCallback() {
        var profileClicked = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                SearchBarWithProfile(
                    onProfileClick = { profileClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("A").performClick()
        assertTrue("Profile avatar click should trigger callback", profileClicked)
    }

    @Test
    fun searchBar_textInput_triggersCallback() {
        var searchQuery = ""

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                SearchBarWithProfile(
                    onSearchQueryChange = { searchQuery = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Search...").performTextInput("test query")
        assertEquals("Search query should be updated", "test query", searchQuery)
    }

    @Test
    fun searchBar_rendersWithoutErrors() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                SearchBarWithProfile()
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun searchBar_darkTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = true) {
                SearchBarWithProfile()
            }
        }

        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    @Test
    fun searchBar_withDefaultCallbacks_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                SearchBarWithProfile(
                    onSearchQueryChange = {},
                    onProfileClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()
    }
}
