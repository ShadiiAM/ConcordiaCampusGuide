package com.example.campusguide.ui.accessibility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun AccessibleAppRoot(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val accessibilityState = LocalAccessibilityState.current

    Box(modifier = modifier.fillMaxSize()) {
        content()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = when (accessibilityState.colorBlindMode) {
                            ColorBlindMode.DEUTERANOPIA -> Color(0xFF51CF66).copy(alpha = 0.18f)
                            ColorBlindMode.PROTANOPIA -> Color(0xFFFF6B6B).copy(alpha = 0.18f)
                            ColorBlindMode.TRITANOPIA -> Color(0xFFFFD43B).copy(alpha = 0.18f)
                            ColorBlindMode.NONE -> Color.Transparent
                        }
                    )
            )
    }
}