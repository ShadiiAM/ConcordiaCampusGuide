package com.example.campusguide.ui.map

import android.content.Context
import androidx.annotation.RawRes
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPoint
import com.google.maps.android.data.geojson.GeoJsonPointStyle
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle

/**
 * Wrapper around a GeoJsonLayer with convenient APIs for styling and filtering features.
 */
class GeoJsonOverlay(
    @RawRes private val geoJsonRawRes: Int,
    private val styleMapper: GeoJsonStyleMapper = GeoJsonStyleMapper(),
) {
    private var layer: GeoJsonLayer? = null
    private var lastMap: GoogleMap? = null

    // Track custom styles to preserve them when restoring
    private val customPolygonStyles = mutableMapOf<GeoJsonFeature, GeoJsonPolygonStyle>()
    private val customPointStyles = mutableMapOf<GeoJsonFeature, GeoJsonPointStyle>()

    /**
     * Add the overlay to the map with default (property-driven) styles.
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
     * Remove the overlay from the map.
     */
    fun removeFromMap() {
        layer?.removeLayerFromMap()
        layer = null
        lastMap = null
        customPolygonStyles.clear()
        customPointStyles.clear()
    }



    /**
     * Change the fill color of all polygon (building) features.
     */
    fun changeAllBuildingColors(fillColor: String) {
        val currentLayer = layer ?: return

        currentLayer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                val style = GeoJsonPolygonStyle()
                styleMapper.applyCustomFillColor(feature, style, fillColor)
                feature.polygonStyle = style
                // Track this custom style
                customPolygonStyles[feature] = style
            }
        }
        redraw()
    }


    /**
     * Change the marker color of all point features.
     */
    fun changeAllPointColors(markerColor: String) {
        val currentLayer = layer ?: return

        currentLayer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint) {
                val style = GeoJsonPointStyle()
                styleMapper.applyCustomMarkerColor(feature, style, markerColor)
                feature.pointStyle = style
                // Track this custom style
                customPointStyles[feature] = style
            }
        }
        redraw()
    }

    /**
     * Hide all polygon (building) features.
     */
    fun removeAllBuildings() {
        val currentLayer = layer ?: return

        currentLayer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                // Create a fully transparent style
                val style = GeoJsonPolygonStyle()
                style.fillColor = 0x00000000
                style.strokeColor = 0x00000000
                style.strokeWidth = 0f
                feature.polygonStyle = style
            }
        }
        redraw()
    }

    /**
     * Restore all polygon (building) features to their previous styles (custom or default).
     */
    fun restoreAllBuildings() {
        val currentLayer = layer ?: return

        currentLayer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                // Check if we have a custom style saved
                val style = customPolygonStyles[feature] ?: GeoJsonPolygonStyle().also {
                    // Apply default if no custom style exists
                    styleMapper.applyPolygonStyle(feature, it)
                }
                feature.polygonStyle = style
            }
        }
        redraw()
    }

    /**
     * Hide all point features.
     */
    fun removeAllPoints() {
        val currentLayer = layer ?: return

        currentLayer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint) {
                // Create an invisible style
                val style = GeoJsonPointStyle()
                style.alpha = 0f // Fully transparent
                feature.pointStyle = style
            }
        }
        redraw()
    }

    /**
     * Restore all point features to their previous styles (custom or default).
     */
    fun restoreAllPoints() {
        val currentLayer = layer ?: return

        currentLayer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint) {
                // Check if we have a custom style saved
                val style = customPointStyles[feature] ?: GeoJsonPointStyle().also {
                    // Apply default if no custom style exists
                    styleMapper.applyPointStyle(feature, it)
                }
                feature.pointStyle = style
            }
        }
        redraw()
    }

    /**
     * Change the fill color of a specific building by its "building-name" property.
     */
    fun changeSpecificBuildingColor(buildingName: String, fillColor: String): Boolean {
        val currentLayer = layer ?: return false
        var updated = false

        currentLayer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon && matchesBuildingName(feature, buildingName)) {
                val style = GeoJsonPolygonStyle()
                styleMapper.applyCustomFillColor(feature, style, fillColor)
                feature.polygonStyle = style
                customPolygonStyles[feature] = style
                updated = true
            }
        }

        if (updated) redraw()
        return updated
    }
    /**
     * Change the marker color of a specific point by its "building-name" property.
     */
    fun changeSpecificPointColor(buildingName: String, markerColor: String): Boolean {
        val currentLayer = layer ?: return false
        var updated = false

        currentLayer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint && matchesBuildingName(feature, buildingName)) {
                val style = GeoJsonPointStyle()
                styleMapper.applyCustomMarkerColor(feature, style, markerColor)
                feature.pointStyle = style
                customPointStyles[feature] = style
                updated = true
            }
        }

        if (updated) redraw()
        return updated
    }

    /**
     * Change both building polygon and point colors for a specific building name.
     */
    fun changeSpecificBuildingColors(
        buildingName: String,
        fillColor: String,
        markerColor: String
    ): Boolean {
        val buildingUpdated = changeSpecificBuildingColor(buildingName, fillColor)
        val pointUpdated = changeSpecificPointColor(buildingName, markerColor)
        return buildingUpdated || pointUpdated
    }

    /**
     * Set the fill opacity for all building polygons.
     */
    fun setAllBuildingOpacity(opacity0to1: Float) {
        val currentLayer = layer ?: return

        currentLayer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon) {
                val style = feature.polygonStyle ?: GeoJsonPolygonStyle().also {
                    styleMapper.applyPolygonStyle(feature, it)
                }
                styleMapper.applyFillOpacity(style, opacity0to1)
                feature.polygonStyle = style
                customPolygonStyles[feature] = style
            }
        }
        redraw()
    }

    /**
     * Set the fill opacity for a specific building polygon by building-name.
     *
     * TOFIX: does not seem to work
     */

    fun setBuildingOpacity(buildingName: String, opacity0to1: Float): Boolean {
        val currentLayer = layer ?: return false
        var updated = false

        currentLayer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPolygon && matchesBuildingName(feature, buildingName)) {
                val style = feature.polygonStyle ?: GeoJsonPolygonStyle().also {
                    styleMapper.applyPolygonStyle(feature, it)
                }
                styleMapper.applyFillOpacity(style, opacity0to1)
                feature.polygonStyle = style
                customPolygonStyles[feature] = style
                updated = true
            }
        }

        if (updated) redraw()
        return updated
    }

    /**
     * Set the opacity for all point markers.
     */
    fun setAllPointOpacity(opacity0to1: Float) {
        val currentLayer = layer ?: return

        currentLayer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint) {
                val style = feature.pointStyle ?: GeoJsonPointStyle().also {
                    styleMapper.applyPointStyle(feature, it)
                }
                styleMapper.applyPointOpacity(style, opacity0to1)
                feature.pointStyle = style
                customPointStyles[feature] = style
            }
        }
        redraw()
    }

    /**
     * Set the opacity for a specific point by building-name.
     */
    fun setPointOpacity(buildingName: String, opacity0to1: Float): Boolean {
        val currentLayer = layer ?: return false
        var updated = false

        currentLayer.features.forEach { feature ->
            if (feature.geometry is GeoJsonPoint && matchesBuildingName(feature, buildingName)) {
                val style = feature.pointStyle ?: GeoJsonPointStyle().also {
                    styleMapper.applyPointStyle(feature, it)
                }
                styleMapper.applyPointOpacity(style, opacity0to1)
                feature.pointStyle = style
                customPointStyles[feature] = style
                updated = true
            }
        }

        if (updated) redraw()
        return updated
    }

    // private helpers
    private fun applyDefaultStyles(targetLayer: GeoJsonLayer) {
        targetLayer.features.forEach { feature ->
            when (feature.geometry) {
                is GeoJsonPolygon -> {
                    val style = GeoJsonPolygonStyle()
                    styleMapper.applyPolygonStyle(feature, style)
                    feature.polygonStyle = style
                }
                is GeoJsonPoint -> {
                    val style = GeoJsonPointStyle()
                    styleMapper.applyPointStyle(feature, style)
                    feature.pointStyle = style
                }
            }
        }
    }

    private fun matchesBuildingName(feature: GeoJsonFeature, buildingName: String): Boolean {
        val name = feature.getProperty("building-name")?.trim() ?: return false
        return name.equals(buildingName.trim(), ignoreCase = true)
    }


    //this was needed at first, but now it messes things up
    //i'll keep it for now
    private fun redraw() {
//        val currentLayer = layer ?: return
//        val map = lastMap ?: return
//
//        currentLayer.removeLayerFromMap()
//        currentLayer.addLayerToMap()
    }
}
