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
