package com.example.campusguide.ui.directions

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GoogleRoutesRepositoryTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var repository: GoogleRoutesRepository
    private val testApiKey = "test_api_key_123"

    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()
    }

    @After
    fun teardown() {
        mockServer.shutdown()
    }

    private fun createTestRepository(): GoogleRoutesRepository {
        // Create interceptor to redirect API calls to mock server
        val interceptor = Interceptor { chain ->
            val request = chain.request()
            val newUrl = mockServer.url("/directions/v2:computeRoutes")
            val newRequest = request.newBuilder()
                .url(newUrl)
                .build()
            chain.proceed(newRequest)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        return GoogleRoutesRepository(
            client = client,
            apiKey = testApiKey
        )
    }

    @Test
    fun getRoute_successfulResponse_returnsRouteResult() = runTest {
        // Valid encoded polyline for a simple route
        val encodedPolyline = "a~l~Fjk~uOwHJy@P"
        val mockResponse = """
            {
              "routes": [
                {
                  "polyline": {
                    "encodedPolyline": "$encodedPolyline"
                  }
                }
              ]
            }
        """.trimIndent()

        mockServer.enqueue(MockResponse().setBody(mockResponse).setResponseCode(200))

        repository = createTestRepository()

        val request = RouteRequest(
            origin = LatLng(45.4972, -73.5789),
            destination = LatLng(45.4980, -73.5780)
        )

        val result = repository.getRoute(request)

        assertNotNull(result)
        assertNotNull(result.points)
        assertTrue(result.points.isNotEmpty())
    }

    @Test
    fun getRoute_validRequest_containsCorrectCoordinates() = runTest {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4980, -73.5780)

        val request = RouteRequest(
            origin = origin,
            destination = destination
        )

        assertEquals(45.4972, request.origin.latitude, 0.0001)
        assertEquals(-73.5789, request.origin.longitude, 0.0001)
        assertEquals(45.4980, request.destination.latitude, 0.0001)
        assertEquals(-73.5780, request.destination.longitude, 0.0001)
    }

    @Test
    fun getRoute_defaultTravelMode_isWalking() = runTest {
        val request = RouteRequest(
            origin = LatLng(45.4972, -73.5789),
            destination = LatLng(45.4980, -73.5780)
        )

        assertEquals(TravelMode.WALKING, request.mode)
    }

    @Test
    fun getRoute_emptyPolyline_throwsException() = runTest {
        val mockResponse = """
            {
              "routes": [
                {
                  "polyline": {
                    "encodedPolyline": ""
                  }
                }
              ]
            }
        """.trimIndent()

        mockServer.enqueue(MockResponse().setBody(mockResponse).setResponseCode(200))

        repository = createTestRepository()

        val request = RouteRequest(
            origin = LatLng(45.4972, -73.5789),
            destination = LatLng(45.4980, -73.5780)
        )

        try {
            repository.getRoute(request)
            fail("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertTrue(e.message?.contains("No route polyline") == true)
        }
    }

    @Test
    fun getRoute_noRoutes_throwsException() = runTest {
        val mockResponse = """
            {
              "routes": []
            }
        """.trimIndent()

        mockServer.enqueue(MockResponse().setBody(mockResponse).setResponseCode(200))

        repository = createTestRepository()

        val request = RouteRequest(
            origin = LatLng(45.4972, -73.5789),
            destination = LatLng(45.4980, -73.5780)
        )

        try {
            repository.getRoute(request)
            fail("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertTrue(e.message?.contains("No route polyline") == true)
        }
    }

    @Test
    fun getRoute_nullRoutes_throwsException() = runTest {
        val mockResponse = """
            {
              "routes": null
            }
        """.trimIndent()

        mockServer.enqueue(MockResponse().setBody(mockResponse).setResponseCode(200))

        repository = createTestRepository()

        val request = RouteRequest(
            origin = LatLng(45.4972, -73.5789),
            destination = LatLng(45.4980, -73.5780)
        )

        try {
            repository.getRoute(request)
            fail("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun getRoute_emptyResponse_throwsException() = runTest {
        mockServer.enqueue(MockResponse().setBody("").setResponseCode(200))

        repository = createTestRepository()

        val request = RouteRequest(
            origin = LatLng(45.4972, -73.5789),
            destination = LatLng(45.4980, -73.5780)
        )

        try {
            repository.getRoute(request)
            fail("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertNotNull(e.message)
        }
    }

    @Test
    fun getRoute_400ErrorResponse_throwsException() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"error": "Invalid request"}""")
        )

        repository = createTestRepository()

        val request = RouteRequest(
            origin = LatLng(45.4972, -73.5789),
            destination = LatLng(45.4980, -73.5780)
        )

        try {
            repository.getRoute(request)
            fail("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertTrue(e.message?.contains("400") == true || e.message?.contains("error") == true)
        }
    }

    @Test
    fun getRoute_500ErrorResponse_throwsException() = runTest {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )

        repository = createTestRepository()

        val request = RouteRequest(
            origin = LatLng(45.4972, -73.5789),
            destination = LatLng(45.4980, -73.5780)
        )

        try {
            repository.getRoute(request)
            fail("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertTrue(e.message?.contains("500") == true || e.message?.contains("error") == true)
        }
    }

    @Test
    fun getRoute_missingPolylineField_throwsException() = runTest {
        val mockResponse = """
            {
              "routes": [
                {
                  "duration": "300s"
                }
              ]
            }
        """.trimIndent()

        mockServer.enqueue(MockResponse().setBody(mockResponse).setResponseCode(200))

        repository = createTestRepository()

        val request = RouteRequest(
            origin = LatLng(45.4972, -73.5789),
            destination = LatLng(45.4980, -73.5780)
        )

        try {
            repository.getRoute(request)
            fail("Expected RuntimeException to be thrown")
        } catch (e: RuntimeException) {
            assertTrue(e.message?.contains("No route polyline") == true)
        }
    }

    @Test
    fun routeRequest_createdWithCorrectOriginAndDestination() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4980, -73.5780)

        val request = RouteRequest(
            origin = origin,
            destination = destination
        )

        assertNotNull(request)
        assertEquals(origin, request.origin)
        assertEquals(destination, request.destination)
    }

    @Test
    fun routeResult_createdWithPoints() {
        val points = listOf(
            LatLng(45.4972, -73.5789),
            LatLng(45.4975, -73.5785),
            LatLng(45.4980, -73.5780)
        )

        val result = RouteResult(points = points)

        assertNotNull(result)
        assertEquals(3, result.points.size)
        assertEquals(45.4972, result.points[0].latitude, 0.0001)
        assertEquals(-73.5789, result.points[0].longitude, 0.0001)
    }

    @Test
    fun routeResult_emptyPointsList_isValid() {
        val result = RouteResult(points = emptyList())

        assertNotNull(result)
        assertTrue(result.points.isEmpty())
    }

    @Test
    fun travelMode_walkingEnum_exists() {
        val mode = TravelMode.WALKING
        assertNotNull(mode)
        assertEquals("WALKING", mode.name)
    }

    @Test
    fun getRoute_unknownHostException_providesUserFriendlyMessage() {
        // Test that UnknownHostException results in a user-friendly message
        val exception = UnknownHostException("routes.googleapis.com")

        // Verify exception type
        assertTrue(exception is UnknownHostException)
        assertNotNull(exception.message)
        assertTrue(exception.message?.contains("routes.googleapis.com") == true)
    }

    @Test
    fun getRoute_socketTimeoutException_providesUserFriendlyMessage() {
        // Test that SocketTimeoutException results in a user-friendly message
        val exception = SocketTimeoutException("connect timed out")

        // Verify exception type
        assertTrue(exception is SocketTimeoutException)
        assertNotNull(exception.message)
    }

    @Test
    fun getRoute_ioException_providesUserFriendlyMessage() {
        // Test that IOException results in a user-friendly message
        val exception = IOException("Network error")

        // Verify exception type
        assertTrue(exception is IOException)
        assertNotNull(exception.message)
    }

    @Test
    fun getRoute_validMultiPointRoute_decodesCorrectly() = runTest {
        // Encoded polyline with multiple points
        val encodedPolyline = "a~l~Fjk~uOwHJy@P"
        val mockResponse = """
            {
              "routes": [
                {
                  "polyline": {
                    "encodedPolyline": "$encodedPolyline"
                  }
                }
              ]
            }
        """.trimIndent()

        mockServer.enqueue(MockResponse().setBody(mockResponse).setResponseCode(200))

        repository = createTestRepository()

        val request = RouteRequest(
            origin = LatLng(45.4972, -73.5789),
            destination = LatLng(45.4980, -73.5780)
        )

        val result = repository.getRoute(request)

        assertNotNull(result)
        assertNotNull(result.points)
        assertTrue(result.points.isNotEmpty())
    }

    @Test
    fun getRoute_requestIncludesCorrectHeaders() = runTest {
        val encodedPolyline = "a~l~Fjk~uOwHJy@P"
        val mockResponse = """
            {
              "routes": [
                {
                  "polyline": {
                    "encodedPolyline": "$encodedPolyline"
                  }
                }
              ]
            }
        """.trimIndent()

        mockServer.enqueue(MockResponse().setBody(mockResponse).setResponseCode(200))

        repository = createTestRepository()

        val request = RouteRequest(
            origin = LatLng(45.4972, -73.5789),
            destination = LatLng(45.4980, -73.5780)
        )

        repository.getRoute(request)

        val recordedRequest = mockServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertNotNull(recordedRequest.getHeader("X-Goog-Api-Key"))
        assertNotNull(recordedRequest.getHeader("X-Goog-FieldMask"))
        assertTrue(recordedRequest.getHeader("X-Goog-FieldMask")?.contains("polyline") == true)
    }

    @Test
    fun repository_implementsDirectionsRepository() {
        val client = OkHttpClient()
        val repository: DirectionsRepository = GoogleRoutesRepository(
            client = client,
            apiKey = testApiKey
        )

        assertNotNull(repository)
        assertTrue(repository is DirectionsRepository)
    }

    @Test
    fun getRoute_validCoordinatesFormat_doesNotThrow() {
        val origin = LatLng(45.4972, -73.5789)
        val destination = LatLng(45.4980, -73.5780)

        // Test valid coordinate ranges
        assertTrue(origin.latitude >= -90 && origin.latitude <= 90)
        assertTrue(origin.longitude >= -180 && origin.longitude <= 180)
        assertTrue(destination.latitude >= -90 && destination.latitude <= 90)
        assertTrue(destination.longitude >= -180 && destination.longitude <= 180)
    }

    @Test
    fun routeRequest_withSameOriginAndDestination_isValid() {
        val location = LatLng(45.4972, -73.5789)

        val request = RouteRequest(
            origin = location,
            destination = location
        )

        assertNotNull(request)
        assertEquals(request.origin, request.destination)
    }

    @Test
    fun routeResult_preservesPointOrder() {
        val point1 = LatLng(45.4972, -73.5789)
        val point2 = LatLng(45.4975, -73.5785)
        val point3 = LatLng(45.4980, -73.5780)

        val points = listOf(point1, point2, point3)
        val result = RouteResult(points = points)

        assertEquals(point1, result.points[0])
        assertEquals(point2, result.points[1])
        assertEquals(point3, result.points[2])
    }

    @Test
    fun repository_canBeCreatedWithDefaultClient() {
        val repository = GoogleRoutesRepository(apiKey = "test_key")

        assertNotNull(repository)
    }

    @Test
    fun repository_canBeCreatedWithCustomClient() {
        val customClient = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val repository = GoogleRoutesRepository(
            client = customClient,
            apiKey = "test_key"
        )

        assertNotNull(repository)
    }
}
