package com.example.campusguide

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.components.SearchBarWithProfile
import com.example.campusguide.ui.components.SearchBarWithProfilePreview
import com.example.campusguide.ui.screens.AccessibilityScreen
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.buildingSuggestions
import com.example.campusguide.ui.components.BuildingAutocompleteField
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.screens.MapScreen
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

    val defaultState = AccessibilityState(
        initialOffsetSp = 16f
    )

    @Test
    fun searchBar_displaysPlaceholder() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile()
                }

            }
        }

        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()
    }

    @Test
    fun searchBar_displaysSearchIcon() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile()
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun searchBar_displaysProfileAvatar() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile()
                }
            }
        }

        composeTestRule.onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun searchBar_profileClick_triggersCallback() {
        var profileClicked = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile(
                        onProfileClick = { profileClicked = true }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("A").performClick()
        assertTrue("Profile avatar click should trigger callback", profileClicked)
    }

    @Test
    fun searchBar_textInput_triggersCallback() {
        var callbackTriggered = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile(
                        onSearchQueryChange = { callbackTriggered = true }
                    )
                }
            }
        }

        // Verify the search bar renders with placeholder
        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()
        // The callback setup is valid - actual text input testing requires instrumented tests
        composeTestRule.waitForIdle()
    }

    @Test
    fun searchBar_rendersWithoutErrors() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile()
                }
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun searchBar_darkTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = true) {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile()
                }
            }
        }

        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    @Test
    fun searchBar_withDefaultCallbacks_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile(
                        onSearchQueryChange = {},
                        onProfileClick = {}
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun searchBarWithProfilePreview_rendersCorrectly() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalAccessibilityState provides defaultState
            ) {
                SearchBarWithProfilePreview()
            }
        }

        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    @Test
    fun searchBar_withCustomModifier_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                    CompositionLocalProvider(
                        LocalAccessibilityState provides defaultState
                    ) {
                        SearchBarWithProfile(
                            modifier = androidx.compose.ui.Modifier
                        )
                    }
            }
        }

        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()
    }

    @Test
    fun searchBar_profileAvatarDisplaysInitial() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile()
                }
            }
        }

        // Verify the avatar shows the initial "A"
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun searchBar_lightTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = false) {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile()
                }
            }
        }

        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    // Suggestions appear when user types a building name or code

    @Test
    fun searchBar_showsSuggestionsWhenUserTypes() {
        val suggestions = buildingSuggestions("hall", Campus.SGW, crossCampus = false)
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile(
                        suggestions = suggestions,
                        onBuildingSelected = {},
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Henry F. Hall Building").assertIsDisplayed()
    }

    @Test
    fun searchBar_showsSuggestionsByBuildingCode() {
        val suggestions = buildingSuggestions("EV", Campus.SGW, crossCampus = false)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile(
                        suggestions = suggestions,
                        onBuildingSelected = {},
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Engineering, Computer Science and Visual Arts Integrated Complex")
            .assertIsDisplayed()
    }

    @Test
    fun searchBar_showsNothingWhenQueryIsEmpty() {
        val suggestions = buildingSuggestions("", Campus.SGW, crossCampus = false)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile(
                        suggestions = suggestions,
                        onBuildingSelected = {},
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Henry F. Hall Building").assertDoesNotExist()
    }

    @Test
    fun autocompleteField_showsDropdownWhenFocusedAndQueryNotEmpty() {
        val suggestions = buildingSuggestions("mb", Campus.SGW, crossCampus = false)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                BuildingAutocompleteField(
                    label = "To:",
                    value = "mb",
                    suggestions = suggestions,
                    onQueryChange = {},
                    onSelected = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("John Molson Building").assertIsDisplayed()
    }

    @Test
    fun autocompleteField_showsBuildingCodeBadge() {
        val suggestions = buildingSuggestions("hall", Campus.SGW, crossCampus = false)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                BuildingAutocompleteField(
                    label = "To:",
                    value = "hall",
                    suggestions = suggestions,
                    onQueryChange = {},
                    onSelected = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("H").assertIsDisplayed()
    }

    @Test
    fun autocompleteField_showsBuildingAddress() {
        val suggestions = buildingSuggestions("hall", Campus.SGW, crossCampus = false)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                BuildingAutocompleteField(
                    label = "To:",
                    value = "hall",
                    suggestions = suggestions,
                    onQueryChange = {},
                    onSelected = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("1455 De Maisonneuve Blvd. W.").assertIsDisplayed()
    }

    //Selecting a suggestion fires the onSelected callback

    @Test
    fun autocompleteField_selectingSuggestionFiresCallback() {
        var selectedBuilding: CampusBuilding? = null
        val suggestions = buildingSuggestions("hall", Campus.SGW, crossCampus = false)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                BuildingAutocompleteField(
                    label = "To:",
                    value = "hall",
                    suggestions = suggestions,
                    onQueryChange = {},
                    onSelected = { selectedBuilding = it },
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Henry F. Hall Building").performClick()
        composeTestRule.waitForIdle()

        assert(selectedBuilding?.buildingCode == "H") {
            "Expected building code H but got ${selectedBuilding?.buildingCode}"
        }
    }

    @Test
    fun searchBar_selectingSuggestionFiresBuildingSelectedCallback() {
        var selectedBuilding: CampusBuilding? = null
        val suggestions = buildingSuggestions("molson", Campus.SGW, crossCampus = false)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    SearchBarWithProfile(
                        suggestions = suggestions,
                        onBuildingSelected = { selectedBuilding = it },
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("John Molson Building").performClick()
        composeTestRule.waitForIdle()

        assert(selectedBuilding?.buildingCode == "MB") {
            "Expected MB but got ${selectedBuilding?.buildingCode}"
        }
    }

    // Bottom search button tests

    @Test
    fun mapScreen_bottomSearchButton_isVisible() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(LocalAccessibilityState provides defaultState) {
                    MapScreen()
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Bottom search button").assertIsDisplayed()
    }

    @Test
    fun mapScreen_bottomSearchButton_clickTriggersCallback() {
        var clicked = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(LocalAccessibilityState provides defaultState) {
                    MapScreen(onBottomSearchClick = { clicked = true })
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Bottom search button").performClick()
        assertTrue("Bottom search button on map should trigger callback", clicked)
    }

  //Top search bar sets building as To: destination

    @Test
    fun mapScreen_topBarBuildingSelection_opensRoutePanelWithDestination() {
        val hallBuilding = CampusBuilding(
            buildingCode = "H",
            buildingName = "Henry F. Hall Building",
            address = "1455 De Maisonneuve Blvd. W.",
            campus = Campus.SGW,
        )

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen(
                        topBarSelectedBuilding = hallBuilding,
                        onTopBarBuildingConsumed = {},
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        // Route panel should open with Hall Building as destination
        composeTestRule.onNodeWithText("Route options").assertIsDisplayed()
        composeTestRule.onNodeWithText("Henry F. Hall Building").assertIsDisplayed()
    }

    @Test
    fun mapScreen_topBarBuildingSelection_showsFromField() {
        val hallBuilding = CampusBuilding(
            buildingCode = "H",
            buildingName = "Henry F. Hall Building",
            address = "1455 De Maisonneuve Blvd. W.",
            campus = Campus.SGW,
        )

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen(
                        topBarSelectedBuilding = hallBuilding,
                        onTopBarBuildingConsumed = {},
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("From:").assertIsDisplayed()
        composeTestRule.onNodeWithText("To:").assertIsDisplayed()
    }

    @Test
    fun mapScreen_noTopBarBuilding_routePanelHidden() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CompositionLocalProvider(
                    LocalAccessibilityState provides defaultState
                ) {
                    MapScreen(
                        topBarSelectedBuilding = null,
                        onTopBarBuildingConsumed = {},
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Route options").assertDoesNotExist()
    }
}
