package com.example.campusguide.ui.viewmodels

import com.example.campusguide.data.ALL_SUGGESTIONS
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.Indoor
import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.indoor.IndoorNode
import com.example.campusguide.indoor.IndoorNodeType
import com.google.common.collect.Maps

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test


class MapsSearchViewModelTest {

    lateinit var viewModel: MapSearchViewModel
    val hBuilding = ALL_SUGGESTIONS
        .filterIsInstance<CampusBuilding>()
        .first { it.buildingCode == "H" }

    val sgwShuttleStop = ALL_SUGGESTIONS
        .filterIsInstance<ShuttleStop>()
        .first { it.id == "sgw_shuttle" }

    private val nodeA = IndoorNode(
        id = "H-2-ESCALATOR-UP-12",
        label= "ESCALATOR UP",
        x= 1722.06f,
        y= 1877.28f,
        type= IndoorNodeType.ESCALATOR,
        floor= 2,
        buildingCode= "H"
    )

    private val indoorA = Indoor(
        node = nodeA,
        buildingCode = "H",
        primaryLabel = "H-Escalator1",
        secondaryLabel = "Escalator",
        tertiaryLabel = "EV Building, Floor 2"
    )


    private val nodeB = IndoorNode(
        id= "MB-1-MB3",
        label= "HALLWAY-JUNCTION",
        x= 495.47f,
        y= 142.75f,
        type= IndoorNodeType.HALLWAY,
        floor= 1,
        buildingCode= "MB",
        description= null

    )

    private val indoorB = Indoor(
        node = nodeB,
        buildingCode = "MB",
        primaryLabel = "MB-HALLWAY",
        secondaryLabel = "HALLWAY-JUNCTION",
        tertiaryLabel = "MB Building, Floor 1"
    )

    @Before
    fun setup(){
        viewModel = MapSearchViewModel()
    }

    @Test
    fun suggestionKeyReturnsCorrectPrefix() {

        assertEquals("b:H", viewModel.suggestionKey(hBuilding))
        assertEquals("s:sgw_shuttle", viewModel.suggestionKey(sgwShuttleStop))
        assertEquals("i:H-2-ESCALATOR-UP-12", viewModel.suggestionKey(indoorA))

    }

    @Test
    fun onSearchSubmit_matchFound_navigatesAndClears() {

        val suggestion = ALL_SUGGESTIONS
        val query = "B Annex"

        viewModel.onSearchSubmit(query)

        assertEquals("", viewModel.searchQuery)
        assertTrue(viewModel.topBarSuggestions.isEmpty())
        assertEquals(suggestion.firstOrNull(), viewModel.topBarSelectedSuggestion)
    }

    @Test
    fun onSuggestionSelectedForIndoor_setsIndoorState() {

        viewModel.onSuggestionSelected(indoorA)

        assertEquals(indoorA, viewModel.topBarSelectedSuggestion)
        assertEquals(indoorA.node, viewModel.pendingIndoorDestination)
        assertEquals(indoorA.node, viewModel.indoorSetDestTrigger)
        assertEquals("H", viewModel.openIndoorBuildingCode)
        assertTrue(viewModel.searchVanish)
    }


    @Test
    fun onIndoorSetAsStart_sameBuilding_setsTrigger() {

        viewModel.onIndoorSetAsStart(indoorA)

        assertEquals(nodeA, viewModel.pendingIndoorStart)
        assertEquals(nodeA, viewModel.indoorSetStartTrigger)
        assertEquals("H", viewModel.openIndoorBuildingCode)
    }


    @Test
    fun onIndoorSetAsDestination_differentBuilding_createsRouteRequest() {

        viewModel.pendingIndoorStart = nodeA


        viewModel.onIndoorSetAsDestination(indoorB)

        assertNotNull(viewModel.indoorOutdoorRouteRequest)
        assertNull(viewModel.openIndoorBuildingCode)
    }

}