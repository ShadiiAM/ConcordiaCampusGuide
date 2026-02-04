package com.example.campusguide.ui.accessibility

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AccessibleText(
    text: String,
    baseFontSizeSp: Float
) {
    val accessibilityState = LocalAccessibilityState.current

    val finalSize = (baseFontSizeSp + accessibilityState.textSizeOffsetSp)
        .coerceAtLeast(8f)
        .sp
    val weight = if (accessibilityState.isBoldEnabled) {
        FontWeight.Bold
    } else {
        FontWeight.Normal
    }

    Text(
        text = text,
        fontSize = finalSize,
        fontWeight = weight,
        style = MaterialTheme.typography.bodyMedium
    )
}