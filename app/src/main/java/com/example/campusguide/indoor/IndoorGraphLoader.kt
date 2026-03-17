package com.example.campusguide.indoor

import android.content.Context
import kotlinx.serialization.json.Json

/**
 * Loads [IndoorFloorGraph] objects from JSON files stored in `assets/indoor/`.
 *
 * Convention: one file per floor, named `{building_lowercase}_{floor}.json`
 * e.g. `assets/indoor/h_1.json`, `assets/indoor/h_8.json`, `assets/indoor/mb_1.json`.
 *
 * Each JSON file maps to [IndoorFloorGraphJson]. The drawable resource name
 * stored in the JSON (e.g. `"hall_floor_1"`) is resolved to an R.drawable ID
 * at load time via [android.content.res.Resources.getIdentifier].
 */
object IndoorGraphLoader {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Scans `assets/indoor/` and loads every `.json` file it finds.
     * Returns a list of [IndoorFloorGraph] ready to be registered.
     */
    fun loadAll(context: Context): List<IndoorFloorGraph> {
        val assetManager = context.assets
        val files = assetManager.list("indoor") ?: return emptyList()
        return files
            .filter { it.endsWith(".json") }
            .mapNotNull { filename ->
                runCatching {
                    val raw = assetManager.open("indoor/$filename")
                        .bufferedReader()
                        .use { it.readText() }
                    parse(context, raw)
                }.onFailure { e ->
                    e.printStackTrace()
                }.getOrNull()
            }
    }

    /**
     * Parses a single JSON string into an [IndoorFloorGraph], resolving the
     * drawable resource name to an actual R.drawable ID.
     */
    fun parse(context: Context, jsonString: String): IndoorFloorGraph {
        val dto = json.decodeFromString<IndoorFloorGraphJson>(jsonString)
        // Strip extension to get the drawable name, e.g. "hall_floor_8.png" -> "hall_floor_8"
        val drawableName = dto.imageName.substringBeforeLast(".")
        val drawableRes = context.resources.getIdentifier(
            drawableName, "drawable", context.packageName
        )
        return IndoorFloorGraph(
            buildingCode         = dto.buildingCode,
            floor                = dto.floor,
            floorPlanDrawableRes = drawableRes,
            imageWidth           = dto.imageWidth,
            imageHeight          = dto.imageHeight,
            nodes                = dto.nodes,
            edges                = dto.edges
        )
    }
}
