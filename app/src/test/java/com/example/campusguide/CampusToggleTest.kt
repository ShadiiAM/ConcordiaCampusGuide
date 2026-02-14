package com.example.campusguide

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.accessibility.AccessibilityState
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.components.CampusToggle
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Tests for Campus enum and CampusToggle composable
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class CampusToggleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val defaultAccessibilityState = AccessibilityState()

    // ==================== Campus enum tests ====================

    @Test
    fun campus_enumHasTwoValues() {
        assertEquals(2, Campus.values().size)
    }

    @Test
    fun campus_SGW_nameIsCorrect() {
        assertEquals("SGW", Campus.SGW.name)
    }

    @Test
    fun campus_LOYOLA_nameIsCorrect() {
        assertEquals("LOYOLA", Campus.LOYOLA.name)
    }

    @Test
    fun campus_valueOfSGW_returnsSGW() {
        assertEquals(Campus.SGW, Campus.valueOf("SGW"))
    }

    @Test
    fun campus_valueOfLOYOLA_returnsLOYOLA() {
        assertEquals(Campus.LOYOLA, Campus.valueOf("LOYOLA"))
    }

    @Test
    fun campus_valueOfInvalid_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            Campus.valueOf("INVALID")
        }
    }

    @Test
    fun campus_SGW_ordinal() {
        assertEquals(0, Campus.SGW.ordinal)
    }

    @Test
    fun campus_LOYOLA_ordinal() {
        assertEquals(1, Campus.LOYOLA.ordinal)
    }

    // ==================== CampusToggle rendering tests ====================

    @Test
    fun campusToggle_rendersSGWLabel() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides defaultAccessibilityState) {
                MaterialTheme {
                    CampusToggle(
                        selectedCampus = Campus.SGW,
                        onCampusSelected = {}
                    )
                }
            }
        }
        composeTestRule.onNode(hasText("SGW")).assertExists()
    }

    @Test
    fun campusToggle_rendersLoyolaLabel() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides defaultAccessibilityState) {
                MaterialTheme {
                    CampusToggle(
                        selectedCampus = Campus.SGW,
                        onCampusSelected = {}
                    )
                }
            }
        }
        composeTestRule.onNode(hasText("Loyola")).assertExists()
    }

    @Test
    fun campusToggle_rendersBothLabelsWithSGWSelected() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides defaultAccessibilityState) {
                MaterialTheme {
                    CampusToggle(
                        selectedCampus = Campus.SGW,
                        onCampusSelected = {}
                    )
                }
            }
        }
        composeTestRule.onNode(hasText("SGW")).assertExists()
        composeTestRule.onNode(hasText("Loyola")).assertExists()
    }

    @Test
    fun campusToggle_rendersBothLabelsWithLoyolaSelected() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides defaultAccessibilityState) {
                MaterialTheme {
                    CampusToggle(
                        selectedCampus = Campus.LOYOLA,
                        onCampusSelected = {}
                    )
                }
            }
        }
        composeTestRule.onNode(hasText("SGW")).assertExists()
        composeTestRule.onNode(hasText("Loyola")).assertExists()
    }

    // ==================== CampusToggle interaction tests ====================

    @Test
    fun campusToggle_clickSGW_callsOnCampusSelectedWithSGW() {
        var clickedCampus: Campus? = null
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides defaultAccessibilityState) {
                MaterialTheme {
                    CampusToggle(
                        selectedCampus = Campus.LOYOLA,
                        onCampusSelected = { clickedCampus = it }
                    )
                }
            }
        }

        composeTestRule.onNode(hasText("SGW")).performClick()
        composeTestRule.waitForIdle()

        assertEquals(Campus.SGW, clickedCampus)
    }

    @Test
    fun campusToggle_clickLoyola_callsOnCampusSelectedWithLoyola() {
        var clickedCampus: Campus? = null
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides defaultAccessibilityState) {
                MaterialTheme {
                    CampusToggle(
                        selectedCampus = Campus.SGW,
                        onCampusSelected = { clickedCampus = it }
                    )
                }
            }
        }

        composeTestRule.onNode(hasText("Loyola")).performClick()
        composeTestRule.waitForIdle()

        assertEquals(Campus.LOYOLA, clickedCampus)
    }

    @Test
    fun campusToggle_clickAlreadySelectedCampus_stillCallsCallback() {
        var callCount = 0
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides defaultAccessibilityState) {
                MaterialTheme {
                    CampusToggle(
                        selectedCampus = Campus.SGW,
                        onCampusSelected = { callCount++ }
                    )
                }
            }
        }

        composeTestRule.onNode(hasText("SGW")).performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, callCount)
    }

    // ==================== CampusToggle showIcon tests ====================

    @Test
    fun campusToggle_withIconsEnabled_rendersSuccessfully() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides defaultAccessibilityState) {
                MaterialTheme {
                    CampusToggle(
                        selectedCampus = Campus.SGW,
                        onCampusSelected = {},
                        showIcon = true
                    )
                }
            }
        }
        composeTestRule.onNode(hasText("SGW")).assertExists()
        composeTestRule.onNode(hasText("Loyola")).assertExists()
    }

    @Test
    fun campusToggle_withIconsDisabled_rendersSuccessfully() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides defaultAccessibilityState) {
                MaterialTheme {
                    CampusToggle(
                        selectedCampus = Campus.SGW,
                        onCampusSelected = {},
                        showIcon = false
                    )
                }
            }
        }
        composeTestRule.onNode(hasText("SGW")).assertExists()
        composeTestRule.onNode(hasText("Loyola")).assertExists()
    }

    @Test
    fun campusToggle_withIconsDisabled_loyolaSelected_rendersSuccessfully() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides defaultAccessibilityState) {
                MaterialTheme {
                    CampusToggle(
                        selectedCampus = Campus.LOYOLA,
                        onCampusSelected = {},
                        showIcon = false
                    )
                }
            }
        }
        composeTestRule.onNode(hasText("SGW")).assertExists()
        composeTestRule.onNode(hasText("Loyola")).assertExists()
    }

    @Test
    fun campusToggle_withIconsEnabled_loyolaSelected_rendersSuccessfully() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityState provides defaultAccessibilityState) {
                MaterialTheme {
                    CampusToggle(
                        selectedCampus = Campus.LOYOLA,
                        onCampusSelected = {},
                        showIcon = true
                    )
                }
            }
        }
        composeTestRule.onNode(hasText("SGW")).assertExists()
        composeTestRule.onNode(hasText("Loyola")).assertExists()
    }
}