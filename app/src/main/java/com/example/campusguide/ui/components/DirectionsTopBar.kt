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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.campusguide.R
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
    errorMessage: String? = null,
    showActions: Boolean = false,
    isLoadingRoute: Boolean = false,
    currentSteps: RouteLeg? = null,
    isPickingOrigin: Boolean = false,
    onOriginClick: () -> Unit = {},
    onMyLocationClick: () -> Unit = {},
    onGoClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
) {
    val purple = Color(0xFF6B4D8A)

    // Local UI state: whether the step-by-step detail panel is open
    var showStepDetails by remember { mutableStateOf(false) }

    // Collapse detail panel whenever a new route loads or steps disappear
    if (currentSteps == null) showStepDetails = false

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            // Origin line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (!isPickingOrigin) Modifier.clickable(onClick = onOriginClick) else Modifier
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPickingOrigin) Color(0xFF1A73E8).copy(alpha = 0.4f)
                            else Color(0xFF1A73E8)
                        )
                )
                Spacer(Modifier.width(8.dp))
                if (isPickingOrigin) {
                    Text(
                        text = "Choose location from map",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onMyLocationClick)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "My Location",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                } else {
                    Text(
                        text = originLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Dotted connector
            repeat(3) {
                Box(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .size(2.dp, 4.dp)
                        .background(Color.Gray.copy(alpha = 0.5f))
                )
                Spacer(Modifier.height(2.dp))
            }

            // Row 3: destination line
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

            // Travel mode chips — hidden while viewing step details
            if (!showStepDetails) {
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
            }

            // Error message
            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            when (selectedMode) {
                                TravelMode.DRIVE -> R.drawable.ic_directions_car
                                TravelMode.TRANSIT -> R.drawable.ic_directions_bus
                                TravelMode.WALK -> R.drawable.ic_directions_walk
                            }
                        ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Unavailable",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── SUMMARY VIEW (route loaded, details collapsed) ──────────────────
            if (!showStepDetails) {
                routeSummary?.let {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                when (selectedMode) {
                                    TravelMode.DRIVE -> R.drawable.ic_directions_car
                                    TravelMode.TRANSIT -> R.drawable.ic_directions_bus
                                    TravelMode.WALK -> R.drawable.ic_directions_walk
                                }
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // "View route details" tonal button — only when steps are available
                if (currentSteps != null && currentSteps.steps.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .width(174.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { showStepDetails = true }
                                .semantics { contentDescription = "View route details" },
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp),
                            ) {
                                Text(
                                    text = "View route details",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }

            // ── STEP DETAIL VIEW ─────────────────────────────────────────────────
            if (showStepDetails && currentSteps != null && currentSteps.steps.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                // "Hide route details" collapse button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .width(174.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .clickable { showStepDetails = false }
                            .semantics { contentDescription = "Hide route details" },
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp),
                        ) {
                            Text(
                                text = "Hide route details",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(currentSteps.steps) { step ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                        ) {
                            // Direction arrow chip (48×48 dp — matches Figma spec)
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(
                                        imageVector = directionIconFor(step.navigationInstruction),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }

                            // Instruction text + distance (Figma: 11sp Medium black / 11sp gray #4D4D4D)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                step.navigationInstruction?.let { instruction ->
                                    Text(
                                        text = instruction,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                    )
                                }
                                step.distanceMeters?.let { m ->
                                    val distanceText = if (m < 1000) "$m m"
                                    else "${"%.1f".format(m / 1000.0)} km"
                                    Text(
                                        text = distanceText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF4D4D4D),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cancel — shown during planning, step details, or when a route is loaded; Go — only during planning
            if (showActions || showStepDetails || routeSummary != null) {
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
                    if (showActions) {
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
}

/** Maps a navigation instruction string to a direction arrow icon. */
private fun directionIconFor(instruction: String?): ImageVector {
    val lower = instruction?.lowercase() ?: return Icons.Default.KeyboardArrowUp
    return when {
        "u-turn" in lower || "uturn" in lower     -> Icons.AutoMirrored.Filled.ArrowBack
        "turn left" in lower || "left onto" in lower
                || "slight left" in lower
                || "keep left" in lower            -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
        "turn right" in lower || "right onto" in lower
                || "slight right" in lower
                || "keep right" in lower           -> Icons.AutoMirrored.Filled.KeyboardArrowRight
        else                                       -> Icons.Default.KeyboardArrowUp
    }
}