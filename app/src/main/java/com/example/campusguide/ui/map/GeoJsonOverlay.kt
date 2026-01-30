package com.example.campusguide.ui.map

import android.content.Context
import android.graphics.Color
import androidx.annotation.RawRes
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPoint
import com.google.maps.android.data.geojson.GeoJsonPointStyle
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle

/**
 *  wrapper around a GeoJsonLayer: loads it, styles it, and adds it to the map.
 */
class GeoJsonOverlay(
    @param:RawRes private val geoJsonRawRes: Int,
    private val styleMapper: GeoJsonStyleMapper = GeoJsonStyleMapper(),
) {

    // Keep a reference so we can remove it later.
    private var layer: GeoJsonLayer? = null

    // We re-add the layer to force redraws when styles change.
    private var lastMap: GoogleMap? = null

    /**
     * Adds the overlay to the map using styles derived from each feature's properties.
     */
    fun addToMap(map: GoogleMap, context: Context): GeoJsonLayer {
        removeFromMap()

        lastMap = map
        val newLayer = GeoJsonLayer(map, geoJsonRawRes, context)

        applyDefaultStyles(newLayer)

        newLayer.addLayerToMap()
        layer = newLayer
        return newLayer
    }

    /**
     * Adds the overlay to the map and forces a single uniform style for all polygons/points.
    **/
    fun addToMap(map: GoogleMap, context: Context, fillColor: String, pointColor: String): GeoJsonLayer {
        removeFromMap()

        lastMap = map
        val newLayer = GeoJsonLayer(map, geoJsonRawRes, context)

        applyCustomStyles(newLayer, fillColor, pointColor)

        newLayer.addLayerToMap()
        layer = newLayer
        return newLayer
    }

    private fun applyCustomStyles(targetLayer: GeoJsonLayer, fillColor: String, pointColor: String) {
        val customFill = safeParseColor(fillColor)
        val customPoint = safeParseColor(pointColor)

        val pointHue = colorToHue(customPoint)
        val callerPointAlpha = (Color.alpha(customPoint) / 255f).coerceIn(0f, 1f)

        targetLayer.features.forEach { feature: GeoJsonFeature ->
            when (feature.geometry) {
                is GeoJsonPolygon -> {
                    val polygonStyle = GeoJsonPolygonStyle()
                    // IMPORTANT: apply defaults first (stroke width/opacities, etc.)
                    styleMapper.applyPolygonStyle(feature, polygonStyle)

                    // Keep the alpha as computed by applyPolygonStyle (fill-opacity), unless the caller
                    // explicitly provided alpha in the custom color.
                    val mappedAlpha = Color.alpha(polygonStyle.fillColor)
                    val customAlpha = Color.alpha(customFill)
                    val effectiveAlpha = if (customAlpha < 255) customAlpha else mappedAlpha

                    polygonStyle.fillColor = (customFill and 0x00FFFFFF) or (effectiveAlpha shl 24)
                    feature.polygonStyle = polygonStyle
                }

                is GeoJsonPoint -> {
                    val pointStyle = GeoJsonPointStyle()
                    // Apply defaults (title/snippet, etc.)
                    styleMapper.applyPointStyle(feature, pointStyle)

                    // Force marker color. Hue controls pin color.
                    pointStyle.icon = BitmapDescriptorFactory.defaultMarker(pointHue)

                    // If caller provided alpha (< 1.0), apply it.
                    if (callerPointAlpha < 1.0f) {
                        pointStyle.alpha = callerPointAlpha
                    }

                    // CRITICAL: assign back to the feature, otherwise nothing renders.
                    feature.pointStyle = pointStyle
                }

                else -> Unit
            }
        }
    }

    private fun safeParseColor(value: String): Int = try {
        Color.parseColor(value.trim())
    } catch (_: IllegalArgumentException) {
        Color.RED
    }

    private fun colorToHue(colorInt: Int): Float {
        val hsv = FloatArray(3)
        Color.colorToHSV(colorInt, hsv)
        return hsv[0]
    }

    /**
     * Apply the normal (property-driven) styles to every feature.
     */
    private fun applyDefaultStyles(targetLayer: GeoJsonLayer) {
        targetLayer.features.forEach { feature: GeoJsonFeature ->
            when (feature.geometry) {
                is GeoJsonPolygon -> {
                    val polygonStyle = GeoJsonPolygonStyle()
                    styleMapper.applyPolygonStyle(feature, polygonStyle)
                    feature.polygonStyle = polygonStyle
                }

                is GeoJsonPoint -> {
                    val pointStyle = GeoJsonPointStyle()
                    styleMapper.applyPointStyle(feature, pointStyle)
                    feature.pointStyle = pointStyle
                }

                else -> {
                    // No-op for other geometry types (LineString, Multi*, etc.)
                }
            }
        }
    }

    /**
     * Re-applies the overlay to the map, forcing a redraw.
     */
    private fun redrawLayer() {
        val current = layer ?: return
        val map = lastMap ?: return

        current.removeLayerFromMap()
        current.addLayerToMap()
        // Keep reference in place.
        layer = current
        lastMap = map
    }

    /**
     * Removes the overlay from the map if it was previously added.
     */
    fun removeFromMap() {
        layer?.removeLayerFromMap()
        layer = null
        lastMap = null
    }

    /** Convenience for callers to check if the overlay is currently on the map. */
    fun isAdded(): Boolean = layer != null
}
