package com.dicoding.storyapp.home.maps

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.R
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.data.Result
import com.dicoding.storyapp.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val boundsBuilder = LatLngBounds.Builder()
    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var progressBar : ProgressBar
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val factory = ViewModelFactory.getInstance(this)
        mapsViewModel = ViewModelProvider(this, factory)[MapsViewModel::class.java]

        observeViewModel()
        progressBar = binding.progressBar
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        getMyLocation()
        setMapStyle()
        mapsViewModel.getAllMarkers()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }

            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }

            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }

            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(userLocation).title("My Location"))
                    mMap.addMarker(MarkerOptions().position(userLocation).snippet("My Home Please Stay Away!, Oh I need you!"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }


    private fun observeViewModel() {
        mapsViewModel.listStoryItem.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    Log.d(TAG, "Loading markers...")
                    progressBar.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    progressBar.visibility = View.GONE
                    val stories = result.data.listStory
                    if (stories.isNullOrEmpty()) {
                        Toast.makeText(this, "Tidak ada data untuk ditampilkan", Toast.LENGTH_SHORT).show()
                    } else {
                        stories.map { story ->
                            val latitude = story?.lat
                            val longitude = story?.lon
                            val placeName = story?.name
                            val description = story?.description

                            if (latitude != null && longitude != null && placeName != null && description != null) {
                                TourismPlace(placeName, latitude, longitude, description)
                            } else {
                                null
                            }
                        }.let { addManyMarker(it) }
                    }
                }


                is Result.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed To Load Data: ${result.error}", Toast.LENGTH_LONG).show()

                }
            }
        }
    }

    data class TourismPlace(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val description: String
    )

    private fun addManyMarker(tourismPlaces: List<TourismPlace?>) {
        tourismPlaces.forEach { tourism ->
            val latLng = tourism?.let {
                LatLng(
                    tourism.latitude,
                    it.longitude
                )
            }
            latLng?.let {
                MarkerOptions()
                    .position(it)
                    .title(tourism.name)
                    .snippet(tourism.description)
            }?.let { mMap.addMarker(it) }
            if (latLng != null) {
                boundsBuilder.include(latLng)
            }
        }

        val bounds: LatLngBounds = boundsBuilder.build()
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                300 // Margin
            )
        )
    }


    companion object {
        private const val TAG = "MapsActivity"
    }
}