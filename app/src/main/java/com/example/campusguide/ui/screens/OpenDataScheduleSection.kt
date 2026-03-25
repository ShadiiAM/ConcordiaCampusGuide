package com.example.campusguide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusguide.R
import com.example.campusguide.data.CourseSchedule
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.viewmodels.ScheduleUiState
import com.example.campusguide.ui.viewmodels.ScheduleViewModel

@Composable
fun OpenDataScheduleSection(viewModel: ScheduleViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (uiState) {
            is ScheduleUiState.Idle -> {
                Button(
                    onClick = { viewModel.loadSchedule("*", "2264") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(stringResource(R.string.open_data_load_schedule))
                }
            }
            is ScheduleUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics { 
                            contentDescription = "Loading schedule data" 
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.open_data_loading))
                }
            }
            is ScheduleUiState.Success -> {
                val courses = (uiState as ScheduleUiState.Success).courses
                if (courses.isEmpty()) {
                    Text(stringResource(R.string.open_data_empty))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(courses) { course ->
                            CourseItemCard(course)
                        }
                    }
                }
            }
            is ScheduleUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = (uiState as ScheduleUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics { 
                            contentDescription = "Error message" 
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadSchedule("*", "2264") }) {
                        Text(stringResource(R.string.open_data_retry))
                    }
                }
            }
        }
    }
}

@Composable
fun CourseItemCard(course: CourseSchedule) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AccessibleText(
                text = "${course.subject} ${course.catalog} (${course.section})",
                baseFontSizeSp = 16f,
                forceFontWeight = FontWeight.Bold
            )
            AccessibleText(
                text = "${course.classDays ?: ""} ${course.classStartTime ?: ""} - ${course.classEndTime ?: ""}",
                baseFontSizeSp = 14f
            )
            AccessibleText(
                text = "${course.buildingCode ?: ""} ${course.roomNumber ?: ""} (${course.campus ?: ""})",
                baseFontSizeSp = 14f
            )
        }
    }
}
