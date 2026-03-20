package com.example.campusguide.ui.shuttle

import com.example.campusguide.data.ShuttleStop
import com.example.campusguide.data.Suggestion
import com.example.campusguide.data.SuggestionData
import com.example.campusguide.ui.components.Campus
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test

class ShuttleStopTest {

    @Test
    fun `default description is set when not provided`() {
        val stop = ShuttleStop(
            id = "test_stop",
            name = "Test Stop",
            campus = Campus.SGW,
            latLng = LatLng(45.0, -73.0)
        )
        assertEquals("Concordia Shuttle Service", stop.description)
    }

    @Test
    fun `custom description overrides default`() {
        val stop = ShuttleStop(
            id = "test_stop",
            name = "Test Stop",
            campus = Campus.SGW,
            latLng = LatLng(45.0, -73.0),
            description = "Custom description"
        )
        assertEquals("Custom description", stop.description)
    }

    @Test
    fun `data class equality works on same values`() {
        val latlng = LatLng(45.4972, -73.5789)
        val stop1 = ShuttleStop("sgw_shuttle", "SGW Shuttle Stop", "Concordia Shuttle Service", Campus.SGW, latlng)
        val stop2 = ShuttleStop("sgw_shuttle", "SGW Shuttle Stop", "Concordia Shuttle Service", Campus.SGW, latlng)


        assertEquals(stop1, stop2)
    }

    @Test
    fun `data class inequality on different id`() {
        val latlng = LatLng(45.4972, -73.5789)
        val stop1 = ShuttleStop("sgw_shuttle", "SGW Shuttle Stop", "Concordia Shuttle Service",Campus.SGW, latlng)
        val stop2 = ShuttleStop("loyola_shuttle", "SGW Shuttle Stop", "Concordia Shuttle Service", Campus.SGW, latlng)
        assertNotEquals(stop1, stop2)
    }
// Unneeded test
//    @Test
//    fun `copy produces independent instance`() {
//        val original = ShuttleStop("sgw_shuttle", "SGW", "Concordia Shuttle Service", Campus.SGW, LatLng(45.0, -73.0))
//        val copy = original.copy(name = "Modified")
//        assertEquals("SGW", original.name)
//        assertEquals("Modified", copy.name)
//    }
}
