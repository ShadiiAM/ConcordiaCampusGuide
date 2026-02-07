package com.example.campusguide

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.AccessibleText
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.intArrayOf

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class AccessibleTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `AccessibleText displays provided text`() {
        val state = AccessibilityState()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides state) {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    AccessibleText(
                        text = "Hello Accessible",
                        baseFontSizeSp = 16f
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithText("Hello Accessible")
            .assertIsDisplayed()
    }

    @Test
    fun `AccessibleText still displays text with large size offset`() {
        val state = AccessibilityState(initialOffsetSp = 50f)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides state) {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    AccessibleText(
                        text = "Large Offset",
                        baseFontSizeSp = 16f
                    )
                }
            }
        }
        composeTestRule
            .onNodeWithText("Large Offset")
            .assertIsDisplayed()
    }

    @Test
    fun `AccessibleText still displays text when bold is toggled`() {
        val state = AccessibilityState(initialBoldEnabled = false)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides state) {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    AccessibleText(
                        text = "Bold Toggle",
                        baseFontSizeSp = 16f
                    )
                }
            }
        }
        composeTestRule
            .onNodeWithText("Bold Toggle")
            .assertIsDisplayed()

        state.setBold(true)
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Bold Toggle")
            .assertIsDisplayed()
    }
}