package com.example.campusguide.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.campusguide.indoor.CrossFloorRouter
import com.example.campusguide.indoor.IndoorFloorGraph
import com.example.campusguide.indoor.IndoorGraphRegistry
import com.example.campusguide.indoor.IndoorNode
import com.example.campusguide.indoor.IndoorPath
import com.example.campusguide.indoor.IndoorPathfinder

/**
 * Sealed state for the indoor navigation result shown in [IndoorMapScreen].
 */
sealed class IndoorNavState {
    object Idle : IndoorNavState()
    object NoPath : IndoorNavState()
    data class NoAccessiblePath(val hasNonAccessibleAlternative: Boolean) : IndoorNavState()

    /** A same-floor path was found. */
    data class SameFloor(val path: IndoorPath) : IndoorNavState()

    /** A cross-floor path was found. */
    data class CrossFloor(val result: CrossFloorRouter.CrossFloorPath) : IndoorNavState()
}

/**
 * ViewModel for indoor navigation (US-5.1 – US-5.6).
 *
 * Holds the currently viewed building + floor, selected origin / destination nodes,
 * the accessibility flag, and the computed [IndoorNavState].
 */
class IndoorNavigationViewModel : ViewModel() {

    // ── Building / floor ────────────────────────────────────────────────────
    var buildingCode by mutableStateOf("H")
        private set

    var selectedFloor by mutableIntStateOf(1)
        private set

    val currentGraph: IndoorFloorGraph?
        get() = IndoorGraphRegistry.get(buildingCode, selectedFloor)

    val availableFloors: List<Int>
        get() = IndoorGraphRegistry.floorsFor(buildingCode)

    // ── Room selection (US-5.2) ─────────────────────────────────────────────
    var originNode by mutableStateOf<IndoorNode?>(null)
        private set

    var destinationNode by mutableStateOf<IndoorNode?>(null)
        private set

    /** Floor the origin room is on. May differ from [selectedFloor] for cross-floor. */
    var originFloor by mutableIntStateOf(1)
        private set

    /** Floor the destination room is on. May differ from [selectedFloor]. */
    var destinationFloor by mutableIntStateOf(1)
        private set

    // ── Accessibility (US-5.4) ──────────────────────────────────────────────
    var requireAccessible by mutableStateOf(false)
    var avoidStairs by mutableStateOf(false)
        private set
    var avoidEscalators by mutableStateOf(false)
        private set

    // ── Nav state ───────────────────────────────────────────────────────────
    var navState by mutableStateOf<IndoorNavState>(IndoorNavState.Idle)
        private set

    // ── Search highlight (US: indoor room search) ─────────────────────────────
    var highlightedNode by mutableStateOf<IndoorNode?>(null)
        private set

    val canRoute: Boolean
        get() = originNode != null && destinationNode != null

    // ── Public API ──────────────────────────────────────────────────────────

    fun openBuilding(code: String) {
        buildingCode = code.uppercase()
        selectedFloor = IndoorGraphRegistry.floorsFor(buildingCode).firstOrNull() ?: 1
        clearSelection()
    }

    fun selectFloor(floor: Int) {
        selectedFloor = floor
    }

    /** Highlight a node (from search) and move the current floor to that node's floor. */
    fun focusNode(node: IndoorNode) {
        highlightedNode = node
        selectedFloor = node.floor
    }

    fun clearHighlight() {
        highlightedNode = null
    }

    fun setHighlightedAsOrigin() {
        highlightedNode?.let { selectOrigin(it) }
    }

    fun setHighlightedAsDestination() {
        highlightedNode?.let { selectDestination(it) }
    }

    fun setRoutingPreferences(
        avoidStairs: Boolean,
        avoidEscalators: Boolean,
    ) {
        this.avoidStairs = avoidStairs
        this.avoidEscalators = avoidEscalators
        this.requireAccessible = avoidStairs || avoidEscalators
    }

    fun selectOrigin(node: IndoorNode) {
        originNode = node
        originFloor = node.floor
        navState = IndoorNavState.Idle
    }

    fun selectDestination(node: IndoorNode) {
        destinationNode = node
        destinationFloor = node.floor
        navState = IndoorNavState.Idle
    }

    fun clearSelection() {
        originNode = null
        destinationNode = null
        navState = IndoorNavState.Idle
        // Keep highlight; callers can clear it explicitly.
    }

    fun swapOriginDestination() {
        val tmpNode = originNode
        val tmpFloor = originFloor
        originNode = destinationNode
        originFloor = destinationFloor
        destinationNode = tmpNode
        destinationFloor = tmpFloor
        navState = IndoorNavState.Idle
    }

    /**
     * Compute the path between the selected origin and destination (US-5.3, US-5.4, US-5.6).
     * Automatically handles same-floor and cross-floor cases.
     */
    fun computePath() {
        val origin = originNode ?: run { navState = IndoorNavState.Idle; return }
        val dest   = destinationNode ?: run { navState = IndoorNavState.Idle; return }

        if (origin.floor == dest.floor) {
            // Same-floor path (US-5.3 / US-5.4)
            val graph = IndoorGraphRegistry.get(buildingCode, origin.floor)
                ?: run { navState = IndoorNavState.NoPath; return }

            val path = IndoorPathfinder.findPath(
                graph = graph,
                originId = origin.id,
                destinationId = dest.id,
                requireAccessible = requireAccessible,
                avoidStairs = avoidStairs,
                avoidEscalators = avoidEscalators,
            )
            if (!path.isEmpty) {
                navState = IndoorNavState.SameFloor(path)
                return
            }

            if (requireAccessible) {
                val fallback = IndoorPathfinder.findPath(
                    graph = graph,
                    originId = origin.id,
                    destinationId = dest.id,
                    requireAccessible = false,
                    avoidStairs = false,
                    avoidEscalators = false,
                )
                navState = IndoorNavState.NoAccessiblePath(hasNonAccessibleAlternative = !fallback.isEmpty)
            } else {
                navState = IndoorNavState.NoPath
            }
        } else {
            // Cross-floor path (US-5.6)
            val originGraph = IndoorGraphRegistry.get(buildingCode, origin.floor)
                ?: run { navState = IndoorNavState.NoPath; return }
            val destGraph = IndoorGraphRegistry.get(buildingCode, dest.floor)
                ?: run { navState = IndoorNavState.NoPath; return }

            val result = CrossFloorRouter.route(
                originFloorGraph      = originGraph,
                destinationFloorGraph = destGraph,
                originId              = origin.id,
                destinationId         = dest.id,
                requireAccessible     = requireAccessible,
                avoidStairs           = avoidStairs,
                avoidEscalators       = avoidEscalators,
                preferEscalators      = !requireAccessible && !avoidEscalators,
            )
            if (result != null) {
                navState = IndoorNavState.CrossFloor(result)
                return
            }

            if (requireAccessible) {
                val fallback = CrossFloorRouter.route(
                    originFloorGraph = originGraph,
                    destinationFloorGraph = destGraph,
                    originId = origin.id,
                    destinationId = dest.id,
                    requireAccessible = false,
                    avoidStairs = false,
                    avoidEscalators = false,
                    preferEscalators = true,
                )
                navState = IndoorNavState.NoAccessiblePath(hasNonAccessibleAlternative = fallback != null)
            } else {
                navState = IndoorNavState.NoPath
            }
        }
    }
}
