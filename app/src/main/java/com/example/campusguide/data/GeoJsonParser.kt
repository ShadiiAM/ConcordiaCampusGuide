package com.example.campusguide.data

import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject

/**
 * Pure Kotlin GeoJSON parser with no Android dependencies.
 * Extracted for fast unit testing without Robolectric.
 */
object GeoJsonParser {

    /**
     * Parse coordinates from a GeoJSON geometry object
     * Returns list of LatLng points, or empty list if invalid
     */
    fun parseCoordinates(geometry: JSONObject?): List<LatLng> {
        if (geometry == null) return emptyList()

        val type = geometry.optString("type", "")
        val coordinates = geometry.optJSONArray("coordinates") ?: return emptyList()

        return when (type) {
            "Polygon" -> parsePolygonCoordinates(coordinates)
            "MultiPolygon" -> parseMultiPolygonCoordinates(coordinates)
            "Point" -> parsePointCoordinates(coordinates)
            else -> emptyList()
        }
    }

    private fun parsePolygonCoordinates(coordinates: JSONArray): List<LatLng> {
        if (coordinates.length() == 0) return emptyList()

        // Polygon coordinates are [[[lon, lat], [lon, lat], ...]]
        val ring = coordinates.optJSONArray(0) ?: return emptyList()
        return parseCoordinateRing(ring)
    }

    private fun parseMultiPolygonCoordinates(coordinates: JSONArray): List<LatLng> {
        if (coordinates.length() == 0) return emptyList()

        // MultiPolygon: [[[[lon, lat], [lon, lat], ...]]]
        // coordinates[0] = first polygon
        // coordinates[0][0] = first ring of first polygon
        val firstPolygon = coordinates.optJSONArray(0) ?: return emptyList()
        val firstRing = firstPolygon.optJSONArray(0) ?: return emptyList()
        return parseCoordinateRing(firstRing)
    }

    private fun parsePointCoordinates(coordinates: JSONArray): List<LatLng> {
        if (coordinates.length() < 2) return emptyList()

        val lon = coordinates.optDouble(0, Double.NaN)
        val lat = coordinates.optDouble(1, Double.NaN)

        if (lon.isNaN() || lat.isNaN()) return emptyList()

        return listOf(LatLng(lat, lon))
    }

    private fun parseCoordinateRing(ring: JSONArray): List<LatLng> {
        val points = mutableListOf<LatLng>()

        for (i in 0 until ring.length()) {
            val coord = ring.optJSONArray(i) ?: continue
            if (coord.length() < 2) continue

            val lon = coord.optDouble(0, Double.NaN)
            val lat = coord.optDouble(1, Double.NaN)

            if (!lon.isNaN() && !lat.isNaN()) {
                points.add(LatLng(lat, lon))
            }
        }

        return points
    }

    /**
     * Validate that a GeoJSON feature has valid geometry
     */
    fun hasValidGeometry(feature: JSONObject): Boolean {
        val geometry = feature.optJSONObject("geometry") ?: return false
        val type = geometry.optString("type", "")
        val coordinates = geometry.optJSONArray("coordinates")

        return type.isNotEmpty() && coordinates != null && coordinates.length() > 0
    }

    /**
     * Extract title from GeoJSON feature properties
     */
    fun extractTitle(feature: JSONObject): String? {
        val properties = feature.optJSONObject("properties") ?: return null
        return properties.optString("title", null)?.takeIf { it.isNotEmpty() }
    }
}
