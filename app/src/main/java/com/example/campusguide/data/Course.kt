package com.example.campusguide.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.Calendar

@Serializable
data class Course(
    val subject: String,
    val termCode: String,
    val courseID: String,
    val catalog: String,
    val section: String,
    val componentCode: String,
    val classNumber: String,
    val courseTitle: String,
    val locationCode: String,
    val buildingCode: String,
    val room: String,
    @SerialName("classStartTime") val startTime: String,
    @SerialName("classEndTime") val endTime: String,
    @SerialName("classStartDate") val startDate: String,
    @SerialName("classEndDate") val endDate: String,
    @SerialName("modays") val mondays: String,
    val tuesdays: String,
    val wednesdays: String,
    val thursdays: String,
    val fridays: String,
    val saturdays: String,
    val sundays: String
) {
    fun meetsOn(dayOfWeek: Int): Boolean {
        return when (dayOfWeek) {
            Calendar.MONDAY -> mondays == "Y"
            Calendar.TUESDAY -> tuesdays == "Y"
            Calendar.WEDNESDAY -> wednesdays == "Y"
            Calendar.THURSDAY -> thursdays == "Y"
            Calendar.FRIDAY -> fridays == "Y"
            Calendar.SATURDAY -> saturdays == "Y"
            Calendar.SUNDAY -> sundays == "Y"
            else -> false
        }
    }
}
