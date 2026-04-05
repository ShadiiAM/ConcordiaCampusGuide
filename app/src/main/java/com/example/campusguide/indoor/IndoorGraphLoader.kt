package com.example.campusguide.indoor

import android.content.Context
import android.util.Log
import com.example.campusguide.R
import kotlinx.serialization.json.Json

/**
 * Loads [IndoorFloorGraph] objects from JSON files stored in `assets/indoor/`.
 *
 * Convention: one file per floor, named `{building_lowercase}_{floor}.json`
 * e.g. `assets/indoor/h_1.json`, `assets/indoor/h_8.json`, `assets/indoor/mb_1.json`.
 *
 * Each JSON file maps to [IndoorFloorGraphJson]. The drawable resource name
 * stored in the JSON (e.g. `"hall_floor_1"`) is resolved to an R.drawable ID
 * via a compile-time mapping.
 */
object IndoorGraphLoader {

    private const val TAG = "IndoorGraphLoader"

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
                    parse(raw)
                }.onFailure { e ->
                    e.printStackTrace()
                }.getOrNull()
            }
    }

    /**
     * Parses a single JSON string into an [IndoorFloorGraph], resolving the
     * drawable resource name to an actual R.drawable ID.
     */
    fun parse(jsonString: String): IndoorFloorGraph {
        val dto = json.decodeFromString<IndoorFloorGraphJson>(jsonString)
        // Strip extension to get the drawable name, e.g. "hall_floor_8.png" -> "hall_floor_8"
        val drawableName = dto.imageName.substringBeforeLast(".")
        val drawableRes = resolveFloorPlanDrawable(drawableName)
        if (drawableRes == 0) {
            Log.w(TAG, "Unknown indoor floor drawable '$drawableName' for ${dto.buildingCode} floor ${dto.floor}")
        }
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
    private fun resolveFloorPlanDrawable(drawableName: String): Int = when (drawableName) {
        "cc_floor_1" -> R.drawable.cc_floor_1
        "hall_floor_1" -> R.drawable.hall_floor_1
        "hall_floor_2" -> R.drawable.hall_floor_2
        "hall_floor_8" -> R.drawable.hall_floor_8
        "hall_floor_9" -> R.drawable.hall_floor_9
        "lb_floor_2" -> R.drawable.lb_floor_2
        "lb_floor_3" -> R.drawable.lb_floor_3
        "lb_floor_4" -> R.drawable.lb_floor_4
        "lb_floor_5" -> R.drawable.lb_floor_5
        "molson_floor_1" -> R.drawable.molson_floor_1
        "molson_floor_s2" -> R.drawable.molson_floor_s2
        "ve_floor_1" -> R.drawable.ve_floor_1
        "ve_floor_2" -> R.drawable.ve_floor_2
        "vl_floor_1" -> R.drawable.vl_floor_1
        "vl_floor_2" -> R.drawable.vl_floor_2
        else -> 0
    }
}
