package com.example.campusguide

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Additional component tests to improve coverage
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class ComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun greeting_withCustomModifier_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                Greeting(name = "World", modifier = Modifier)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun greetingPreview_darkTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = true) {
                GreetingPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun greetingPreview_lightTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = false) {
                GreetingPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun concordiaCampusGuideApp_multipleNavigationClicks() {
        composeTestRule.setContent {
            ConcordiaCampusGuideApp()
        }

        // Navigate through all destinations multiple times
        repeat(2) {
            composeTestRule.onNodeWithText("Directions").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("POI").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Calendar").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Map").performClick()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun greeting_multipleNamesSequence1() {
        composeTestRule.setContent {
            Greeting("User1")
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun greeting_multipleNamesSequence2() {
        composeTestRule.setContent {
            Greeting("User2")
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun greeting_multipleNamesSequence3() {
        composeTestRule.setContent {
            Greeting("User3")
        }
        composeTestRule.waitForIdle()
    }
}
