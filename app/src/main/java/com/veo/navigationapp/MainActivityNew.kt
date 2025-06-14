package com.veo.navigationapp

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.LatLng
import com.veo.navigationapp.databinding.ActivityMainNewBinding
import com.veo.navigationapp.model.TripSummary
import com.veo.navigationapp.service.AMapService
import com.veo.navigationapp.service.IMapService
import com.veo.navigationapp.service.MapServiceFactory
import com.veo.navigationapp.utils.DirectionsHelper
import com.veo.navigationapp.utils.LocationHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * New version of the main activity class for navigation app
 * 
 * @author Haisheng Wang
 * @email haislien@163.com
 * @description Refactored main interface of navigation app with service-oriented architecture, supporting AMap navigation, logging and trip management features
 */
class MainActivityNew : AppCompatActivity() {
    private lateinit var binding: ActivityMainNewBinding
    private lateinit var locationHelper: LocationHelper
    private lateinit var directionsHelper: DirectionsHelper
    private lateinit var mapService: IMapService
    
    private var currentLocation: Location? = null
    private var destinationMarker: Any? = null
    private var destinationLocation: LatLng? = null
    private var currentRoute: Any? = null
    private var isNavigating = false
    private var tripStartTime: Long = 0
    private var totalDistance: Float = 0f
    
    // AMap related
    private var aMap: AMap? = null
    private var aMapView: MapView? = null
    
    // Log related
    private val logBuffer = StringBuilder()
    private var isLogVisible = false
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val TAG = "NavigationApp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize log
        initLog()
        addLog("Application started")
        
        // Initialize location service
        locationHelper = LocationHelper(this)
        directionsHelper = DirectionsHelper(this)
        addLog("Location service initialization completed")
        
        // Create map service
        mapService = MapServiceFactory.createMapService(this)
        addLog("Map service creation completed")
        
        // Test API connection
        testDirectionsApi()
        
        // Initialize AMap
        initAMap()
        
