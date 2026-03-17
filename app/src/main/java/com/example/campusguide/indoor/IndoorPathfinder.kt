package com.example.campusguide.indoor

import java.util.PriorityQueue

/**
 * Finds the shortest path between two nodes on the same floor using Dijkstra's algorithm.
 *
 * @param requireAccessible When true, edges with [IndoorEdge.isAccessible] == false
 *                          (stairs, escalators) are ignored, ensuring the route is
 *                          usable by mobility-impaired users (US-5.4).
 */
object IndoorPathfinder {

    /**
     * Compute the shortest path from [originId] to [destinationId] within [graph].
     *
     * @return [IndoorPath] containing the ordered nodes and total weight,
     *         or an empty [IndoorPath] if no path exists.
     */
    fun findPath(
        graph: IndoorFloorGraph,
        originId: String,
        destinationId: String,
        requireAccessible: Boolean = false
    ): IndoorPath {
        if (originId == destinationId) {
            val node = graph.nodeMap[originId] ?: return IndoorPath(emptyList(), 0f)
            return IndoorPath(listOf(node), 0f)
        }

        // Build adjacency list (undirected), respecting accessibility filter
        val adjacency = buildAdjacency(graph, requireAccessible)

        // Dijkstra
        val dist = mutableMapOf<String, Float>().withDefault { Float.MAX_VALUE }
        val prev = mutableMapOf<String, String?>()
        dist[originId] = 0f

        // PriorityQueue: (cost, nodeId)
        val queue = PriorityQueue<Pair<Float, String>>(compareBy { it.first })
        queue.add(0f to originId)

        while (queue.isNotEmpty()) {
            val polled = queue.poll() ?: break
            val (cost, current) = polled

            if (cost > (dist[current] ?: Float.MAX_VALUE)) continue
            if (current == destinationId) break

            adjacency[current]?.forEach { (neighbour, weight) ->
                val newDist = cost + weight
                if (newDist < (dist[neighbour] ?: Float.MAX_VALUE)) {
                    dist[neighbour] = newDist
                    prev[neighbour] = current
                    queue.add(newDist to neighbour)
                }
            }
        }

        val totalWeight = dist[destinationId] ?: Float.MAX_VALUE
        if (totalWeight == Float.MAX_VALUE) return IndoorPath(emptyList(), Float.MAX_VALUE)

        // Reconstruct path
        val path = mutableListOf<IndoorNode>()
        var current: String? = destinationId
        while (current != null) {
            val node = graph.nodeMap[current] ?: break
            path.add(0, node)
            current = prev[current]
        }

        return IndoorPath(path, totalWeight)
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun buildAdjacency(
        graph: IndoorFloorGraph,
        requireAccessible: Boolean
    ): Map<String, List<Pair<String, Float>>> {
        val adj = mutableMapOf<String, MutableList<Pair<String, Float>>>()

        for (edge in graph.edges) {
            if (requireAccessible && !edge.isAccessible) continue
            adj.getOrPut(edge.fromId) { mutableListOf() }.add(edge.toId to edge.weight)
            adj.getOrPut(edge.toId)   { mutableListOf() }.add(edge.fromId to edge.weight)
        }
        return adj
    }
}
