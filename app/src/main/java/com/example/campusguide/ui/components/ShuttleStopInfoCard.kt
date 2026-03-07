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

private val ShuttleBlue = Color(0xFF1565C0)

@Composable
fun ShuttleStopInfoCard(
    stop: ShuttleStop,
    isOperational: Boolean = true,
    onDismiss: () -> Unit
) {
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
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp)
    )
}
