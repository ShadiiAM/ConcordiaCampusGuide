package com.example.campusguide.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusguide.ui.directions.DirectionsStep
import com.example.campusguide.ui.directions.DirectionsUiState
import com.example.campusguide.ui.directions.GoogleRoutesRepository
import com.example.campusguide.ui.directions.RouteRequest
import com.example.campusguide.ui.map.utils.BuildingHit
import com.example.campusguide.ui.map.utils.BuildingLocator
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun DirectionsScreen(
    modifier: Modifier = Modifier,
    onMapReady: ((GoogleMap) -> Unit)? = null,
    onPolygonClick: ((LatLng, BuildingHit?) -> Unit)? = null
) {
    val repo = remember { GoogleRoutesRepository() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current


    var defaultOrigin by remember { mutableStateOf(LatLng(45.4972, -73.5789)) }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) return@LaunchedEffect

        val fused = LocationServices.getFusedLocationProviderClient(context)
        runCatching {
            fused.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        defaultOrigin = LatLng(loc.latitude, loc.longitude)
                    }
                }
        }.onFailure {
        }
    }

    var uiState by remember { mutableStateOf(DirectionsUiState()) }

    // while planning route, allow "tap to change origin" behavior
    var isPickingOrigin by remember { mutableStateOf(false) }

    // route drawing
    var routePolylineRef by remember {
        mutableStateOf<com.google.android.gms.maps.model.Polyline?>(null)
    }
    var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }

    // Check location permission
    val hasLocationPermission = remember {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        fineGranted || coarseGranted
    }

    Box(modifier = modifier.fillMaxSize()) {
        MapScreen(
            onMapReady = { map ->
                googleMapRef = map
                onMapReady?.invoke(map)
            },
            onPolygonClick = { latLng, buildingInfo ->
                // Convert BuildingInfo to BuildingHit
                val buildingHit = buildingInfo?.let { info ->
                    BuildingHit(
                        id = info.buildingCode,
                        properties = org.json.JSONObject().apply {
                            put("building-code", info.buildingCode)
                            put("building-name", info.buildingName)
                            put("address", info.address)
                        }
                    )
                }

                onPolygonClick?.invoke(latLng, buildingHit)

                when (val step = uiState.step) {
                    is DirectionsStep.PickDestination -> {
                        uiState = uiState.copy(
                            step = DirectionsStep.ConfirmDestination(latLng, buildingHit),
                            errorMessage = null
                        )
                    }

                    is DirectionsStep.PlanRoute -> {
                        if (isPickingOrigin) {
                            uiState = uiState.copy(
                                step = step.copy(origin = latLng),
                                errorMessage = null
                            )
                            isPickingOrigin = false
                        }
                    }

                    is DirectionsStep.ConfirmDestination,
                    is DirectionsStep.ShowingRoute -> {
                        // ignore taps in these steps for now
                    }
                }
            }
        )

        // Bottom panel depending on step
        when (val step = uiState.step) {
            is DirectionsStep.PickDestination -> {

            }

            is DirectionsStep.ConfirmDestination -> {
                val title = buildingTitle(step.buildingHit, step.destination)
                val snippet = buildingSnippet(step.buildingHit)

                AlertDialog(
                    onDismissRequest = {
                        uiState = uiState.copy(step = DirectionsStep.PickDestination)
                    },
                    title = { Text(title) },
                    text = { Text(snippet) },
                    confirmButton = {
                        Button(onClick = {
                            uiState = uiState.copy(
                                step = DirectionsStep.PlanRoute(
                                    origin = defaultOrigin,
                                    destination = step.destination,
                                    buildingHit = step.buildingHit
                                )
                            )
                        }) {
                            Text("Directions")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = {
                            uiState = uiState.copy(step = DirectionsStep.PickDestination)
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            is DirectionsStep.PlanRoute -> {
                BottomCard {
                    Text(
                        text = "Route options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("From:", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = { isPickingOrigin = true }) {
                            Text(
                                if (isPickingOrigin)
                                    "Tap map to choose origin..."
                                else
                                    latLngShort(step.origin)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("To:", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(8.dp))
                        Text(buildingTitle(step.buildingHit, step.destination))
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            uiState = uiState.copy(
                                isLoadingRoute = true,
                                errorMessage = null
                            )

                            scope.launch {
                                runCatching {
                                    repo.getRoute(
                                        RouteRequest(
                                            origin = step.origin,
                                            destination = step.destination
                                        )
                                    )
                                }.onSuccess { route ->
                                    val map = googleMapRef
                                    if (map != null) {
                                        routePolylineRef?.remove()
                                        routePolylineRef = map.addPolyline(
                                            PolylineOptions()
                                                .addAll(route.points)
                                                .color(0xFF1565C0.toInt())
                                                .width(12f)
                                        )
                                    }

                                    uiState = uiState.copy(
                                        isLoadingRoute = false,
                                        step = DirectionsStep.ShowingRoute(
                                            origin = step.origin,
                                            destination = step.destination,
                                            buildingHit = step.buildingHit,
                                            route = route
                                        )
                                    )
                                }.onFailure { e ->
                                    uiState = uiState.copy(
                                        isLoadingRoute = false,
                                        errorMessage = e.message ?: "Failed to get route"
                                    )
                                }
                            }
                        }
                    ) {
                        Text(if (uiState.isLoadingRoute) "Loading..." else "Go")
                    }

                    uiState.errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            is DirectionsStep.ShowingRoute -> {
                BottomCard {
                    Text(
                        text = "Directions ready",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("From: ${latLngShort(step.origin)}")
                    Text("To: ${buildingTitle(step.buildingHit, step.destination)}")

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            uiState = uiState.copy(
                                step = DirectionsStep.PlanRoute(
                                    origin = step.origin,
                                    destination = step.destination,
                                    buildingHit = step.buildingHit
                                )
                            )
                        }
                    ) {
                        Text("Edit")
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            routePolylineRef?.remove()
                            routePolylineRef = null
                            uiState = uiState.copy(
                                step = DirectionsStep.PickDestination,
                                errorMessage = null
                            )
                        }
                    ) {
                        Text("Clear")
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomCard(
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                content()
            }
        }
    }
}

private fun buildingTitle(
    hit: BuildingHit?,
    fallback: LatLng
): String {
    val props = hit?.properties
    val name = props
        ?.optString("building-name")
        ?.takeIf { it.isNotBlank() }

    return name ?: "Destination (${latLngShort(fallback)})"
}

private fun buildingSnippet(hit: BuildingHit?): String {
    if (hit?.properties == null) {
        return "No building info found for this location."
    }

    val props = hit.properties
    val code = props.optString("building-code").takeIf { it.isNotBlank() }
    val address = props.optString("address").takeIf { it.isNotBlank() }

    return buildString {
        append("Selected building")
        if (code != null) append(" • Code: $code")
        if (address != null) append("\n$address")
    }
}

private fun latLngShort(p: LatLng): String =
    "%.5f, %.5f".format(p.latitude, p.longitude)

private fun getSavedCampusFromPrefs(context: android.content.Context): com.example.campusguide.ui.components.Campus {
    val prefs = context.getSharedPreferences("campus_preferences", android.content.Context.MODE_PRIVATE)
    val savedCampusName = prefs.getString("selected_campus", com.example.campusguide.ui.components.Campus.SGW.name)
    return try {
        com.example.campusguide.ui.components.Campus.valueOf(savedCampusName ?: com.example.campusguide.ui.components.Campus.SGW.name)
    } catch (e: IllegalArgumentException) {
        com.example.campusguide.ui.components.Campus.SGW
    }
}