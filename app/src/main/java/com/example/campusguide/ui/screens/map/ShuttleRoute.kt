package com.example.campusguide.ui.screens.map

import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.directions.RouteLeg
import com.example.campusguide.ui.directions.RouteResult
import com.example.campusguide.ui.directions.RouteStep
import com.example.campusguide.ui.directions.TravelMode
import com.example.campusguide.ui.directions.detectCampus
import com.example.campusguide.ui.shuttle.NearestShuttleStopFinder
import com.example.campusguide.ui.shuttle.NearestShuttleStopFinder.distanceBetween
import com.example.campusguide.ui.shuttle.ShuttleTracker
import com.example.campusguide.ui.shuttle.StaticShuttleDataSource
import com.google.android.gms.maps.model.LatLng

fun getShuttleRoute(origin: LatLng, destination: LatLng): RouteResult {

    val sgwToLoyolaPoints = listOf(
        LatLng(45.4971, -73.5785),
        LatLng(45.495758, -73.579364),
        LatLng(45.496600, -73.581724),
        LatLng(45.493592, -73.584449),
        LatLng(45.491200, -73.588075),
        LatLng(45.487650, -73.592367),
        LatLng(45.486838, -73.594427),
        LatLng(45.474034, -73.609190),
        LatLng(45.469996, -73.617102),
        LatLng(45.463751, -73.628883),
        LatLng(45.459161, -73.637273),
        LatLng(45.4579, -73.6389)
    )

    val loyolaToSgwPoints = listOf(
        LatLng(45.4576, -73.6390),
        LatLng(45.463725, -73.629001),
        LatLng(45.461618, -73.623851),
        LatLng(45.462552, -73.621576),
        LatLng(45.463394, -73.617585),
        LatLng(45.468451, -73.610161),
        LatLng(45.471791, -73.606170),
        LatLng(45.472927, -73.605709),
        LatLng(45.475869, -73.602243),
        LatLng(45.475410, -73.601149),
        LatLng(45.484614, -73.589269),
        LatLng(45.484531, -73.588303),
        LatLng(45.488142, -73.582810),
        LatLng(45.490186, -73.578535),
        LatLng(45.495334, -73.573638),
        LatLng(45.497395, -73.578133),
        LatLng(45.4971, -73.5785)
    )

    val shuttlePoints = if (detectCampus(destination) == Campus.LOYOLA) sgwToLoyolaPoints else loyolaToSgwPoints
    val shuttleStop = shuttlePoints.first()
    val arrivalStop = shuttlePoints.last()

    val distanceFromOriginToShuttle = distanceBetween(origin, shuttleStop).toInt()
    val distanceFromArrivalToDestination = distanceBetween(arrivalStop, destination).toInt()
    //Average walking speed ≈ 1.4 m/s (about 5 km/h)
    val durationSecondsOriginToShuttleEstimate = (distanceFromOriginToShuttle/1.4).toInt()
    val durationSecondsArrivalToDestinationEstimate = (distanceFromArrivalToDestination/1.4).toInt()


    val walkToStopStep = RouteStep(
        durationSeconds = durationSecondsOriginToShuttleEstimate,       // ~5 min walk estimate
        distanceMeters = distanceFromOriginToShuttle,
        navigationInstruction = "Walk to shuttle stop",
        travelMode = TravelMode.WALK,
        polyline = listOf(origin, shuttleStop)
    )

    val shuttleStep = RouteStep(
        durationSeconds = 1500,      // ~25 min estimate
        distanceMeters = 7500,       // ~7.5km estimate
        navigationInstruction = "Take the Concordia shuttle",
        travelMode = TravelMode.TRANSIT,
        polyline = shuttlePoints
    )

    val walkFromStopStep = RouteStep(
        durationSeconds = durationSecondsArrivalToDestinationEstimate,       // ~5 min walk estimate
        distanceMeters = distanceFromArrivalToDestination,
        navigationInstruction = "Walk to destination",
        travelMode = TravelMode.WALK,
        polyline = listOf(arrivalStop, destination)
    )

    val leg = RouteLeg(
        durationSeconds = durationSecondsOriginToShuttleEstimate + 2100 + durationSecondsArrivalToDestinationEstimate,      // sum of all steps
        distanceMeters = distanceFromOriginToShuttle + 7500 + distanceFromArrivalToDestination,       // sum of all steps
        steps = listOf(walkToStopStep, shuttleStep, walkFromStopStep)
    )

    val allPoints = listOf(origin) + shuttlePoints + listOf(destination)

    return RouteResult(
        points = allPoints,
        durationSeconds = leg.durationSeconds,
        distanceMeters = leg.distanceMeters,
        legs = listOf(leg),
        isShuttleRoute = true
    )
}

fun canUseShuttle(origin: LatLng, destination: LatLng, mode: TravelMode): Boolean {
    val nearOriginStop = (NearestShuttleStopFinder.find(origin, StaticShuttleDataSource().getShuttleStops())?.distanceMetres?: 0f) < 500
    val nearDestStop = (NearestShuttleStopFinder.find(destination, StaticShuttleDataSource().getShuttleStops())?.distanceMetres?: 0f) < 500
    val isCrossCampus = detectCampus(origin) != detectCampus(destination)
    val isTransit = mode == TravelMode.TRANSIT
    val shuttleRunning = ShuttleTracker().getNextShuttleForCampus(detectCampus(destination)) != null

    return isTransit && nearOriginStop && nearDestStop && isCrossCampus && shuttleRunning
}