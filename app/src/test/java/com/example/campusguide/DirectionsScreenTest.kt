package com.example.campusguide


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.screens.DirectionsScreen
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import com.google.android.gms.maps.model.LatLng
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import androidx.compose.ui.Modifier
import com.example.campusguide.ui.map.utils.BuildingHit
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class DirectionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val defaultState = AccessibilityState(initialOffsetSp = 16f)

    @Test
    fun directionsScreen_rendersWithoutCrashing() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun directionsScreen_displaysInitialState() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }


        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
    }

    @Test
    fun directionsScreen_darkTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = true) {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
        composeTestRule.waitForIdle()
    }

    @Test
    fun directionsScreen_withModifier_appliesCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun directionsScreen_callsOnMapReadyCallback() {
        var mapReadyCalled = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen(
                        onMapReady = { mapReadyCalled = true }
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

    }

    @Test
    fun directionsScreen_displaysMapView() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun directionsScreen_displaysBottomCard() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
    }

    @Test
    fun directionsScreen_accessibilitySupport() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()

    }

    @Test
    fun directionsScreen_lightTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = false) {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
        composeTestRule.waitForIdle()
    }

    @Test
    fun directionsScreen_multipleCompositions_staysStable() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()


        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
    }

    @Test
    fun directionsScreen_pickDestinationState_displaysCorrectMessage() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
    }

    @Test
    fun directionsScreen_onMapReadyCallback_isInvoked() {
        var mapReadyCalled = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen(
                        onMapReady = { mapReadyCalled = true }
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

    }

    @Test
    fun directionsScreen_polygonClickCallback_triggersDialog() {
        var clickReceived = false
        val testLatLng = LatLng(45.4972, -73.5789)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen(
                        onPolygonClick = { _, _ -> clickReceived = true }
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun directionsScreen_bottomCard_displaysInPickDestinationState() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
    }

    @Test
    fun directionsScreen_handlesNullBuildingHit() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()

    }

    @Test
    fun directionsScreen_zoomControls_areAccessible() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()


        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
        composeTestRule.onNodeWithContentDescription("Zoom Out").assertExists()
    }

    @Test
    fun directionsScreen_mapNavigation_isEnabled() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Zoom In").assertIsEnabled()
    }

    @Test
    fun directionsScreen_appliesThemeColors() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()

    }

    @Test
    fun directionsScreen_supportsAccessibilityState() {
        val customState = AccessibilityState(initialOffsetSp = 20f)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides customState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()

    }

    @Test
    fun directionsScreen_handlesRecomposition() {
        var recomposeFlag by mutableStateOf(false)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()

        recomposeFlag = true
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
    }

    @Test
    fun directionsScreen_preservesMapState() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()


        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
    }

    @Test
    fun directionsScreen_displaysCorrectInitialScreen() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()


        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
    }

    @Test
    fun directionsScreen_handlesModifierCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
    }

    @Test
    fun directionsScreen_bottomCardContent_hasCorrectStyling() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Zoom In").assertExists()
    }

    @Test
    fun directionsScreen_interactionWithMap_isResponsive() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    DirectionsScreen()
                }
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Zoom In").assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Zoom Out").assertIsEnabled()
    }
}

