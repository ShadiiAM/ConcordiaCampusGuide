package com.example.campusguide.ui.screens

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
    label.contains("ELEVATOR", ignoreCase = true)  -> "Elevator"
    label.contains("ESCALATOR", ignoreCase = true) -> "Escalator"
    else -> label.replace("-", " ").lowercase().replaceFirstChar { it.uppercase() }
}

internal fun poiInferredDescription(label: String): String = when {
    label == "BATHROOM-M"     -> "Men's washroom"
    label == "BATHROOM-F"     -> "Women's washroom"
    label == "BATHROOM-WC"    -> "All-gender washroom · wheelchair accessible"
    label == "BATHROOM-MWC"   -> "Men's washroom · wheelchair accessible stall available"
    label == "BATHROOM-FWC"   -> "Women's washroom · wheelchair accessible stall available"
    label == "WATER-FOUNTAIN" -> "Drinking water available"
    label.startsWith("EMERGENCY-STAIR") -> "Emergency exit staircase"
    label.contains("ELEVATOR", ignoreCase = true)  -> "Takes you between floors · wheelchair accessible"
    label.contains("ESCALATOR", ignoreCase = true) -> "Takes you between floors"
    else -> label.replace("-", " ").lowercase().replaceFirstChar { it.uppercase() }
}
