package com.example.campusguide.indoor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IndoorPathfinderTest {

    private fun graphForPathTests(): IndoorFloorGraph {
        val roomA = IndoorNode("H-1-101", "101", 0f, 0f, IndoorNodeType.ROOM, 1, "H")
        val roomB = IndoorNode("H-1-102", "102", 10f, 0f, IndoorNodeType.ROOM, 1, "H")
        val hall = IndoorNode("H-1-H1", "Hall", 5f, 0f, IndoorNodeType.HALLWAY, 1, "H")
        val stair = IndoorNode("H-1-S1", "Stair", 5f, 5f, IndoorNodeType.STAIRCASE, 1, "H")

        return IndoorFloorGraph(
            buildingCode = "H",
            floor = 1,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(roomA, roomB, hall, stair),
            edges = listOf(
                IndoorEdge("H-1-101", "H-1-H1", 2f, isAccessible = true),
                IndoorEdge("H-1-H1", "H-1-102", 2f, isAccessible = true),
                IndoorEdge("H-1-101", "H-1-S1", 1f, isAccessible = false),
                IndoorEdge("H-1-S1", "H-1-102", 1f, isAccessible = false)
            )
        )
    }

    @Test
    fun findPath_sameOriginAndDestination_returnsSingleNode() {
        val graph = graphForPathTests()

        val path = IndoorPathfinder.findPath(graph, "H-1-101", "H-1-101")

        assertEquals(1, path.nodes.size)
        assertEquals("H-1-101", path.nodes.first().id)
        assertEquals(0f, path.totalWeight)
    }

    @Test
    fun findPath_sameOriginAndDestination_returnsEmptyWhenNodeMissing() {
        val graph = graphForPathTests()

        val path = IndoorPathfinder.findPath(graph, "H-1-404", "H-1-404")

        assertTrue(path.isEmpty)
        assertEquals(0f, path.totalWeight)
    }

    @Test
    fun findPath_prefersLowerWeightPath_whenAccessibilityNotRequired() {
        val graph = graphForPathTests()

        val path = IndoorPathfinder.findPath(graph, "H-1-101", "H-1-102", requireAccessible = false)

        assertEquals(listOf("H-1-101", "H-1-S1", "H-1-102"), path.nodes.map { it.id })
        assertEquals(2f, path.totalWeight)
    }

    @Test
    fun findPath_ignoresInaccessibleEdges_whenAccessibilityRequired() {
        val graph = graphForPathTests()

        val path = IndoorPathfinder.findPath(graph, "H-1-101", "H-1-102", requireAccessible = true)

        assertEquals(listOf("H-1-101", "H-1-H1", "H-1-102"), path.nodes.map { it.id })
        assertEquals(4f, path.totalWeight)
    }

    @Test
    fun findPath_returnsEmpty_whenDestinationUnreachable() {
        val isolatedNode = IndoorNode("H-1-999", "999", 20f, 0f, IndoorNodeType.ROOM, 1, "H")
        val base = graphForPathTests()
        val graph = base.copy(nodes = base.nodes + isolatedNode)

        val path = IndoorPathfinder.findPath(graph, "H-1-101", "H-1-999", requireAccessible = true)

        assertTrue(path.isEmpty)
        assertEquals(Float.MAX_VALUE, path.totalWeight)
    }
}
