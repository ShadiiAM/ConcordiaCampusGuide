package com.example.campusguide.indoor

/**
 * Routes between two rooms on *different* floors of the same building (US-5.6).
 *
 * Strategy:
 * 1. Find the best vertical-circulation node (elevator or staircase) on the
 *    origin floor that is reachable from [originId].
 * 2. Find the matching node on the destination floor (same physical shaft/stairwell,
 *    identified by matching label prefix stripped of the floor number).
 * 3. Stitch: path on floor A → describe floor change → path on floor B.
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
        requireAccessible: Boolean = false,
        avoidStairs: Boolean = requireAccessible,
        avoidEscalators: Boolean = requireAccessible,
        preferEscalators: Boolean = true,
    ): CrossFloorPath? {
        val accessibleMode = requireAccessible || avoidStairs || avoidEscalators
        val transferPriority: List<IndoorNodeType> = if (accessibleMode) {
            listOf(IndoorNodeType.ELEVATOR)
        } else {
            if (preferEscalators) {
                listOf(IndoorNodeType.ESCALATOR, IndoorNodeType.STAIRCASE, IndoorNodeType.ELEVATOR)
            } else {
                listOf(IndoorNodeType.STAIRCASE, IndoorNodeType.ESCALATOR, IndoorNodeType.ELEVATOR)
            }
        }

        for (transferType in transferPriority) {
            val crossPath = routeForTransferType(
                transferType = transferType,
                originFloorGraph = originFloorGraph,
                destinationFloorGraph = destinationFloorGraph,
                originId = originId,
                destinationId = destinationId,
                requireAccessible = requireAccessible,
                avoidStairs = avoidStairs,
                avoidEscalators = avoidEscalators,
            )
            if (crossPath != null) {
                return crossPath
            }
        }

        return null
    }

    private data class Anchor(
        val key: String,
        val node: IndoorNode,
        val floor: Int,
        val isTransfer: Boolean,
    )

    private data class EdgeInfo(
        val from: String,
        val to: String,
        val weight: Float,
        val kind: Kind,
        val path: IndoorPath? = null,
    ) {
        enum class Kind { HORIZONTAL, VERTICAL }
    }

    private fun routeForTransferType(
        transferType: IndoorNodeType,
        originFloorGraph: IndoorFloorGraph,
        destinationFloorGraph: IndoorFloorGraph,
        originId: String,
        destinationId: String,
        requireAccessible: Boolean,
        avoidStairs: Boolean,
        avoidEscalators: Boolean,
    ): CrossFloorPath? {
        val buildingCode = originFloorGraph.buildingCode
        val registryGraphs = IndoorGraphRegistry.floorsFor(buildingCode)
            .mapNotNull { floor -> IndoorGraphRegistry.get(buildingCode, floor) }
            .associateBy { it.floor }
        val graphs = registryGraphs.toMutableMap().apply {
            put(originFloorGraph.floor, originFloorGraph)
            put(destinationFloorGraph.floor, destinationFloorGraph)
        }

        val originNode = originFloorGraph.nodeMap[originId] ?: return null
        val destinationNode = destinationFloorGraph.nodeMap[destinationId] ?: return null

        val anchors = mutableListOf<Anchor>()
        val anchorByKey = mutableMapOf<String, Anchor>()

        fun addAnchor(node: IndoorNode, isTransfer: Boolean): Anchor {
            val key = "${node.floor}:${node.id}"
            return anchorByKey.getOrPut(key) {
                val anchor = Anchor(key = key, node = node, floor = node.floor, isTransfer = isTransfer)
                anchors.add(anchor)
                anchor
            }
        }

        val startAnchor = addAnchor(originNode, isTransfer = false)
        val endAnchor = addAnchor(destinationNode, isTransfer = false)

        val transferAnchorsByFloor = mutableMapOf<Int, MutableList<Anchor>>()
        for ((floor, graph) in graphs) {
            graph.nodes
                .filter { it.type == transferType }
                .forEach { transferNode ->
                    val anchor = addAnchor(transferNode, isTransfer = true)
                    transferAnchorsByFloor.getOrPut(floor) { mutableListOf() }.add(anchor)
                }
        }

        if (transferAnchorsByFloor.isEmpty()) return null

        val adjacency = mutableMapOf<String, MutableList<EdgeInfo>>()
        fun addDirectedEdge(edge: EdgeInfo) {
            adjacency.getOrPut(edge.from) { mutableListOf() }.add(edge)
        }
        fun addUndirectedHorizontal(a: Anchor, b: Anchor, path: IndoorPath) {
            addDirectedEdge(
                EdgeInfo(
                    from = a.key,
                    to = b.key,
                    weight = path.totalWeight,
                    kind = EdgeInfo.Kind.HORIZONTAL,
                    path = path,
                )
            )
            addDirectedEdge(
                EdgeInfo(
                    from = b.key,
                    to = a.key,
                    weight = path.totalWeight,
                    kind = EdgeInfo.Kind.HORIZONTAL,
                    path = IndoorPath(path.nodes.reversed(), path.totalWeight),
                )
            )
        }

        for ((floor, floorAnchors) in transferAnchorsByFloor) {
            val graph = graphs[floor] ?: continue
            val anchorsOnFloor = buildList {
                addAll(floorAnchors)
                if (startAnchor.floor == floor) add(startAnchor)
                if (endAnchor.floor == floor) add(endAnchor)
            }

            for (i in anchorsOnFloor.indices) {
                for (j in i + 1 until anchorsOnFloor.size) {
                    val a = anchorsOnFloor[i]
                    val b = anchorsOnFloor[j]
                    if (a.key == b.key) continue
                    val path = IndoorPathfinder.findPath(
                        graph = graph,
                        originId = a.node.id,
                        destinationId = b.node.id,
                        requireAccessible = requireAccessible,
                        avoidStairs = avoidStairs,
                        avoidEscalators = avoidEscalators,
                    )
                    if (path.isEmpty || path.totalWeight == Float.MAX_VALUE) continue
                    addUndirectedHorizontal(a, b, path)
                }
            }
        }

        val transferAnchors = anchors.filter { it.isTransfer }
        for (i in transferAnchors.indices) {
            for (j in i + 1 until transferAnchors.size) {
                val a = transferAnchors[i]
                val b = transferAnchors[j]
                if (a.floor == b.floor) continue
                if (canonicalKey(a.node) != canonicalKey(b.node)) continue

                val verticalCost = kotlin.math.abs(a.floor - b.floor).toFloat()
                addDirectedEdge(
                    EdgeInfo(
                        from = a.key,
                        to = b.key,
                        weight = verticalCost,
                        kind = EdgeInfo.Kind.VERTICAL,
                    )
                )
                addDirectedEdge(
                    EdgeInfo(
                        from = b.key,
                        to = a.key,
                        weight = verticalCost,
                        kind = EdgeInfo.Kind.VERTICAL,
                    )
                )
            }
        }

        val dist = mutableMapOf<String, Float>().withDefault { Float.MAX_VALUE }
        val prevNode = mutableMapOf<String, String?>()
        val prevEdge = mutableMapOf<String, EdgeInfo?>()
        dist[startAnchor.key] = 0f

        val queue = java.util.PriorityQueue<Pair<Float, String>>(compareBy { it.first })
        queue.add(0f to startAnchor.key)

        while (queue.isNotEmpty()) {
            val (cost, current) = queue.poll() ?: break
            if (cost > (dist[current] ?: Float.MAX_VALUE)) continue
            if (current == endAnchor.key) break

            adjacency[current].orEmpty().forEach { edge ->
                val newCost = cost + edge.weight
                if (newCost < (dist[edge.to] ?: Float.MAX_VALUE)) {
                    dist[edge.to] = newCost
                    prevNode[edge.to] = current
                    prevEdge[edge.to] = edge
                    queue.add(newCost to edge.to)
                }
            }
        }

        val total = dist[endAnchor.key] ?: Float.MAX_VALUE
        if (total == Float.MAX_VALUE) return null

        val orderedEdges = mutableListOf<EdgeInfo>()
        var cursor: String? = endAnchor.key
        while (cursor != null && cursor != startAnchor.key) {
            val edge = prevEdge[cursor] ?: return null
            orderedEdges.add(0, edge)
            cursor = prevNode[cursor]
        }

        val nodes = mutableListOf<IndoorNode>()
        var firstAppend = true
        orderedEdges.forEach { edge ->
            when (edge.kind) {
                EdgeInfo.Kind.HORIZONTAL -> {
                    val pathNodes = edge.path?.nodes.orEmpty()
                    if (pathNodes.isEmpty()) return@forEach
                    if (firstAppend) {
                        nodes.addAll(pathNodes)
                        firstAppend = false
                    } else {
                        nodes.addAll(pathNodes.drop(1))
                    }
                }
                EdgeInfo.Kind.VERTICAL -> {
                    val toNode = anchorByKey[edge.to]?.node ?: return@forEach
                    if (nodes.lastOrNull()?.id != toNode.id || nodes.lastOrNull()?.floor != toNode.floor) {
                        nodes.add(toNode)
                    }
                }
            }
        }

        if (nodes.isEmpty()) return null
        val finalTransferNode = orderedEdges.lastOrNull { it.kind == EdgeInfo.Kind.VERTICAL }
            ?.let { anchorByKey[it.to]?.node }
            ?: nodes.last()

        val direction = if (destinationFloorGraph.floor > originFloorGraph.floor) "up" else "down"
        val floorChangeInstruction = when (transferType) {
            IndoorNodeType.ESCALATOR -> "Take escalators $direction to floor ${destinationFloorGraph.floor}"
            IndoorNodeType.STAIRCASE -> "Take the stairs $direction to floor ${destinationFloorGraph.floor}"
            IndoorNodeType.ELEVATOR -> "Take the elevator $direction to floor ${destinationFloorGraph.floor}"
            else -> "Proceed $direction to floor ${destinationFloorGraph.floor}"
        }

        val stitched = IndoorPath(nodes = nodes, totalWeight = total)
        return CrossFloorPath(
            legOrigin = stitched,
            transferNodeDest = finalTransferNode,
            legDest = IndoorPath(emptyList(), 0f),
            floorChangeInstruction = floorChangeInstruction,
        )
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Finds a matching vertical-circulation node on the destination floor.
     * Matching is done by stripping the floor segment from the node ID:
     * "H-1-ELEV1" → "H-ELEV1", then matching "H-2-ELEV1" → "H-ELEV1".
     */
    private fun canonicalKey(node: IndoorNode): String {
        // Remove the floor-number segment: "H-1-ELEV1" → "H-ELEV1"
        val parts = node.id.split("-"). filter {it.isNotEmpty()}
        return if (parts.size >= 3) "${parts[0]}-${parts.drop(2).joinToString("-")}" else node.id
    }
}
