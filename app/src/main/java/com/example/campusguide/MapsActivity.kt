package com.example.campusguide

import android.content.pm.PackageManager
import android.Manifest
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.example.campusguide.databinding.ActivityMapsBinding
import com.example.campusguide.ui.map.GeoJsonOverlay
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.concurrent.TimeUnit


class MapsActivity() : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var callback: LocationCallback


    private val sgwBuildingsOverlay = GeoJsonOverlay(R.raw.sgw_buildings)
    private val loyBuildingsOverlay = GeoJsonOverlay(R.raw.loy_buildings)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        onGPS()

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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker at Concordia University (SGW Campus) and move the camera
        val concordiaSGW = LatLng(45.4972, -73.5789)
        mMap.addMarker(MarkerOptions()
            .position(concordiaSGW)
            .title("Concordia University - SGW Campus"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(concordiaSGW, 15f))

        //Adds The Overlay
        sgwBuildingsOverlay.addToMap(mMap, this)
        loyBuildingsOverlay.addToMap(mMap, this)

        sgwBuildingsOverlay.changeAllBuildingColors("#ffaca6")
        sgwBuildingsOverlay.changeAllPointColors("#bc4949")
        loyBuildingsOverlay.changeAllBuildingColors("#ffaca6")
        loyBuildingsOverlay.changeAllPointColors("#bc4949")


        sgwBuildingsOverlay.removeAllPoints()

        //Add a marker at User Position

        callback = object: LocationCallback(){}

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if(location != null){
                val userLocation = LatLng(location.latitude,location.longitude)
                mMap.addMarker(MarkerOptions()
                    .position(userLocation)
                    .title("Your Location"))
            }else{
                requestLocation()
            }
        }
    }


    private fun onGPS(){
        if(!isLocationEnabled()){
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }else{
            fetchLocation()
        }
    }

    //Request Authorizations for location
    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),200)
        }
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),200)
        }
    }


    //Start the location updates
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun requestLocation(){
        val requestLocation = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(10)
            ).setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5))
            .build()
        fusedLocationProviderClient.requestLocationUpdates(
            requestLocation, callback , Looper.getMainLooper()
        )
    }


    //Check if the location and network services are on
    private fun isLocationEnabled() : Boolean{
        val locationManager= applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}