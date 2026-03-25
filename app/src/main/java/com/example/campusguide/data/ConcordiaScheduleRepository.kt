package com.example.campusguide.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConcordiaScheduleRepository(
    private val apiService: ConcordiaApiService = ConcordiaApiClient.service
) {
    private val cache = mutableMapOf<String, List<CourseSchedule>>()

    suspend fun getSchedule(subject: String, termCode: String): List<CourseSchedule> = withContext(Dispatchers.IO) {
        val cacheKey = "${subject}_${termCode}"
        if (cache.containsKey(cacheKey)) {
            return@withContext cache[cacheKey]!!
        }

        val schedule = apiService.getSchedule(subject, termCode)
        cache[cacheKey] = schedule
        schedule
    }
}
