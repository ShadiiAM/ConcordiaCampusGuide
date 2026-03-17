package com.example.campusguide.ui.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Small holder for the global text-size offset
class AccessibilityState(
    initialOffsetSp: Float = 0f,
    initialBoldEnabled: Boolean = false,
    initialTextColor: Color = Color.Unspecified,
    colorBlindMode: ColorBlindMode = ColorBlindMode.NONE,
    initialAvoidStairs: Boolean = false,
    initialAvoidEscalators: Boolean = false,
    // Callback invoked whenever the state changes; default is no-op
    private val onStateChanged: (AccessibilityState) -> Unit = {}
) {
    var textSizeOffsetSp by mutableFloatStateOf(initialOffsetSp)
        private set

    var isBoldEnabled by mutableStateOf(initialBoldEnabled)
        private set

    var textColor by mutableStateOf(initialTextColor)
        private set

    var colorBlindMode by mutableStateOf(colorBlindMode)
        private set

    var avoidStairs by mutableStateOf(initialAvoidStairs)
        private set

    var avoidEscalators by mutableStateOf(initialAvoidEscalators)
        private set

    private fun notifyChanged() {
        onStateChanged(this)
    }

    fun increaseTextSize() {
        if (textSizeOffsetSp < 6f) {
            textSizeOffsetSp += 1f
            notifyChanged()
        }
    }

    fun decreaseTextSize() {
        if (textSizeOffsetSp > -2f) {
            textSizeOffsetSp -= 1f
            notifyChanged()
        }
    }

    fun setBold(enabled: Boolean) {
        if (isBoldEnabled != enabled) {
            isBoldEnabled = enabled
            notifyChanged()
        }
    }

    fun cycleColorBlindMode() {
        colorBlindMode = when (colorBlindMode) {
            ColorBlindMode.NONE -> ColorBlindMode.PROTANOPIA
            ColorBlindMode.PROTANOPIA -> ColorBlindMode.DEUTERANOPIA
            ColorBlindMode.DEUTERANOPIA -> ColorBlindMode.TRITANOPIA
            ColorBlindMode.TRITANOPIA -> ColorBlindMode.NONE
        }
        notifyChanged()
    }

    fun updateAvoidStairs(enabled: Boolean) {
        if (avoidStairs != enabled) {
            avoidStairs = enabled
            notifyChanged()
        }
    }

    fun updateAvoidEscalators(enabled: Boolean) {
        if (avoidEscalators != enabled) {
            avoidEscalators = enabled
            notifyChanged()
        }
    }

    fun setFrom(other: AccessibilityState) {
        textSizeOffsetSp = other.textSizeOffsetSp
        isBoldEnabled = other.isBoldEnabled
        textColor = other.textColor
        colorBlindMode = other.colorBlindMode
        avoidStairs = other.avoidStairs
        avoidEscalators = other.avoidEscalators
        notifyChanged()
    }
}

// CompositionLocal to access it from any composable
val LocalAccessibilityState = staticCompositionLocalOf<AccessibilityState> {
    error("AccessibilityState not provided")
}

// Helper to create it once at the app root
@Composable
fun rememberAccessibilityState(
    initialOffsetSp: Float = 0f,
    initialBoldEnabled: Boolean = false,
    initialTextColor: Color = Color.Unspecified,
    colorBlindMode: ColorBlindMode = ColorBlindMode.NONE,
    initialAvoidStairs: Boolean = false,
    initialAvoidEscalators: Boolean = false,
    onStateChanged: (AccessibilityState) -> Unit = {}
): AccessibilityState {
    return remember {
        AccessibilityState(
            initialOffsetSp = initialOffsetSp,
            initialBoldEnabled = initialBoldEnabled,
            initialTextColor = initialTextColor,
            colorBlindMode = colorBlindMode,
            initialAvoidStairs = initialAvoidStairs,
            initialAvoidEscalators = initialAvoidEscalators,
            onStateChanged = onStateChanged
        )
    }
}

enum class ColorBlindMode {
    NONE,          // normal vision
    PROTANOPIA,    // red-weak
    DEUTERANOPIA,  // green-weak
    TRITANOPIA     // blue-weak
}
