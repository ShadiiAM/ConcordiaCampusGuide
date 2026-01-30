package com.example.campusguide.ui.map

import android.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonPointStyle
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle

/**
 * Allows geojson.io styles to work with Google Maps Android Utils.
 */
class GeoJsonStyleMapper {

    fun applyPolygonStyle(feature: GeoJsonFeature, style: GeoJsonPolygonStyle) {
        // Colors are strings like "#ff0000".
        feature.getProperty("stroke")?.let { style.strokeColor = safeParseColor(it) }
        feature.getProperty("fill")?.let { style.fillColor = safeParseColor(it) }

        // Width/opacities might be numbers in the JSON but arrive as strings.
        feature.getProperty("stroke-width")?.toFloatOrNull()?.let { style.strokeWidth = it }

        feature.getProperty("stroke-opacity")?.toFloatOrNull()?.let { opacity ->
            style.strokeColor = withAlpha(style.strokeColor, opacity)
        }

        feature.getProperty("fill-opacity")?.toFloatOrNull()?.let { opacity ->
            style.fillColor = withAlpha(style.fillColor, opacity)
        }

    }

    /**
     * Styles Point features (markers).
     */
    fun applyPointStyle(feature: GeoJsonFeature, style: GeoJsonPointStyle) {

        //TO FIX: Have the actual color show up and not just the hue
        feature.getProperty("marker-color")?.let { hex ->
            val colorInt = safeParseColor(hex)
            // maps-utils renders point styling primarily via the marker icon.
            style.icon = BitmapDescriptorFactory.defaultMarker(colorToHue(colorInt))
        }


        feature.getProperty("title")?.let { style.title = it }
        feature.getProperty("name")?.let { if (style.title.isNullOrBlank()) style.title = it }
        feature.getProperty("building-name")?.let { if (style.title.isNullOrBlank()) style.title = it }

        feature.getProperty("description")?.let { style.snippet = it }
    }

    private fun safeParseColor(value: String): Int = try {
        Color.parseColor(value.trim())
    } catch (_: IllegalArgumentException) {
        // If bad input, fall back to a visible default.
        Color.RED
    }

    private fun withAlpha(color: Int, opacity0to1: Float): Int {
        val clamped = opacity0to1.coerceIn(0f, 1f)
        val alpha = (clamped * 255f).toInt()
        return (color and 0x00FFFFFF) or (alpha shl 24)
    }

    private fun colorToHue(color: Int): Float {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv[0]
    }
}
