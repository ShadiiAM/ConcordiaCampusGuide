package com.example.campusguide.ui.components

import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.indoor.IndoorNode

/** Suggestions shown in the shared top search bar. */
sealed class TopSearchSuggestion {
    data class Building(val building: CampusBuilding) : TopSearchSuggestion()

    /** Indoor room / indoor point suggestion. */
    data class Indoor(
        val node: IndoorNode,
        val buildingCode: String,
        val primaryLabel: String,
        val secondaryLabel: String,
        val tertiaryLabel: String,
    ) : TopSearchSuggestion()
}
