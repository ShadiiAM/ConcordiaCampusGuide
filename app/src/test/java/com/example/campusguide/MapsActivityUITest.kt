package com.example.campusguide

import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.components.Campus
import com.example.campusguide.ui.components.CampusToggle
import com.example.campusguide.ui.components.CampusTogglePreview
import com.example.campusguide.ui.components.SearchBarWithProfile
import com.example.campusguide.ui.screens.AccessibilityScreen
import com.example.campusguide.ui.screens.ProfileScreen
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * UI tests for MapsActivity compose components to improve coverage
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MapsActivityUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== Profile/Accessibility flow simulation tests ====================

    @Test
    fun profileScreen_showsAndHidesCorrectly() {
        var showProfile by mutableStateOf(true)
        var showAccessibility by mutableStateOf(false)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                if (!showProfile && !showAccessibility) {
                    // Both hidden - simulates overlay visibility = GONE
                }

                if (showAccessibility) {
                    AccessibilityScreen(
                        onBackClick = {
                            showAccessibility = false
                        }
                    )
                } else if (showProfile) {
                    ProfileScreen(
                        onBackClick = {
                            showProfile = false
                        },
                        onProfileClick = { /* Profile details */ },
                        onAccessibilityClick = { showAccessibility = true }
                    )
                }
            }
        }

        // Initially should show profile
        composeTestRule.onNodeWithText("User settings").assertIsDisplayed()

        // Click accessibility to navigate
        composeTestRule.onNodeWithText("Accessibility").performClick()
        composeTestRule.waitForIdle()

        // Now should show accessibility screen
        composeTestRule.onNodeWithText("Display and Text Size").assertIsDisplayed()
    }

    @Test
    fun accessibilityScreen_backButton_navigatesCorrectly() {
        var showAccessibility = true

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                if (showAccessibility) {
                    AccessibilityScreen(
                        onBackClick = {
                            showAccessibility = false
                        }
                    )
                }
            }
        }

        // Should show accessibility screen initially
        composeTestRule.onNodeWithText("Accessibility").assertIsDisplayed()

        // Click back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        // showAccessibility should be false now
        assertFalse(showAccessibility)
    }

    @Test
    fun profileScreen_backButton_navigatesCorrectly() {
        var showProfile = true

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                if (showProfile) {
                    ProfileScreen(
                        onBackClick = {
                            showProfile = false
                        },
                        onProfileClick = {},
                        onAccessibilityClick = {}
                    )
                }
            }
        }

        // Should show profile screen
        composeTestRule.onNodeWithText("User settings").assertIsDisplayed()

        // Click back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        assertFalse(showProfile)
    }

    @Test
    fun profileScreen_accessibilityClick_setsCorrectState() {
        var showAccessibility = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                ProfileScreen(
                    onBackClick = {},
                    onProfileClick = {},
                    onAccessibilityClick = { showAccessibility = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Accessibility").performClick()
        composeTestRule.waitForIdle()

        assertTrue(showAccessibility)
    }

    @Test
    fun searchBarWithProfile_onProfileClick_triggersCallback() {
        var profileClicked = false

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                SearchBarWithProfile(
                    onSearchQueryChange = {},
                    onProfileClick = { profileClicked = true }
                )
            }
        }

        // Click on profile avatar
        composeTestRule.onNodeWithText("A").performClick()
        composeTestRule.waitForIdle()

        assertTrue(profileClicked)
    }

    // ==================== Campus toggle tests — proper recomposition ====================

    @Test
    fun campusToggle_onCampusSelected_triggersCallback() {
        var selectedCampus by mutableStateOf(Campus.SGW)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CampusToggle(
                    selectedCampus = selectedCampus,
                    onCampusSelected = { campus ->
                        selectedCampus = campus
                    },
                    showIcon = true
                )
            }
        }

        // Click Loyola — recomposition flips all selectedCampus branches
        composeTestRule.onNodeWithText("Loyola").performClick()
        composeTestRule.waitForIdle()

        assertEquals(Campus.LOYOLA, selectedCampus)
    }

    @Test
    fun campusToggle_switchingBackAndForth_worksCorrectly() {
        var selectedCampus by mutableStateOf(Campus.SGW)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CampusToggle(
                    selectedCampus = selectedCampus,
                    onCampusSelected = { campus ->
                        selectedCampus = campus
                    },
                    showIcon = true
                )
            }
        }

        // Switch to Loyola
        composeTestRule.onNodeWithText("Loyola").performClick()
        composeTestRule.waitForIdle()
        assertEquals(Campus.LOYOLA, selectedCampus)

        // Switch back to SGW
        composeTestRule.onNodeWithText("SGW").performClick()
        composeTestRule.waitForIdle()
        assertEquals(Campus.SGW, selectedCampus)
    }

    @Test
    fun campusToggle_startWithLoyola_switchToSGW() {
        var selectedCampus by mutableStateOf(Campus.LOYOLA)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CampusToggle(
                    selectedCampus = selectedCampus,
                    onCampusSelected = { campus ->
                        selectedCampus = campus
                    },
                    showIcon = true
                )
            }
        }

        // Initially LOYOLA is selected — exercises LOYOLA-selected branches on first render
        composeTestRule.onNodeWithText("SGW").performClick()
        composeTestRule.waitForIdle()
        assertEquals(Campus.SGW, selectedCampus)
    }

    @Test
    fun campusToggle_showIconFalse_SGW_to_LOYOLA() {
        var selectedCampus by mutableStateOf(Campus.SGW)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CampusToggle(
                    selectedCampus = selectedCampus,
                    onCampusSelected = { campus ->
                        selectedCampus = campus
                    },
                    showIcon = false
                )
            }
        }

        // showIcon=false exercises the null leadingIcon branches
        composeTestRule.onNodeWithText("Loyola").performClick()
        composeTestRule.waitForIdle()
        assertEquals(Campus.LOYOLA, selectedCampus)

        composeTestRule.onNodeWithText("SGW").performClick()
        composeTestRule.waitForIdle()
        assertEquals(Campus.SGW, selectedCampus)
    }

    @Test
    fun campusToggle_showIconFalse_startLoyola() {
        var selectedCampus by mutableStateOf(Campus.LOYOLA)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                CampusToggle(
                    selectedCampus = selectedCampus,
                    onCampusSelected = { campus ->
                        selectedCampus = campus
                    },
                    showIcon = false
                )
            }
        }

        composeTestRule.onNodeWithText("SGW").performClick()
        composeTestRule.waitForIdle()
        assertEquals(Campus.SGW, selectedCampus)
    }

    @Test
    fun campusTogglePreview_rendersWithoutCrash() {
        composeTestRule.setContent {
            CampusTogglePreview()
        }
        // Preview renders SGW selected by default
        composeTestRule.onNodeWithText("SGW").assertIsDisplayed()
        composeTestRule.onNodeWithText("Loyola").assertIsDisplayed()
    }

    // ==================== ProfileOverlayContent tests (covers showProfileOverlay compose body) ====================

    @Test
    fun profileOverlayContent_showsProfileScreenInitially() {
        composeTestRule.setContent {
            ProfileOverlayContent(onDismiss = {})
        }

        composeTestRule.onNodeWithText("User settings").assertIsDisplayed()
    }

    @Test
    fun profileOverlayContent_navigateToAccessibility() {
        composeTestRule.setContent {
            ProfileOverlayContent(onDismiss = {})
        }

        composeTestRule.onNodeWithText("Accessibility").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Display and Text Size").assertIsDisplayed()
    }

    @Test
    fun profileOverlayContent_accessibilityBackCallsDismiss() {
        var dismissed = false

        composeTestRule.setContent {
            ProfileOverlayContent(onDismiss = { dismissed = true })
        }

        // Go to accessibility
        composeTestRule.onNodeWithText("Accessibility").performClick()
        composeTestRule.waitForIdle()

        // Back from accessibility
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        assertTrue(dismissed)
    }

    @Test
    fun profileOverlayContent_profileBackCallsDismiss() {
        var dismissed = false

        composeTestRule.setContent {
            ProfileOverlayContent(onDismiss = { dismissed = true })
        }

        // Back from profile directly
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        assertTrue(dismissed)
    }

    // ==================== Combined flow tests ====================

    @Test
    fun profileOverlay_fullFlow_worksCorrectly() {
        var showProfile = false
        var showAccessibility = false
        var overlayVisibility = View.GONE

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                // Simulate the overlay logic from MapsActivity.showProfileOverlay()
                SearchBarWithProfile(
                    onProfileClick = {
                        overlayVisibility = View.VISIBLE
                        showProfile = true
                    }
                )

                if (overlayVisibility == View.VISIBLE) {
                    if (!showProfile && !showAccessibility) {
                        overlayVisibility = View.GONE
                    }

                    if (showAccessibility) {
                        AccessibilityScreen(
                            onBackClick = {
                                showAccessibility = false
                                overlayVisibility = View.GONE
                            }
                        )
                    } else if (showProfile) {
                        ProfileScreen(
                            onBackClick = {
                                showProfile = false
                                overlayVisibility = View.GONE
                            },
                            onProfileClick = {},
                            onAccessibilityClick = { showAccessibility = true }
                        )
                    }
                }
            }
        }

        // Click profile
        composeTestRule.onNodeWithText("A").performClick()
        composeTestRule.waitForIdle()

        assertTrue(showProfile)
        assertEquals(View.VISIBLE, overlayVisibility)
    }

    @Test
    fun nestedConditionals_inProfileOverlay_executeCorrectly() {
        var showProfile by mutableStateOf(true)
        var showAccessibility by mutableStateOf(false)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                // Test the exact conditional structure from showProfileOverlay()
                if (!showProfile && !showAccessibility) {
                    // This branch should hide overlay
                }

                if (showAccessibility) {
                    AccessibilityScreen(
                        onBackClick = {
                            showAccessibility = false
                        }
                    )
                } else if (showProfile) {
                    ProfileScreen(
                        onBackClick = {
                            showProfile = false
                        },
                        onProfileClick = {},
                        onAccessibilityClick = { showAccessibility = true }
                    )
                }
            }
        }

        // Navigate to accessibility
        composeTestRule.onNodeWithText("Accessibility").performClick()
        composeTestRule.waitForIdle()

        assertTrue(showAccessibility)

        // Go back
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        assertFalse(showAccessibility)
    }
}
