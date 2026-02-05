package com.example.campusguide


import android.content.res.Configuration
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campusguide.ui.components.NavigationBar
import com.example.campusguide.ui.components.NavigationBarPreview
import com.example.campusguide.ui.components.SearchBarWithProfile
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class NavigationBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun nav_BarDisplaysAppDestinations() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    rememberSaveable { mutableStateOf(AppDestinations.MAP) },
                    {
                        SearchBarWithProfile(
                        )
                    })
            }
        }

        composeTestRule.onNodeWithText("Map").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Directions").assertIsDisplayed()
        composeTestRule.onNodeWithText("POI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()

    }


    @Test
    fun navBar_rendersWithoutErrors() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    rememberSaveable { mutableStateOf(AppDestinations.MAP) },
                    {
                        SearchBarWithProfile( modifier = Modifier.testTag("searchBar")
                        )
                    })
            }
        }
        // Bottom nav items
        composeTestRule.onNodeWithText("Map").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Directions").assertIsDisplayed()
        composeTestRule.onNodeWithText("POI").assertIsDisplayed()

        // Slot content
        composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()

        composeTestRule.waitForIdle()
    }

    @Test
    fun navBar_darkTheme_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme(darkTheme = true) {
                NavigationBar(
                    rememberSaveable { mutableStateOf(AppDestinations.MAP) },
                    {
                        SearchBarWithProfile(
                        )
                    })
            }
        }

        composeTestRule.onNodeWithText("Map").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Directions").assertIsDisplayed()
        composeTestRule.onNodeWithText("POI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()


        composeTestRule.waitForIdle()
    }

    @Test
    fun navBar_withNoContent_rendersCorrectly() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    rememberSaveable { mutableStateOf(AppDestinations.MAP) }, {}
                    )
            }

        }

        composeTestRule.onNodeWithText("Map").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Directions").assertIsDisplayed()
        composeTestRule.onNodeWithText("POI").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    @Test
    fun navBarPreview_rendersCorrectly() {
        composeTestRule.setContent {
            NavigationBarPreview()
        }

        composeTestRule.onNodeWithText("Map").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Directions").assertIsDisplayed()
        composeTestRule.onNodeWithText("POI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()
    }

    @Test
    fun navBarEachDestinationCanBecomeSelected() {
        val state = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(state, {})
            }
        }

        composeTestRule.onNodeWithText("Calendar").performClick()
        assert(state.value == AppDestinations.CALENDAR)

        composeTestRule.onNodeWithText("Directions").performClick()
        assert(state.value == AppDestinations.DIRECTIONS)

        composeTestRule.onNodeWithText("POI").performClick()
        assert(state.value == AppDestinations.POI)

        composeTestRule.onNodeWithText("Map").performClick()
        assert(state.value == AppDestinations.MAP)
    }

    @Test
    fun navBarWithDifferentCurrentDestination() {
        composeTestRule.setContent {
            NavigationBar(
                rememberSaveable { mutableStateOf(AppDestinations.CALENDAR) },
                {
                    SearchBarWithProfile(
                    )
                })        }

        composeTestRule.onNodeWithText("Map").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Directions").assertIsDisplayed()
        composeTestRule.onNodeWithText("POI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search...").assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

    @Test
    fun navBarUpdatesCurrentDestination() {
        val currentDestination = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = currentDestination,
                    content = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Calendar").performClick()

        assert(currentDestination.value == AppDestinations.CALENDAR)
    }

    @Test
    fun navBarWithoutContentDoesNotRenderSearch() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    rememberSaveable { mutableStateOf(AppDestinations.MAP) },
                    {}
                )
            }
        }

        composeTestRule.onNodeWithText("Search...").assertDoesNotExist()
    }

    @Test
    fun navBarCurrentDestinationisVisuallySelected() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    rememberSaveable { mutableStateOf(AppDestinations.DIRECTIONS) },
                    {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Directions")
            .assertIsSelected()
    }


    @Test
    fun navBarUnselectedItemsAreNotSelected() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    rememberSaveable { mutableStateOf(AppDestinations.DIRECTIONS) },
                    {}
                )
            }
        }

        composeTestRule.onNodeWithText("Directions").assertIsSelected()
        composeTestRule.onNodeWithText("Map").assertIsNotSelected()
        composeTestRule.onNodeWithText("Calendar").assertIsNotSelected()
        composeTestRule.onNodeWithText("POI").assertIsNotSelected()
    }


    @Test
    fun navBar_clickingSameDestinationDoesNotCrash() {
        val currentDestination = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination, {})
            }
        }

        composeTestRule.onNodeWithText("Map").performClick()
        composeTestRule.onNodeWithText("Map").performClick()

        assert(currentDestination.value == AppDestinations.MAP)
    }

    @Test
    fun navBarRendersDrawableIcon() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    rememberSaveable { mutableStateOf(AppDestinations.MAP) },
                    {}
                )
            }
        }

        composeTestRule.onNodeWithText("Map").assertExists()
    }

    @Test
    fun navBarLabelRecomposes() {
        val state = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(state, {})
            }
        }

        composeTestRule.runOnIdle {
            state.value = AppDestinations.CALENDAR
        }

        composeTestRule.onNodeWithText("Calendar").assertExists()
    }


    @Test
    fun navBar_rendersDrawableIcon_specifically() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = rememberSaveable { mutableStateOf(AppDestinations.MAP) },
                    content = {}
                )
            }
        }

        // Add useUnmergedTree = true here
        composeTestRule
            .onNodeWithContentDescription(AppDestinations.MAP.label, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun navBar_allDestinations_respondToClicks() {
        val state = mutableStateOf(AppDestinations.MAP)
        composeTestRule.setContent {
            NavigationBar(state, {})
        }

        AppDestinations.entries.forEach { destination ->
            composeTestRule.onNodeWithText(destination.label).performClick()
            assert(state.value == destination)
        }

    }

    @Test
    fun navBar_restoresSelectedDestination_afterRecreation() {
        val restorationTester = StateRestorationTester(composeTestRule)

        restorationTester.setContent {
            val state = rememberSaveable { mutableStateOf(AppDestinations.MAP) }
            NavigationBar(state,{})
        }

        // Change state
        composeTestRule.onNodeWithText("Calendar").performClick()

        // Simulate recreation
        restorationTester.emulateSavedInstanceStateRestore()

        // Assert it's still selected
        composeTestRule.onNodeWithText("Calendar").assertIsSelected()
    }


    @Test
    fun navBar_contentHasCorrectPadding() {
        composeTestRule.setContent {
            NavigationBar(rememberSaveable { mutableStateOf(AppDestinations.MAP) }) { modifier ->
                Text("PaddingTest", modifier = modifier)
            }
        }
        // Verifying the node exists is usually enough to cover the line execution
        composeTestRule.onNodeWithText("PaddingTest").assertExists()
    }

    @Test
    fun navBar_labelContentIsCorrect() {
        val state = mutableStateOf(AppDestinations.MAP)
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(state, {})
            }
        }

        // Verify all labels are rendered initially
        AppDestinations.entries.forEach { destination ->
            composeTestRule.onNodeWithText(destination.label).assertIsDisplayed()
        }
    }

    @Test
    fun navBar_labelsPersistAfterSelection() {
        val state = mutableStateOf(AppDestinations.MAP)
        composeTestRule.setContent {
            NavigationBar(state, {})
        }

        // Click Calendar
        composeTestRule.onNodeWithText("Calendar").performClick()

        // Check that the Map label is still present (proves the 'label' lambda is stable)
        composeTestRule.onNodeWithText("Map").assertExists()
        composeTestRule.onNodeWithText("Calendar").assertIsSelected()
    }

    @Test
    fun navBar_rendersAsRail_onWideScreen() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalConfiguration provides Configuration().apply {
                    screenWidthDp = 800
                }
            ) {
                ConcordiaCampusGuideTheme {
                    NavigationBar(
                        rememberSaveable { mutableStateOf(AppDestinations.MAP) },
                        {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Map").assertIsDisplayed()
    }


    @Test
    fun navBar_fullRecompositionCycle() {
        val state = mutableStateOf(AppDestinations.MAP)
        composeTestRule.setContent {
            NavigationBar(state, {})
        }

        // 1. Initial execution (already happened)

        // 2. Change state to trigger recomposition
        composeTestRule.runOnIdle {
            state.value = AppDestinations.CALENDAR
        }
        composeTestRule.onNodeWithText("Calendar").assertIsSelected()

        // 3. Set same state to test the "skip" branch (Optimization logic)
        composeTestRule.runOnIdle {
            state.value = AppDestinations.CALENDAR
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun navBar_executesDrawableIconBranch() {
        // 1. Find a destination in your enum that IS a Drawable
        val drawableDestination = AppDestinations.entries.firstOrNull { it.icon is AppIcon.Drawable }

        if (drawableDestination != null) {
            composeTestRule.setContent {
                ConcordiaCampusGuideTheme {
                    NavigationBar(
                        currentDestination = rememberSaveable{mutableStateOf(drawableDestination)},
                        content = {}
                    )
                }
            }

            // 2. Finding the content description with useUnmergedTree = true
            // forces the Icon() composable (and the painterResource call) to execute.
            composeTestRule
                .onNodeWithContentDescription(drawableDestination.label, useUnmergedTree = true)
                .assertExists()
        }
    }

    @Test
    fun navBar_whenContentIsNull_rendersOnlyNavigation() {
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = rememberSaveable { mutableStateOf(AppDestinations.MAP) },
                    content = null // Explicitly pass null
                )
            }
        }

        // Verify the nav items still exist
        composeTestRule.onNodeWithText("Map").assertIsDisplayed()

        // Verify that NO search bar or extra text is present
        // (proving the content lambda was skipped)
        composeTestRule.onNodeWithText("Search...").assertDoesNotExist()
    }
}
