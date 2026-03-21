package com.example.campusguide.data

import com.example.campusguide.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import android.util.Log
import java.io.IOException

interface CalendarRepository {
    /**
     * Fetches and filters courses.
     * @throws IOException for network or server errors.
     */
    suspend fun fetchAndFilterCourse(
        subject: String,
        catalog: String,
        termCode: String,
        section: String
    ): List<Course>
}

class CalendarRepositoryImpl(private val client: OkHttpClient = OkHttpClient()) : CalendarRepository {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    override suspend fun fetchAndFilterCourse(
        subject: String,
        catalog: String,
        termCode: String,
        section: String
    ): List<Course> = withContext(Dispatchers.IO) {
        val url = "https://opendata.concordia.ca/API/v1/course/schedule/filter/*/$subject/$catalog" // did not use courseID because value is hard to find

        /*
        to check what subject and catalog value is being used for the api
        Log.d("CalendarRepo", "REQUESTING URL: $url")
         */

        val authHeader = Credentials.basic(BuildConfig.CONCORDIA_API_USER, BuildConfig.CONCORDIA_API_KEY)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", authHeader)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("CalendarRepo", "API Error: ${response.code} - ${response.message}")
                if (response.code in 400..499) return@withContext emptyList()
                throw IOException("Server Error: ${response.code}")
            }
            
            val body = response.body?.string() ?: return@withContext emptyList()
            val results = json.decodeFromString<List<Course>>(body)
            
            results.filter {
                it.termCode == termCode && it.section.trim().equals(section.trim(), ignoreCase = true)
            }
        }
    }
}
