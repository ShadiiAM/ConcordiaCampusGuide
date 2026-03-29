package com.example.campusguide.ui.screens

/**
 * Utility functions for indoor POI labels (US-5.5).
 * Extracted as internal so they can be covered by unit tests.
 */

/**
 * Returns a human-readable display name for a POI node based on its label.
 * Matches the naming conventions defined in the indoor graph data:
 * BATHROOM-M, BATHROOM-F, BATHROOM-WC, BATHROOM-MWC, BATHROOM-FWC,
 * WATER-FOUNTAIN, EMERGENCY-STAIR-{n}, etc.
 */
internal fun poiDisplayName(label: String): String = when {
    label == "BATHROOM-M"     -> "Men's Washroom"
    label == "BATHROOM-F"     -> "Women's Washroom"
    label == "BATHROOM-WC"    -> "All-Gender Washroom"
    label == "BATHROOM-MWC"   -> "Men's Accessible Washroom"
    label == "BATHROOM-FWC"   -> "Women's Accessible Washroom"
    label == "WATER-FOUNTAIN" -> "Water Fountain"
    label.startsWith("EMERGENCY-STAIR") -> {
        val num = label.removePrefix("EMERGENCY-STAIR").trimStart('-')
        if (num.isNotEmpty()) "Emergency Staircase $num" else "Emergency Staircase"
    }
    else -> label.replace("-", " ").lowercase().replaceFirstChar { it.uppercase() }
}

/**
 * Returns a meaningful description for a POI when no JSON description is set.
 * Based on the label naming conventions (BATHROOM-M, BATHROOM-MWC, etc.).
 */
internal fun poiInferredDescription(label: String): String = when {
    label == "BATHROOM-M"     -> "Men's washroom"
    label == "BATHROOM-F"     -> "Women's washroom"
    label == "BATHROOM-WC"    -> "All-gender washroom · wheelchair accessible"
    label == "BATHROOM-MWC"   -> "Men's washroom · wheelchair accessible stall available"
    label == "BATHROOM-FWC"   -> "Women's washroom · wheelchair accessible stall available"
    label == "WATER-FOUNTAIN" -> "Drinking water available"
    label.startsWith("EMERGENCY-STAIR") -> "Emergency exit staircase · not for regular use"
    else -> label.replace("-", " ").lowercase().replaceFirstChar { it.uppercase() }
}
