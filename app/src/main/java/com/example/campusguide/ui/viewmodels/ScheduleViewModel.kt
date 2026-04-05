package com.example.campusguide.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusguide.data.ConcordiaScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * ViewModel for the open-data schedule screen.
 * Fetches course schedules from the Concordia open-data API and exposes them as a [StateFlow].
 */
class ScheduleViewModel(
    private val repository: ConcordiaScheduleRepository = ConcordiaScheduleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Idle)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    /**
     * Fetches the schedule for the given [subject] and [termCode].
     * Emits [ScheduleUiState.Loading] while the request is in flight,
     * then [ScheduleUiState.Success] or [ScheduleUiState.Error] on completion.
     */
    fun loadSchedule(subject: String, termCode: String) {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            try {
                val courses = repository.getSchedule(subject, termCode)
                _uiState.value = ScheduleUiState.Success(courses)
            } catch (e: IOException) {
                // Device is offline or the connection dropped
                _uiState.value = ScheduleUiState.Error("Network unavailable")
            } catch (e: HttpException) {
                // Server returned a non-2xx status code
                _uiState.value = ScheduleUiState.Error("Server error (${e.code()})")
            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error("An unexpected error occurred")
            }
        }
    }
}
