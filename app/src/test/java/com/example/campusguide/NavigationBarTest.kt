package com.example.campusguide


import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
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
        composeTestRule.waitForIdle()
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

}