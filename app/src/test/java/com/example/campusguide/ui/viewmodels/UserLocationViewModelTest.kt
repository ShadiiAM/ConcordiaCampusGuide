package com.example.campusguide.ui.viewmodels

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.model.LatLng
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class UserLocationViewModelTest {

    lateinit var viewModel: UserLocationViewModel

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = UserLocationViewModel(application)
    }

    @Test
    fun updateNearestId_nullLocation_setsNull() {

        viewModel.updateNearestId(null)

        assertNull(viewModel.nearestId.value)
    }

    @Test
    fun onLocationUpdated_updatesFlow() {
        val latLng = LatLng(45.4973, -73.5786)

        viewModel.onLocationUpdated(latLng)

        assertEquals(latLng, viewModel.userLatLng.value)
    }
}