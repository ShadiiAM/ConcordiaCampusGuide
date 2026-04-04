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


class MapSearchViewModel : ViewModel() {

    var topBarSuggestions by mutableStateOf<List<Suggestion>>(emptyList())
    var topBarSelectedSuggestion by mutableStateOf<Suggestion?>(null)
    var searchQuery by mutableStateOf("")
    var searchCounter by mutableIntStateOf(0)


    var openIndoorBuildingCode by  mutableStateOf<String?>(null)
    var indoorSetStartTrigger by  mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null)
    var indoorSetDestTrigger by  mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null)
    var pendingIndoorStart by  mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null)
    var pendingIndoorDestination by  mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null)

    var indoorOutdoorRouteRequest by mutableStateOf<IndoorOutdoorRouteRequest?>(null)

    var searchVanish by  mutableStateOf(false)

    var indoorTopCardActive by  mutableStateOf(false)
    var indoorFocusNodeTrigger by  mutableStateOf<com.example.campusguide.indoor.IndoorNode?>(null)

    val suggestionKey: (Suggestion) -> Any = { suggestion ->
        when (suggestion) {
            is CampusBuilding -> "b:${suggestion.buildingCode}"
            is ShuttleStop -> "s:${suggestion.id}"
            is Indoor -> "i:${suggestion.node.id}"
            is OutsidePOI -> "poi:${suggestion.name}"
        }
    }

    fun navigateToMapWithSuggestion(suggestion: Suggestion) {
        topBarSelectedSuggestion = suggestion
        topBarSuggestions = emptyList()
    }

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

    fun onIndoorSetAsStart(indoor: Indoor) {
        pendingIndoorStart = indoor.node
        val existingDest = pendingIndoorDestination
        if (existingDest != null && existingDest.buildingCode != indoor.node.buildingCode) {
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

    fun onIndoorSetAsDestination(indoor: Indoor) {

        pendingIndoorDestination = indoor.node
        val existingStart = pendingIndoorStart
        if (existingStart != null && existingStart.buildingCode != indoor.node.buildingCode) {
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

    fun onPOISearchQueryChange(query: String) {
        topBarSuggestions = fullPOISuggestions(
            query = query,
        )
    }
}