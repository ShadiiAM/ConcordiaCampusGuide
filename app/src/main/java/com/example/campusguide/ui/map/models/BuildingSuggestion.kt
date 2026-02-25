package com.example.campusguide.ui.map.models

import com.example.campusguide.ui.components.Campus

/**
 * Represents a building suggestion for autocomplete UI.
 * Used to display matching results in search dropdowns.
 */
data class BuildingSuggestion(
    val buildingCode: String,
    val buildingName: String,
    val address: String? = null,
    val campus: Campus,
    val displayText: String = "$buildingCode - $buildingName"
)

