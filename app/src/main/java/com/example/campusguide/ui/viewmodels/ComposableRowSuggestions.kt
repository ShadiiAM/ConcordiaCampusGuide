package com.example.campusguide.ui.viewmodels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusguide.R
import com.example.campusguide.UsabilityTrackerIRLUsers
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.Indoor
import com.example.campusguide.data.OutsidePOI
import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.data.Suggestion
import com.example.campusguide.ui.components.getPOIColorAndDrawable
import com.example.campusguide.ui.shuttle.NearestShuttleStopFinder
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

@Composable
fun BuildingRow(
    suggestion: Suggestion,
    nearestId: String?,
    nearestPOIName: String?,

    userLatLng: LatLng?,
    onSuggestionSelected: (Suggestion) -> Unit,
    onIndoorSetAsStart: (Indoor) -> Unit,
    onIndoorSetAsDestination: (Indoor) -> Unit,
) {

    val firebaseAnalytics = Firebase.analytics
    when (suggestion) {
        is CampusBuilding -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(suggestion.buildingName)
                    .clickable {
                        onSuggestionSelected(suggestion)
                        firebaseAnalytics.logEvent("building_suggestion_selected", null)
                        UsabilityTrackerIRLUsers.userInteractionRecord("building_suggestion_selected")

                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = suggestion.buildingCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        suggestion.buildingName,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        suggestion.address,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        is ShuttleStop -> {
            val distance = userLatLng?.let {
                NearestShuttleStopFinder.distanceBetween(it, suggestion.latLng)
            }
            val isNearest = suggestion.id == nearestId

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(suggestion.id)
                    .clickable {
                        onSuggestionSelected(suggestion)
                        firebaseAnalytics.logEvent("shuttle_suggestion_selected", null)
                        UsabilityTrackerIRLUsers.userInteractionRecord("shuttle_suggestion_selected")
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_directions_bus),
                        contentDescription = "shuttle_suggestion_icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isNearest) {
                                NearestBadge()
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                text = suggestion.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = suggestion.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (distance != null) {
                    Text(
                        text = NearestShuttleStopFinder.formatDistance(distance),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        is Indoor -> {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSuggestionSelected(suggestion)
                        firebaseAnalytics.logEvent("indoor_suggestion_selected", null)
                        UsabilityTrackerIRLUsers.userInteractionRecord("indoor_suggestion_selected")

                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .semantics {
                        // Give screen readers a useful announcement.
                        contentDescription = "${suggestion.primaryLabel}, ${suggestion.secondaryLabel}, ${suggestion.tertiaryLabel}"
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(suggestion.primaryLabel, style = MaterialTheme.typography.bodySmall)
                    Text(
                        suggestion.secondaryLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        suggestion.tertiaryLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TextButton(onClick = {
                            onIndoorSetAsStart(suggestion)
                            firebaseAnalytics.logEvent("indoor_set_as_start", null)
                            UsabilityTrackerIRLUsers.userInteractionRecord("indoor_set_as_start")

                        }) {
                            Text("Set as start")
                        }
                        TextButton(onClick = {
                            onIndoorSetAsDestination(suggestion)
                            firebaseAnalytics.logEvent("indoor_set_as_destination", null)
                            UsabilityTrackerIRLUsers.userInteractionRecord("indoor_set_as_destination")
                        }) {
                            Text("Set as destination")
                        }
                    }
                }
            }




        }

        is OutsidePOI ->{
            val iconToUse = getPOIColorAndDrawable(suggestion.category).second
            val isNearest = suggestion.name == nearestPOIName
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(suggestion.name)
                    .clickable {
                        onSuggestionSelected(suggestion)
                        firebaseAnalytics.logEvent("poi_set_as_destination", null)
                        UsabilityTrackerIRLUsers.userInteractionRecord("poi_set_as_destination")
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconToUse),
                        contentDescription = "poiCardIcon",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isNearest) {
                            NearestBadge()
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            suggestion.name,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        suggestion.address,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(thickness = 0.5.dp)

        }
    }
    HorizontalDivider(thickness = 0.5.dp)
}


@Composable
private fun NearestBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(4.dp)
        )
    ) {
        Text(
            text = "Nearest",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}