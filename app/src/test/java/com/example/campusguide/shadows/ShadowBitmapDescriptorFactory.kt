package com.example.campusguide.shadows

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.mockito.Mockito.mock

/**
 * Shadow for BitmapDescriptorFactory to allow unit testing with Robolectric
 */
@Implements(BitmapDescriptorFactory::class)
class ShadowBitmapDescriptorFactory {

    companion object {
        @JvmStatic
        @Implementation
        fun defaultMarker(): BitmapDescriptor {
            return mock(BitmapDescriptor::class.java)
        }

        @JvmStatic
        @Implementation
        fun defaultMarker(hue: Float): BitmapDescriptor {
            return mock(BitmapDescriptor::class.java)
        }

        @JvmStatic
        @Implementation
        fun fromResource(resourceId: Int): BitmapDescriptor {
            return mock(BitmapDescriptor::class.java)
        }
    }
}
