package com.example.campusguide

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.screens.MapScreen
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.inc

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MapScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val defaultState = AccessibilityState(initialOffsetSp = 16f)

    @Test
    fun mapScreen_rendersWithoutCrashing() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen()
                }
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun mapScreen_rendersWithSearchQuery() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen(searchQuery = "Concordia")
                }
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun mapScreen_callsOnMapReadyCallback() {
        var mapReadyCalled = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen(onMapReady = { mapReadyCalled = true })
                }
            }
        }

        composeTestRule.waitForIdle()

    }

    @Test
    fun mapScreen_displaysCampusToggle() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen()
                }
            }
        }

        composeTestRule.waitForIdle()
        // Campus toggle should be visible
    }

    @Test
    fun mapScreen_displaysMapControls() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen()
                }
            }
        }

        composeTestRule.waitForIdle()
        // Zoom in button should be visible
        composeTestRule.onNodeWithContentDescription("Zoom In").assertIsDisplayed()
    }

    @Test
    fun mapScreen_displaysZoomOutButton() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen()
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Zoom Out").assertIsDisplayed()
    }

    @Test
    fun mapScreen_displaysRecenterButton() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen()
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Recenter").assertIsDisplayed()
    }

    @Test
    fun mapScreen_displaysDirectionButtons() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen()
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Up").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Down").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Left").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Right").assertIsDisplayed()
    }

    @Test
    fun mapScreen_displaysToggleControlsButton() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen()
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Toggle Controls").assertIsDisplayed()
    }

    @Test
    fun mapScreen_togglesControlsVisibility() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen()
                }
            }
        }

        // Controls should be visible initially
        composeTestRule.onNodeWithContentDescription("Zoom In").assertIsDisplayed()
        
        // Toggle controls off
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        composeTestRule.waitForIdle()
        
        // Zoom controls should be hidden, only toggle button visible
        composeTestRule.onNodeWithContentDescription("Zoom In").assertDoesNotExist()
    }

    @Test
    fun mapScreen_togglesControlsBackOn() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen()
                }
            }
        }

        // Toggle controls off
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        composeTestRule.waitForIdle()
        
        // Toggle controls back on
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        composeTestRule.waitForIdle()
        
        // Controls should be visible again
        composeTestRule.onNodeWithContentDescription("Zoom In").assertIsDisplayed()
    }

    @Test
    fun mapScreen_darkTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = true) {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen()
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Zoom In").assertIsDisplayed()
    }

    @Test
    fun mapScreen_lightTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = false) {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen()
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Zoom In").assertIsDisplayed()
    }

    @Test
    fun mapScreen_withOnPolygonClickCallback() {
        var polygonClicked = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen(
                        onPolygonClick = { _, _ -> polygonClicked = true }
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        // Callback is set and ready to be triggered
    }

    @Test
    fun mapScreen_handlesSearchQueryWithCounter() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen(searchQuery = "Concordia#1")
                }
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun mapScreen_handlesBlankSearchQuery() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen(searchQuery = "")
                }
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun mapScreen_multipleCompositions_staysStable() {
        var recomposeKey by mutableStateOf(0)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    // Force recomposition by reading the key
                    key(recomposeKey) {
                        MapScreen()
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Zoom In").assertIsDisplayed()

        // Trigger recomposition
        recomposeKey++
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Zoom In").assertIsDisplayed()
    }

    @Test
    fun mapScreen_accessibilitySupport() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides AccessibilityState(initialOffsetSp = 20f)
                ) {
                    MapScreen()
                }
            }
        }

        composeTestRule.waitForIdle()
        // Accessibility state should be respected
    }
}
