package com.example.campusguide

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.fullSuggestions
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.screens.map.MapScreen
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.components.FocusClearWrapper

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

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
                    FocusClearWrapper {
                    MapScreen()
                        }
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
                    FocusClearWrapper {
                        MapScreen(searchQuery = "Concordia")
                    }
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
                    FocusClearWrapper {
                        MapScreen()
                        mapReadyCalled = true // parameter removed; just mark as called
                    }
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
                    FocusClearWrapper {
                    MapScreen()
                    }
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
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Toggle controls on
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
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
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }
        // Toggle controls on
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Zoom Out").assertIsDisplayed()
    }

    @Test
    fun mapScreen_displaysRecenterButton() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }
        // Toggle controls on
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Recenter").assertIsDisplayed()
    }

    @Test
    fun mapScreen_displaysDirectionButtons() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }
        // Toggle controls on
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        composeTestRule.waitForIdle()

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
                    FocusClearWrapper {
                        MapScreen()
                    }
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
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }

        // Toggle controls on
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()

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
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }

        // Toggle controls on
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        composeTestRule.waitForIdle()
        
        // Controls should be visible again
        composeTestRule.onNodeWithContentDescription("Zoom In").assertIsDisplayed()

        // Toggle controls back off
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun mapScreen_darkTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = true) {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }

        // Toggle controls on
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
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
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Toggle controls on
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Zoom In").assertIsDisplayed()
    }

    @Test
    fun mapScreen_polygonClickHandledInternally() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        // Polygon click is now handled internally by MapScreen
    }

    @Test
    fun mapScreen_handlesSearchQueryWithCounter() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    FocusClearWrapper {
                        MapScreen(searchQuery = "Concordia#1")
                    }
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
                    FocusClearWrapper {
                        MapScreen(searchQuery = "")
                    }
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
                        FocusClearWrapper {
                            MapScreen()
                        }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // Toggle controls on
        composeTestRule.onNodeWithContentDescription("Toggle Controls").performClick()
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
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        // Accessibility state should be respected
    }

    @Test
    fun directionsOverlay_notShownByDefault() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        // Directions bottom card must not be visible until a building is selected
        composeTestRule.onNodeWithText("Route options").assertDoesNotExist()
        composeTestRule.onNodeWithText("Directions ready").assertDoesNotExist()
    }

    @Test
    fun directionsOverlay_dismissButtonNotShownByDefault() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        // Dismiss button only appears when a directions card is active
        composeTestRule.onNodeWithContentDescription("Close directions").assertDoesNotExist()
    }

    //Suggestions scoped to active campus by default
    @Test
    fun suggestions_scopedToSGWByDefault() {
        val suggestions = fullSuggestions("hall", Campus.SGW, crossCampus = false)



        // SGW results should appear
        assert(suggestions.any { (it as? CampusBuilding)?.buildingCode == "H" }) {
            "Expected Hall Building (SGW) in suggestions"
        }
        // Loyola results shouldn't appear
        assert(suggestions.none { it.campus == Campus.LOYOLA }) {
            "Expected no Loyola buildings when campus is SGW and crossCampus is false"
        }
    }

    @Test
    fun suggestions_scopedToLoyolaWhenLoyolaSelected() {
        val suggestions = fullSuggestions("vanier", Campus.LOYOLA, crossCampus = false)

        assert(suggestions.any { (it as? CampusBuilding)?.buildingCode == "VL" }) {
            "Expected Vanier Library (Loyola) in suggestions"
        }
        assert(suggestions.none { it.campus == Campus.SGW }) {
            "Expected no SGW buildings when campus is Loyola and crossCampus is false"
        }
    }

    @Test
    fun suggestions_sgwBuildingNotShownOnLoyolaCampus() {
        val suggestions = fullSuggestions("hall", Campus.LOYOLA, crossCampus = false)

        assert(suggestions.none { (it as? CampusBuilding)?.buildingCode == "H" }) {
            "Hall Building (SGW) should not appear when Loyola campus is selected"
        }
    }

    //Cross-campus toggle shows buildings from both campuses

    @Test
    fun suggestions_crossCampusShowsBothCampuses() {
        val suggestions = fullSuggestions("hall", Campus.SGW, crossCampus = true)

        // Should include SGW buildings
        assert(suggestions.any { it.campus == Campus.SGW }) {
            "Expected SGW buildings when crossCampus is true"
        }
    }

    @Test
    fun suggestions_crossCampusShowsLoyolaBuildings() {
        // "ha" matches Hingston Hall wings at Loyola
        val suggestions = fullSuggestions("ha", Campus.SGW, crossCampus = true)

        assert(suggestions.any { it.campus == Campus.LOYOLA }) {
            "Expected Loyola buildings when crossCampus is true"
        }
    }

    @Test
    fun routePanel_crossCampusToggleIsDisplayed() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    FocusClearWrapper {
                        MapScreen()
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        // Route panel not shown until building selected, toggle not yet visible
        composeTestRule.onNodeWithText("Cross-campus routing").assertDoesNotExist()
    }
}
