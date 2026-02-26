package com.example.campusguide.ui.directions

import com.example.campusguide.ui.map.utils.BuildingHit
import com.google.android.gms.maps.model.LatLng

sealed class DirectionsStep {
    data object PickDestination : DirectionsStep()
    data class ConfirmDestination(
        val destination: LatLng,
        val buildingHit: BuildingHit?,
    ) : DirectionsStep()

    data class PlanRoute(
        val origin: LatLng,
        val destination: LatLng,
        val buildingHit: BuildingHit?,
    ) : DirectionsStep()

    data class ShowingRoute(
        val origin: LatLng,
        val destination: LatLng,
        val buildingHit: BuildingHit?,
        val route: RouteResult,
    ) : DirectionsStep()
}

data class DirectionsUiState(
    val step: DirectionsStep = DirectionsStep.PickDestination,
    val isLoadingRoute: Boolean = false,
    val errorMessage: String? = null,
)
