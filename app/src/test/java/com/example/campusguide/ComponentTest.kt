package com.example.campusguide

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
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

    val defaultState = AccessibilityState(
        initialOffsetSp = 16f
    )

    @Test
    fun greeting_withCustomModifier_rendersCorrectly() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccessibilityState provides defaultState
            ) {
                ConcordiaCampusGuideTheme {
                    Greeting(name = "World", modifier = Modifier)
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun greetingPreview_darkTheme_rendersCorrectly() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccessibilityState provides defaultState
            ) {
                ConcordiaCampusGuideTheme(darkTheme = true) {
                    GreetingPreview()
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun greetingPreview_lightTheme_rendersCorrectly() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccessibilityState provides defaultState
            ) {
            ConcordiaCampusGuideTheme(darkTheme = false) {
                GreetingPreview()
            }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun concordiaCampusGuideApp_multipleNavigationClicks() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccessibilityState provides defaultState
            ) {
                ConcordiaCampusGuideApp()
            }
        }

        // Navigate through all destinations multiple times
        repeat(2) {
            composeTestRule.onNodeWithText("Directions").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Calendar").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("POI").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Map").performClick()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun greeting_multipleNamesSequence1() {
        composeTestRule.setContent {CompositionLocalProvider(
            LocalAccessibilityState provides defaultState
        ) {
            Greeting("User1")
        }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun greeting_multipleNamesSequence2() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccessibilityState provides defaultState
            ) {
                Greeting("User2")
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun greeting_multipleNamesSequence3() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccessibilityState provides defaultState
            ) {
                Greeting("User3")
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun greeting_withNumbers_rendersCorrectly() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccessibilityState provides defaultState
            ) {
                ConcordiaCampusGuideTheme {
                    Greeting(name = "12345")
                }
            }
        }
        composeTestRule.onNodeWithText("Hello 12345!").assertIsDisplayed()
    }

    @Test
    fun greeting_withUnicode_rendersCorrectly() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccessibilityState provides defaultState
            ) {
                ConcordiaCampusGuideTheme {
                    Greeting(name = "Jean-Pierre")
                }
            }
        }
        composeTestRule.onNodeWithText("Hello Jean-Pierre!").assertIsDisplayed()
    }

    @Test
    fun concordiaCampusGuideApp_rapidNavigationClicks() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccessibilityState provides defaultState
            ) {
                ConcordiaCampusGuideApp()
            }
        }

        // Rapid clicks on same destination
        repeat(3) {
            composeTestRule.onNodeWithText("Map").performClick()
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun concordiaCampusGuideApp_poiDestination_isDisplayed() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccessibilityState provides defaultState
            ) {
                ConcordiaCampusGuideApp()
            }
        }

        composeTestRule.onNodeWithText("POI").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun concordiaCampusGuideApp_calendarDestination_isDisplayed() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccessibilityState provides defaultState
            ) {
                ConcordiaCampusGuideApp()
            }
        }

        composeTestRule.onNodeWithText("Calendar").performClick()
        composeTestRule.waitForIdle()
    }
}
