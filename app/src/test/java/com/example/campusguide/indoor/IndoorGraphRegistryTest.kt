package com.example.campusguide.indoor

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IndoorGraphRegistryTest {

    @After
    fun tearDown() {
        TestIndoorRegistry.clear()
    }

    @Test
    fun getAndFloorsFor_returnExpectedGraphs() {
        val h1 = graph("H", 1)
        val h8 = graph("H", 8)
        val mb1 = graph("MB", 1)
        TestIndoorRegistry.seed(h8, mb1, h1)

        assertNotNull(IndoorGraphRegistry.get("h", 1))
        assertNull(IndoorGraphRegistry.get("H", 2))
        assertEquals(listOf(1, 8), IndoorGraphRegistry.floorsFor("H"))
    }

    @Test
    fun hasIndoorMapAndBuildings_reflectRegistryContent() {
        TestIndoorRegistry.seed(graph("H", 1), graph("H", 2), graph("MB", 1))

        assertTrue(IndoorGraphRegistry.hasIndoorMap("h"))
        assertFalse(IndoorGraphRegistry.hasIndoorMap("EV"))
        assertEquals(listOf("H", "MB"), IndoorGraphRegistry.buildings())
        assertEquals(3, IndoorGraphRegistry.allGraphs().size)
    }

    private fun graph(building: String, floor: Int): IndoorFloorGraph {
        return IndoorFloorGraph(
            buildingCode = building,
            floor = floor,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode(
                    id = "$building-$floor-101",
                    label = "101",
                    x = 0f,
                    y = 0f,
                    type = IndoorNodeType.ROOM,
                    floor = floor,
                    buildingCode = building,
                )
            ),
            edges = emptyList(),
        )
    }
}
