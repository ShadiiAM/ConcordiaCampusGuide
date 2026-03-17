package com.example.campusguide.indoor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrossFloorRouterTest {

    private fun floor1(): IndoorFloorGraph {
        return IndoorFloorGraph(
            buildingCode = "H",
            floor = 1,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("H-1-101", "101", 0f, 0f, IndoorNodeType.ROOM, 1, "H"),
                IndoorNode("H-1-H1", "H1", 0f, 0f, IndoorNodeType.HALLWAY, 1, "H"),
                IndoorNode("H-1-ELEV1", "Elev 1", 0f, 0f, IndoorNodeType.ELEVATOR, 1, "H"),
                IndoorNode("H-1-STAIR1", "Stair 1", 0f, 0f, IndoorNodeType.STAIRCASE, 1, "H")
            ),
            edges = listOf(
                IndoorEdge("H-1-101", "H-1-H1", 1f, true),
                IndoorEdge("H-1-H1", "H-1-ELEV1", 3f, true),
                IndoorEdge("H-1-H1", "H-1-STAIR1", 1f, false)
            )
        )
    }

    private fun floor2(): IndoorFloorGraph {
        return IndoorFloorGraph(
            buildingCode = "H",
            floor = 2,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("H-2-201", "201", 0f, 0f, IndoorNodeType.ROOM, 2, "H"),
                IndoorNode("H-2-H1", "H1", 0f, 0f, IndoorNodeType.HALLWAY, 2, "H"),
                IndoorNode("H-2-ELEV1", "Elev 1", 0f, 0f, IndoorNodeType.ELEVATOR, 2, "H"),
                IndoorNode("H-2-STAIR1", "Stair 1", 0f, 0f, IndoorNodeType.STAIRCASE, 2, "H")
            ),
            edges = listOf(
                IndoorEdge("H-2-ELEV1", "H-2-H1", 3f, true),
                IndoorEdge("H-2-STAIR1", "H-2-H1", 1f, false),
                IndoorEdge("H-2-H1", "H-2-201", 1f, true)
            )
        )
    }

    @Test
    fun route_returnsCrossFloorPath_forAccessibleRequest_usingElevator() {
        val result = CrossFloorRouter.route(
            originFloorGraph = floor1(),
            destinationFloorGraph = floor2(),
            originId = "H-1-101",
            destinationId = "H-2-201",
            requireAccessible = true
        )

        assertNotNull(result)
        result!!
        assertEquals("H-2-ELEV1", result.transferNodeDest.id)
        assertTrue(result.floorChangeInstruction.contains("elevator", ignoreCase = true))
        assertTrue(result.floorChangeInstruction.contains("up", ignoreCase = true))
    }

    @Test
    fun route_prefersStairs_whenNotAccessibleAndCheaper() {
        val result = CrossFloorRouter.route(
            originFloorGraph = floor1(),
            destinationFloorGraph = floor2(),
            originId = "H-1-101",
            destinationId = "H-2-201",
            requireAccessible = false
        )

        assertNotNull(result)
        result!!
        assertEquals("H-2-STAIR1", result.transferNodeDest.id)
        assertTrue(result.floorChangeInstruction.contains("stairs", ignoreCase = true))
    }

    @Test
    fun route_returnsNull_whenNoMatchingTransferNodeOnDestinationFloor() {
        val modifiedFloor2 = floor2().copy(
            nodes = floor2().nodes.filterNot { it.id == "H-2-ELEV1" }
        )

        val result = CrossFloorRouter.route(
            originFloorGraph = floor1(),
            destinationFloorGraph = modifiedFloor2,
            originId = "H-1-101",
            destinationId = "H-2-201",
            requireAccessible = true
        )

        assertNull(result)
    }

    @Test
    fun route_returnsNull_whenOriginHasNoTransferNodes() {
        val originWithoutTransfers = floor1().copy(
            nodes = floor1().nodes.filterNot {
                it.type == IndoorNodeType.ELEVATOR || it.type == IndoorNodeType.STAIRCASE || it.type == IndoorNodeType.ESCALATOR
            }
        )

        val result = CrossFloorRouter.route(
            originFloorGraph = originWithoutTransfers,
            destinationFloorGraph = floor2(),
            originId = "H-1-101",
            destinationId = "H-2-201",
            requireAccessible = false
        )

        assertNull(result)
    }

    @Test
    fun route_returnsNull_whenDestinationHasNoTransferNodes() {
        val destinationWithoutTransfers = floor2().copy(
            nodes = floor2().nodes.filterNot {
                it.type == IndoorNodeType.ELEVATOR || it.type == IndoorNodeType.STAIRCASE || it.type == IndoorNodeType.ESCALATOR
            }
        )

        val result = CrossFloorRouter.route(
            originFloorGraph = floor1(),
            destinationFloorGraph = destinationWithoutTransfers,
            originId = "H-1-101",
            destinationId = "H-2-201",
            requireAccessible = false
        )

        assertNull(result)
    }

    @Test
    fun route_returnsNull_whenAccessibleModeHasNoElevatorTransfer() {
        val stairsOnlyOrigin = floor1().copy(
            nodes = floor1().nodes.filterNot { it.type == IndoorNodeType.ELEVATOR }
        )

        val result = CrossFloorRouter.route(
            originFloorGraph = stairsOnlyOrigin,
            destinationFloorGraph = floor2(),
            originId = "H-1-101",
            destinationId = "H-2-201",
            requireAccessible = true
        )

        assertNull(result)
    }

    @Test
    fun route_returnsNull_whenOriginLegCannotReachTransfer() {
        val disconnectedOrigin = floor1().copy(
            edges = listOf(
                IndoorEdge("H-1-H1", "H-1-ELEV1", 3f, true),
                IndoorEdge("H-1-H1", "H-1-STAIR1", 1f, false)
            )
        )

        val result = CrossFloorRouter.route(
            originFloorGraph = disconnectedOrigin,
            destinationFloorGraph = floor2(),
            originId = "H-1-101",
            destinationId = "H-2-201",
            requireAccessible = false
        )

        assertNull(result)
    }

    @Test
    fun route_returnsNull_whenDestinationLegCannotReachDestination() {
        val disconnectedDestination = floor2().copy(
            edges = listOf(
                IndoorEdge("H-2-ELEV1", "H-2-H1", 3f, true),
                IndoorEdge("H-2-STAIR1", "H-2-H1", 1f, false)
            )
        )

        val result = CrossFloorRouter.route(
            originFloorGraph = floor1(),
            destinationFloorGraph = disconnectedDestination,
            originId = "H-1-101",
            destinationId = "H-2-201",
            requireAccessible = false
        )

        assertNull(result)
    }

    @Test
    fun route_usesEscalatorVerb_andDownDirection_whenGoingToLowerFloor() {
        val upper = IndoorFloorGraph(
            buildingCode = "H",
            floor = 3,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("H-3-301", "301", 0f, 0f, IndoorNodeType.ROOM, 3, "H"),
                IndoorNode("H-3-H1", "H1", 0f, 0f, IndoorNodeType.HALLWAY, 3, "H"),
                IndoorNode("H-3-ESC1", "Esc 1", 0f, 0f, IndoorNodeType.ESCALATOR, 3, "H")
            ),
            edges = listOf(
                IndoorEdge("H-3-301", "H-3-H1", 1f, true),
                IndoorEdge("H-3-H1", "H-3-ESC1", 1f, true)
            )
        )
        val lower = IndoorFloorGraph(
            buildingCode = "H",
            floor = 2,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("H-2-201", "201", 0f, 0f, IndoorNodeType.ROOM, 2, "H"),
                IndoorNode("H-2-H1", "H1", 0f, 0f, IndoorNodeType.HALLWAY, 2, "H"),
                IndoorNode("H-2-ESC1", "Esc 1", 0f, 0f, IndoorNodeType.ESCALATOR, 2, "H")
            ),
            edges = listOf(
                IndoorEdge("H-2-ESC1", "H-2-H1", 1f, true),
                IndoorEdge("H-2-H1", "H-2-201", 1f, true)
            )
        )

        val result = CrossFloorRouter.route(
            originFloorGraph = upper,
            destinationFloorGraph = lower,
            originId = "H-3-301",
            destinationId = "H-2-201",
            requireAccessible = false
        )

        assertNotNull(result)
        result!!
        assertTrue(result.floorChangeInstruction.contains("escalator", ignoreCase = true))
        assertTrue(result.floorChangeInstruction.contains("down", ignoreCase = true))
    }

    @Test
    fun route_selectsBestTransfer_whenMultipleCandidatesExist() {
        val origin = IndoorFloorGraph(
            buildingCode = "H",
            floor = 1,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("H-1-101", "101", 0f, 0f, IndoorNodeType.ROOM, 1, "H"),
                IndoorNode("H-1-H1", "H1", 0f, 0f, IndoorNodeType.HALLWAY, 1, "H"),
                IndoorNode("H-1-ELEV1", "Elev 1", 0f, 0f, IndoorNodeType.ELEVATOR, 1, "H"),
                IndoorNode("H-1-ELEV2", "Elev 2", 0f, 0f, IndoorNodeType.ELEVATOR, 1, "H")
            ),
            edges = listOf(
                IndoorEdge("H-1-101", "H-1-H1", 1f, true),
                IndoorEdge("H-1-H1", "H-1-ELEV1", 10f, true),
                IndoorEdge("H-1-H1", "H-1-ELEV2", 1f, true)
            )
        )
        val destination = IndoorFloorGraph(
            buildingCode = "H",
            floor = 2,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("H-2-201", "201", 0f, 0f, IndoorNodeType.ROOM, 2, "H"),
                IndoorNode("H-2-H1", "H1", 0f, 0f, IndoorNodeType.HALLWAY, 2, "H"),
                IndoorNode("H-2-ELEV1", "Elev 1", 0f, 0f, IndoorNodeType.ELEVATOR, 2, "H"),
                IndoorNode("H-2-ELEV2", "Elev 2", 0f, 0f, IndoorNodeType.ELEVATOR, 2, "H")
            ),
            edges = listOf(
                IndoorEdge("H-2-ELEV1", "H-2-H1", 10f, true),
                IndoorEdge("H-2-ELEV2", "H-2-H1", 1f, true),
                IndoorEdge("H-2-H1", "H-2-201", 1f, true)
            )
        )

        val result = CrossFloorRouter.route(
            originFloorGraph = origin,
            destinationFloorGraph = destination,
            originId = "H-1-101",
            destinationId = "H-2-201",
            requireAccessible = true
        )

        assertNotNull(result)
        assertEquals("H-2-ELEV2", result!!.transferNodeDest.id)
    }

    @Test
    fun route_matchesTransferNodes_whenIdsDoNotContainFloorSegments() {
        val origin = IndoorFloorGraph(
            buildingCode = "H",
            floor = 1,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("R101", "101", 0f, 0f, IndoorNodeType.ROOM, 1, "H"),
                IndoorNode("H1", "Hall", 0f, 0f, IndoorNodeType.HALLWAY, 1, "H"),
                IndoorNode("E1", "Elev 1", 0f, 0f, IndoorNodeType.ELEVATOR, 1, "H")
            ),
            edges = listOf(
                IndoorEdge("R101", "H1", 1f, true),
                IndoorEdge("H1", "E1", 1f, true)
            )
        )
        val destination = IndoorFloorGraph(
            buildingCode = "H",
            floor = 2,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("R201", "201", 0f, 0f, IndoorNodeType.ROOM, 2, "H"),
                IndoorNode("H2", "Hall", 0f, 0f, IndoorNodeType.HALLWAY, 2, "H"),
                IndoorNode("E1", "Elev 1", 0f, 0f, IndoorNodeType.ELEVATOR, 2, "H")
            ),
            edges = listOf(
                IndoorEdge("E1", "H2", 1f, true),
                IndoorEdge("H2", "R201", 1f, true)
            )
        )

        val result = CrossFloorRouter.route(
            originFloorGraph = origin,
            destinationFloorGraph = destination,
            originId = "R101",
            destinationId = "R201",
            requireAccessible = true
        )

        assertNotNull(result)
        assertEquals("E1", result!!.transferNodeDest.id)
    }
}
