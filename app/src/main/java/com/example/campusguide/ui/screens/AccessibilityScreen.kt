package com.example.campusguide.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campusguide.ui.accessibility.AccessibilityPreferences
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.accessibility.ColorBlindMode
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(
    onBackClick: () -> Unit = {}
) {
    var isBoldEnabled by remember { mutableStateOf(true) }
    val accessibilityState = LocalAccessibilityState.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun persist() {
        scope.launch {
            AccessibilityPreferences.saveFromState(context, accessibilityState)
        }
    }

    // Current state is written at least once when screen opens
    LaunchedEffect(Unit) {
        persist()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    AccessibleText(
                        text = "Accessibility",
                        baseFontSizeSp = 18f,
                        forceFontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Display and AccessibleText Size Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // Tt icon in circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8E0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    AccessibleText(
                        text = "Tt",
                        fallbackColor = Color(0xFF6B4D8A),
                        baseFontSizeSp = 16f
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                AccessibleText(
                    text = "Display and Text Size",
                    baseFontSizeSp = 16f,
                    forceFontWeight = FontWeight.Bold,
                    fallbackColor = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AccessibleText Size Setting
            SettingRow(
                icon = {
                    AccessibleText(
                        text = "Tt",
                        baseFontSizeSp = 16f,
                        fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = "Text size",
                action = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                accessibilityState.decreaseTextSize()
                                persist()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            AccessibleText(
                                text = "-",
                                baseFontSizeSp = 20f,
                                fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                accessibilityState.increaseTextSize()
                                persist()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            AccessibleText(
                                text = "+",
                                baseFontSizeSp = 20f,
                                fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // AccessibleText Colour Setting
            SettingRow(
                icon = {
                    AccessibleText(
                        text = "A",
                        baseFontSizeSp = 18f,
                        fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = "Colorblind mode",
                action = {
                    var mode = accessibilityState.colorBlindMode
                    var boxColor = when (mode) {
                        ColorBlindMode.NONE -> Color.White
                        ColorBlindMode.PROTANOPIA -> Color(0xFFE57373) // Red tint
                        ColorBlindMode.DEUTERANOPIA -> Color(0xFF81C784) // Green tint
                        ColorBlindMode.TRITANOPIA -> Color(0xFF64B5F6) // Blue tint
                    }
                    var textColor = if (mode == ColorBlindMode.NONE) {
                        Color(0xFF6B4D8A)
                    } else {
                        Color.Black
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(
                                width = 2.dp,
                                color = Color(0xFF6B4D8A),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(boxColor)
                            .clickable {
                                accessibilityState.cycleColorBlindMode()
                                persist()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AccessibleText(
                            text = "A",
                            baseFontSizeSp = 16f,
                            forceFontWeight = FontWeight.Bold,
                            fallbackColor = textColor
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bold Setting
            SettingRow(
                icon = {
                    AccessibleText(
                        text = "B",
                        baseFontSizeSp = 18f,
                        forceFontWeight = FontWeight.Bold,
                        fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = "Bold",
                action = {
                    Switch(
                        checked = accessibilityState.isBoldEnabled,
                        onCheckedChange = { checked ->
                            accessibilityState.setBold(checked)
                            persist()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF6B4D8A),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: @Composable () -> Unit,
    label: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            AccessibleText(
                text = label,
                baseFontSizeSp = 16f,
                fallbackColor = MaterialTheme.colorScheme.onSurface
            )
        }


        action()
    }
}

@Preview(showBackground = true)
@Composable
fun AccessibilityScreenPreview() {
    ConcordiaCampusGuideTheme {
        CompositionLocalProvider(
            LocalAccessibilityState provides AccessibilityState(initialOffsetSp = 16f)
        ) {
            AccessibilityScreen()
        }
    }
}
