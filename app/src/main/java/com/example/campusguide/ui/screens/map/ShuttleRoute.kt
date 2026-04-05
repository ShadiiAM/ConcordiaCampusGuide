package com.example.campusguide.ui.screens.map

import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.directions.GoogleRoutesRepository
import com.example.campusguide.ui.directions.RouteLeg
import com.example.campusguide.ui.directions.RouteRequest
import com.example.campusguide.ui.directions.RouteResult
import com.example.campusguide.ui.directions.RouteStep
import com.example.campusguide.ui.directions.TravelMode
import com.example.campusguide.ui.directions.detectCampus
import com.example.campusguide.ui.shuttle.DepartureResult
import com.example.campusguide.ui.shuttle.NearestShuttleStopFinder
import com.example.campusguide.ui.shuttle.NearestShuttleStopFinder.distanceBetween
import com.example.campusguide.ui.shuttle.ShuttleSchedule
import com.example.campusguide.ui.shuttle.StaticShuttleDataSource
import com.google.android.gms.maps.model.LatLng

suspend fun getShuttleRoute(origin: LatLng, destination: LatLng, repo: GoogleRoutesRepository, departure: DepartureResult.Soon): RouteResult {

    val sgwToLoyolaPoints = listOf(
        LatLng(45.4971, -73.5785), //shuttle stop LatLng


        LatLng(45.4963463, -73.5790519),
        LatLng(45.496147, -73.5791968),
        LatLng(45.495944, -73.5792665),
        LatLng(45.4957484, -73.5792826),
        LatLng(45.4961169, -73.5801463),
        LatLng(45.4964591, -73.5812084),
        LatLng(45.496617, -73.5816966),
        LatLng(45.4964715, -73.5819302),
        LatLng(45.4953472, -73.5828797),
        LatLng(45.4941025, -73.5839687),
        LatLng(45.4936776, -73.5843656),
        LatLng(45.4932, -73.5850684),
        LatLng(45.492557, -73.5861573),
        LatLng(45.4920681, -73.5869727),
        LatLng(45.4914175, -73.5878471),
        LatLng(45.4905118, -73.5888634),
        LatLng(45.4896995, -73.5897485),
        LatLng(45.4891267, -73.5903924),
        LatLng(45.4883181, -73.5914277),
        LatLng(45.4878894, -73.5919481),
        LatLng(45.4876901, -73.5923343),
        LatLng(45.4873366, -73.5932838),
        LatLng(45.4870432, -73.5940885),
        LatLng(45.4868101, -73.5944908),
        LatLng(45.4861105, -73.5953223),
        LatLng(45.4856893, -73.5958158),
        LatLng(45.4849033, -73.5966956),
        LatLng(45.4840082, -73.5977202),
        LatLng(45.4833161, -73.5985302),
        LatLng(45.4820674, -73.5999679),
        LatLng(45.4811422, -73.60103),
        LatLng(45.4802846, -73.6020439),
        LatLng(45.4791966, -73.6032334),
        LatLng(45.4775942, -73.6050573),
        LatLng(45.4757661, -73.6071923),
        LatLng(45.4747393, -73.6084082),
        LatLng(45.4740397, -73.6092128),
        LatLng(45.4738666, -73.6094703),
        LatLng(45.473009, -73.6111977),
        LatLng(45.4725274, -73.6121525),
        LatLng(45.4717074, -73.613719),
        LatLng(45.4710227, -73.6150279),
        LatLng(45.4699242, -73.6172058),
        LatLng(45.468773, -73.6194696),
        LatLng(45.4679379, -73.6210575),
        LatLng(45.466892, -73.623096),
        LatLng(45.4657483, -73.6251559),
        LatLng(45.4649356, -73.6267867),
        LatLng(45.4637618, -73.6289324),
        LatLng(45.4621815, -73.6319043),
        LatLng(45.4606916, -73.6347797),
        LatLng(45.4596079, -73.636743),
        LatLng(45.4587952, -73.6377515),
        LatLng(45.4581831, -73.6385016),


        LatLng(45.4579, -73.6389) //shuttle stop LatLng
    )

    val loyolaToSgwPoints = listOf(
        LatLng(45.4576, -73.6390), //shuttle stop LatLng

        LatLng(45.4592798, -73.6370917),
        LatLng(45.4599119, -73.6362065),
        LatLng(45.4613831, -73.6333849),
        LatLng(45.4630913, -73.6301126),
        LatLng(45.4636707, -73.6289968),
        LatLng(45.4632192, -73.6279507),
        LatLng(45.4621695, -73.625585),
        LatLng(45.4615637, -73.6242439),
        LatLng(45.4614546, -73.6240883),
        LatLng(45.461733, -73.6235948),
        LatLng(45.4618948, -73.6233588),
        LatLng(45.4623764, -73.6220552),
        LatLng(45.4629032, -73.6195286),
        LatLng(45.4631553, -73.6182411),
        LatLng(45.4633848, -73.6174043),
        LatLng(45.4642463, -73.6157413),
        LatLng(45.4648784, -73.6147489),
        LatLng(45.4650364, -73.614368),
        LatLng(45.465202, -73.6136653),
        LatLng(45.465296, -73.6129035),
        LatLng(45.4653788, -73.6124744),
        LatLng(45.4656271, -73.6118092),
        LatLng(45.4660071, -73.6111118),
        LatLng(45.4662178, -73.6107309),
        LatLng(45.4669477, -73.6124368),
        LatLng(45.4675571, -73.6114658),
        LatLng(45.4680725, -73.6107899),
        LatLng(45.4686707, -73.6098994),
        LatLng(45.4697391, -73.6084725),
        LatLng(45.4703072, -73.6076732),
        LatLng(45.4712401, -73.6063911),
        LatLng(45.471684, -73.6059888),
        LatLng(45.472711, -73.6057474),
        LatLng(45.4730044, -73.6055543),
        LatLng(45.473896, -73.6045726),
        LatLng(45.4753781, -73.6027755),
        LatLng(45.4758106, -73.6022605),
        LatLng(45.475427, -73.6013164),
        LatLng(45.4754307, -73.6009462),
        LatLng(45.475615, -73.6003347),
        LatLng(45.4758896, -73.5997982),
        LatLng(45.4762131, -73.5995032),
        LatLng(45.4771498, -73.5983391),
        LatLng(45.4779472, -73.5973628),
        LatLng(45.4781691, -73.5970409),
        LatLng(45.4784738, -73.5963972),
        LatLng(45.4790079, -73.5957427),
        LatLng(45.4793953, -73.5953029),
        LatLng(45.4808321, -73.5935809),
        LatLng(45.4820508, -73.5921432),
        LatLng(45.4838486, -73.5900618),
        LatLng(45.4845406, -73.5892893),
        LatLng(45.4844579, -73.5887958),
        LatLng(45.4845256, -73.5883023),
        LatLng(45.4846534, -73.5880341),
        LatLng(45.4849618, -73.5877551),
        LatLng(45.4857667, -73.5870041),
        LatLng(45.4862255, -73.5863282),
        LatLng(45.486737, -73.5854591),
        LatLng(45.4872861, -73.5844614),
        LatLng(45.4880533, -73.5831632),
        LatLng(45.4885271, -73.5817362),
        LatLng(45.4896177, -73.5795261),
        LatLng(45.4903849, -73.5782172),
        LatLng(45.4908738, -73.5777129),
        LatLng(45.4924607, -73.5762431),
        LatLng(45.4943184, -73.5744514),
        LatLng(45.4958676, -73.5729493),
        LatLng(45.4959879, -73.5732283),
        LatLng(45.4972213, -73.5757495),
        LatLng(45.4980635, -73.5775359),

        LatLng(45.4971, -73.5785) //shuttle stop LatLng
    )

    val shuttlePoints = if (detectCampus(destination) == Campus.LOYOLA) sgwToLoyolaPoints else loyolaToSgwPoints
    val shuttleStop = shuttlePoints.first()
    val arrivalStop = shuttlePoints.last()

    val distanceFromOriginToShuttle = distanceBetween(origin, shuttleStop).toInt()
    val distanceFromArrivalToDestination = distanceBetween(arrivalStop, destination).toInt()
    //Average walking speed ≈ 1.4 m/s (about 5 km/h)
    val durationSecondsOriginToShuttleEstimate = (distanceFromOriginToShuttle/1.4).toInt()
    val durationSecondsArrivalToDestinationEstimate = (distanceFromArrivalToDestination/1.4).toInt()

    val walkToShuttleLeg = runCatching {
        repo.getRoute(RouteRequest(origin = origin, destination = shuttleStop, mode = TravelMode.WALK))
    }.fold(
        onSuccess = { result ->
            RouteLeg(
                durationSeconds = result.durationSeconds,
                distanceMeters = result.distanceMeters,
                steps = result.legs.flatMap { it.steps }  // all steps, single leg
            )
        },
        onFailure = {
            RouteLeg(
                durationSeconds = durationSecondsOriginToShuttleEstimate,
                distanceMeters = distanceFromOriginToShuttle,
                steps = listOf(RouteStep(
                    durationSeconds = durationSecondsOriginToShuttleEstimate,
                    distanceMeters = distanceFromOriginToShuttle,
                    navigationInstruction = "Walk to shuttle stop",
                    travelMode = TravelMode.WALK,
                    polyline = listOf(origin, shuttleStop)
                ))
            )
        }
    )

    val walkFromShuttleLeg = runCatching {
        repo.getRoute(RouteRequest(origin = arrivalStop, destination = destination, mode = TravelMode.WALK))
    }.fold(
        onSuccess = { result ->
            RouteLeg(
                durationSeconds = result.durationSeconds,
                distanceMeters = result.distanceMeters,
                steps = result.legs.flatMap { it.steps }
            )
        },
        onFailure = {
            RouteLeg(
                durationSeconds = durationSecondsArrivalToDestinationEstimate,
                distanceMeters = distanceFromArrivalToDestination,
                steps = listOf(RouteStep(
                    durationSeconds = durationSecondsArrivalToDestinationEstimate,
                    distanceMeters = distanceFromArrivalToDestination,
                    navigationInstruction = "Walk to destination",
                    travelMode = TravelMode.WALK,
                    polyline = listOf(arrivalStop, destination)
                ))
            )
        }
    )


    val shuttleStep = RouteStep(
        durationSeconds = 1500,      // ~25 min estimate
        distanceMeters = 7500,       // ~7.5km estimate
        navigationInstruction = "Take the ${departure.departure} Concordia shuttle",
        travelMode = TravelMode.TRANSIT,
        polyline = shuttlePoints
    )

    val shuttleLeg = RouteLeg(
        durationSeconds = 1500,
        distanceMeters = 7500,
        steps = listOf(shuttleStep)
    )

    val allPoints = walkToShuttleLeg.steps.flatMap { it.polyline } +
            shuttlePoints +
            walkFromShuttleLeg.steps.flatMap { it.polyline }
    val allSteps = walkToShuttleLeg.steps + shuttleLeg.steps + walkFromShuttleLeg.steps

    val fullRouteLeg = RouteLeg(
        durationSeconds = (walkToShuttleLeg.durationSeconds ?: 0) + (shuttleLeg.durationSeconds ?: 0) + (walkFromShuttleLeg.durationSeconds ?: 0),
        distanceMeters = (walkToShuttleLeg.distanceMeters ?: 0) + (shuttleLeg.distanceMeters ?: 0) + (walkFromShuttleLeg.distanceMeters ?: 0),
        steps = allSteps
    )

    return RouteResult(
        points = allPoints,
        durationSeconds = (walkToShuttleLeg.durationSeconds ?: 0) + (shuttleLeg.durationSeconds ?: 0) + (walkFromShuttleLeg.durationSeconds ?: 0),
        distanceMeters = (walkToShuttleLeg.distanceMeters ?: 0) + (shuttleLeg.distanceMeters ?: 0) + (walkFromShuttleLeg.distanceMeters ?: 0),
        legs = listOf(fullRouteLeg),
        isShuttleRoute = true
    )
}

fun canUseShuttle(origin: LatLng, destination: LatLng, mode: TravelMode): DepartureResult.Soon? {
    val nearOriginStop = (NearestShuttleStopFinder.find(origin, StaticShuttleDataSource().getShuttleStops())?.distanceMetres?: 0f) < 500
    val nearDestStop = (NearestShuttleStopFinder.find(destination, StaticShuttleDataSource().getShuttleStops())?.distanceMetres?: 0f) < 500
    val isCrossCampus = detectCampus(origin) != detectCampus(destination)
    val isTransit = mode == TravelMode.TRANSIT
    val departure = ShuttleSchedule.nextDeparture(detectCampus(destination)) as? DepartureResult.Soon

    return if (isTransit && nearOriginStop && nearDestStop && isCrossCampus && departure != null) departure else null
}