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

/**
 * Production implementation that calls the Concordia Open Data API.
 * Uses Basic auth credentials stored in BuildConfig.
 */
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
        val cleanSubject = subject.trim().uppercase()
        val cleanCatalog = catalog.trim().uppercase()
        // Wildcard for termCode in the URL; we filter the response ourselves because
        // the API does not expose a reliable courseID for direct lookup.
        val url = "https://opendata.concordia.ca/API/v1/course/schedule/filter/*/$cleanSubject/$cleanCatalog"

        val authHeader = Credentials.basic(BuildConfig.CONCORDIA_API_USER, BuildConfig.CONCORDIA_API_KEY)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", authHeader)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("CalendarRepo", "API Error: ${response.code} - ${response.message}")
                // 4xx usually means the course doesn't exist; return empty rather than throwing
                if (response.code in 400..499) return@withContext emptyList()
                throw IOException("Server Error: ${response.code}")
            }

            val body = response.body?.string()?.takeIf { it.isNotBlank() } ?: return@withContext emptyList()
            val results = json.decodeFromString<List<Course>>(body)

            // The API returns all sections; narrow down to the requested term and section
            results.filter {
                it.termCode.trim() == termCode.trim() && it.section.trim().equals(section.trim(), ignoreCase = true)
            }
        }
    }
}
