package com.example.campusguide.ui.map.models

import org.json.JSONObject

/**
 * Represents comprehensive metadata for a campus building.
 * All fields except buildingCode are nullable to handle incomplete data.
 */
data class BuildingInfo(
    val buildingCode: String,
    val buildingName: String? = null,
    val address: String? = null,
    val campus: String? = null,
    val departments: String? = null,
    val services: String? = null,
    val venues: String? = null,
    val accessibility: String? = null,
    val notes: String? = null,
    val hours: String? = null
) {
    companion object {
        /**
         * Parse BuildingInfo from GeoJSON feature properties.
         * Returns null if buildingCode is missing or empty.
         */
        fun fromJson(props: JSONObject?): BuildingInfo? {
            if (props == null) return null

            val buildingCode = props.optString("buildingCode", "").trim()
            if (buildingCode.isEmpty()) return null

            return BuildingInfo(
                buildingCode = buildingCode,
                buildingName = props.optString("buildingName", "").trim().ifBlank { null },
                address = props.optString("address", "").trim().ifBlank { null },
                campus = props.optString("campus", "").trim().ifBlank { null },
                departments = props.optString("departments", "").trim().ifBlank { null },
                services = props.optString("services", "").trim().ifBlank { null },
                venues = props.optString("venues", "").trim().ifBlank { null },
                accessibility = props.optString("accessibility", "").trim().ifBlank { null },
                notes = props.optString("notes", "").trim().ifBlank { null },
                hours = props.optString("hours", "").trim().ifBlank { null }
            )
        }
    }
}
