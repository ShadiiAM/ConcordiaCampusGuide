package com.example.campusguide.ui.directions

import com.example.campusguide.BuildConfig
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Google Routes API implementation.
 * Docs: https://developers.google.com/maps/documentation/routes
 */
class GoogleRoutesRepository(
    private val client: OkHttpClient = OkHttpClient(),
    private val apiKey: String = BuildConfig.MAPS_API_KEY,
) : DirectionsRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    override suspend fun getRoute(request: RouteRequest): RouteResult = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://routes.googleapis.com/directions/v2:computeRoutes"

            val bodyObj = ComputeRoutesRequest(
                origin = Waypoint(Location(LatLngLiteral(request.origin.latitude, request.origin.longitude))),
                destination = Waypoint(Location(LatLngLiteral(request.destination.latitude, request.destination.longitude))),
                travelMode = "WALK",
                polylineEncoding = "ENCODED_POLYLINE",
                polylineQuality = "OVERVIEW",
            )

            val bodyStr = json.encodeToString(ComputeRoutesRequest.serializer(), bodyObj)
            val req = Request.Builder()
                .url(url)
                .post(bodyStr.toRequestBody("application/json".toMediaType()))
                .header("X-Goog-FieldMask",
                    "routes.duration," +
                    "routes.distanceMeters," +
                    "routes.polyline.encodedPolyline," +
                    "routes.legs.duration," +
                    "routes.legs.distanceMeters," +
                    "routes.legs.steps.distanceMeters," +
                    "routes.legs.steps.staticDuration," +
                    "routes.legs.steps.navigationInstruction," +
                    "routes.legs.steps.transitDetails")
                .header("X-Goog-Api-Key", apiKey)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    val err = resp.body?.string()
                    throw IllegalStateException("Routes API error ${resp.code}: ${err ?: resp.message}")
                }
                val text = resp.body?.string() ?: throw IllegalStateException("Empty response")
                val decoded = json.decodeFromString(ComputeRoutesResponse.serializer(), text)

                val route = decoded.routes
                    ?.firstOrNull()
                    ?: throw IllegalStateException("No route returned")

                val encoded = route.polyline
                    ?.encodedPolyline
                    ?.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("No route polyline returned")

                val pts = PolyUtil.decode(encoded)

                // Parse duration (e.g., "123s" -> 123)
                val durationSeconds = route.duration?.removeSuffix("s")?.toIntOrNull()

                // Parse legs
                val legs = route.legs?.map { leg ->
                    val legDuration = leg.duration?.removeSuffix("s")?.toIntOrNull()
                    val steps = leg.steps?.map { step ->
                        val stepDuration = step.staticDuration?.removeSuffix("s")?.toIntOrNull()
                        val transitDetails = step.transitDetails?.let { td ->
                            TransitDetails(
                                stopDetails = td.stopDetails?.let { sd ->
                                    TransitStopDetails(
                                        arrivalStop = sd.arrivalStop?.let { stop ->
                                            TransitStop(
                                                name = stop.name,
                                                location = stop.location?.latLng?.let {
                                                    LatLng(it.latitude, it.longitude)
                                                }
                                            )
                                        },
                                        departureStop = sd.departureStop?.let { stop ->
                                            TransitStop(
                                                name = stop.name,
                                                location = stop.location?.latLng?.let {
                                                    LatLng(it.latitude, it.longitude)
                                                }
                                            )
                                        }
                                    )
                                },
                                localizedValues = td.localizedValues?.let { lv ->
                                    TransitLocalizedValues(
                                        arrivalTime = lv.arrivalTime?.text,
                                        departureTime = lv.departureTime?.text
                                    )
                                },
                                headsign = td.headsign,
                                transitLine = td.transitLine?.let { tl ->
                                    TransitLine(
                                        name = tl.name,
                                        shortName = tl.shortName,
                                        color = tl.color,
                                        vehicle = tl.vehicle?.let { v ->
                                            TransitVehicle(
                                                name = v.name,
                                                type = v.type
                                            )
                                        }
                                    )
                                },
                                stopCount = td.stopCount
                            )
                        }
                        RouteStep(
                            durationSeconds = stepDuration,
                            distanceMeters = step.distanceMeters,
                            navigationInstruction = step.navigationInstruction?.instructions,
                            transitDetails = transitDetails
                        )
                    } ?: emptyList()

                    RouteLeg(
                        durationSeconds = legDuration,
                        distanceMeters = leg.distanceMeters,
                        steps = steps
                    )
                } ?: emptyList()

                RouteResult(
                    points = pts,
                    durationSeconds = durationSeconds,
                    distanceMeters = route.distanceMeters,
                    legs = legs
                )
            }
        }.getOrElse { t ->
            throw RuntimeException(t.toUserFriendlyMessage(), t)
        }
    }

    private fun Throwable.toUserFriendlyMessage(): String = when (this) {
        is UnknownHostException ->
            "Unable to resolve host (DNS). Your emulator/device can’t reach Google right now. " +
                "Check Wi‑Fi, Private DNS settings, VPN/firewall, or try a cold boot/wipe of the emulator."
        is SocketTimeoutException ->
            "Network timed out while contacting Google. Check your internet/VPN/firewall and try again."
        is IOException -> message ?: "Network error while calling Routes API."
        else -> message ?: "Unexpected error while calling Routes API."
    }
}

