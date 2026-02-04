package com.example.campusguide.ui.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

// Small holder for the global text-size offset
class AccessibilityState(
    initialOffsetSp: Float = 0f,
    initialBoldEnabled: Boolean = false
) {
    var textSizeOffsetSp by mutableFloatStateOf(initialOffsetSp)
        private set

    var isBoldEnabled by mutableStateOf(initialBoldEnabled)
        private set

    fun increaseTextSize() {
        textSizeOffsetSp += 1f
    }

    fun decreaseTextSize() {
        if (textSizeOffsetSp > 0f) {
            textSizeOffsetSp -= 1f
        }
    }

    fun setBold(enabled: Boolean) {
        isBoldEnabled = enabled
    }
}

// CompositionLocal to access it from any composable
val LocalAccessibilityState = staticCompositionLocalOf<AccessibilityState> {
    error("AccessibilityState not provided")
}

// Helper to create it once at the app root
@Composable
fun rememberAccessibilityState(initialOffsetSp: Float = 0f): AccessibilityState {
    return remember { AccessibilityState(initialOffsetSp) }
}