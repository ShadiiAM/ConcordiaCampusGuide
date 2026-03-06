package com.example.campusguide.ui.shuttle

import com.example.campusguide.ui.components.Campus
import org.junit.Assert.*
import org.junit.Test

class StaticShuttleDataSourceTest {

    private val dataSource = StaticShuttleDataSource()

    @Test
    fun `returns exactly three stops`() {
        assertEquals(3, dataSource.getShuttleStops().size)
    }

    @Test
    fun `SGW stop has correct id and campus`() {
        val stop = dataSource.getShuttleStops().first { it.campus == Campus.SGW }
        assertEquals("sgw_shuttle", stop.id)
        assertEquals(Campus.SGW, stop.campus)
    }

    @Test
    fun `two Loyola stops exist`() {
        val loyolaStops = dataSource.getShuttleStops().filter { it.campus == Campus.LOYOLA }
        assertEquals(2, loyolaStops.size)
    }

    @Test
    fun `Loyola arrival stop has correct id`() {
        val stop = dataSource.getShuttleStops().first { it.id == "loyola_shuttle_arrival" }
        assertEquals(Campus.LOYOLA, stop.campus)
    }

    @Test
    fun `Loyola departure stop has correct id`() {
        val stop = dataSource.getShuttleStops().first { it.id == "loyola_shuttle_departure" }
        assertEquals(Campus.LOYOLA, stop.campus)
    }

    @Test
    fun `all stops have non-null latLng`() {
        dataSource.getShuttleStops().forEach { stop ->
            assertNotNull("LatLng should not be null for stop ${stop.id}", stop.latLng)
        }
    }

    @Test
    fun `all stops have non-blank name and description`() {
        dataSource.getShuttleStops().forEach { stop ->
            assertTrue("Name should not be blank for ${stop.id}", stop.name.isNotBlank())
            assertTrue("Description should not be blank for ${stop.id}", stop.description.isNotBlank())
        }
    }

    @Test
    fun `SGW stop latLng matches Hall Building coordinates`() {
        val stop = dataSource.getShuttleStops().first { it.campus == Campus.SGW }
        assertEquals(45.4971, stop.latLng.latitude, 0.0001)
        assertEquals(-73.5785, stop.latLng.longitude, 0.0001)
    }

    @Test
    fun `Loyola arrival and departure stops have distinct coordinates`() {
        val arrival = dataSource.getShuttleStops().first { it.id == "loyola_shuttle_arrival" }
        val departure = dataSource.getShuttleStops().first { it.id == "loyola_shuttle_departure" }
        assertNotEquals(arrival.latLng.latitude, departure.latLng.latitude, 0.0)
    }
}
