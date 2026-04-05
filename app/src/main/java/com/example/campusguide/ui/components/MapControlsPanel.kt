package com.example.campusguide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.campusguide.R
import com.example.campusguide.UsabilityTrackerIRLUsers
import com.example.campusguide.ui.screens.map.moveDown
import com.example.campusguide.ui.screens.map.moveLeft
import com.example.campusguide.ui.screens.map.moveRight
import com.example.campusguide.ui.screens.map.moveUp
import com.example.campusguide.ui.screens.map.recenter
import com.example.campusguide.ui.screens.map.zoomIn
import com.example.campusguide.ui.screens.map.zoomOut
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

@Composable
fun BoxScope.MapBottomSearchBar(
    selectedCampus: Campus,
    onCampusSelected: (Campus) -> Unit,
    onBottomSearchClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 16.dp, bottom = 10.dp)
            .ignoreFocusClearOnTouch(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onBottomSearchClick)
                .semantics { contentDescription = "Bottom search button" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        CampusToggle(
            selectedCampus = selectedCampus,
            onCampusSelected = onCampusSelected,
            showIcon = true
        )
    }
}

@Composable
fun BoxScope.MapControlsPanel(
    googleMap: GoogleMap?,
    fusedLocationProviderClient: FusedLocationProviderClient,
    controlsVisible: Boolean,
    onToggleControls: () -> Unit,
) {
    val context = LocalContext.current
    val firebaseAnalytics = Firebase.analytics
    if (controlsVisible) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .testTag("mapControls")
                .semantics { contentDescription = "Map Controls" }
                .padding(end = 16.dp, bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { zoomIn(googleMap)
                    firebaseAnalytics.logEvent("map_controls_zoom_in", null)
                    UsabilityTrackerIRLUsers.userInteractionRecord("map_controls_zoom_in")

                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.zoom_in_button),
                    contentDescription = "Zoom In",
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { moveLeft(googleMap)

                        firebaseAnalytics.logEvent("map_controls_move_left", null)
                        UsabilityTrackerIRLUsers.userInteractionRecord("map_controls_move_left")
                              },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.left_button),
                        contentDescription = "Left",
                        tint = Color.Unspecified,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { moveUp(googleMap)
                            firebaseAnalytics.logEvent("map_controls_move_up", null)
                            UsabilityTrackerIRLUsers.userInteractionRecord("map_controls_move_up")

                        },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.up_button),
                            contentDescription = "Up",
                            tint = Color.Unspecified,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    IconButton(
                        onClick = { recenter(googleMap, fusedLocationProviderClient, context)
                            firebaseAnalytics.logEvent("map_controls_recenter", null)
                            UsabilityTrackerIRLUsers.userInteractionRecord("map_controls_recenter")

                        },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.recenter_button),
                            contentDescription = "Recenter",
                            tint = Color.Unspecified,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    IconButton(
                        onClick = { moveDown(googleMap)
                            firebaseAnalytics.logEvent("map_controls_move_down", null)
                            UsabilityTrackerIRLUsers.userInteractionRecord("map_controls_move_down")

                        },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.down_button),
                            contentDescription = "Down",
                            tint = Color.Unspecified,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                IconButton(
                    onClick = { moveRight(googleMap)
                        firebaseAnalytics.logEvent("map_controls_move_right", null)
                        UsabilityTrackerIRLUsers.userInteractionRecord("map_controls_move_right")

                    },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.right_button),
                        contentDescription = "Right",
                        tint = Color.Unspecified,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            IconButton(
                onClick = { zoomOut(googleMap)

                    firebaseAnalytics.logEvent("map_controls_zoom_out", null)
                    UsabilityTrackerIRLUsers.userInteractionRecord("map_controls_zoom_out")

                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.zoom_out_button),
                    contentDescription = "Zoom Out",
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            IconButton(
                onClick = {onToggleControls()
                    firebaseAnalytics.logEvent("map_controls_toggle_controls_off", null)
                    UsabilityTrackerIRLUsers.userInteractionRecord("map_controls_toggle_controls_off")

                },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.panel_button),
                    contentDescription = "Toggle Controls",
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    } else {
        IconButton(
            onClick = {

                onToggleControls()

                firebaseAnalytics.logEvent("map_controls_toggle_controls_on", null)
                UsabilityTrackerIRLUsers.userInteractionRecord("map_controls_toggle_controls_on")

            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 60.dp)
                .size(50.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.panel_button),
                contentDescription = "Toggle Controls",
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
