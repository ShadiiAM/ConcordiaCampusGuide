package com.example.campusguide.ui.shuttle

import com.example.campusguide.ui.components.Campus
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** Compact representation of a scheduled bus departure. */
data class DepartureTime(val hour: Int, val minute: Int) {
    val localTime: LocalTime get() = LocalTime.of(hour, minute)
    override fun toString() = "%02d:%02d".format(hour, minute)
}

/** Result of a next-departure lookup, split by how far away the bus is. */
sealed class DepartureResult {
    /** The next departure is within 30 minutes. */
    data class Soon(val departure: DepartureTime?) : DepartureResult()
    /** There is a departure today but it is more than 30 minutes away. */
    data class TooFarAway(val departure: DepartureTime?) : DepartureResult()
    /** No more departures today (weekend or after the last run). */
    object NoMoreToday : DepartureResult()
}

/**
 * Static Concordia shuttle timetable.
 * Schedules differ by campus and by weekday (Mon-Thu vs Friday).
 * The shuttle does not operate on weekends.
 */
object ShuttleSchedule {

    // Monday-Thursday departures from Loyola campus
    val loyolaMonThur = listOf(
        DepartureTime(9,15), DepartureTime(9,30), DepartureTime(9,45),
        DepartureTime(10,0), DepartureTime(10,15), DepartureTime(10,30),
        DepartureTime(10,45), DepartureTime(11,0), DepartureTime(11,15),
        DepartureTime(11,30), DepartureTime(11,45), DepartureTime(12,30),
        DepartureTime(12,45), DepartureTime(13,0), DepartureTime(13,15),
        DepartureTime(13,30), DepartureTime(13,45), DepartureTime(14,0),
        DepartureTime(14,15), DepartureTime(14,30), DepartureTime(14,45),
        DepartureTime(15,0), DepartureTime(15,15), DepartureTime(15,30),
        DepartureTime(15,45), DepartureTime(16,30), DepartureTime(16,45),
        DepartureTime(17,0), DepartureTime(17,15), DepartureTime(17,30),
        DepartureTime(17,45), DepartureTime(18,0), DepartureTime(18,15),
        DepartureTime(18,30)
    )

    // Monday-Thursday departures from SGW campus
    val sgwMonThur = listOf(
        DepartureTime(9,30), DepartureTime(9,45), DepartureTime(10,0),
        DepartureTime(10,15), DepartureTime(10,30), DepartureTime(10,45),
        DepartureTime(11,0), DepartureTime(11,15), DepartureTime(11,30),
        DepartureTime(12,15), DepartureTime(12,30), DepartureTime(12,45),
        DepartureTime(13,0), DepartureTime(13,15), DepartureTime(13,30),
        DepartureTime(13,45), DepartureTime(14,0), DepartureTime(14,15),
        DepartureTime(14,30), DepartureTime(14,45), DepartureTime(15,0),
        DepartureTime(15,15), DepartureTime(15,30), DepartureTime(16,0),
        DepartureTime(16,15), DepartureTime(16,45), DepartureTime(17,0),
        DepartureTime(17,15), DepartureTime(17,30), DepartureTime(17,45),
        DepartureTime(18,0), DepartureTime(18,15), DepartureTime(18,30)
    )

    // Friday departures from Loyola campus (fewer runs than Mon-Thu)
    val loyolaFriday = listOf(
        DepartureTime(9,15), DepartureTime(9,30), DepartureTime(9,45),
        DepartureTime(10,15), DepartureTime(10,45), DepartureTime(11,0),
        DepartureTime(11,15), DepartureTime(12,0), DepartureTime(12,15),
        DepartureTime(12,45), DepartureTime(13,0), DepartureTime(13,15),
        DepartureTime(13,45), DepartureTime(14,15), DepartureTime(14,30),
        DepartureTime(14,45), DepartureTime(15,15), DepartureTime(15,30),
        DepartureTime(15,45), DepartureTime(16,45), DepartureTime(17,15),
        DepartureTime(17,45), DepartureTime(18,15)
    )

    // Friday departures from SGW campus
    val sgwFriday = listOf(
        DepartureTime(9,45), DepartureTime(10,0), DepartureTime(10,15),
        DepartureTime(10,45), DepartureTime(11,15), DepartureTime(11,30),
        DepartureTime(12,15), DepartureTime(12,30), DepartureTime(12,45),
        DepartureTime(13,15), DepartureTime(13,45), DepartureTime(14,0),
        DepartureTime(14,15), DepartureTime(14,45), DepartureTime(15,0),
        DepartureTime(15,15), DepartureTime(15,45), DepartureTime(16,0),
        DepartureTime(16,45), DepartureTime(17,15), DepartureTime(17,45),
        DepartureTime(18,15)
    )

    /**
     * Returns the next departure time for a given campus in real time.
     * Returns null if no more departures today (weekend or after last bus).
     */
    fun nextDeparture(campus: Campus, now: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Montreal"))): DepartureResult {
        val dow = now.dayOfWeek
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return DepartureResult.NoMoreToday

        val times = when (campus) {
            Campus.SGW     -> if (dow == DayOfWeek.FRIDAY) sgwFriday    else sgwMonThur
            Campus.LOYOLA  -> if (dow == DayOfWeek.FRIDAY) loyolaFriday else loyolaMonThur
        }

        val currentTime = now.toLocalTime()
        // Find the first departure that hasn't happened yet
        val closestDeparture = times.firstOrNull { it.localTime.isAfter(currentTime) }

        if (closestDeparture != null) {
            val minutesUntilDeparture = Duration.between(currentTime, closestDeparture.localTime).toMinutes()
            // Only surface the bus as "Soon" if it departs within 30 minutes
            if (minutesUntilDeparture > 30) {return DepartureResult.TooFarAway(closestDeparture)}
            return DepartureResult.Soon(closestDeparture)
        }
        else{
            return DepartureResult.NoMoreToday
        }
    }

    /**
     * Returns the next departure on the next operating day, for the
     * "No more departures today. Next departure: DAY HH:mm" message.
     */
    fun nextDepartureNextDay(campus: Campus, now: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Montreal"))): Pair<String, DepartureTime>? {
        return when (val dow = now.dayOfWeek) {
            // Friday/weekend: next operating day is always Monday
            DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> {
                val first = if (campus == Campus.SGW) sgwMonThur.first() else loyolaMonThur.first()
                Pair("Mon", first)
            }
            else -> {
                // Mon-Thu: advance to the next weekday and pick the right schedule
                val nextDow = dow.plus(1)
                val isFriday = nextDow == DayOfWeek.FRIDAY
                val first = when (campus) {
                    Campus.SGW    -> if (isFriday) sgwFriday.first()    else sgwMonThur.first()
                    Campus.LOYOLA -> if (isFriday) loyolaFriday.first() else loyolaMonThur.first()
                }
                // Shorten day name to 3 chars, e.g. "Wednesday" -> "Wed"
                val label = nextDow.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
                Pair(label, first)
            }
        }
    }
}