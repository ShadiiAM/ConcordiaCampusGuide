package com.example.campusguide.ui.viewmodels

import com.example.campusguide.data.CourseSchedule

sealed class ScheduleUiState {
    object Idle : ScheduleUiState()
    object Loading : ScheduleUiState()
    data class Success(val courses: List<CourseSchedule>) : ScheduleUiState()
    data class Error(val message: String) : ScheduleUiState()
}
