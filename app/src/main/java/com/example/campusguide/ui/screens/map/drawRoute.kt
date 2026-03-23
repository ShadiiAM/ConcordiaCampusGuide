package com.example.campusguide.ui.screens.map

import com.example.campusguide.ui.directions.DirectionsStep
import com.example.campusguide.ui.directions.DirectionsUiState
import com.example.campusguide.ui.directions.GoogleRoutesRepository
import com.example.campusguide.ui.directions.RouteRequest
import com.example.campusguide.ui.directions.RouteResult
import com.example.campusguide.ui.directions.TravelMode
import com.example.campusguide.ui.directions.getCrossCampusErrorMessage
import com.example.campusguide.ui.directions.getCrossCampusMessage
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlin.collections.mutableListOf


suspend fun drawRoute(
    step: DirectionsStep.PlanRoute,
    origin: LatLng,
    destination: LatLng,
    travelMode: String,
    googleMap: GoogleMap?,
    directionsUiState: DirectionsUiState,
    onDirectionsUiStateChange: (DirectionsUiState) -> Unit,
    repo: GoogleRoutesRepository,
    isCrossCampus: Boolean
): DrawRouteResult{
    lateinit var polylineFormatting: PolylineOptions
    lateinit var route: RouteResult
    var travelType = TravelMode.TRANSIT
    val routePolylines = mutableListOf<Polyline?>()


    if(travelMode == "SHUTTLE") {

            route = getShuttleRoute(origin, destination)

            route.legs.forEach { leg ->
                leg.steps.forEach { step ->
                    when (step.travelMode) {
                        TravelMode.TRANSIT -> {
                            polylineFormatting = PolylineOptions()
                                .addAll(step.polyline)
                                .color(0xFFE53935.toInt())
                                .width(18f)
                        }

                        else -> {
                            polylineFormatting = PolylineOptions()
                                .addAll(step.polyline)
                                .color(0xFF1565C0.toInt())
                                .width(14f)
                                .pattern(listOf(Dash(20f), Gap(10f)))
                        }
                    }
                    val polyline = googleMap?.addPolyline(polylineFormatting)
                    routePolylines.add(polyline)
                }
            }
    }
    else{
        travelType = enumValueOf<TravelMode>(travelMode)

        runCatching {
            repo.getRoute(
                RouteRequest(
                    origin = step.origin,
                    destination = step.destination,
                    mode = travelType,
                )
            )
        }.onSuccess { route ->


            when (travelType) {
                TravelMode.DRIVE -> {
                    polylineFormatting = PolylineOptions()
                        .addAll(route.points)
                        .color(0xFF1565C0.toInt())
                        .width(18f)

                    val polyline = googleMap?.addPolyline(polylineFormatting)
                    routePolylines.add(polyline)
                }

                TravelMode.WALK -> {
                    polylineFormatting = PolylineOptions()
                        .addAll(route.points)
                        .color(0xFF1565C0.toInt())
                        .width(14f)
                        .pattern(listOf(Dash(20f), Gap(10f)))

                    val polyline = googleMap?.addPolyline(polylineFormatting)
                    routePolylines.add(polyline)
                }

                TravelMode.TRANSIT -> {
                    route.legs.forEach { leg ->
                        leg.steps.forEach { step ->
                            when (step.travelMode) {
                                TravelMode.TRANSIT -> {
                                    polylineFormatting = PolylineOptions()
                                        .addAll(step.polyline)
                                        .color(0xFF7B1FA2.toInt())
                                        .width(18f)
                                }

                                else -> {
                                    polylineFormatting = PolylineOptions()
                                        .addAll(step.polyline)
                                        .color(0xFF1565C0.toInt())
                                        .width(14f)
                                        .pattern(listOf(Dash(20f), Gap(10f)))
                                }
                            }
                            val polyline = googleMap?.addPolyline(polylineFormatting)
                            routePolylines.add(polyline)
                        }
                    }
                }
            }


            // Show helpful message for cross-campus routes


            onDirectionsUiStateChange(directionsUiState.copy(
                isLoadingRoute = false,
                step = DirectionsStep.ShowingRoute(
                    origin = origin,
                    destination = destination,
                    buildingHit = step.buildingHit,
                    route = route,
                ),
            ))

        }.onFailure { e ->
            // Check if this is a cross-campus route and provide helpful error message
            val errorMsg = if (isCrossCampus) {
                getCrossCampusErrorMessage(travelType)
            } else {
                e.message ?: "Failed to get route"
            }
            onDirectionsUiStateChange(directionsUiState.copy(
                isLoadingRoute = false,
                errorMessage = errorMsg,
                ))
        }
    }
    val message = getCrossCampusMessage(travelType)

    return DrawRouteResult(routePolylines, message)
}


data class DrawRouteResult(
    val polylines: List<Polyline?>,
    val snackBarMessage: String
)