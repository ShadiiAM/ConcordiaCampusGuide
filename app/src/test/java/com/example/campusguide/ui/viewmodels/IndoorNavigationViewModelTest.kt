package com.example.campusguide.ui.viewmodels

import com.example.campusguide.indoor.CrossFloorRouter
import com.example.campusguide.indoor.IndoorEdge
import com.example.campusguide.indoor.IndoorFloorGraph
import com.example.campusguide.indoor.IndoorGraphRegistry
import com.example.campusguide.indoor.IndoorNode
import com.example.campusguide.indoor.IndoorNodeType
import com.example.campusguide.indoor.TestIndoorRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IndoorNavigationViewModelTest {

    @After
    fun tearDown() {
        TestIndoorRegistry.clear()
    }

    @Test
    fun openBuilding_selectsFirstAvailableFloor_andClearsSelection() {
        TestIndoorRegistry.seed(graphFloor2(), graphFloor1())
        val vm = IndoorNavigationViewModel()

        vm.selectOrigin(graphFloor1().nodes.first { it.id == "H-1-101" })
        vm.selectDestination(graphFloor1().nodes.first { it.id == "H-1-102" })

        vm.openBuilding("h")

        assertEquals("H", vm.buildingCode)
        assertEquals(1, vm.selectedFloor)
        assertEquals(null, vm.originNode)
        assertEquals(null, vm.destinationNode)
    }

    @Test
    fun focusNode_updatesHighlightAndSelectedFloor() {
        TestIndoorRegistry.seed(graphFloor1())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")
        val node = graphFloor1().nodes.first { it.id == "H-1-102" }

        vm.focusNode(node)

        assertEquals(node, vm.highlightedNode)
        assertEquals(1, vm.selectedFloor)
    }

    @Test
    fun setHighlightedAsOrigin_andDestination_assignNodes() {
        TestIndoorRegistry.seed(graphFloor1())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")
        val node = graphFloor1().nodes.first { it.id == "H-1-101" }

        vm.focusNode(node)
        vm.setHighlightedAsOrigin()
        vm.setHighlightedAsDestination()

        assertEquals(node, vm.originNode)
        assertEquals(node, vm.destinationNode)
    }

    @Test
    fun selectOriginAndDestination_enableRouting_withoutAutoPath() {
        TestIndoorRegistry.seed(graphFloor1())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")

        vm.selectOrigin(graphFloor1().nodes.first { it.id == "H-1-101" })
        vm.selectDestination(graphFloor1().nodes.first { it.id == "H-1-102" })

        assertTrue(vm.canRoute)
        assertEquals(IndoorNavState.Idle, vm.navState)
    }

    @Test
    fun computePath_sameFloor_setsSameFloorState() {
        TestIndoorRegistry.seed(graphFloor1())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")
        vm.selectOrigin(graphFloor1().nodes.first { it.id == "H-1-101" })
        vm.selectDestination(graphFloor1().nodes.first { it.id == "H-1-102" })

        vm.computePath()

        val state = vm.navState
        assertTrue(state is IndoorNavState.SameFloor)
        state as IndoorNavState.SameFloor
        assertTrue(state.path.nodes.isNotEmpty())
    }

    @Test
    fun computePath_crossFloor_setsCrossFloorState() {
        TestIndoorRegistry.seed(graphFloor1(), graphFloor2())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")
        vm.selectOrigin(graphFloor1().nodes.first { it.id == "H-1-101" })
        vm.selectDestination(graphFloor2().nodes.first { it.id == "H-2-201" })

        vm.computePath()

        val state = vm.navState
        assertTrue(state is IndoorNavState.CrossFloor)
        val result: CrossFloorRouter.CrossFloorPath = (state as IndoorNavState.CrossFloor).result
        assertNotNull(result.floorChangeInstruction)
    }

    @Test
    fun computePath_noPath_setsNoPathState() {
        val disconnected = IndoorFloorGraph(
            buildingCode = "H",
            floor = 1,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("H-1-101", "101", 0f, 0f, IndoorNodeType.ROOM, 1, "H"),
                IndoorNode("H-1-999", "999", 0f, 0f, IndoorNodeType.ROOM, 1, "H")
            ),
            edges = emptyList()
        )
        TestIndoorRegistry.seed(disconnected)
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")
        vm.selectOrigin(disconnected.nodes[0])
        vm.selectDestination(disconnected.nodes[1])

        vm.computePath()

        assertEquals(IndoorNavState.NoPath, vm.navState)
    }

    @Test
    fun swapOriginDestination_swapsAndKeepsIdle() {
        TestIndoorRegistry.seed(graphFloor1())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")
        val origin = graphFloor1().nodes.first { it.id == "H-1-101" }
        val destination = graphFloor1().nodes.first { it.id == "H-1-102" }
        vm.selectOrigin(origin)
        vm.selectDestination(destination)

        vm.swapOriginDestination()

        assertEquals(destination, vm.originNode)
        assertEquals(origin, vm.destinationNode)
        assertEquals(IndoorNavState.Idle, vm.navState)
    }

    @Test
    fun selectFloor_updatesSelectedFloor() {
        TestIndoorRegistry.seed(graphFloor1(), graphFloor2())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")

        vm.selectFloor(2)

        assertEquals(2, vm.selectedFloor)
    }

    @Test
    fun clearHighlight_clearsHighlightedNode() {
        TestIndoorRegistry.seed(graphFloor1())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")

        vm.focusNode(graphFloor1().nodes.first { it.id == "H-1-101" })
        vm.clearHighlight()

        assertNull(vm.highlightedNode)
    }

    @Test
    fun setHighlightedActions_doNothing_whenNoHighlight() {
        TestIndoorRegistry.seed(graphFloor1())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")

        vm.setHighlightedAsOrigin()
        vm.setHighlightedAsDestination()

        assertNull(vm.originNode)
        assertNull(vm.destinationNode)
    }

    @Test
    fun clearSelection_resetsNodesAndNavState() {
        TestIndoorRegistry.seed(graphFloor1())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")
        vm.selectOrigin(graphFloor1().nodes.first { it.id == "H-1-101" })
        vm.selectDestination(graphFloor1().nodes.first { it.id == "H-1-102" })
        vm.computePath()

        vm.clearSelection()

        assertNull(vm.originNode)
        assertNull(vm.destinationNode)
        assertEquals(IndoorNavState.Idle, vm.navState)
    }

    @Test
    fun computePath_withoutOrigin_staysIdle() {
        TestIndoorRegistry.seed(graphFloor1())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")
        vm.selectDestination(graphFloor1().nodes.first { it.id == "H-1-102" })

        vm.computePath()

        assertEquals(IndoorNavState.Idle, vm.navState)
    }

    @Test
    fun computePath_withoutDestination_staysIdle() {
        TestIndoorRegistry.seed(graphFloor1())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")
        vm.selectOrigin(graphFloor1().nodes.first { it.id == "H-1-101" })

        vm.computePath()

        assertEquals(IndoorNavState.Idle, vm.navState)
    }

    @Test
    fun computePath_sameFloor_missingGraph_setsNoPath() {
        TestIndoorRegistry.seed(graphFloor1())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("X")
        val origin = IndoorNode("X-1-101", "101", 0f, 0f, IndoorNodeType.ROOM, 1, "X")
        val destination = IndoorNode("X-1-102", "102", 0f, 0f, IndoorNodeType.ROOM, 1, "X")
        vm.selectOrigin(origin)
        vm.selectDestination(destination)

        vm.computePath()

        assertEquals(IndoorNavState.NoPath, vm.navState)
    }

    @Test
    fun computePath_crossFloor_missingDestinationGraph_setsNoPath() {
        TestIndoorRegistry.seed(graphFloor1())
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")
        vm.selectOrigin(graphFloor1().nodes.first { it.id == "H-1-101" })
        vm.selectDestination(IndoorNode("H-2-201", "201", 0f, 0f, IndoorNodeType.ROOM, 2, "H"))

        vm.computePath()

        assertEquals(IndoorNavState.NoPath, vm.navState)
    }

    @Test
    fun computePath_withAccessibilityConstraint_andOnlyStairPath_setsNoAccessiblePath() {
        val constrainedGraph = IndoorFloorGraph(
            buildingCode = "H",
            floor = 1,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("H-1-101", "101", 0f, 0f, IndoorNodeType.ROOM, 1, "H"),
                IndoorNode("H-1-S1", "Stair", 0f, 0f, IndoorNodeType.STAIRCASE, 1, "H"),
                IndoorNode("H-1-102", "102", 0f, 0f, IndoorNodeType.ROOM, 1, "H")
            ),
            edges = listOf(
                IndoorEdge("H-1-101", "H-1-S1", 1f, false),
                IndoorEdge("H-1-S1", "H-1-102", 1f, false)
            )
        )
        TestIndoorRegistry.seed(constrainedGraph)
        val vm = IndoorNavigationViewModel()
        vm.openBuilding("H")
        vm.setRoutingPreferences(avoidStairs = true, avoidEscalators = false)
        vm.selectOrigin(constrainedGraph.nodes.first { it.id == "H-1-101" })
        vm.selectDestination(constrainedGraph.nodes.first { it.id == "H-1-102" })

        vm.computePath()

        val state = vm.navState
        assertTrue(state is IndoorNavState.NoAccessiblePath)
        state as IndoorNavState.NoAccessiblePath
        assertTrue(state.hasNonAccessibleAlternative)
    }

    private fun graphFloor1(): IndoorFloorGraph {
        return IndoorFloorGraph(
            buildingCode = "H",
            floor = 1,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("H-1-101", "101", 0f, 0f, IndoorNodeType.ROOM, 1, "H"),
                IndoorNode("H-1-102", "102", 0f, 0f, IndoorNodeType.ROOM, 1, "H"),
                IndoorNode("H-1-H1", "Hall", 0f, 0f, IndoorNodeType.HALLWAY, 1, "H"),
                IndoorNode("H-1-ELEV1", "Elevator", 0f, 0f, IndoorNodeType.ELEVATOR, 1, "H")
            ),
            edges = listOf(
                IndoorEdge("H-1-101", "H-1-H1", 1f, true),
                IndoorEdge("H-1-H1", "H-1-102", 1f, true),
                IndoorEdge("H-1-H1", "H-1-ELEV1", 1f, true)
            )
        )
    }

    private fun graphFloor2(): IndoorFloorGraph {
        return IndoorFloorGraph(
            buildingCode = "H",
            floor = 2,
            floorPlanDrawableRes = 0,
            imageWidth = 100,
            imageHeight = 100,
            nodes = listOf(
                IndoorNode("H-2-201", "201", 0f, 0f, IndoorNodeType.ROOM, 2, "H"),
                IndoorNode("H-2-H1", "Hall", 0f, 0f, IndoorNodeType.HALLWAY, 2, "H"),
                IndoorNode("H-2-ELEV1", "Elevator", 0f, 0f, IndoorNodeType.ELEVATOR, 2, "H")
            ),
            edges = listOf(
                IndoorEdge("H-2-ELEV1", "H-2-H1", 1f, true),
                IndoorEdge("H-2-H1", "H-2-201", 1f, true)
            )
        )
    }
}
