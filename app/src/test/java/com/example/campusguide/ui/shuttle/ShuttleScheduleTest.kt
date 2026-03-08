package com.example.campusguide.ui.shuttle

import com.example.campusguide.ui.components.Campus
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

class ShuttleScheduleTest {

    private val montreal = ZoneId.of("America/Montreal")

    private fun timeOn(dow: DayOfWeek, hour: Int, minute: Int): ZonedDateTime {
        var dt = ZonedDateTime.of(2026, 1, 12, hour, minute, 0, 0, montreal)
        while (dt.dayOfWeek != dow) dt = dt.plusDays(1)
        return dt
    }

    // AC: displays upcoming departure times relevant to current time
    @Test
    fun nextDeparture_duringOperatingHours_returnsUpcomingTime() {
        val now = timeOn(DayOfWeek.MONDAY, 10, 0)
        val result = ShuttleSchedule.nextDeparture(Campus.SGW, now)
        assertNotNull(result)
        assertTrue(result!!.localTime.isAfter(now.toLocalTime()))
    }

    // AC: clearly indicates "next departure" for SGW
    @Test
    fun nextDeparture_sgw_returnsCorrectNextTime() {
        val now = timeOn(DayOfWeek.MONDAY, 9, 0)
        assertEquals(DepartureTime(9, 30), ShuttleSchedule.nextDeparture(Campus.SGW, now))
    }

    // AC: clearly indicates "next departure" for Loyola
    @Test
    fun nextDeparture_loyola_returnsCorrectNextTime() {
        val now = timeOn(DayOfWeek.MONDAY, 9, 0)
        assertEquals(DepartureTime(9, 15), ShuttleSchedule.nextDeparture(Campus.LOYOLA, now))
    }

    // AC: outside operating hours shows "No more shuttles today"
    @Test
    fun nextDeparture_afterLastBus_returnsNull() {
        val now = timeOn(DayOfWeek.MONDAY, 18, 31)
        assertNull(ShuttleSchedule.nextDeparture(Campus.SGW, now))
        assertNull(ShuttleSchedule.nextDeparture(Campus.LOYOLA, now))
    }

    // AC: outside operating hours — weekend
    @Test
    fun nextDeparture_weekend_returnsNull() {
        val now = timeOn(DayOfWeek.SATURDAY, 10, 0)
        assertNull(ShuttleSchedule.nextDeparture(Campus.SGW, now))
        assertNull(ShuttleSchedule.nextDeparture(Campus.LOYOLA, now))
    }

    // AC: next departure on next operating day shown when no more today
    @Test
    fun nextDepartureNextDay_afterLastBus_returnsNextDayTime() {
        val now = timeOn(DayOfWeek.MONDAY, 19, 0)
        val result = ShuttleSchedule.nextDepartureNextDay(Campus.SGW, now)
        assertNotNull(result)
        assertEquals("Tue", result!!.first)
    }

    // AC: Friday wraps to Monday
    @Test
    fun nextDepartureNextDay_friday_returnsMonday() {
        val now = timeOn(DayOfWeek.FRIDAY, 19, 0)
        val result = ShuttleSchedule.nextDepartureNextDay(Campus.SGW, now)
        assertEquals("Mon", result!!.first)
    }

    // AC: Friday uses correct schedule (different from Mon–Thu)
    @Test
    fun nextDeparture_friday_usesFridaySchedule() {
        val now = timeOn(DayOfWeek.FRIDAY, 9, 0)
        assertEquals(DepartureTime(9, 45), ShuttleSchedule.nextDeparture(Campus.SGW, now))
    }

    // AC: schedule data is available (non-empty)
    @Test
    fun scheduleData_allListsNonEmpty() {
        assertTrue(ShuttleSchedule.sgwMonThur.isNotEmpty())
        assertTrue(ShuttleSchedule.loyolaMonThur.isNotEmpty())
        assertTrue(ShuttleSchedule.sgwFriday.isNotEmpty())
        assertTrue(ShuttleSchedule.loyolaFriday.isNotEmpty())
    }

    // AC: times are in order (screen reader reads them top-to-bottom correctly)
    @Test
    fun scheduleData_timesAreSortedAscending() {
        listOf(
            ShuttleSchedule.sgwMonThur,
            ShuttleSchedule.loyolaMonThur,
            ShuttleSchedule.sgwFriday,
            ShuttleSchedule.loyolaFriday
        ).forEach { list ->
            val times = list.map { it.localTime }
            assertEquals(times, times.sorted())
        }
    }
}