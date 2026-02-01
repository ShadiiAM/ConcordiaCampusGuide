package com.example.campusguide.ui.map

import android.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonPointStyle
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle

class GeoJsonStyleMapper {



    /**
     * Apply polygon styling from feature properties (stroke, fill, opacity, etc.).
     */
    fun applyPolygonStyle(feature: GeoJsonFeature, style: GeoJsonPolygonStyle) {
        feature.getProperty("stroke")?.let { style.strokeColor = parseColor(it) }
        feature.getProperty("fill")?.let { style.fillColor = parseColor(it) }
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
        feature.getProperty("marker-color")?.let { hex ->
            applyMarkerColor(style, parseColor(hex))
        }

        // Set title from various possible property names
        feature.getProperty("title")?.let { style.title = it }
            ?: feature.getProperty("name")?.let { style.title = it }
            ?: feature.getProperty("building-name")?.let { style.title = it }

        feature.getProperty("description")?.let { style.snippet = it }
    }

    /**
     * Apply a custom fill color to a polygon style, preserving other properties
     */
    fun applyCustomFillColor(feature: GeoJsonFeature, style: GeoJsonPolygonStyle, fillColor: String) {
        // Apply non-fill properties from GeoJSON
        feature.getProperty("stroke")?.let { style.strokeColor = parseColor(it) }
        feature.getProperty("stroke-width")?.toFloatOrNull()?.let { style.strokeWidth = it }
        feature.getProperty("stroke-opacity")?.toFloatOrNull()?.let { opacity ->
            style.strokeColor = withAlpha(style.strokeColor, opacity)
        }

        // Force constant 0.5 fill opacity for custom colors.
        val customColorInt = parseColor(fillColor)
        style.fillColor = withAlpha(customColorInt, 0.5f)
    }

    /**
     * Apply a custom marker color to a point style, preserving other properties.
     */
    fun applyCustomMarkerColor(feature: GeoJsonFeature, style: GeoJsonPointStyle, markerColor: String) {
        // Apply non-color properties (title, snippet)
        feature.getProperty("title")?.let { style.title = it }
            ?: feature.getProperty("name")?.let { style.title = it }
            ?: feature.getProperty("building-name")?.let { style.title = it }
        feature.getProperty("description")?.let { style.snippet = it }

        // Apply custom marker color
        applyMarkerColor(style, parseColor(markerColor))
    }



    /**
     * Apply a marker color (including opacity) to a point style.
     */
    private fun applyMarkerColor(style: GeoJsonPointStyle, colorInt: Int) {
        val hue = colorToHue(colorInt)
        val alpha = (Color.alpha(colorInt) / 255f).coerceIn(0f, 1f)

        style.icon = BitmapDescriptorFactory.defaultMarker(hue)
        // Always set alpha (even if 1.0) to ensure it's tracked properly
        style.alpha = alpha
    }

    private fun parseColor(value: String): Int = try {
        Color.parseColor(value.trim())
    } catch (_: IllegalArgumentException) {
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

    private fun stableHueForString(input: String): Float {
        var hash = 0x811C9DC5.toInt()
        val prime = 0x01000193
        input.trim().lowercase().forEach { c ->
            hash = hash xor c.code
            hash *= prime
        }
        val positive = hash.toLong() and 0xFFFF_FFFFL
        return (positive % 360L).toFloat()
    }

}