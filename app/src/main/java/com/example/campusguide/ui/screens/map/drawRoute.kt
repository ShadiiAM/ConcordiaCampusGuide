package com.example.campusguide.ui.screens.map

import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.Suggestion
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.collections.mutableListOf


suspend fun drawRoute(
    step: DirectionsStep.PlanRoute,
    origin: LatLng,
    destination: LatLng,
    travelMode: String,
    googleMap: GoogleMap?,
    getDirectionsUiState: () -> DirectionsUiState,
    onDirectionsUiStateChange: (DirectionsUiState) -> Unit,
    repo: GoogleRoutesRepository,
    isCrossCampus: Boolean,
    requestGeneration: Int,
    routeRequestGeneration: Int,
    isIndoorOutdoorFlow: Boolean,
    destinationBuilding: Suggestion?,
    indoorOutdoorFallbackParts: List<String>,
    onLegFallbackMessage: (String?) -> Unit,
    defaultOrigin: LatLng,
    legLabels: List<String>,
    onLegLabels: (List<String>) -> Unit
): DrawRouteResult{
    lateinit var polylineFormatting: PolylineOptions
    lateinit var route: RouteResult
    var travelType = TravelMode.TRANSIT
    val routePolylines = mutableListOf<Polyline?>()

    var message = getCrossCampusMessage(travelType)


    if(travelMode == "SHUTTLE") {

        route = getShuttleRoute(origin, destination, repo)

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
                withContext(Dispatchers.Main.immediate) {
                    val polyline = googleMap?.addPolyline(polylineFormatting)
                    routePolylines.add(polyline)
                }
                yield()
            }
        }

        onDirectionsUiStateChange(getDirectionsUiState().copy(
            isLoadingRoute = false,
            step = DirectionsStep.ShowingRoute(
                origin = origin,
                destination = destination,
                buildingHit = step.buildingHit,
                route = route,
            ),
        ))
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
            if (requestGeneration != routeRequestGeneration) return@onSuccess
            when (travelType) {
                TravelMode.DRIVE -> {
                    polylineFormatting = PolylineOptions()
                        .addAll(route.points)
                        .color(0xFF1565C0.toInt())
                        .width(18f)

                    withContext(Dispatchers.Main.immediate) {
                        val polyline = googleMap?.addPolyline(polylineFormatting)
                        routePolylines.add(polyline)
                    }
                    yield()
                }

                TravelMode.WALK -> {
                    polylineFormatting = PolylineOptions()
                        .addAll(route.points)
                        .color(0xFF1565C0.toInt())
                        .width(14f)
                        .pattern(listOf(Dash(20f), Gap(10f)))

                    withContext(Dispatchers.Main.immediate) {
                        val polyline = googleMap?.addPolyline(polylineFormatting)
                        routePolylines.add(polyline)
                    }
                    yield()
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
                            withContext(Dispatchers.Main.immediate) {
                                val polyline = googleMap?.addPolyline(polylineFormatting)
                                routePolylines.add(polyline)
                            }
                            yield()
                        }
                    }
                }
            }


            // Show helpful message for cross-campus routes or generic message for normal routes
            message = if (isCrossCampus) {
                getCrossCampusMessage(travelType)
            }else{
                "Be sure to reach your destination safely!"
            }


            onDirectionsUiStateChange(getDirectionsUiState().copy(
                isLoadingRoute = false,
                step = DirectionsStep.ShowingRoute(
                    origin = origin,
                    destination = destination,
                    buildingHit = step.buildingHit,
                    route = route,
                ),
            ))

        }.onFailure { e ->

            if (requestGeneration != routeRequestGeneration) return@onFailure


            if (requestGeneration != routeRequestGeneration) return@onFailure
            if (isIndoorOutdoorFlow) {
                val fallbackRoute = runCatching {
                    repo.getRoute(
                        RouteRequest(
                            origin = defaultOrigin,
                            destination = step.destination,
                            mode = TravelMode.DRIVE,
                        )
                    )
                }.getOrNull()

                if (fallbackRoute != null) {
                    if (requestGeneration != routeRequestGeneration) return@onFailure
                    withContext(Dispatchers.Main) {

                        polylineFormatting = PolylineOptions()
                            .addAll(fallbackRoute.points)
                            .color(0xFF1565C0.toInt())
                            .width(12f)

                        withContext(Dispatchers.Main.immediate) {
                            val polyline = googleMap?.addPolyline(polylineFormatting)
                            routePolylines.add(polyline)
                        }
                        yield()
                    }

                    val destinationCode = step.buildingHit?.id ?: (destinationBuilding as? CampusBuilding)?.buildingCode ?: "destination"
                    onLegLabels( buildList {
                        addAll(legLabels)
                        add("Fallback outdoor leg: Current location → $destinationCode")
                    })
                    val prefix = if (indoorOutdoorFallbackParts.isNotEmpty()) {
                        "${indoorOutdoorFallbackParts.joinToString(". ")}. "
                    } else {
                        ""
                    }
                    onLegFallbackMessage( prefix + "Primary outdoor leg failed. Showing fallback route to destination building.")

                    onDirectionsUiStateChange(getDirectionsUiState().copy(
                        isLoadingRoute = false,
                        errorMessage = null,
                        step = DirectionsStep.ShowingRoute(
                            origin = defaultOrigin,
                            destination = step.destination,
                            buildingHit = step.buildingHit,
                            route = fallbackRoute,
                        ),
                    ))
                    return@onFailure
                }
            }

            // Check if this is a cross-campus route and provide helpful error message
            message = if (isCrossCampus) {
                getCrossCampusErrorMessage(travelType)
            } else {
                e.message ?: "Failed to get route"
            }
            onDirectionsUiStateChange(getDirectionsUiState().copy(
                isLoadingRoute = false,
                errorMessage = message,
            ))
        }
    }

    return DrawRouteResult(routePolylines, message)
}


data class DrawRouteResult(
    val polylines: List<Polyline?>,
    val snackBarMessage: String
)