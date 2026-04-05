package com.example.campusguide.data

import retrofit2.http.GET
import retrofit2.http.Path

interface ConcordiaApiService {
    @GET("API/v1/course/scheduleTerm/filter/{subject}/{termCode}")
    suspend fun getSchedule(
        @Path("subject") subject: String,
        @Path("termCode") termCode: String
    ): List<CourseSchedule>
}
