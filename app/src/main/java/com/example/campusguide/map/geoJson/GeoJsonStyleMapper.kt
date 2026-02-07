package com.example.campusguide.map.geoJson
import com.google.maps.android.data.geojson.GeoJsonFeature

fun interface GeoJsonStyleMapper {
    fun styleFor(feature: GeoJsonFeature): GeoJsonStyle
}

class DefaultGeoJsonStyleMapper : GeoJsonStyleMapper {

    override fun styleFor(feature: GeoJsonFeature): GeoJsonStyle {
        // Common GeoJSON styling property names:
        // fill, fill-opacity, stroke, stroke-opacity, stroke-width
        val fillRaw = feature.getProperty("fill")
        val fillOpacityRaw = feature.getProperty("fill-opacity")

        val strokeRaw = feature.getProperty("stroke")
        val strokeOpacityRaw = feature.getProperty("stroke-opacity")
        val strokeWidthRaw = feature.getProperty("stroke-width")

        val fill = fillRaw?.let { safeParseColor(it) }
        val fillOpacity = fillOpacityRaw?.toFloatOrNull()
        val fillFinal = if (fill != null && fillOpacity != null)
            GeoJsonColorUtils.withOpacity(fill, fillOpacity)
        else fill

        val stroke = strokeRaw?.let { safeParseColor(it) }
        val strokeOpacity = strokeOpacityRaw?.toFloatOrNull()
        val strokeFinal = if (stroke != null && strokeOpacity != null)
            GeoJsonColorUtils.withOpacity(stroke, strokeOpacity)
        else stroke

        val width = strokeWidthRaw?.toFloatOrNull()

        return GeoJsonStyle(
            fillColor = fillFinal,
            strokeColor = strokeFinal,
            strokeWidth = width,
            clickable = true
        )
    }

    private fun safeParseColor(raw: String): Int? {
        return try {
            GeoJsonColorUtils.parse(raw)
        } catch (_: Throwable) {
            null
        }
    }
}