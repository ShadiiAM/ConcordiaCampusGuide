package com.example.campusguide.data

import com.google.gson.annotations.SerializedName

data class CourseSchedule(
    @SerializedName("courseID") val courseID: String?,
    @SerializedName("termCode") val termCode: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("catalog") val catalog: String?,
    @SerializedName("section") val section: String?,
    @SerializedName("componentCode") val componentCode: String?,
    @SerializedName("classStartTime") val classStartTime: String?,
    @SerializedName("classEndTime") val classEndTime: String?,
    @SerializedName("classDays") val classDays: String?,
    @SerializedName("buildingCode") val buildingCode: String?,
    @SerializedName("roomNumber") val roomNumber: String?,
    @SerializedName("campus") val campus: String?
)
