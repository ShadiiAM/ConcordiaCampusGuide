package com.example.campusguide.ui.viewmodels

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.campusguide.R
import com.example.campusguide.data.ALL_SUGGESTIONS
import com.example.campusguide.data.CampusBuilding
import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.data.Suggestion
import com.example.campusguide.data.fullSuggestions
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.shuttle.NearestShuttleStopFinder
import com.google.android.gms.maps.model.LatLng

class MapSearchViewModel : ViewModel() {

    var topBarSuggestions by mutableStateOf<List<Suggestion>>(emptyList())
    var topBarSelectedSuggestion by mutableStateOf<Suggestion?>(null)
    var searchQuery by mutableStateOf("")
    var searchCounter by mutableIntStateOf(0)

    val suggestionKey: (Suggestion) -> Any = { suggestion ->
        when (suggestion) {
            is CampusBuilding -> suggestion.buildingCode
            is ShuttleStop -> suggestion.id
        }
    }

    fun navigateToMapWithSuggestion(suggestion: Suggestion) {
        topBarSelectedSuggestion = suggestion
        topBarSuggestions = emptyList()
    }

    fun onSearchQueryChange(query: String) {
        topBarSuggestions = fullSuggestions(
            query = query,
            activeCampus = Campus.SGW,
            crossCampus = true,
        )
    }

    fun onSearchSubmit(query: String) {
        val match = ALL_SUGGESTIONS.firstOrNull { it.matches(query) }
        topBarSuggestions = emptyList()
        if (match != null) {
            navigateToMapWithSuggestion(match)
        } else {
            searchQuery = query
            searchCounter++
        }
    }

    fun onSuggestionSelected(suggestion: Suggestion) {
        topBarSelectedSuggestion = suggestion
        topBarSuggestions = emptyList()
        searchQuery = ""
    }


    @Composable
    fun BuildingRow(suggestion: Suggestion, nearestId: String?, userLatLng: LatLng?) {
        when (suggestion) {
            is CampusBuilding -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(suggestion.buildingName)
                        .clickable {
                            searchQuery = ""
                            onSuggestionSelected(suggestion)
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
                            searchQuery = ""
                            onSuggestionSelected(suggestion)
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
        }
        HorizontalDivider(thickness = 0.5.dp)
    }

}