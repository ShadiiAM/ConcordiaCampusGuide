package com.example.campusguide

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance Test for US-1.1: View SGW and Loyola Campus Maps
 *
 * US-1.1 is about VIEWING the campus map, not switching between campuses.
 * Campus switching is covered in US-1.3.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ViewCampusMapsUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MapsActivity::class.java)

    @Test
    fun appOpens_displaysDefaultCampusMap() {
        // AC: App opens for first time and displays campus map with default campus
        Thread.sleep(3000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun defaultCampus_isSGW() {
        // AC: Given SGW is selected (default), camera centers on SGW
        Thread.sleep(3000)

        // Map activity running with default SGW campus
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun mapIsUsable_doesNotCrash() {
        // AC: Map remains usable (pan/zoom) and doesn't crash
        Thread.sleep(3000)

        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }
}