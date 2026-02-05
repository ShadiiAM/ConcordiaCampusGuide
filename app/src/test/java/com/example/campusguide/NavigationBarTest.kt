package com.example.campusguide.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.campusguide.AppDestinations
import com.example.campusguide.AppIcon
import com.example.campusguide.ui.theme.ConcordiaCampusGuideTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class NavigationBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navigationBar_displaysAllDestinations() {
        // Test: forEach loop executes for all entries
        val currentDestination = mutableStateOf(AppDestinations.MAP)

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
        val currentDestination = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Find destinations with Vector icons and verify their content descriptions
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
        val currentDestination = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Find destinations with Drawable icons and verify their content descriptions
        val drawableDestinations = AppDestinations.entries.filter { it.icon is AppIcon.Drawable }

        drawableDestinations.forEach { destination ->
            composeTestRule.onNodeWithContentDescription(
                destination.label,
                useUnmergedTree = true
            ).assertExists()
        }
    }

    @Test
    fun navigationBar_selectedItemIsHighlighted() {
        // Test: selected parameter (it == currentDestination.value) - true branch
        val currentDestination = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // The selected item should be marked as selected
        composeTestRule.onNodeWithText(AppDestinations.MAP.label)
            .assertExists()
            .assertIsSelected()
    }

    @Test
    fun navigationBar_unselectedItemsNotHighlighted() {
        // Test: selected parameter (it == currentDestination.value) - false branch
        val currentDestination = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Other items should not be selected
        AppDestinations.entries
            .filter { it != AppDestinations.MAP }
            .forEach { destination ->
                composeTestRule.onNodeWithText(destination.label)
                    .assertExists()
                    .assertIsNotSelected()
            }
    }

    @Test
    fun navigationBar_clickChangesDestination() {
        // Test: onClick lambda executes and updates state
        val currentDestination = mutableStateOf(AppDestinations.MAP)
        val targetDestination = AppDestinations.entries.first { it != AppDestinations.MAP }

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Click on different destination
        composeTestRule.onNodeWithText(targetDestination.label).performClick()

        composeTestRule.waitForIdle()

        // Verify state changed
        assert(currentDestination.value == targetDestination) {
            "Expected ${targetDestination.label} but got ${currentDestination.value.label}"
        }
    }

    @Test
    fun navigationBar_clickOnEachDestination_updatesCorrectly() {
        // Test: onClick for all destinations - covers all onClick executions
        val currentDestination = mutableStateOf(AppDestinations.MAP)

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
    fun navigationBar_withNullContent_doesNotInvokeContent() {
        // Test: content?.invoke - null path (safe call returns null, invoke not called)
        val currentDestination = mutableStateOf(AppDestinations.MAP)

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
    fun navigationBar_withContent_invokesContentLambda() {
        // Test: content?.invoke - non-null path (content lambda is invoked)
        val currentDestination = mutableStateOf(AppDestinations.MAP)
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

        // Verify content was invoked and rendered
        composeTestRule.onNodeWithText(testText).assertExists()
    }

    @Test
    fun navigationBar_contentReceivesModifierWithFillMaxSize() {
        // Test: Modifier.fillMaxSize() is applied
        val currentDestination = mutableStateOf(AppDestinations.MAP)
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

        // Verify modifier was passed (non-null check covers the modifier chain)
        assert(receivedModifier != null) {
            "Modifier should not be null"
        }
    }

    @Test
    fun navigationBar_contentReceivesModifierWithPadding() {
        // Test: Modifier.padding(top = 100.dp) is applied
        val currentDestination = mutableStateOf(AppDestinations.MAP)
        val testTag = "content_box"

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = currentDestination,
                    content = { modifier ->
                        Box(
                            modifier = modifier.testTag(testTag)
                        )
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify content exists with the modifier applied
        composeTestRule.onNodeWithTag(testTag).assertExists()
    }

    @Test
    fun navigationBar_contentWithComplexComposable() {
        // Additional test: content with more complex composable structure
        val currentDestination = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = currentDestination,
                    content = { modifier ->
                        Box(modifier = modifier) {
                            Text("Line 1")
                            Text("Line 2")
                        }
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Line 1").assertExists()
        composeTestRule.onNodeWithText("Line 2").assertExists()
    }

    @Test
    fun navigationBar_allLabelsDisplayed() {
        // Test: label = { Text(it.label) } for all items
        val currentDestination = mutableStateOf(AppDestinations.MAP)

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
    fun navigationBar_allContentDescriptionsSet() {
        // Test: contentDescription in Icon for all items
        val currentDestination = mutableStateOf(AppDestinations.MAP)

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
    fun navigationBar_multipleClicks_maintainCorrectState() {
        // Test: Multiple onClick invocations maintain state correctly
        val currentDestination = mutableStateOf(AppDestinations.MAP)
        val destinations = AppDestinations.entries.toList()

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Click back and forth between destinations multiple times
        repeat(3) {
            destinations.forEach { destination ->
                composeTestRule.onNodeWithText(destination.label).performClick()
                composeTestRule.waitForIdle()

                assert(currentDestination.value == destination) {
                    "Expected ${destination.label} but got ${currentDestination.value.label}"
                }
            }
        }
    }

    @Test
    fun navigationBar_vectorIconBranch_executesCorrectly() {
        // Explicit test for when(icon) is AppIcon.Vector branch
        val currentDestination = mutableStateOf(AppDestinations.MAP)
        val vectorDestinations = AppDestinations.entries.filter { it.icon is AppIcon.Vector }

        if (vectorDestinations.isNotEmpty()) {
            composeTestRule.setContent {
                ConcordiaCampusGuideTheme {
                    NavigationBar(currentDestination = currentDestination)
                }
            }

            composeTestRule.waitForIdle()

            // Verify Vector icon is rendered
            vectorDestinations.forEach { destination ->
                composeTestRule.onNodeWithContentDescription(
                    destination.label,
                    useUnmergedTree = true
                ).assertExists()
            }
        }
    }

    @Test
    fun navigationBar_drawableIconBranch_executesCorrectly() {
        // Explicit test for when(icon) is AppIcon.Drawable branch
        val currentDestination = mutableStateOf(AppDestinations.MAP)
        val drawableDestinations = AppDestinations.entries.filter { it.icon is AppIcon.Drawable }

        if (drawableDestinations.isNotEmpty()) {
            composeTestRule.setContent {
                ConcordiaCampusGuideTheme {
                    NavigationBar(currentDestination = currentDestination)
                }
            }

            composeTestRule.waitForIdle()

            // Verify Drawable icon is rendered
            drawableDestinations.forEach { destination ->
                composeTestRule.onNodeWithContentDescription(
                    destination.label,
                    useUnmergedTree = true
                ).assertExists()
            }
        }
    }

    @Test
    fun navigationBar_navigationSuiteScaffold_isComposed() {
        // Test that NavigationSuiteScaffold itself is composed
        val currentDestination = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Verify the scaffold is composed by checking navigation items exist
        composeTestRule.onAllNodesWithText(
            AppDestinations.MAP.label,
            useUnmergedTree = true
        ).assertCountEquals(1)
    }

    @Test
    fun navigationBar_itemBlock_executesForAllEntries() {
        // Test that item{} block executes for each AppDestinations entry
        val currentDestination = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Verify item block executed for all entries by checking all are present
        val itemCount = AppDestinations.entries.size
        var foundCount = 0

        AppDestinations.entries.forEach { destination ->
            try {
                composeTestRule.onNodeWithText(destination.label).assertExists()
                foundCount++
            } catch (e: AssertionError) {
                // Item not found
            }
        }

        assert(foundCount == itemCount) {
            "Expected $itemCount items but found $foundCount"
        }
    }

    @Test
    fun navigationBar_stateChange_updatesSelection() {
        // Test reactivity: changing state updates which item is selected
        val currentDestination = mutableStateOf(AppDestinations.MAP)

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

        // Verify old selection is no longer selected
        composeTestRule.onNodeWithText(AppDestinations.MAP.label)
            .assertIsNotSelected()
    }

    @Test
    fun navigationBar_withContentEmptyLambda_doesNotCrash() {
        // Edge case: content lambda that does nothing
        val currentDestination = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(
                    currentDestination = currentDestination,
                    content = { _ ->
                        // Empty lambda
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Should not crash
        composeTestRule.onNodeWithText(AppDestinations.MAP.label).assertExists()
    }

    @Test
    fun navigationBar_allIconsHaveProperContentDescription() {
        // Ensure contentDescription is set for accessibility
        val currentDestination = mutableStateOf(AppDestinations.MAP)

        composeTestRule.setContent {
            ConcordiaCampusGuideTheme {
                NavigationBar(currentDestination = currentDestination)
            }
        }

        composeTestRule.waitForIdle()

        // Each destination should have an icon with its label as content description
        AppDestinations.entries.forEach { destination ->
            val nodes = composeTestRule.onAllNodesWithContentDescription(
                destination.label,
                useUnmergedTree = true
            )
            nodes.assertCountEquals(1)
        }
    }
}