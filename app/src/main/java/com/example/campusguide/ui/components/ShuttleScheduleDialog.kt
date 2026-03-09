package com.example.campusguide.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusguide.ui.shuttle.ShuttleSchedule

@Composable
fun ShuttleScheduleDialog(onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Shuttle schedule", fontWeight = FontWeight.Normal, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
            ) {
                // Scroll progress indicator
                if (scrollState.maxValue > 0) {
                    LinearProgressIndicator(
                        progress = { scrollState.value.toFloat() / scrollState.maxValue },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                Text(
                    "Winter departure times",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Schedule in effect: Jan. 12 – Apr. 15, 2026",
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "The following departure times are approximate and may vary due to unexpected circumstances, traffic and weather.",
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                ScheduleSection(
                    title = "Monday — Thursday",
                    loyolaTimes = ShuttleSchedule.loyolaMonThur.map { it.toString() },
                    sgwTimes = ShuttleSchedule.sgwMonThur.map { it.toString() }
                )

                Spacer(Modifier.height(16.dp))

                ScheduleSection(
                    title = "Friday",
                    loyolaTimes = ShuttleSchedule.loyolaFriday.map { it.toString() },
                    sgwTimes = ShuttleSchedule.sgwFriday.map { it.toString() }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@Composable
private fun ScheduleSection(
    title: String,
    loyolaTimes: List<String>,
    sgwTimes: List<String>
) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth()) {
        // LOY column
        Column(Modifier.weight(1f)) {
            Text(
                "LOY departures",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(4.dp))
            loyolaTimes.forEach { time ->
                Text(
                    time,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        // SGW column
        Column(Modifier.weight(1f)) {
            Text(
                "SGW departures",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(4.dp))
            sgwTimes.forEach { time ->
                Text(
                    time,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}