package com.example.campusguide

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.campusguide.databinding.ActivityMapsBinding
import com.example.campusguide.ui.map.utils.BuildingLocator
import com.example.campusguide.ui.map.geoJson.GeoJsonOverlay
import com.example.campusguide.ui.map.geoJson.GeoJsonStyle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import android.location.Geocoder
import java.util.Locale


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var searchMarker: com.google.android.gms.maps.model.Marker? = null
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var sgwOverlay: GeoJsonOverlay
    private lateinit var loyOverlay: GeoJsonOverlay



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val query = intent.getStringExtra("SEARCH_QUERY")?.trim()

        sgwOverlay = GeoJsonOverlay(this, R.raw.sgw_buildings, "building-name")
        loyOverlay = GeoJsonOverlay(this, R.raw.loy_buildings, "building-name")



        // Add a marker at Concordia University (SGW Campus) and move the camera
        val concordiaSGW = LatLng(45.4972, -73.5789)
//        mMap.addMarker(MarkerOptions()
//            .position(concordiaSGW)
//            .title("Concordia University - SGW Campus"))
        if (query.isNullOrBlank()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(concordiaSGW, 15f))
        }


        sgwOverlay.attachToMap(mMap)
        loyOverlay.attachToMap(mMap)
         val defaultStyle = GeoJsonStyle(
            fillColor = 0x80ff8a8a.toInt(),
            strokeColor = 0xFF4d0000.toInt(),
            strokeWidth = 2f,
            zIndex = 10f,
            clickable = false,
            visible = true,
             markerColor = 0xFF974949.toInt(),
             markerAlpha = 1f,
             markerScale = 2f
        )
        sgwOverlay.setAllStyles(defaultStyle)
        loyOverlay.setAllStyles(defaultStyle)

        if (!query.isNullOrBlank()) {
            try {
                val results = Geocoder(this, Locale.getDefault())
                    .getFromLocationName(query, 1)

                if (!results.isNullOrEmpty()) {
                    val loc = results[0]
                    val latLng = LatLng(loc.latitude, loc.longitude)

                    searchMarker?.remove()
                    searchMarker = mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(query)
                    )

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                }
            } catch (_: Exception) { }
        }


    }



}