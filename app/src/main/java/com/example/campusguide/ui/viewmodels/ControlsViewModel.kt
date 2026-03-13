package com.example.campusguide.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel


class ControlsViewModel : ViewModel() {
    var controlsVisible by mutableStateOf(false)
}