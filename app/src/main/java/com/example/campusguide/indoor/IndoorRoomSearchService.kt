package com.example.campusguide.indoor

/** Lightweight indoor room/POI search across loaded [IndoorFloorGraph]s. */
object IndoorRoomSearchService {

    enum class Scope {
        /** Search only within one building code. */
        Building,
        /** Search across all buildings. */
        Global
    }

    data class Result(
        val node: IndoorNode,
        val buildingCode: String,
        val floor: Int,
        /** Label to show in UI (e.g., "H.803"). */
        val primaryLabel: String,
        /** Room type (e.g., "Classroom", "POI", "Stairs"). */
        val typeLabel: String,
        /** Location line (e.g., "H floor 8" or "H S1"). */
        val locationLabel: String,
        /** Basic match score for sorting; higher is better. */
        val score: Int
    )

    /**
     * Search for rooms/indoor points.
     * - Matches against node label and node id.
     * - Returns ROOM nodes only (hallways and other node types excluded).
     */
    fun search(
        query: String,
        scope: Scope,
        buildingCode: String? = null,
        limit: Int = 20
    ): List<Result> {
        val qRaw = query.trim()
        if (qRaw.isEmpty()) return emptyList()

        val q = normalize(qRaw)
        val candidates: Sequence<IndoorNode> = when (scope) {
            Scope.Building -> {
                val code = buildingCode?.uppercase() ?: return emptyList()
                IndoorGraphRegistry.floorsFor(code)
                    .asSequence()
                    .mapNotNull { floor -> IndoorGraphRegistry.get(code, floor) }
                    .flatMap { it.nodes.asSequence() }
            }
            Scope.Global -> {
                IndoorGraphRegistry.allGraphs()
                    .asSequence()
                    .flatMap { it.nodes.asSequence() }
            }
        }

        return candidates
            .filter { node -> node.type == IndoorNodeType.ROOM }
            .mapNotNull { node ->
                val nLabel = normalize(node.label)
                val nId = normalize(node.id)

                val score = scoreMatch(q, nLabel, nId)
                if (score <= 0) return@mapNotNull null

                val floorLabel = formatFloorLabelShort(node.floor)
                val typeLabel = "Classroom"
                val roomLabel = roomLabelFromId(node)
                val primary = "${node.buildingCode.uppercase()}.${roomLabel}"
                Result(
                    node = node,
                    buildingCode = node.buildingCode.uppercase(),
                    floor = node.floor,
                    primaryLabel = primary,
                    typeLabel = typeLabel,
                    locationLabel = "${node.buildingCode.uppercase()} $floorLabel",
                    score = score
                )
            }
            .sortedWith(
                compareByDescending<Result> { it.score }
                    .thenBy { it.buildingCode }
                    .thenBy { it.floor }
                    .thenBy { it.primaryLabel }
            )
            .take(limit)
            .toList()
    }

    fun formatFloorLabel(floor: Int): String = when {
        floor < 0 -> "S${-floor}"
        floor == 0 -> "0" // UI excludes 0
        else -> "Floor $floor"
    }

    fun formatFloorLabelShort(floor: Int): String = when {
        floor < 0 -> "S${-floor}"
        floor == 0 -> "0"
        else -> "floor $floor"
    }

    private fun normalize(s: String): String =
        s.uppercase()
            .replace(" ", "")
            .replace("-", "")
            .replace("_", "")

    /**
     * Prefer the room number encoded in ids like "H-8-851"; fall back to label when needed.
     */
    private fun roomLabelFromId(node: IndoorNode): String {
        val raw = node.id.trim()
        val parts = raw.split("-").filter { it.isNotBlank() }
        val room = parts.lastOrNull()?.trim()
        return if (!room.isNullOrBlank() && room.any { it.isDigit() }) room else node.label
    }

    /** Simple scoring: exact > prefix > contains; id slightly lower priority than label. */
    private fun scoreMatch(q: String, label: String, id: String): Int {
        var score = 0
        if (label == q) score = maxOf(score, 100)
        if (label.startsWith(q)) score = maxOf(score, 80)
        if (label.contains(q)) score = maxOf(score, 60)

        if (id == q) score = maxOf(score, 90)
        if (id.startsWith(q)) score = maxOf(score, 70)
        if (id.contains(q)) score = maxOf(score, 50)

        return score
    }
}
