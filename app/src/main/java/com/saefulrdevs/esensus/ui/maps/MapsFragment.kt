package com.saefulrdevs.esensus.ui.maps

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.saefulrdevs.esensus.databinding.FragmentMapsBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.saefulrdevs.esensus.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import java.io.IOException

import org.osmdroid.bonuspack.location.GeocoderNominatim
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker


class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var textViewCurrentLoc: TextView
    private lateinit var geocoder: GeocoderNominatim
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationUpdatesStarted = false
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private var marker: Marker? = null
    private var lastTapTime: Long = 0
    private val doubleTapInterval = 300

    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Maps"
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mapView = binding.mapView
        autoCompleteTextView = binding.autoCompleteTextView
        textViewCurrentLoc = binding.textViewCurrentLoc

        Configuration.getInstance().userAgentValue = requireContext().packageName
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapView.setUseDataConnection(true)

        val zoomController = mapView.zoomController
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
        mapView.setMultiTouchControls(true)
        mapView.invalidate()

        geocoder = GeocoderNominatim("Maps Tracker")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    fetchLocationSuggestions(query)
                }
            }
        })

        binding.btnSearch.setOnClickListener {
            val query = autoCompleteTextView.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }

        binding.fabLiveUser.setOnClickListener {
            checkLocationPermission()
        }

        binding.fabQuestion.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            val popupView = layoutInflater.inflate(R.layout.activity_popup, null)
            alertDialogBuilder.setView(popupView)
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                val latitude = location.latitude
                val longitude = location.longitude
                updateUserLocation(latitude, longitude)
            }
        }
        addMapClickListener()

        binding.btnSave.setOnClickListener {
            val result = Bundle().apply {
                putString("latitude", latitude.toString())
                putString("longitude", longitude.toString())
            }
            setFragmentResult("locationRequestKey", result)

            view?.let { it1 -> Navigation.findNavController(it1).popBackStack() }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (locationUpdatesStarted) {
            startLocationUpdates()
        } else {
            stopLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(
            createLocationRequest(),
            locationCallback,
            Looper.getMainLooper()
        )
        locationUpdatesStarted = true
    }

    private fun stopLocationUpdates() {
        if (locationUpdatesStarted) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            locationUpdatesStarted = false
        }
    }

    private fun fetchLocationSuggestions(query: String) {
        lifecycleScope.launch {
            val results = withContext(Dispatchers.IO) {
                try {
                    geocoder.getFromLocationName(query, 5)
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
            }

            results?.let {
                val suggestions = it.map { address -> address.getAddressLine(0) }
                showLocationSuggestions(suggestions)
            }
        }
    }


    private fun showLocationSuggestions(suggestions: List<String>) {
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.showDropDown()
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    geocoder.getFromLocationName(query, 1).firstOrNull()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            result?.let {
                val point = GeoPoint(it.latitude, it.longitude)
                mapView.controller.setCenter(point)
                mapView.controller.setZoom(20)

                latitude = it.latitude
                longitude = it.longitude

                val latitudeText = "Latitude: ${it.latitude}"
                val longitudeText = "Longitude: ${it.longitude}"

                binding.tvLongitude.text = longitudeText
                binding.tvLatitude.text = latitudeText

                getAddressFromLocation(it.latitude, it.longitude)
            } ?: run {
                Toast.makeText(requireContext(), "Pencarian tidak ditemukan.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    geocoder.getFromLocation(latitude, longitude, 1).firstOrNull()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            result?.let {
                val addressBuilder = StringBuilder().apply {
                    it.locality?.let { append(it) }
                    it.subAdminArea?.let { if (isNotEmpty()) append(", "); append(it) }
                    it.adminArea?.let { if (isNotEmpty()) append(", "); append(it) }
                    it.countryName?.let { if (isNotEmpty()) append(", "); append(it) }
                }
                textViewCurrentLoc.text = addressBuilder.toString()
            } ?: run {
                textViewCurrentLoc.text = "Alamat tidak ditemukan."
            }
        }
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableMyLocation()
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(
            createLocationRequest(),
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude
                        updateUserLocation(latitude, longitude)
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    private fun updateUserLocation(latitude: Double, longitude: Double) {
        mapView.controller.setCenter(GeoPoint(latitude, longitude))
        mapView.controller.setZoom(20)

        val latitudeText = "Latitude: $latitude"
        val longitudeText = "Longitude: $longitude"
        binding.tvLongitude.text = longitudeText
        binding.tvLatitude.text = latitudeText

        getAddressFromLocation(latitude, longitude)
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun addMapClickListener() {
        val receiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < doubleTapInterval) {
                        removeMarker()
                    } else {
                        latitude = it.latitude
                        longitude = it.longitude
                        binding.tvLongitude.text = "Longitude: $longitude"
                        binding.tvLatitude.text = "Latitude: $latitude"
                        addMarkerToMap(latitude!!, longitude!!)
                    }
                    lastTapTime = currentTime
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }

        val mapEventsOverlay = MapEventsOverlay(receiver)
        mapView.overlays.add(mapEventsOverlay)
    }

    private fun addMarkerToMap(latitude: Double, longitude: Double) {
        marker?.let {
            mapView.overlays.remove(it)
        }

        marker = Marker(mapView)
        marker?.position = GeoPoint(latitude, longitude)
        marker?.title = "Lokasi yang dipilih"
        marker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker!!)
        mapView.invalidate()
    }

    private fun removeMarker() {
        marker?.let {
            mapView.overlays.remove(it)
            marker = null
            binding.tvLongitude.text = ""
            binding.tvLatitude.text = ""
            longitude = null
            latitude = null
        }
    }
}