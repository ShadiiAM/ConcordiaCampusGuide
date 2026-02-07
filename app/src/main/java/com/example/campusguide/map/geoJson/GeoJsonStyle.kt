package com.example.campusguide.map.geoJson

import androidx.annotation.ColorInt

data class GeoJsonStyle(
    @ColorInt val fillColor: Int? = null,
    @ColorInt val strokeColor: Int? = null,
    val strokeWidth: Float? = null,
    val zIndex: Float? = null,
    val clickable: Boolean? = null,
    val visible: Boolean? = null,

    // Marker style
    @ColorInt val markerColor: Int? = null,
    val markerAlpha: Float? = null,   // 0f..1f
    val markerScale: Float? = null    // 1.0 = normal
)