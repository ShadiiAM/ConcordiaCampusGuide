package com.example.campusguide.indoor

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IndoorRoomSearchServiceTest {

    @After
    fun tearDown() {
        TestIndoorRegistry.clear()
    }

    @Test
    fun search_returnsEmpty_forBlankQuery() {
        TestIndoorRegistry.seed(buildGraph("H", 1, "H-1-851", IndoorNodeType.ROOM))

        val results = IndoorRoomSearchService.search("   ", IndoorRoomSearchService.Scope.Building, "H")

        assertTrue(results.isEmpty())
    }

    @Test
    fun search_buildingScope_filtersToSpecifiedBuilding() {
        TestIndoorRegistry.seed(
            buildGraph("H", 1, "H-1-851", IndoorNodeType.ROOM),
            buildGraph("MB", 1, "MB-1-101", IndoorNodeType.ROOM)
        )

        val results = IndoorRoomSearchService.search("101", IndoorRoomSearchService.Scope.Building, "H")

        assertTrue(results.none { it.buildingCode == "MB" })
    }

    @Test
    fun search_returnsOnlyRoomNodes_andFormatsPrimaryLabel() {
        val graph = IndoorFloorGraph(
            buildingCode = "H",
            floor = -1,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("H--1-851", "851", 0f, 0f, IndoorNodeType.ROOM, -1, "H"),
                IndoorNode("H--1-H1", "Hall", 0f, 0f, IndoorNodeType.HALLWAY, -1, "H"),
                IndoorNode("H--1-POI1", "Cafe", 0f, 0f, IndoorNodeType.POI, -1, "H")
            ),
            edges = emptyList()
        )
        TestIndoorRegistry.seed(graph)

        val results = IndoorRoomSearchService.search("851", IndoorRoomSearchService.Scope.Building, "H")

        assertEquals(1, results.size)
        assertEquals("H.851", results.first().primaryLabel)
        assertEquals("H S1", results.first().locationLabel)
    }

    @Test
    fun search_globalScope_includesAllBuildings() {
        TestIndoorRegistry.seed(
            buildGraph("H", 1, "H-1-851", IndoorNodeType.ROOM),
            buildGraph("MB", 1, "MB-1-101", IndoorNodeType.ROOM)
        )

        val results = IndoorRoomSearchService.search("1", IndoorRoomSearchService.Scope.Global)

        assertTrue(results.any { it.buildingCode == "H" })
        assertTrue(results.any { it.buildingCode == "MB" })
    }

    @Test
    fun search_buildingScope_withoutBuildingCode_returnsEmpty() {
        TestIndoorRegistry.seed(buildGraph("H", 1, "H-1-851", IndoorNodeType.ROOM))

        val results = IndoorRoomSearchService.search("851", IndoorRoomSearchService.Scope.Building, null)

        assertTrue(results.isEmpty())
    }

    @Test
    fun search_fallsBackToNodeLabel_whenIdHasNoRoomDigits() {
        val graph = IndoorFloorGraph(
            buildingCode = "H",
            floor = 1,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("H-1-ROOM", "LabX", 0f, 0f, IndoorNodeType.ROOM, 1, "H")
            ),
            edges = emptyList()
        )
        TestIndoorRegistry.seed(graph)

        val results = IndoorRoomSearchService.search("lab", IndoorRoomSearchService.Scope.Building, "H")

        assertEquals(1, results.size)
        assertEquals("H.LabX", results.first().primaryLabel)
    }

    @Test
    fun floorLabelHelpers_formatExpectedValues() {
        assertEquals("S2", IndoorRoomSearchService.formatFloorLabel(-2))
        assertEquals("Floor 8", IndoorRoomSearchService.formatFloorLabel(8))
        assertEquals("0", IndoorRoomSearchService.formatFloorLabel(0))

        assertEquals("S1", IndoorRoomSearchService.formatFloorLabelShort(-1))
        assertEquals("floor 3", IndoorRoomSearchService.formatFloorLabelShort(3))
        assertEquals("0", IndoorRoomSearchService.formatFloorLabelShort(0))
    }

    private fun buildGraph(
        building: String,
        floor: Int,
        nodeId: String,
        type: IndoorNodeType
    ): IndoorFloorGraph {
        return IndoorFloorGraph(
            buildingCode = building,
            floor = floor,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode(nodeId, nodeId.substringAfterLast("-"), 0f, 0f, type, floor, building)
            ),
            edges = emptyList()
        )
    }
}
