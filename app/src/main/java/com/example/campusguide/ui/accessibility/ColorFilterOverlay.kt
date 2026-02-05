package com.example.campusguide.ui.accessibility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun AccessibleAppRoot(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val accessibilityState = LocalAccessibilityState.current

    Box(modifier = modifier.fillMaxSize()) {
        content()

        if (accessibilityState.isColorFilterEnabled()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = when (accessibilityState.colorBlindMode) {
//                            ColorBlindMode.DEUTERANOPIA -> Color(0x330000FF)
//                            ColorBlindMode.PROTANOPIA  -> Color(0x3300FF00)
//                            ColorBlindMode.TRITANOPIA  -> Color(0x33FFFF00)
                            ColorBlindMode.HIGH_CONTRAST -> Color(0x66000000)
                            ColorBlindMode.NONE -> Color.Transparent
                        }
                    )
            )
        }
    }
}