package com.example.campusguide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusguide.R
import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.shuttle.ShuttleSchedule
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
private val ShuttleBlue = Color(0xFF1565C0)

@Composable
fun ShuttleStopInfoCard(
    stop: ShuttleStop,
    isOperational: Boolean = true,
    onDismiss: () -> Unit,
    onDirectionsClick: (() -> Unit)? = null
) {
    var showSchedule by remember { mutableStateOf(false) }

    if (showSchedule) {
        ShuttleScheduleDialog(onDismiss = { showSchedule = false })
        return
    }

    // Compute next departure
    val nextDep = ShuttleSchedule.nextDeparture(stop.campus)
    val nextDepText = if (nextDep != null) {
        "Next departure: $nextDep"
    } else {
        val nextDay = ShuttleSchedule.nextDepartureNextDay(stop.campus)
        if (nextDay != null) "No more departures today.\nNext departure: ${nextDay.first} ${nextDay.second}"
        else "No more departures today."
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ShuttleBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_directions_bus),
                        contentDescription = "Shuttle bus",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                AccessibleText(
                    text = stop.name,
                    baseFontSizeSp = 16f,
                    forceFontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!isOperational) {
                    AccessibleText(
                        text = "Shuttle data unavailable",
                        baseFontSizeSp = 14f,
                        fallbackColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    AccessibleText(
                        text = stop.description,
                        baseFontSizeSp = 14f,
                        fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AccessibleText(
                        text = nextDepText,
                        baseFontSizeSp = 14f,
                        forceFontWeight = FontWeight.SemiBold,
                        fallbackColor = if (nextDep != null)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.error
                    )
                    val campusLabel = when (stop.campus) {
                        Campus.SGW -> "SGW"
                        Campus.LOYOLA -> "Loyola"
                    }
                    AccessibleText(
                        text = "$campusLabel Campus  •  Mon\u2013Fri  •  ~30 min ride",
                        baseFontSizeSp = 12f,
                        fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onDirectionsClick != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onDirectionsClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Directions")
                    }
                }
                if (isOperational) {
                    Button(
                        onClick = { showSchedule = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ShuttleBlue)
                    ) {
                        Text("View schedule")
                    }
                }
                TextButton(onClick = onDismiss) { Text("OK") }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp)
    )
}
