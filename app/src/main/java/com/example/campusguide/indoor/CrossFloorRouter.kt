package com.example.campusguide.indoor

/**
 * Routes between two rooms on *different* floors of the same building (US-5.6).
 *
 * Strategy:
 * 1. Find the best vertical-circulation node (elevator or staircase) on the
 *    origin floor that is reachable from [originId].
 * 2. Find the matching node on the destination floor (same physical shaft/stairwell,
 *    identified by matching label prefix stripped of the floor number).
 * 3. Stitch: path on floor A -> describe floor change -> path on floor B.
 *
 * [requireAccessible] = true forces elevator-only connections (no stairs).
 */
object CrossFloorRouter {

    data class CrossFloorPath(
        /** Ordered path on the origin floor up to (and including) the transfer node. */
        val legOrigin: IndoorPath,
        /** The node on the destination floor where navigation resumes (elevator/stair exit). */
        val transferNodeDest: IndoorNode,
        /** Ordered path on the destination floor from transfer node to destination. */
        val legDest: IndoorPath,
        /** Human-readable instruction for the floor change step. */
        val floorChangeInstruction: String
    )

    /**
     * @param originFloorGraph      Graph of the floor where the trip starts.
     * @param destinationFloorGraph Graph of the floor where the trip ends.
     * @param originId              Node ID of the starting room on [originFloorGraph].
     * @param destinationId         Node ID of the destination room on [destinationFloorGraph].
     * @param requireAccessible     If true, only elevators are used for floor changes.
     * @return [CrossFloorPath] or null if no path exists.
     */
    fun route(
        originFloorGraph: IndoorFloorGraph,
        destinationFloorGraph: IndoorFloorGraph,
        originId: String,
        destinationId: String,
        requireAccessible: Boolean = false
    ): CrossFloorPath? {

        val allowedTypes = if (requireAccessible) {
            setOf(IndoorNodeType.ELEVATOR)
        } else {
            setOf(IndoorNodeType.ELEVATOR, IndoorNodeType.STAIRCASE, IndoorNodeType.ESCALATOR)
        }

        // All vertical-circulation nodes on origin floor
        val originTransferNodes = originFloorGraph.nodes.filter { it.type in allowedTypes }
        if (originTransferNodes.isEmpty()) return null

        // All vertical-circulation nodes on destination floor, same type
        val destTransferNodes = destinationFloorGraph.nodes.filter { it.type in allowedTypes }
        if (destTransferNodes.isEmpty()) return null

        // Find best pair: shortest (path to transfer on origin + path from transfer on dest)
        var bestCrossPath: CrossFloorPath? = null
        var bestTotal = Float.MAX_VALUE

        for (originTransfer in originTransferNodes) {
            // Find matching node on dest floor (same label, just different floor)
            val destTransfer = findMatchingNode(originTransfer, destTransferNodes) ?: continue

            val legA = IndoorPathfinder.findPath(
                originFloorGraph, originId, originTransfer.id, requireAccessible
            )
            if (legA.isEmpty || legA.totalWeight == Float.MAX_VALUE) continue

            val legB = IndoorPathfinder.findPath(
                destinationFloorGraph, destTransfer.id, destinationId, requireAccessible
            )
            if (legB.isEmpty || legB.totalWeight == Float.MAX_VALUE) continue

            val total = legA.totalWeight + legB.totalWeight
            if (total < bestTotal) {
                bestTotal = total
                val verb = when (originTransfer.type) {
                    IndoorNodeType.ELEVATOR   -> "Take the elevator"
                    IndoorNodeType.STAIRCASE  -> "Take the stairs"
                    IndoorNodeType.ESCALATOR  -> "Take the escalator"
                    else -> "Proceed"
                }
                val direction = if (destinationFloorGraph.floor > originFloorGraph.floor) "up" else "down"
                bestCrossPath = CrossFloorPath(
                    legOrigin = legA,
                    transferNodeDest = destTransfer,
                    legDest = legB,
                    floorChangeInstruction = "$verb $direction to floor ${destinationFloorGraph.floor}"
                )
            }
        }

        return bestCrossPath
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Finds a matching vertical-circulation node on the destination floor.
     * Matching is done by stripping the floor segment from the node ID:
     * "H-1-ELEV1" -> "H-ELEV1", then matching "H-2-ELEV1" -> "H-ELEV1".
     */
    private fun findMatchingNode(
        origin: IndoorNode,
        candidates: List<IndoorNode>
    ): IndoorNode? {
        val originKey = canonicalKey(origin)
        return candidates.firstOrNull { canonicalKey(it) == originKey }
    }

    private fun canonicalKey(node: IndoorNode): String {
        // Remove the floor-number segment: "H-1-ELEV1" -> "H-ELEV1"
        val parts = node.id.split("-")
        return if (parts.size >= 3) "${parts[0]}-${parts.drop(2).joinToString("-")}" else node.id
    }
}
