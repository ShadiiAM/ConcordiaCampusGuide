package com.example.campusguide.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.campusguide.data.ALL_POI
import com.example.campusguide.data.ALL_SUGGESTIONS
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.Indoor
import com.example.campusguide.data.OutsidePOI
import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.data.Suggestion
import com.example.campusguide.data.fullPOISuggestions
import com.example.campusguide.data.fullSuggestions
import com.example.campusguide.indoor.IndoorRoomSearchService
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.directions.IndoorOutdoorRouteRequest


/**
 * ViewModel for the main map search bar and suggestion list.
 *
 * Tracks the current search text, the displayed suggestion list, and
 * any pending indoor navigation triggers that need to be handed off to
 * [IndoorNavigationViewModel].
 */
class MapSearchViewModel : ViewModel() {

    // Suggestions shown in the dropdown while the user types
    var topBarSuggestions by mutableStateOf<List<Suggestion>>(emptyList())

    // The suggestion the user tapped; consumed by MapScreen to pan/open the result
    var topBarSelectedSuggestion by mutableStateOf<Suggestion?>(null)

    var searchQuery by mutableStateOf("")

    // Incremented each time a free-text search is submitted so the map can react
    var searchCounter by mutableIntStateOf(0)

    // Building code whose indoor map should be opened, or null for outdoor view
    var openIndoorBuildingCode by  mutableStateOf<String?>(null)

    // One-shot triggers passed to IndoorNavigationViewModel to set start/dest
    var indoorSetStartTrigger by  mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null)
    var indoorSetDestTrigger by  mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null)

    // Remembered start/dest nodes used to detect cross-building routes
    var pendingIndoorStart by  mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null)
    var pendingIndoorDestination by  mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null)

    // Set when start and destination are in different buildings (cross-building route)
    var indoorOutdoorRouteRequest by mutableStateOf<IndoorOutdoorRouteRequest?>(null)

    // True while the search bar should hide itself after a selection
    var searchVanish by  mutableStateOf(false)

    var indoorTopCardActive by  mutableStateOf(false)

    // Node to highlight and scroll to in the indoor map after a room search
    var indoorFocusNodeTrigger by  mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null)

    /**
     * Stable key used by Compose lazy lists to avoid recomposing unchanged items.
     */
    val suggestionKey: (Suggestion) -> Any = { suggestion ->
        when (suggestion) {
            is CampusBuilding -> "b:${suggestion.buildingCode}"
            is ShuttleStop -> "s:${suggestion.id}"
            is Indoor -> "i:${suggestion.node.id}"
            is OutsidePOI -> "poi:${suggestion.name}"
        }
    }

    /** Selects a suggestion and hides the dropdown. */
    fun navigateToMapWithSuggestion(suggestion: Suggestion) {
        topBarSelectedSuggestion = suggestion
        topBarSuggestions = emptyList()
    }

    /**
     * Called on every keystroke. Merges building/POI suggestions with
     * indoor room results from [IndoorRoomSearchService].
     */
    fun onSearchQueryChange(query: String) {

        val indoor = IndoorRoomSearchService.search(
            query = query,
            scope = IndoorRoomSearchService.Scope.Global,
        ).map {
            Indoor(
                node = it.node,
                buildingCode = it.buildingCode,
                primaryLabel = it.primaryLabel,
                secondaryLabel = it.typeLabel,
                tertiaryLabel = it.locationLabel,
            )
        }

        topBarSuggestions = fullSuggestions(
            query = query,
            activeCampus = Campus.SGW,
        ) + indoor
    }

    /**
     * Called when the user submits the search bar (keyboard action).
     * Tries to match the query against a known suggestion; falls back to
     * incrementing [searchCounter] so the map layer can perform its own lookup.
     */
    fun onSearchSubmit(query: String) {
        val match = ALL_SUGGESTIONS.firstOrNull { it.matches(query) }
        searchQuery = ""
        topBarSuggestions = emptyList()
        if (match != null) {
            navigateToMapWithSuggestion(match)
        } else {
            searchQuery = query
            searchCounter++
        }
    }

    /** Called when the user taps a suggestion in the dropdown. */
    fun onSuggestionSelected(suggestion: Suggestion) {
        topBarSelectedSuggestion = suggestion

        if(suggestion is Indoor) {
            pendingIndoorDestination = suggestion.node
            if (openIndoorBuildingCode == null) {
                openIndoorBuildingCode = suggestion.buildingCode
            }
            indoorSetDestTrigger = suggestion.node
        }

        searchQuery = ""
        topBarSuggestions = emptyList()

        searchVanish = true
    }

    /**
     * Marks an indoor room as the route start.
     * If there is already a destination in a different building, a cross-building
     * [IndoorOutdoorRouteRequest] is created instead of opening the indoor map.
     */
    fun onIndoorSetAsStart(indoor: Indoor) {
        pendingIndoorStart = indoor.node
        val existingDest = pendingIndoorDestination
        if (existingDest != null && existingDest.buildingCode != indoor.node.buildingCode) {
            // Different buildings: hand off to the outdoor routing flow
            indoorOutdoorRouteRequest = IndoorOutdoorRouteRequest(
                startNode = indoor.node,
                destinationNode = existingDest,
            )
            openIndoorBuildingCode = null
        } else {
            if (openIndoorBuildingCode == null) {
                openIndoorBuildingCode = indoor.buildingCode
            }
            indoorSetStartTrigger = indoor.node
        }
        topBarSuggestions = emptyList()
        searchQuery = ""

    }

    /**
     * Marks an indoor room as the route destination.
     * If there is already a start in a different building, a cross-building
     * [IndoorOutdoorRouteRequest] is created instead of opening the indoor map.
     */
    fun onIndoorSetAsDestination(indoor: Indoor) {

        pendingIndoorDestination = indoor.node
        val existingStart = pendingIndoorStart
        if (existingStart != null && existingStart.buildingCode != indoor.node.buildingCode) {
            // Different buildings: hand off to the outdoor routing flow
            indoorOutdoorRouteRequest = IndoorOutdoorRouteRequest(
                startNode = existingStart,
                destinationNode = indoor.node,
            )
            openIndoorBuildingCode = null
        } else {
            if (openIndoorBuildingCode == null) {
                openIndoorBuildingCode = indoor.buildingCode
            }
            indoorSetDestTrigger = indoor.node
        }
        topBarSuggestions = emptyList()
        searchQuery = ""

    }

    /** Same as [onSearchSubmit] but restricted to POI suggestions only. */
    fun onPOISearchSubmit(query: String) {
        val match = ALL_POI.firstOrNull { it.matches(query) }
        searchQuery = ""
        topBarSuggestions = emptyList()
        if (match != null) {
            navigateToMapWithSuggestion(match)
        } else {
            searchQuery = query
            searchCounter++
        }
    }

    /** Filters the POI suggestion list as the user types in the POI search bar. */
    fun onPOISearchQueryChange(query: String) {
        topBarSuggestions = fullPOISuggestions(
            query = query,
        )
    }
}