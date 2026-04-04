package com.example.campusguide.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConcordiaScheduleRepository(
    private val apiService: ConcordiaApiService = ConcordiaApiClient.service
) {
    private val cache = mutableMapOf<String, List<CourseSchedule>>()

    suspend fun getSchedule(subject: String, termCode: String): List<CourseSchedule> = withContext(Dispatchers.IO) {
        val cacheKey = "${subject}_${termCode}"
        cache.getOrPut(cacheKey) { apiService.getSchedule(subject, termCode) }
    }
}
