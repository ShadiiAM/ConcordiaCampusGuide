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

    // Track visibility state to avoid redundant operations
    private var isVisible: Boolean = true

    /**
     * Add the overlay to the map with default (property-driven) styles.
     */
    fun addToMap(map: GoogleMap, context: Context): GeoJsonLayer {
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
        isVisible = true // Reset to default state
    }

    /**
     * Hide the overlay from the map without removing it.
     */
    fun hideFromMap() {
        // Early return if already hidden to avoid redundant operations
        if (!isVisible) return

        val currentLayer = layer ?: return
        currentLayer.features.forEach { feature ->
            when (feature.geometry) {
                is GeoJsonPolygon -> {
                    // Save current style before hiding if not already saved
                    if (!customPolygonStyles.containsKey(feature)) {
                        customPolygonStyles[feature] = feature.polygonStyle
                    }
                    // Make transparent
                    val style = GeoJsonPolygonStyle()
                    style.fillColor = 0x00000000 // Fully transparent
                    style.strokeColor = 0x00000000 // Fully transparent
                    style.strokeWidth = 0f
                    feature.polygonStyle = style
                }
                is GeoJsonPoint -> {
                    // Save current style before hiding if not already saved
                    if (!customPointStyles.containsKey(feature)) {
                        customPointStyles[feature] = feature.pointStyle
                    }
                    // Make transparent
                    val style = feature.pointStyle
                    style.alpha = 0f
                    feature.pointStyle = style
                }
            }
        }
        isVisible = false
    }

    /**
     * Show the overlay on the map (restore visibility).
     */
    fun showOnMap() {
        // Early return if already visible to avoid redundant operations
        if (isVisible) return

        val currentLayer = layer ?: return
        currentLayer.features.forEach { feature ->
            when (feature.geometry) {
                is GeoJsonPolygon -> {
                    // Simply restore the saved style
                    val savedStyle = customPolygonStyles[feature]
                    if (savedStyle != null) {
                        feature.polygonStyle = savedStyle
                    }
                }
                is GeoJsonPoint -> {
                    // Simply restore the saved style
                    val savedStyle = customPointStyles[feature]
                    if (savedStyle != null) {
                        savedStyle.alpha = 1f
                        feature.pointStyle = savedStyle
                    }
                }
            }
        }
        isVisible = true
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

}
