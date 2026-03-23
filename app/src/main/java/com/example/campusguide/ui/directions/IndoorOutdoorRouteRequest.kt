package com.example.campusguide.ui.directions

import com.example.campusguide.indoor.IndoorNode

/**
 * Request to build a multi-leg route from one indoor room to another,
 * potentially across buildings/campuses.
 */
data class IndoorOutdoorRouteRequest(
    val startNode: IndoorNode,
    val destinationNode: IndoorNode,
)
