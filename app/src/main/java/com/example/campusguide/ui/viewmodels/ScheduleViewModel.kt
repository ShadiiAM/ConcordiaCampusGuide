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

class ScheduleViewModel(
    private val repository: ConcordiaScheduleRepository = ConcordiaScheduleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Idle)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    fun loadSchedule(subject: String, termCode: String) {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            try {
                val courses = repository.getSchedule(subject, termCode)
                _uiState.value = ScheduleUiState.Success(courses)
            } catch (e: IOException) {
                _uiState.value = ScheduleUiState.Error("Network unavailable")
            } catch (e: HttpException) {
                _uiState.value = ScheduleUiState.Error("Server error (${e.code()})")
            } catch (e: Exception) {
                _uiState.value = ScheduleUiState.Error("An unexpected error occurred")
            }
        }
    }
}
