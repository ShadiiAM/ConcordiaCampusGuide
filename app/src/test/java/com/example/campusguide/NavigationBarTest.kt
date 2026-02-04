package com.example.campusguide

import android.content.Intent
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.components.NavigationBar
import com.example.campusguide.ui.components.NavigationBarPreview
import com.example.campusguide.ui.components.SearchBarWithProfile
import com.example.campusguide.ui.components.SearchBarWithProfilePreview
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class NavigationBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun nav_BarDisplaysAppDestinations() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    rememberSaveable { mutableStateOf(AppDestinations.MAP) },
                    {
                        SearchBarWithProfile(
                        )
                    })
            }
        }

        composeTestRule.onNodeWithText("Map").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Directions").assertIsDisplayed()
        composeTestRule.onNodeWithText("POI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()

    }


    @Test
    fun navBar_rendersWithoutErrors() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    rememberSaveable { mutableStateOf(AppDestinations.MAP) },
                    {
                        SearchBarWithProfile(
                        )
                    })
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun navBar_darkTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = true) {
                NavigationBar(
                    rememberSaveable { mutableStateOf(AppDestinations.MAP) },
                    {
                        SearchBarWithProfile(
                        )
                    })
            }
        }

        composeTestRule.onNodeWithText("Map").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Directions").assertIsDisplayed()
        composeTestRule.onNodeWithText("POI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()


        composeTestRule.waitForIdle()
    }

    @Test
    fun navBar_withNoContent_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    rememberSaveable { mutableStateOf(AppDestinations.MAP) }, {}
                    )
            }

        }
        
        composeTestRule.onNodeWithText("Map").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Directions").assertIsDisplayed()
        composeTestRule.onNodeWithText("POI").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    @Test
    fun navBarPreview_rendersCorrectly() {
        composeTestRule.setContent {
            NavigationBarPreview()
        }

        composeTestRule.onNodeWithText("Map").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Directions").assertIsDisplayed()
        composeTestRule.onNodeWithText("POI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

}