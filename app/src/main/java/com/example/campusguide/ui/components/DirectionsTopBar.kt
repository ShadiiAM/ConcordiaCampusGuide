package com.example.campusguide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.campusguide.R
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.directions.RouteLeg
import com.example.campusguide.ui.directions.TravelMode


@Composable
fun DirectionsTopBar(
    modifier: Modifier = Modifier,
    originLabel: String = "Your location",
    destinationLabel: String = "Destination",
    isCrossCampus: Boolean = false,
    selectedMode: TravelMode = TravelMode.DRIVE,
    onModeSelected: (TravelMode) -> Unit = {},
    routeSummary: String? = null,
    showActions: Boolean = false,
    isLoadingRoute: Boolean = false,
    currentSteps: RouteLeg? = null,
    onGoClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val purple = Color(0xFF6B4D8A)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            // Row 1: back arrow + origin/destination
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to search",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onBackClick)
                        .padding(top = 2.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A73E8))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = originLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .size(2.dp, 4.dp)
                                .background(Color.Gray.copy(alpha = 0.5f))
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_poi),
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = destinationLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close directions",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onBackClick)
                )
            }

            // Cross-campus badge
            if (isCrossCampus) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .border(1.5.dp, Color(0xFF9C7ABD), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "This is a cross-campus route",
                        style = MaterialTheme.typography.labelMedium,
                        color = purple,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // Travel mode icons
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Choose your travel mode:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TravelMode.entries.forEach { mode ->
                    val isSelected = mode == selectedMode
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) purple else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onModeSelected(mode) }
                            .semantics { contentDescription = mode.contentDescription },
                        contentAlignment = Alignment.Center,
                    ) {
                        val iconRes = when (mode) {
                            TravelMode.DRIVE -> R.drawable.ic_directions_car
                            TravelMode.TRANSIT -> R.drawable.ic_directions_bus
                            TravelMode.WALK -> R.drawable.ic_directions_walk
                        }
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            // TODO: Implement Concordia Shuttle bus option for cross-campus TRANSIT routes
            routeSummary?.let {
                Spacer(Modifier.height(6.dp))
                AccessibleText(it,
                    baseFontSizeSp = 14f,
                    fallbackColor = Color(0xFF6B4D8A),
                    forceFontWeight = FontWeight.SemiBold)
            }

            if(!showActions){
                //todo potencial modifier for the lazy column
                LazyColumn {
                    items(currentSteps?.steps ?: emptyList()) { index ->
                        if (currentSteps?.steps==null){
                            AccessibleText(
                                "Unable to generate steps for this route.",
                                baseFontSizeSp = 10f,
                                fallbackColor = Color(0xFF6B4D8A)
                            )
                        }
                        else{
                            //todo transit type or direction icon
                            //val iconRes = when (index.transitDetails.transitLine.) {
                            //    TravelMode.DRIVE -> R.drawable.ic_directions_car
                            //    TravelMode.TRANSIT -> R.drawable.ic_directions_bus
                            //    TravelMode.WALK -> R.drawable.ic_directions_walk
                            //    else -> {R.drawable.poi_icon}
                            //
                            //}
                        index.navigationInstruction?.let {
                            AccessibleText(
                                it,
                                baseFontSizeSp = 10f,
                                fallbackColor = Color(0xFF6B4D8A)
                            )
                        }
                            //todo display
                            val seconds = index.durationSeconds

                        }
                    }
                }
            }


            // Go / Cancel
            if (showActions) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onCancelClick)
                    ) {
                        Text("Cancel", modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = purple,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = !isLoadingRoute, onClick = onGoClick)
                            .semantics { contentDescription = "Start navigation" }
                    ) {
                        Text(
                            text = if (isLoadingRoute) "…" else "Go",
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}