package com.example.campusguide.ui.directions

import com.example.campusguide.BuildConfig
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
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
                .header("X-Goog-FieldMask", "routes.polyline.encodedPolyline")
                .header("X-Goog-Api-Key", apiKey)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    val err = resp.body?.string()
                    throw IllegalStateException("Routes API error ${resp.code}: ${err ?: resp.message}")
                }
                val text = resp.body?.string() ?: throw IllegalStateException("Empty response")
                val decoded = json.decodeFromString(ComputeRoutesResponse.serializer(), text)

                val encoded = decoded.routes
                    ?.firstOrNull()
                    ?.polyline
                    ?.encodedPolyline
                    ?.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("No route polyline returned")

                val pts = PolyUtil.decode(encoded)
                RouteResult(points = pts)
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
    val routes: List<Route> ?= null,
)

@Serializable
private data class Route(
    val polyline: RoutePolyline? = null,
)

@Serializable
private data class RoutePolyline(
    @SerialName("encodedPolyline")
    val encodedPolyline: String? = null,
)
