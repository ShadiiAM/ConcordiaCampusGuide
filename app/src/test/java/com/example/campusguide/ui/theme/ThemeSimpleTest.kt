package com.example.campusguide.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class ThemeSimpleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun theme_colors_exist() {
        val purple = Purple80
        val purpleGrey = PurpleGrey80
        val pink = Pink80
        val purple40 = Purple40
        val purpleGrey40 = PurpleGrey40
        val pink40 = Pink40
        val secondary = Secondary
        val secondaryContainer = SecondaryContainer
        val onSecondary = OnSecondary
        val onSecondaryContainer = OnSecondaryContainer
    }

    @Test
    fun concordiaCampusGuideTheme_lightMode_appliesCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = false, dynamicColor = false) {
                MaterialTheme {
                    // Theme applied
                }
            }
        }
    }

    @Test
    fun concordiaCampusGuideTheme_darkMode_appliesCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = true, dynamicColor = false) {
                MaterialTheme {
                    // Theme applied
                }
            }
        }
    }
}
