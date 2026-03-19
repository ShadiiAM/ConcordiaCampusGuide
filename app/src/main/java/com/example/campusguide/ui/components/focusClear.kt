package com.example.campusguide.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

// 1. CompositionLocal to hold the latest "protected" event ID
val LocalIgnoreFocusClearEventId =
    compositionLocalOf<MutableState<Long?>> { error("No event id provider found! This modifier must be used inside a FocusClearWrapper") }

/**
 * Modifier to mark a composable as "ignore focus clear on touch".
 * Usage: Modifier.ignoreFocusClearOnTouch()
 */
fun Modifier.ignoreFocusClearOnTouch(): Modifier = composed {
    val ignoreEventIdState = LocalIgnoreFocusClearEventId.current
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.type == PointerEventType.Press) {
                    ignoreEventIdState.value = event.changes.first().id.value
                }
            }
        }
    }
}

/**
 * A composable wrapper that:
 * - Provides a CompositionLocal for tracking ignored touch event IDs.
 * - Clears focus on any pointer event NOT marked as ignored.
 *
 * @param modifier Modifier for the root box.
 * @param content The UI inside the wrapper.
 */

@Composable
fun FocusClearWrapper(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val ignoreEventId = remember { mutableStateOf<Long?>(null) }

    CompositionLocalProvider(LocalIgnoreFocusClearEventId provides ignoreEventId) {
        Box(
            modifier = modifier.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Release) {
                            val currentIgnored = ignoreEventId.value
                            val eventId = event.changes.first().id.value
                            if (eventId != currentIgnored) {
                                focusManager.clearFocus()
                            }
                        }
                    }
                }
            }
        ) {
            content()
        }
    }
}