        setupUI()
        updateMapProviderDisplay()
    }
    

    
    private fun initAMap() {
        try {
            addLog("Starting AMap initialization")
            
            // Initialize AMap SDK
            MapsInitializer.updatePrivacyShow(this, true, true)
            MapsInitializer.updatePrivacyAgree(this, true)
            addLog("AMap SDK initialization completed")
            
            // Create AMap MapView
            aMapView = MapView(this)
            aMapView?.onCreate(null)
            binding.mapContainer.removeAllViews()
            binding.mapContainer.addView(aMapView)
            addLog("MapView created successfully")
            
            // Get AMap instance
            aMap = aMapView?.getMap()
            aMap?.let { map ->
                addLog("AMap instance obtained successfully")
                (mapService as AMapService).setAMap(map)
                map.uiSettings?.isZoomControlsEnabled = false
                map.uiSettings?.isMyLocationButtonEnabled = false
                onMapReady()
                addLog("Map initialization completed")
            } ?: addLog("Error: Unable to get AMap instance")
        } catch (e: Exception) {
            addLog("Map initialization failed: ${e.message}")
            Log.e(TAG, "Map initialization failed", e)
        }
    }

    private fun setupUI() {
        binding.btnStartNavigation.setOnClickListener {
            if (!isNavigating) {
                startNavigation()
            } else {
                stopNavigation()
            }
        }

        binding.btnCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }
        
        // Log related buttons
        binding.btnToggleLog.setOnClickListener {
            toggleLogPanel()
        }
        
        binding.btnClearLog.setOnClickListener {
            clearLog()
        }
        
        binding.btnCopyLog.setOnClickListener {
            copyLogToClipboard()
        }
        
        // Hide map switch button as only AMap is supported
        binding.btnSwitchMap.visibility = android.view.View.GONE
    }
    

    
    private fun updateMapProviderDisplay() {
        val providerName = MapServiceFactory.getCurrentProviderName()
        binding.tvMapProvider.text = getString(R.string.current_map_provider, providerName)
    }


    
    private fun onMapReady() {
        addLog("Map ready")
        
        // Initialize map configuration
        mapService.initializeMap()
        addLog("Map configuration completed")
        
        // Enable location display
        enableMyLocation()
        
        // Set map click listener to select destination
        mapService.setOnMapClickListener { latLng ->
            setDestination(latLng)
        }
        
        // Get current location
        getCurrentLocation()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            addLog("Location permission granted, enabling my location")
            mapService.enableMyLocation()
        } else {
            addLog("Requesting location permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getCurrentLocation() {
        addLog("Starting to get current location")
        locationHelper.getCurrentLocation { location ->
            location?.let {
                currentLocation = it
                val currentLatLng = LatLng(it.latitude, it.longitude)
                mapService.moveCamera(currentLatLng, 15f)
                addLog("Current location obtained: ${it.latitude}, ${it.longitude}")
            } ?: addLog("Failed to get current location")
        }
    }

    private fun setDestination(latLng: LatLng) {
        // Clear previous destination marker
        destinationMarker?.let {
            mapService.removeMarker(it)
        }
        
        // Add new destination marker and save location
        destinationMarker = mapService.addMarker(latLng, "Destination")
        destinationLocation = latLng
        
        binding.btnStartNavigation.isEnabled = true
        addLog("Destination set: ${latLng.latitude}, ${latLng.longitude}")
        addLog("destinationLocation variable updated: ${destinationLocation?.let { it.latitude.toString() + ", " + it.longitude.toString() } ?: "null"}")
        Toast.makeText(this, getString(R.string.destination_set), Toast.LENGTH_SHORT).show()
    }

    private fun startNavigation() {
        addLog("Starting navigation")
        addLog("Current location: ${currentLocation?.let { it.latitude.toString() + ", " + it.longitude.toString() } ?: "null"}")
        addLog("Destination location: ${destinationLocation?.let { it.latitude.toString() + ", " + it.longitude.toString() } ?: "null"}")
        
        if (currentLocation == null || destinationLocation == null) {
            Toast.makeText(this, "Please set destination first", Toast.LENGTH_SHORT).show()
            addLog("Navigation failed: destination not set - currentLocation=${currentLocation != null}, destinationLocation=${destinationLocation != null}")
            return
        }

        val origin = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        val destination = destinationLocation!!
        addLog("Route planning: from " + origin.latitude + ", " + origin.longitude + " to " + destination.latitude + ", " + destination.longitude)
        
        // Get route
        addLog("Requesting route planning...")
        directionsHelper.getDirections(origin, destination) { route ->
            route?.let {
                addLog("Route planning successful, obtained ${it.size} path points")
                displayRoute(it)
                isNavigating = true
                tripStartTime = System.currentTimeMillis()
                binding.btnStartNavigation.text = getString(R.string.stop_navigation)
                
                // Start real-time location updates
                startLocationUpdates()
                
                addLog("Navigation started")
                Toast.makeText(this, getString(R.string.navigation_started), Toast.LENGTH_SHORT).show()
            } ?: run {
                addLog("Route planning failed: unable to get route")
                addLog("Please check the following possible causes:")
                addLog("1. Is network connection normal")
                addLog("2. Is API key valid")
                addLog("3. Are start and end coordinates correct")
                addLog("4. Is AMap service available")
                addLog("For detailed error information, please check Logcat logs (tag: DirectionsHelper)")
                Toast.makeText(this, "Unable to get route, please check logs for detailed reasons", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun stopNavigation() {
        isNavigating = false
        binding.btnStartNavigation.text = getString(R.string.start_navigation)
        
        // Stop location updates
        locationHelper.stopLocationUpdates()
        
        // Show trip summary
        showTripSummary()
        
        // Clear route
        currentRoute?.let {
            mapService.removeRoute(it)
            currentRoute = null
        }
    }

    private fun displayRoute(points: List<LatLng>) {
        // Clear previous route
        currentRoute?.let {
            mapService.removeRoute(it)
        }
        
        // Draw new route
        currentRoute = mapService.drawRoute(
            points,
            ContextCompat.getColor(this, R.color.route_color),
            8f
        )
        
        // Calculate total distance
        totalDistance = calculateTotalDistance(points)
    }

    private fun calculateTotalDistance(points: List<LatLng>): Float {
        var distance = 0f
        for (i in 0 until points.size - 1) {
            val results = FloatArray(1)
            Location.distanceBetween(
                points[i].latitude, points[i].longitude,
                points[i + 1].latitude, points[i + 1].longitude,
                results
            )
            distance += results[0]
        }
        return distance
    }

    private fun startLocationUpdates() {
        locationHelper.startLocationUpdates { location ->
            if (isNavigating) {
                currentLocation = location
                // Navigation guidance logic can be added here
            }
        }
    }

    private fun showTripSummary() {
        val tripDuration = System.currentTimeMillis() - tripStartTime
        val tripSummary = TripSummary(
            duration = tripDuration,
            distance = totalDistance,
            route = emptyList() // Simplified processing
        )
        
        // Show summary dialog
        TripSummaryDialog.show(this, tripSummary)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addLog("Location permission granted")
                    enableMyLocation()
                } else {
                    addLog("Location permission denied")
                    Toast.makeText(this, "Location permission is required to use navigation features", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        aMapView?.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        aMapView?.onPause()
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        aMapView?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        aMapView?.onDestroy()
        locationHelper.stopLocationUpdates()
        mapService.cleanup()
    }
    
    // Log related methods
    private fun initLog() {
        addLog("Log system initialized")
    }
    
    private fun addLog(message: String) {
        val timestamp = dateFormat.format(Date())
        val logEntry = "[$timestamp] $message\n"
        logBuffer.append(logEntry)
        
        // Limit log length to avoid memory overflow
        if (logBuffer.length > 10000) {
            val excess = logBuffer.length - 8000
            logBuffer.delete(0, excess)
        }
        
        // Update UI
        runOnUiThread {
            binding.tvLogContent.text = logBuffer.toString()
            // Auto scroll to bottom
            binding.tvLogContent.parent?.let { parent ->
                if (parent is android.widget.ScrollView) {
                    parent.post {
                        parent.fullScroll(android.view.View.FOCUS_DOWN)
                    }
                }
            }
        }
        
        // Also output to system log
        Log.d(TAG, message)
    }
    
    private fun testDirectionsApi() {
        lifecycleScope.launch {
            try {
                addLog("Starting AMap API connection test...")
                val directionsHelper = DirectionsHelper(this@MainActivityNew)
                val success = directionsHelper.testApiConnection()
                
                if (success) {
                    addLog("✅ API connection test successful")
                } else {
                    addLog("❌ API connection test failed, please check network and API key")
                }
            } catch (e: Exception) {
                addLog("❌ API test exception: ${e.message}")
                Log.e(TAG, "API test exception", e)
            }
        }
    }
    
    private fun toggleLogPanel() {
        isLogVisible = !isLogVisible
        binding.logPanel.visibility = if (isLogVisible) View.VISIBLE else View.GONE
        addLog(if (isLogVisible) "Show log panel" else "Hide log panel")
    }
    
    private fun clearLog() {
        logBuffer.clear()
        binding.tvLogContent.text = ""
        addLog("Log cleared")
    }
    
    private fun copyLogToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Navigation Log", logBuffer.toString())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
        addLog("Log copied to clipboard")
    }
}