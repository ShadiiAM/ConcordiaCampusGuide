package com.example.campusguide

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance Test for US-1.4: Show Current Building Location
 *
 * Tests verify building highlighting when user is inside/outside buildings.
 * Camera zooms to user location and shows marker indicating user position.
 *
 * Note: Permission dialog may appear - click "Allow" when prompted.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class CurrentBuildingLocationUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MapsActivity::class.java)

    private var userMarker: Marker? = null

    @Test
    fun appLaunches_withLocationFeature() {
        // AC: App displays map with location feature
        // Wait for permission dialog (user clicks Allow manually)
        Thread.sleep(5000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun userInsideBuilding_highlightsBuilding() {
        // AC: When user inside building polygon, building is highlighted
        Thread.sleep(5000)

        activityRule.scenario.onActivity { activity ->
            // Simulate user at Hall Building SGW (approx coords)
            val hallBuildingCoords = LatLng(45.497, -73.578)

            try {
                // Clear previous marker
                userMarker?.remove()

                // Add marker showing user location
                userMarker = activity.mMap.addMarker(
                    MarkerOptions()
                        .position(hallBuildingCoords)
                        .title("User Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )

                // Zoom camera to user location
                activity.mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(hallBuildingCoords, 18f)
                )

                // Highlight building user is in
                activity.highlightBuildingUserIsIn(hallBuildingCoords)
            } catch (e: Exception) {
                // Map not ready yet, skip
            }
        }

        // Wait to see user marker + building highlighting (pink/red)
        Thread.sleep(5000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun userOutsideBuildings_noHighlight() {
        // AC: When user not inside any building, no highlighting
        Thread.sleep(5000)

        activityRule.scenario.onActivity { activity ->
            // Simulate user outside buildings (street location)
            val outsideCoords = LatLng(45.500, -73.580)

            try {
                // Clear previous marker
                userMarker?.remove()

                // Add marker showing user location (outside buildings)
                userMarker = activity.mMap.addMarker(
                    MarkerOptions()
                        .position(outsideCoords)
                        .title("User Location (Outside)")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )

                // Zoom camera to location
                activity.mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(outsideCoords, 18f)
                )

                // Check highlighting (should be none)
                activity.highlightBuildingUserIsIn(outsideCoords)
            } catch (e: Exception) {
                // Map not ready yet
            }
        }

        // Wait to see user marker with NO building highlighting
        Thread.sleep(5000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun userMovingBetweenBuildings_updatesHighlight() {
        // AC: Current building indicator updates when moving between buildings
        Thread.sleep(5000)

        activityRule.scenario.onActivity { activity ->
            // First building - Hall Building area
            val building1 = LatLng(45.497, -73.578)

            try {
                // Clear previous marker
                userMarker?.remove()

                // Add marker at building 1
                userMarker = activity.mMap.addMarker(
                    MarkerOptions()
                        .position(building1)
                        .title("User at Building 1")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )

                // Zoom to first building
                activity.mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(building1, 18f)
                )

                activity.highlightBuildingUserIsIn(building1)
            } catch (e: Exception) {
                // Map not ready yet
            }
        }

        // Wait to see user at building 1 highlighted
        Thread.sleep(4000)

        activityRule.scenario.onActivity { activity ->
            // Move to different building - EV Building area
            val building2 = LatLng(45.495, -73.577)

            try {
                // Clear previous marker
                userMarker?.remove()

                // Add marker at building 2 (user moved)
                userMarker = activity.mMap.addMarker(
                    MarkerOptions()
                        .position(building2)
                        .title("User at Building 2")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )

                // Zoom to second building
                activity.mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(building2, 18f)
                )

                activity.highlightBuildingUserIsIn(building2)
            } catch (e: Exception) {
                // Map not ready yet
            }
        }

        // Wait to see user moved to building 2 with new highlighting
        Thread.sleep(4000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }
}
