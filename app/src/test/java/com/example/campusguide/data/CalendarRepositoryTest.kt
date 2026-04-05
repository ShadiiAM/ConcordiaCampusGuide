package com.example.campusguide.data

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CalendarRepositoryTest {

    private val server = MockWebServer()
    private lateinit var repository: CalendarRepositoryImpl

    // Redirects all requests to MockWebServer while preserving the path
    private fun mockClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request().url
            val redirected = original.newBuilder()
                .scheme(server.url("/").scheme)
                .host(server.url("/").host)
                .port(server.url("/").port)
                .build()
            chain.proceed(chain.request().newBuilder().url(redirected).build())
        }
        .build()

    @Before
    fun setUp() {
        server.start()
        repository = CalendarRepositoryImpl(mockClient())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // Returns valid JSON for a single course matching the given term/section
    private fun courseJson(
        termCode: String = "2244",
        section: String = "UU",
        subject: String = "SOEN",
        catalog: String = "390"
    ) = """[{
        "subject": "$subject",
        "termCode": "$termCode",
        "courseID": "1",
        "catalog": "$catalog",
        "section": "$section",
        "componentCode": "LEC",
        "classNumber": "1234",
        "courseTitle": "Software Engineering",
        "locationCode": "SGW",
        "buildingCode": "H",
        "room": "110",
        "classStartTime": "08:45:00",
        "classEndTime": "11:35:00",
        "classStartDate": "2025-01-01",
        "classEndDate": "2025-04-30",
        "modays": "N",
        "tuesdays": "N",
        "wednesdays": "Y",
        "thursdays": "N",
        "fridays": "N",
        "saturdays": "N",
        "sundays": "N"
    }]"""

    @Test
    fun `returns matching courses on success`() = runTest {
        server.enqueue(MockResponse().setBody(courseJson()).setResponseCode(200))

        val result = repository.fetchAndFilterCourse("SOEN", "390", "2244", "UU")

        assertEquals(1, result.size)
        assertEquals("SOEN", result[0].subject)
        assertEquals("2244", result[0].termCode)
        assertEquals("UU", result[0].section)
    }

    @Test
    fun `filters out courses that do not match term code`() = runTest {
        val body = courseJson(termCode = "2241") // different term
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))

        val result = repository.fetchAndFilterCourse("SOEN", "390", "2244", "UU")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `filters out courses that do not match section`() = runTest {
        val body = courseJson(section = "WW") // different section
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))

        val result = repository.fetchAndFilterCourse("SOEN", "390", "2244", "UU")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `section matching is case insensitive`() = runTest {
        val body = courseJson(section = "uu") // lowercase in API response
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))

        val result = repository.fetchAndFilterCourse("SOEN", "390", "2244", "UU")

        assertEquals(1, result.size)
    }

    @Test
    fun `returns empty list when API returns 404`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val result = repository.fetchAndFilterCourse("ZZZZ", "999", "9999", "ZZ")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty list when API returns any 4xx error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))

        val result = repository.fetchAndFilterCourse("SOEN", "390", "2244", "UU")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `throws IOException when API returns 5xx error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        var thrown: IOException? = null
        try {
            repository.fetchAndFilterCourse("SOEN", "390", "2244", "UU")
        } catch (e: IOException) {
            thrown = e
        }

        assertTrue("Expected IOException for 500 response", thrown != null)
    }

    @Test
    fun `returns empty list when response body is empty`() = runTest {
        server.enqueue(MockResponse().setBody("").setResponseCode(200))

        val result = repository.fetchAndFilterCourse("SOEN", "390", "2244", "UU")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty list when response body is empty array`() = runTest {
        server.enqueue(MockResponse().setBody("[]").setResponseCode(200))

        val result = repository.fetchAndFilterCourse("SOEN", "390", "2244", "UU")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `trims and uppercases subject in request URL`() = runTest {
        server.enqueue(MockResponse().setBody("[]").setResponseCode(200))

        repository.fetchAndFilterCourse("  soen  ", "390", "2244", "UU")

        val path = server.takeRequest().path ?: ""
        assertTrue("URL should contain uppercased SOEN", path.contains("/SOEN/"))
    }

    @Test
    fun `trims and uppercases catalog in request URL`() = runTest {
        server.enqueue(MockResponse().setBody("[]").setResponseCode(200))

        repository.fetchAndFilterCourse("SOEN", " 390 ", "2244", "UU")

        val path = server.takeRequest().path ?: ""
        assertTrue("URL should contain trimmed catalog 390", path.contains("/390"))
    }

    @Test
    fun `returns multiple matching courses from same response`() = runTest {
        val body = """[
            ${courseJson(section = "UU").trimStart('[').trimEnd(']')},
            ${courseJson(section = "UU", subject = "SOEN").trimStart('[').trimEnd(']')}
        ]"""
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))

        val result = repository.fetchAndFilterCourse("SOEN", "390", "2244", "UU")

        assertEquals(2, result.size)
    }
}
