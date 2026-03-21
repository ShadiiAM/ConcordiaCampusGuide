package com.example.campusguide.ui.viewmodels

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusguide.data.CalendarRepository
import com.example.campusguide.data.Course
import com.example.campusguide.ui.screens.CalendarTab
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Calendar

sealed class CalendarError {
    object NotFound : CalendarError()
    object Network : CalendarError()
    data class Unknown(val message: String) : CalendarError()
}

data class CalendarUiState(
    val isLoading: Boolean = false,
    val error: CalendarError? = null,
    val lastAddedCourses: List<Course>? = null,
    val trackedCourses: List<Course> = emptyList()
)

class CalendarViewModel(private val repository: CalendarRepository) : ViewModel() {

    var uiState by mutableStateOf(CalendarUiState())
        private set

    var selectedDate: Calendar by mutableStateOf(Calendar.getInstance())
        private set

    var selectedTab by mutableStateOf(CalendarTab.DAILY_SCHEDULE)

    /**
     * Derived state that automatically updates whenever trackedCourses or selectedDate changes.
     */
    val coursesForSelectedDay by derivedStateOf {
        val dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK)
        uiState.trackedCourses.filter { it.meetsOn(dayOfWeek) }
    }

    fun incrementDate(days: Int) {
        val newDate = selectedDate.clone() as Calendar
        newDate.add(Calendar.DAY_OF_MONTH, days)
        selectedDate = newDate
    }

    fun addCourse(subject: String, catalog: String, termCode: String, section: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val matchedComponents = repository.fetchAndFilterCourse(subject, catalog, termCode, section)

                uiState = if (matchedComponents.isNotEmpty()) {
                    uiState.copy(
                        trackedCourses = uiState.trackedCourses + matchedComponents,
                        lastAddedCourses = matchedComponents
                    )
                } else {
                    uiState.copy(error = CalendarError.NotFound)
                }
            } catch (_: IOException) {
                uiState = uiState.copy(error = CalendarError.Network)
            } catch (e: Exception) {
                uiState = uiState.copy(error = CalendarError.Unknown(e.message ?: "An unexpected error occurred"))
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun removeCourse(course: Course) {
        uiState = uiState.copy(
            trackedCourses = uiState.trackedCourses.filter { it != course }
        )
    }
}
