package com.example.campusguide.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.campusguide.AppDestinations
import com.example.campusguide.AppIcon
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class NavigationBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var currentDestination: MutableState<AppDestinations>

    @Before
    fun setUp() {
        currentDestination = mutableStateOf(AppDestinations.MAP)
    }

    @Test
    fun navigationBar_composesSuccessfully() {
        // Test: Basic composition without crashes
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Verify navigation bar is composed
        composeTestRule.onNodeWithText(AppDestinations.MAP.label).assertExists()
    }

    @Test
    fun navigationBar_displaysAllDestinations() {
        // Test: forEach loop executes for all entries
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Verify all destinations are displayed
        AppDestinations.entries.forEach { destination ->
            composeTestRule.onNodeWithText(destination.label).assertExists()
        }
    }

    @Test
    fun navigationBar_displaysVectorIcons() {
        // Test: AppIcon.Vector branch in when expression
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Find destinations with Vector icons
        val vectorDestinations = AppDestinations.entries.filter { it.icon is AppIcon.Vector }

        vectorDestinations.forEach { destination ->
            composeTestRule.onNodeWithContentDescription(
                destination.label,
                useUnmergedTree = true
            ).assertExists()
        }
    }

    @Test
    fun navigationBar_displaysDrawableIcons() {
        // Test: AppIcon.Drawable branch in when expression
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Find destinations with Drawable icons
        val drawableDestinations = AppDestinations.entries.filter { it.icon is AppIcon.Drawable }

        drawableDestinations.forEach { destination ->
            composeTestRule.onNodeWithContentDescription(
                destination.label,
                useUnmergedTree = true
            ).assertExists()
        }
    }

    @Test
    fun navigationBar_currentDestinationIsSelected() {
        // Test: selected = true branch (it == currentDestination.value)
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // The current destination should be selected
        composeTestRule.onNodeWithText(AppDestinations.MAP.label)
            .assertIsSelected()
    }

    @Test
    fun navigationBar_otherDestinationsNotSelected() {
        // Test: selected = false branch (it != currentDestination.value)
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Other destinations should not be selected
        AppDestinations.entries
            .filter { it != AppDestinations.MAP }
            .forEach { destination ->
                composeTestRule.onNodeWithText(destination.label)
                    .assertIsNotSelected()
            }
    }

    @Test
    fun navigationBar_clickUpdatesDestination() {
        // Test: onClick lambda execution
        val targetDestination = AppDestinations.entries.first { it != AppDestinations.MAP }

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Click on a different destination
        composeTestRule.onNodeWithText(targetDestination.label).performClick()

        composeTestRule.waitForIdle()

        // Verify state changed
        assert(currentDestination.value == targetDestination) {
            "Expected ${targetDestination.label} but got ${currentDestination.value.label}"
        }
    }

    @Test
    fun navigationBar_clickEachDestination_updatesCorrectly() {
        // Test: onClick for all destinations
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Click each destination and verify state updates
        AppDestinations.entries.forEach { destination ->
            composeTestRule.onNodeWithText(destination.label).performClick()
            composeTestRule.waitForIdle()

            assert(currentDestination.value == destination) {
                "Expected ${destination.label} but got ${currentDestination.value.label}"
            }
        }
    }

    @Test
    fun navigationBar_selectionUpdates_whenStateChanges() {
        // Test: Reactivity of selection based on state
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Verify initial selection
        composeTestRule.onNodeWithText(AppDestinations.MAP.label)
            .assertIsSelected()

        // Change state programmatically
        val newDestination = AppDestinations.entries.first { it != AppDestinations.MAP }
        currentDestination.value = newDestination

        composeTestRule.waitForIdle()

        // Verify new selection
        composeTestRule.onNodeWithText(newDestination.label)
            .assertIsSelected()

        // Verify old selection is deselected
        composeTestRule.onNodeWithText(AppDestinations.MAP.label)
            .assertIsNotSelected()
    }

    @Test
    fun navigationBar_withNullContent_displaysNavigation() {
        // Test: content?.invoke - null path
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = currentDestination,
                    content = null
                )
            }
        }

        composeTestRule.waitForIdle()

        // Should display navigation without crashing
        composeTestRule.onNodeWithText(AppDestinations.MAP.label).assertExists()
    }

    @Test
    fun navigationBar_withContent_rendersContent() {
        // Test: content?.invoke - non-null path
        val testText = "Custom Content"

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = currentDestination,
                    content = { modifier ->
                        Text(
                            text = testText,
                            modifier = modifier
                        )
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify content was rendered
        composeTestRule.onNodeWithText(testText).assertExists()
    }

    @Test
    fun navigationBar_contentReceivesModifier() {
        // Test: Modifier is passed to content lambda
        var receivedModifier: Modifier? = null

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = currentDestination,
                    content = { modifier ->
                        receivedModifier = modifier
                        Box(modifier = modifier)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify modifier was passed
        assert(receivedModifier != null) {
            "Modifier should not be null"
        }
    }

    @Test
    fun navigationBar_contentWithModifier_isDisplayed() {
        // Test: Modifier.fillMaxSize().padding(top = 100.dp) is applied
        val testTag = "content_box"

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = currentDestination,
                    content = { modifier ->
                        Box(
                            modifier = modifier.testTag(testTag)
                        ) {
                            Text("Content")
                        }
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify content with modifier exists
        composeTestRule.onNodeWithTag(testTag).assertExists()
        composeTestRule.onNodeWithText("Content").assertExists()
    }

    @Test
    fun navigationBar_allLabelsRendered() {
        // Test: label = { Text(it.label) } for all items
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Verify all labels are rendered
        AppDestinations.entries.forEach { destination ->
            composeTestRule.onNodeWithText(
                destination.label,
                useUnmergedTree = true
            ).assertExists()
        }
    }

    @Test
    fun navigationBar_allIconsHaveContentDescription() {
        // Test: contentDescription is set for all icons
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Verify all icons have content descriptions
        AppDestinations.entries.forEach { destination ->
            composeTestRule.onNodeWithContentDescription(
                destination.label,
                useUnmergedTree = true
            ).assertExists()
        }
    }

    @Test
    fun navigationBar_multipleClicksCycles_maintainState() {
        // Test: Multiple onClick invocations in sequence
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        val destinations = AppDestinations.entries.toList()

        // Click through all destinations multiple times
        repeat(3) { cycle ->
            destinations.forEach { destination ->
                composeTestRule.onNodeWithText(destination.label).performClick()
                composeTestRule.waitForIdle()

                assert(currentDestination.value == destination) {
                    "Cycle $cycle: Expected ${destination.label} but got ${currentDestination.value.label}"
                }
            }
        }
    }

    @Test
    fun navigationBar_complexContentComposable() {
        // Test: Complex content composable structure
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = currentDestination,
                    content = { modifier ->
                        Box(modifier = modifier) {
                            Text("Line 1")
                            Text("Line 2")
                            Text("Line 3")
                        }
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Line 1").assertExists()
        composeTestRule.onNodeWithText("Line 2").assertExists()
        composeTestRule.onNodeWithText("Line 3").assertExists()
    }

    @Test
    fun navigationBar_emptyContentLambda_doesNotCrash() {
        // Test: Edge case - empty content lambda
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = currentDestination,
                    content = { _ ->
                        // Empty lambda - does nothing
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Should not crash
        composeTestRule.onNodeWithText(AppDestinations.MAP.label).assertExists()
    }

    @Test
    fun navigationBar_vectorIconType_rendersCorrectly() {
        // Explicit test for Vector icon rendering
        val vectorDestinations = AppDestinations.entries.filter { it.icon is AppIcon.Vector }

        if (vectorDestinations.isNotEmpty()) {
            composeTestRule.setContent {
                ConcordiaCampusGuideTheme {
                    NavigationBar(currentDestination = currentDestination)
                }
            }

            composeTestRule.waitForIdle()

            vectorDestinations.forEach { destination ->
                composeTestRule.onNodeWithContentDescription(
                    destination.label,
                    useUnmergedTree = true
                ).assertExists()
            }
        }
    }

    @Test
    fun navigationBar_drawableIconType_rendersCorrectly() {
        // Explicit test for Drawable icon rendering
        val drawableDestinations = AppDestinations.entries.filter { it.icon is AppIcon.Drawable }

        if (drawableDestinations.isNotEmpty()) {
            composeTestRule.setContent {
                ConcordiaCampusGuideTheme {
                    NavigationBar(currentDestination = currentDestination)
                }
            }

            composeTestRule.waitForIdle()

            drawableDestinations.forEach { destination ->
                composeTestRule.onNodeWithContentDescription(
                    destination.label,
                    useUnmergedTree = true
                ).assertExists()
            }
        }
    }

    @Test
    fun navigationBar_navigationItemsCount_matchesDestinationsCount() {
        // Test: Verify all items are created in forEach
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        val expectedCount = AppDestinations.entries.size
        var actualCount = 0

        AppDestinations.entries.forEach { destination ->
            try {
                composeTestRule.onNodeWithText(destination.label).assertExists()
                actualCount++
            } catch (e: AssertionError) {
                // Item not found
            }
        }

        assert(actualCount == expectedCount) {
            "Expected $expectedCount items but found $actualCount"
        }
    }

    @Test
    fun navigationBar_clickingSameDestination_maintainsSelection() {
        // Test: Clicking already selected destination
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Click on already selected destination
        composeTestRule.onNodeWithText(AppDestinations.MAP.label).performClick()
        composeTestRule.waitForIdle()

        // Should still be selected
        assert(currentDestination.value == AppDestinations.MAP) {
            "Expected MAP but got ${currentDestination.value.label}"
        }

        composeTestRule.onNodeWithText(AppDestinations.MAP.label).assertIsSelected()
    }

    @Test
    fun navigationBar_allDestinations_haveCorrectLabelAndIcon() {
        // Comprehensive test: Verify all destinations have both label and icon
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        AppDestinations.entries.forEach { destination ->
            // Verify label exists
            composeTestRule.onNodeWithText(
                destination.label,
                useUnmergedTree = true
            ).assertExists()

            // Verify icon exists (via content description)
            composeTestRule.onNodeWithContentDescription(
                destination.label,
                useUnmergedTree = true
            ).assertExists()
        }
    }

    @Test
    fun navigationBar_stateChangeProgrammatically_updatesUI() {
        // Test: UI updates when state changes programmatically (not via click)
        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Change each destination programmatically
        AppDestinations.entries.forEach { destination ->
            currentDestination.value = destination
            composeTestRule.waitForIdle()

            // Verify selection updated
            composeTestRule.onNodeWithText(destination.label).assertIsSelected()
        }
    }

    @Test
    fun navigationBar_withContentAndNavigation_bothVisible() {
        // Test: Both navigation and content are visible simultaneously
        val contentText = "Custom Content Text"

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = currentDestination,
                    content = { modifier ->
                        Text(text = contentText, modifier = modifier)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Both navigation and content should be visible
        composeTestRule.onNodeWithText(AppDestinations.MAP.label).assertExists()
        composeTestRule.onNodeWithText(contentText).assertExists()
    }
}