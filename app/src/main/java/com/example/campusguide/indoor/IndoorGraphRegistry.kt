package com.example.campusguide.indoor

import android.content.Context

/**
 * Central registry: maps (buildingCode, floor) to [IndoorFloorGraph].
 *
 * Graphs are loaded from JSON files in `assets/indoor/` via [IndoorGraphLoader].
 * Call [IndoorGraphRegistry.init] once (e.g. in Application.onCreate) before any lookups.
 */
object IndoorGraphRegistry {

    private val registry = mutableMapOf<Pair<String, Int>, IndoorFloorGraph>()

    fun init(context: Context) {
        IndoorGraphLoader.loadAll(context).forEach { graph ->
            registry[graph.buildingCode.uppercase() to graph.floor] = graph
        }
    }

    /** Returns the graph for the given building + floor, or null if not loaded. */
    fun get(buildingCode: String, floor: Int): IndoorFloorGraph? =
        registry[buildingCode.uppercase() to floor]

    /** Returns all available floors for a building, sorted ascending. */
    fun floorsFor(buildingCode: String): List<Int> =
        registry.keys
            .filter { (code, _) -> code == buildingCode.uppercase() }
            .map { (_, floor) -> floor }
            .sorted()

    /** Returns true if at least one floor is mapped for the given building. */
    fun hasIndoorMap(buildingCode: String): Boolean =
        floorsFor(buildingCode).isNotEmpty()

    /** Returns all building codes that currently have at least one indoor graph loaded. */
    fun buildings(): List<String> =
        registry.keys
            .map { (code, _) -> code }
            .distinct()
            .sorted()

    /** Returns all loaded floor graphs (read-only snapshot). */
    fun allGraphs(): List<IndoorFloorGraph> = registry.values.toList()
}
