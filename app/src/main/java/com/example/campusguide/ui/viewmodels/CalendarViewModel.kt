package com.example.campusguide.ui.viewmodels

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusguide.data.CalendarRepository
import com.example.campusguide.data.CalendarStorage
import com.example.campusguide.data.Course
import com.example.campusguide.ui.screens.CalendarTab
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class CalendarError {
    object NotFound : CalendarError()
    object Network : CalendarError()
    data class Unknown(val message: String) : CalendarError()
}

data class CalendarUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: CalendarError? = null,
    val refreshError: Boolean = false,
    val lastAddedCourses: List<Course>? = null,
    val trackedCourses: List<Course> = emptyList(),
    val termCode: String = "",
    val subject: String = "",
    val catalog: String = "",
    val section: String = ""
)

data class NextCourseResult(
    val course: Course,
    val date: Calendar
)

class CalendarViewModel(
    private val repository: CalendarRepository,
    private val storage: CalendarStorage? = null
) : ViewModel() {

    var uiState by mutableStateOf(CalendarUiState())
        private set

    var selectedDate: Calendar by mutableStateOf(Calendar.getInstance())
        private set

    private var _selectedTab by mutableStateOf(CalendarTab.DAILY_SCHEDULE)
    var selectedTab: CalendarTab
        get() = _selectedTab
        set(value) {
            if (_selectedTab != value) {
                // Clear success and error states when navigating between tabs
                uiState = uiState.copy(lastAddedCourses = null, error = null, refreshError = false)
                _selectedTab = value
            }
        }

    init {
        loadTrackedCourses()
    }

    private fun loadTrackedCourses() {
        storage?.let {
            viewModelScope.launch {
                val courses = it.trackedCourses.first()
                uiState = uiState.copy(trackedCourses = courses)
            }
        }
    }

    private fun saveTrackedCourses(courses: List<Course>) {
        storage?.let {
            viewModelScope.launch {
                it.saveCourses(courses)
            }
        }
    }

    /**
     * automatically updates whenever trackedCourses or selectedDate changes.
     */
    val coursesForSelectedDay by derivedStateOf {
        val dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK)
        uiState.trackedCourses.filter { it.meetsOn(dayOfWeek) }
    }

    /**
     * Computes the next class across the next 7 days.
     */
    val nextUpcomingCourse by derivedStateOf {
        val now = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val nowTimeStr = timeFormat.format(now.time)

        for (i in 0..7) {
            val checkDate = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }
            val dayOfWeek = checkDate.get(Calendar.DAY_OF_WEEK)
            
            val coursesOnDay = uiState.trackedCourses
                .filter { it.meetsOn(dayOfWeek) }
                .sortedBy { it.startTime }

            val nextOnThisDay = if (i == 0) {
                // Today: first class starting after now
                coursesOnDay.firstOrNull { it.startTime > nowTimeStr }
            } else {
                // Future day: first class of the day
                coursesOnDay.firstOrNull()
            }

            if (nextOnThisDay != null) {
                return@derivedStateOf NextCourseResult(nextOnThisDay, checkDate)
            }
        }
        null
    }

    fun jumpToNextClass() {
        nextUpcomingCourse?.let { result ->
            selectedDate = result.date
            selectedTab = CalendarTab.DAILY_SCHEDULE
        }
    }

    fun incrementDate(days: Int) {
        val newDate = selectedDate.clone() as Calendar
        newDate.add(Calendar.DAY_OF_MONTH, days)
        selectedDate = newDate
    }

    fun updateInput(
        termCode: String = uiState.termCode,
        subject: String = uiState.subject,
        catalog: String = uiState.catalog,
        section: String = uiState.section
    ) {
        uiState = uiState.copy(
            termCode = termCode,
            subject = subject,
            catalog = catalog,
            section = section
        )
    }

    fun addCourse() {
        val subject = uiState.subject
        val catalog = uiState.catalog
        val termCode = uiState.termCode
        val section = uiState.section

        viewModelScope.launch {
            // Clear success and error states when starting a new search
            uiState = uiState.copy(isLoading = true, error = null, lastAddedCourses = null)
            try {
                val matchedComponents = repository.fetchAndFilterCourse(subject, catalog, termCode, section)

                if (matchedComponents.isNotEmpty()) {
                    val newTrackedCourses = uiState.trackedCourses + matchedComponents
                    uiState = uiState.copy(
                        trackedCourses = newTrackedCourses,
                        lastAddedCourses = matchedComponents,
                        // Clear fields on success
                        termCode = "",
                        subject = "",
                        catalog = "",
                        section = ""
                    )
                    saveTrackedCourses(newTrackedCourses)
                } else {
                    uiState = uiState.copy(error = CalendarError.NotFound)
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

    fun refreshSchedules() {
        if (uiState.trackedCourses.isEmpty()) return

        viewModelScope.launch {
            uiState = uiState.copy(isRefreshing = true, refreshError = false)
            val updatedCourses = mutableListOf<Course>()
            var anyFailed = false

            uiState.trackedCourses.forEach { course ->
                try {
                    val fresh = repository.fetchAndFilterCourse(
                        course.subject,
                        course.catalog,
                        course.termCode,
                        course.section
                    )
                    if (fresh.isNotEmpty()) {
                        updatedCourses.addAll(fresh)
                    } else {
                        updatedCourses.add(course)
                        anyFailed = true
                    }
                } catch (e: Exception) {
                    updatedCourses.add(course)
                    anyFailed = true
                }
            }

            val finalCourses = updatedCourses.distinct()
            uiState = uiState.copy(
                trackedCourses = finalCourses,
                isRefreshing = false,
                refreshError = anyFailed
            )
            saveTrackedCourses(finalCourses)
        }
    }

    fun removeCourse(course: Course) {
        val newTrackedCourses = uiState.trackedCourses.filter { it != course }
        uiState = uiState.copy(trackedCourses = newTrackedCourses)
        saveTrackedCourses(newTrackedCourses)
    }
}