@Serializable
private data class ComputeRoutesRequest(
    val origin: Waypoint,
    val destination: Waypoint,
    val travelMode: String,
    // See https://developers.google.com/maps/documentation/routes/reference/rest/v2/ComputeRoutesRequest
    val polylineEncoding: String? = null,
    val polylineQuality: String? = null,
)

@Serializable
private data class Waypoint(
    val location: Location,
)

@Serializable
private data class Location(
    val latLng: LatLngLiteral,
)

@Serializable
private data class LatLngLiteral(
    val latitude: Double,
    val longitude: Double,
)

@Serializable
private data class ComputeRoutesResponse(
    val routes: List<Route>? = null,
)

@Serializable
private data class Route(
    val polyline: RoutePolyline? = null,
    val duration: String? = null,  // e.g., "123s"
    val distanceMeters: Int? = null,
    val legs: List<ApiRouteLeg>? = null,
)

@Serializable
private data class RoutePolyline(
    @SerialName("encodedPolyline")
    val encodedPolyline: String? = null,
)

@Serializable
private data class ApiRouteLeg(
    val duration: String? = null,
    val distanceMeters: Int? = null,
    val steps: List<ApiRouteStep>? = null,
)

@Serializable
private data class ApiRouteStep(
    val distanceMeters: Int? = null,
    val staticDuration: String? = null,
    val navigationInstruction: NavigationInstruction? = null,
    val transitDetails: ApiTransitDetails? = null,
)

@Serializable
private data class NavigationInstruction(
    val instructions: String? = null,
)

@Serializable
private data class ApiTransitDetails(
    val stopDetails: ApiTransitStopDetails? = null,
    val localizedValues: ApiTransitLocalizedValues? = null,
    val headsign: String? = null,
    val transitLine: ApiTransitLine? = null,
    val stopCount: Int? = null,
)

@Serializable
private data class ApiTransitStopDetails(
    val arrivalStop: ApiTransitStop? = null,
    val departureStop: ApiTransitStop? = null,
)

@Serializable
private data class ApiTransitStop(
    val name: String? = null,
    val location: Location? = null,
)

@Serializable
private data class ApiTransitLocalizedValues(
    val arrivalTime: LocalizedText? = null,
    val departureTime: LocalizedText? = null,
)

@Serializable
private data class LocalizedText(
    val text: String? = null,
)

@Serializable
private data class ApiTransitLine(
    val name: String? = null,
    val shortName: String? = null,
    val color: String? = null,
    val vehicle: ApiTransitVehicle? = null,
)

@Serializable
private data class ApiTransitVehicle(
    val name: String? = null,
    val type: String? = null,
)

