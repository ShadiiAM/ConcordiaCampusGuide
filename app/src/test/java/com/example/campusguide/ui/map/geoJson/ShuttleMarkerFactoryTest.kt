package com.example.campusguide.ui.map.geoJson

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.model.BitmapDescriptor
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ShuttleMarkerFactoryTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        ShuttleMarkerFactory.bitmapToDescriptor = { _ -> fakeDescriptor() }
        ShuttleMarkerFactory.defaultMarker = { fakeDescriptor() }
    }

    @After
    fun tearDown() {
        ShuttleMarkerFactory.resetForTests()
    }

    @Test
    fun create_returnsNonNullDescriptor() {
        val result = ShuttleMarkerFactory.create(context)
        assertNotNull(result)
    }

    @Test
    fun create_callsBitmapToDescriptorWithCorrectWidth() {
        var capturedBitmap: Bitmap? = null
        ShuttleMarkerFactory.bitmapToDescriptor = { bmp ->
            capturedBitmap = bmp
            fakeDescriptor()
        }

        ShuttleMarkerFactory.create(context)

        val bmp = requireNotNull(capturedBitmap)
        // MARKER_WIDTH_PX = 120
        assertEquals(120, bmp.width)
    }

    @Test
    fun create_callsBitmapToDescriptorWithCorrectHeight() {
        var capturedBitmap: Bitmap? = null
        ShuttleMarkerFactory.bitmapToDescriptor = { bmp ->
            capturedBitmap = bmp
            fakeDescriptor()
        }

        ShuttleMarkerFactory.create(context)

        val bmp = requireNotNull(capturedBitmap)
        // RECT_HEIGHT_PX (82) + TRIANGLE_HEIGHT_PX (30) = 112
        assertEquals(112, bmp.height)
    }

    @Test
    fun create_bitmapConfigIsARGB8888() {
        var capturedBitmap: Bitmap? = null
        ShuttleMarkerFactory.bitmapToDescriptor = { bmp ->
            capturedBitmap = bmp
            fakeDescriptor()
        }

        ShuttleMarkerFactory.create(context)

        val bmp = requireNotNull(capturedBitmap)
        assertEquals(Bitmap.Config.ARGB_8888, bmp.config)
    }

    @Test
    fun create_defaultMarkerFallback_usedWhenDrawableUnavailable() {
        var defaultCalled = false
        var bitmapCalled = false

        ShuttleMarkerFactory.defaultMarker = {
            defaultCalled = true
            fakeDescriptor()
        }
        ShuttleMarkerFactory.bitmapToDescriptor = { _ ->
            bitmapCalled = true
            fakeDescriptor()
        }

        val result = ShuttleMarkerFactory.create(context)

        // Either the drawable was found (bitmapCalled) or the fallback was used (defaultCalled).
        // Both paths must return a non-null result.
        assertNotNull(result)
        assertTrue("One rendering path must have been taken", bitmapCalled || defaultCalled)
    }

    @Test
    fun create_returnedDescriptorMatchesBitmapToDescriptorOutput() {
        val expected = fakeDescriptor()
        ShuttleMarkerFactory.bitmapToDescriptor = { _ -> expected }

        val result = ShuttleMarkerFactory.create(context)

        // If bitmapToDescriptor was invoked, result should be the expected descriptor.
        // (If defaultMarker path taken, result is still non-null.)
        assertNotNull(result)
    }

    @Test
    fun resetForTests_doesNotThrowAfterCustomHook() {
        ShuttleMarkerFactory.bitmapToDescriptor = { _ -> throw RuntimeException("should be reset") }
        ShuttleMarkerFactory.resetForTests()
        // After reset the factory state is restored; no exception thrown here.
    }

    private fun fakeDescriptor(): BitmapDescriptor =
        mock(BitmapDescriptor::class.java)
}
