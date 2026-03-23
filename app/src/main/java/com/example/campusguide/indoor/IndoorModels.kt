package com.example.campusguide.indoor

import kotlinx.serialization.Serializable

/**
 * Type of an indoor navigation node.
 */
@Serializable
enum class IndoorNodeType {
    ROOM,       // A named room / office
    HALLWAY,    // A corridor junction
    ELEVATOR,   // Elevator - always accessible
    STAIRCASE,  // Staircase - not accessible for mobility-impaired users
    RAMP,       // Ramp - always accessible
    ENTRY,      // Building entrance / exit
    POI,        // Point of interest (washroom, café, etc.)
    ESCALATOR   // Escalator - treated as non-accessible (same as stairs)
}

/**
 * A single node in the indoor floor graph.
 *
 * @param id Unique identifier (e.g. "H-1-101" for Hall floor 1 room 101).
 * @param label Human-readable label (room number, stairwell name, etc.).
 * @param x Pixel x-coordinate in the floor-plan PNG (at its native resolution).
 * @param y Pixel y-coordinate in the floor-plan PNG (at its native resolution).
 * @param type Node classification - used for accessible routing and POI highlighting.
 * @param floor Floor number this node belongs to.
 * @param buildingCode Building this node belongs to (e.g. "H").
 * @param description Optional longer description shown on tap.
 */
@Serializable
data class IndoorNode(
    val id: String,
    val label: String,
    val x: Float,
    val y: Float,
    val type: IndoorNodeType,
    val floor: Int,
    val buildingCode: String,
    val description: String? = null
)

/**
 * A weighted, undirected edge between two [IndoorNode]s.
 *
 * @param fromId ID of the origin node.
 * @param toId ID of the destination node.
 * @param weight Walking cost in arbitrary units (defaults to pixel distance).
 * @param isAccessible False for stairs / escalators - excluded when
 *                     [requireAccessible] is true in [IndoorPathfinder].
 */
@Serializable
data class IndoorEdge(
    val fromId: String,
    val toId: String,
    val weight: Float,
    val isAccessible: Boolean = true
)

/**
 * JSON-serializable form of a floor graph.
 * [imageName] is the PNG filename (e.g. "hall_floor_1.png") that
 * [IndoorGraphLoader] strips and resolves to an R.drawable ID at runtime.
 */
@Serializable
data class IndoorFloorGraphJson(
    val buildingCode: String,
    val floor: Int,
    /** PNG filename as stored in the JSON, e.g. "hall_floor_8.png". */
    val imageName: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val nodes: List<IndoorNode>,
    val edges: List<IndoorEdge>
)

/**
 * All nodes and edges for one floor of one building, runtime form with a
 * resolved drawable resource ID.
 */
data class IndoorFloorGraph(
    val buildingCode: String,
    val floor: Int,
    /** Resolved R.drawable resource ID of the floor-plan PNG. */
    val floorPlanDrawableRes: Int,
    val imageWidth: Int,
    val imageHeight: Int,
    val nodes: List<IndoorNode>,
    val edges: List<IndoorEdge>
) {
    /** Quick look-up map from node id -> node. */
    val nodeMap: Map<String, IndoorNode> by lazy { nodes.associateBy { it.id } }
}

/**
 * The result of a pathfinding operation, an ordered list of nodes from
 * origin to destination (inclusive).
 */
data class IndoorPath(
    val nodes: List<IndoorNode>,
    val totalWeight: Float
) {
    val isEmpty: Boolean get() = nodes.isEmpty()
}
