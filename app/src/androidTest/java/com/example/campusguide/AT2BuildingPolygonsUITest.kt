package com.example.campusguide

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance Test for US-1.2: Render Campus Building Shapes (Polygons)
 *
 * Tests verify that building polygons are rendered on the map.
 * Visual verification of polygon rendering is done via GIF recording.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AT2BuildingPolygonsUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)


    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val composeTestRule = createEmptyComposeRule()


    @Test
    fun mapLoads_withBuildingPolygons() {
        // AC: Building polygons for campus are rendered on map
        // Visual verification: GIF shows polygons loaded with map
        Thread.sleep(3000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        composeTestRule.onNodeWithContentDescription("Loyola Campus").performClick()

        Thread.sleep(2000)

        composeTestRule.onNodeWithContentDescription("Left").performClick()
        composeTestRule.onNodeWithContentDescription("Left").performClick()

        Thread.sleep(2000)


    }
}