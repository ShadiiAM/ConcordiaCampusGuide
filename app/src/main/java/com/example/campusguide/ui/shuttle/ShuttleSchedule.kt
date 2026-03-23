package com.example.campusguide.ui.shuttle

import com.example.campusguide.ui.components.Campus
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class DepartureTime(val hour: Int, val minute: Int) {
    val localTime: LocalTime get() = LocalTime.of(hour, minute)
    override fun toString() = "%02d:%02d".format(hour, minute)
}

sealed class DepartureResult {
    data class Soon(val departure: DepartureTime?) : DepartureResult()
    data class TooFarAway(val departure: DepartureTime?) : DepartureResult()  // still today, but hours away
    object NoMoreToday : DepartureResult()
}

object ShuttleSchedule {

    // Monday–Thursday
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

    // Friday
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
        val closestDeparture =  times.firstOrNull { it.localTime.isAfter(currentTime) }

        // Return null if next departure is more than 3 hours away
        if (closestDeparture != null) {
            val hoursUntilDeparture = Duration.between(currentTime, closestDeparture.localTime).toHours()
            if (hoursUntilDeparture > 2) {return DepartureResult.TooFarAway(closestDeparture)}
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
            DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> {
                val first = if (campus == Campus.SGW) sgwMonThur.first() else loyolaMonThur.first()
                Pair("Mon", first)
            }
            else -> {
                val nextDow = dow.plus(1)
                val isFriday = nextDow == DayOfWeek.FRIDAY
                val first = when (campus) {
                    Campus.SGW    -> if (isFriday) sgwFriday.first()    else sgwMonThur.first()
                    Campus.LOYOLA -> if (isFriday) loyolaFriday.first() else loyolaMonThur.first()
                }
                val label = nextDow.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
                Pair(label, first)
            }
        }
    }
}