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
    private var originMarker: Any? = null
    private var destinationLocation: LatLng? = null
    private var originLocation: LatLng? = null
    private var currentRoute: Any? = null
    private var isNavigating = false
    private var tripStartTime: Long = 0
    private var totalDistance: Float = 0f
    private var actualWalkedDistance: Float = 0f
    private var currentSpeed: Float = 0f
    private var lastLocationTime: Long = 0L
    
    // Two-click mode state
    private var isSettingOrigin = true // true: setting origin, false: setting destination
    private var clickCount = 0
    
    // User path tracking
    private val userPath = mutableListOf<LatLng>()
    private var userPathRoute: Any? = null
    
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
        
        // Test API connection (disabled to avoid log confusion)
        // testDirectionsApi()
        
        // Initialize AMap
        initAMap()
        
        setupUI()
        updateMapProviderDisplay()
        
        // Initialize instruction text
        binding.tvInstruction.text = "请点击地图设置起点"
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
        
        // Set map click listener for two-click mode (origin then destination)
        mapService.setOnMapClickListener { latLng ->
            handleMapClick(latLng)
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

    private fun handleMapClick(latLng: LatLng) {
        clickCount++
        
        if (isSettingOrigin) {
            setOrigin(latLng)
        } else {
            setDestination(latLng)
        }
    }
    
    private fun setOrigin(latLng: LatLng) {
        // Clear previous origin marker
        originMarker?.let {
            mapService.removeMarker(it)
        }
        
        // Add new origin marker and save location
        originMarker = mapService.addMarker(latLng, "起点")
        originLocation = latLng
        
        isSettingOrigin = false
        addLog("起点已设置: ${latLng.latitude}, ${latLng.longitude}")
        addLog("请点击地图设置终点")
        Toast.makeText(this, "起点已设置，请点击地图设置终点", Toast.LENGTH_SHORT).show()
        
        // Update instruction text
        binding.tvInstruction.text = "请点击地图设置终点"
    }
    
    private fun setDestination(latLng: LatLng) {
        // Clear previous destination marker
        destinationMarker?.let {
            mapService.removeMarker(it)
        }
        
        // Add new destination marker and save location
        destinationMarker = mapService.addMarker(latLng, "终点")
        destinationLocation = latLng
        
        binding.btnStartNavigation.isEnabled = true
        addLog("终点已设置: ${latLng.latitude}, ${latLng.longitude}")
        addLog("起点和终点都已设置，可以开始导航")
        Toast.makeText(this, "终点已设置，可以开始导航", Toast.LENGTH_SHORT).show()
        
        // Update instruction text
        binding.tvInstruction.text = "起点和终点已设置，点击开始导航"
    }

    private fun startNavigation() {
        addLog("=== 开始导航流程 ===")
        
        // Check if both origin and destination are set
        if (originLocation == null) {
            Toast.makeText(this, "请先点击地图设置起点", Toast.LENGTH_SHORT).show()
            addLog("导航失败: 起点未设置")
            return
        }
        
        if (destinationLocation == null) {
            Toast.makeText(this, "请先点击地图设置终点", Toast.LENGTH_SHORT).show()
            addLog("导航失败: 终点未设置")
            return
        }
        
        val origin = originLocation!!
        val destination = destinationLocation!!
        
        addLog("=== 导航起始信息 ===")
        addLog("用户设置的起点坐标: ${origin.latitude}, ${origin.longitude}")
        addLog("用户设置的终点坐标: ${destination.latitude}, ${destination.longitude}")
        addLog("开始路径规划...")
        
        // 开始路径规划
        directionsHelper.getDirections(origin, destination) { route ->
            route?.let {
                addLog("路径规划成功，获得 ${it.size} 个路径点")
                addLog("路线起点: ${it.first().latitude}, ${it.first().longitude}")
                addLog("路线终点: ${it.last().latitude}, ${it.last().longitude}")
                
                // Clear previous user path and reset tracking variables
                userPath.clear()
                userPathRoute?.let { pathRoute ->
                    mapService.removeRoute(pathRoute)
                    userPathRoute = null
                }
                actualWalkedDistance = 0f
                currentSpeed = 0f
                lastLocationTime = 0L
                addLog("已清空之前的用户路径，重置距离和速度追踪，准备开始新的路径追踪")
                
                // Add current location as the first point of user path
                currentLocation?.let { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    userPath.add(currentLatLng)
                    lastLocationTime = System.currentTimeMillis()
                    addLog("已将当前位置设为用户路径起点: ${location.latitude}, ${location.longitude}")
                }
                
                displayRoute(it)
                isNavigating = true
                tripStartTime = System.currentTimeMillis()
                binding.btnStartNavigation.text = getString(R.string.stop_navigation)
                
                // Start real-time location updates
                startLocationUpdates()
                
                addLog("导航已开始，开始实时追踪用户路径")
                Toast.makeText(this@MainActivityNew, getString(R.string.navigation_started), Toast.LENGTH_SHORT).show()
                
                // Update instruction text
                binding.tvInstruction.text = "导航进行中"
            } ?: run {
                addLog("路径规划失败")
                Toast.makeText(this@MainActivityNew, "路径规划失败，请重试", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopNavigation() {
        isNavigating = false
        binding.btnStartNavigation.text = getString(R.string.start_navigation)
        binding.btnStartNavigation.isEnabled = false
        
        // Clear all markers
        originMarker?.let {
            mapService.removeMarker(it)
            originMarker = null
        }
        
        destinationMarker?.let {
            mapService.removeMarker(it)
            destinationMarker = null
        }
        
        // Reset two-click mode state
        isSettingOrigin = true
        clickCount = 0
        originLocation = null
        destinationLocation = null
        
        // Stop location updates
        locationHelper.stopLocationUpdates()
        
        // Show trip summary
        showTripSummary()
        
        // Clear route
        currentRoute?.let {
            mapService.removeRoute(it)
            currentRoute = null
        }
        
        // Record user path statistics before clearing
        val pathPointsCount = userPath.size
        val finalWalkedDistance = actualWalkedDistance
        
        // Clear user path
        userPathRoute?.let {
            mapService.removeRoute(it)
            userPathRoute = null
        }
        userPath.clear()
        
        // Reset tracking variables
        actualWalkedDistance = 0f
        currentSpeed = 0f
        lastLocationTime = 0L
        
        // Update instruction text
        binding.tvInstruction.text = "请点击地图设置起点"
        
        addLog("导航已停止，已清除用户路径，重置所有设置")
        addLog("本次导航共记录了 ${pathPointsCount} 个路径点")
        addLog("本次导航实际走过距离: ${String.format("%.1f", finalWalkedDistance)}m")
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
                val currentTime = System.currentTimeMillis()
                val currentLatLng = LatLng(location.latitude, location.longitude)
                
                // Calculate actual walked distance and speed
                if (userPath.isNotEmpty()) {
                    val lastPoint = userPath.last()
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        lastPoint.latitude, lastPoint.longitude,
                        currentLatLng.latitude, currentLatLng.longitude,
                        results
                    )
                    val distanceFromLast = results[0]
                    
                    // Only add distance if movement is significant (> 1 meter)
                    if (distanceFromLast > 1.0f) {
                        actualWalkedDistance += distanceFromLast
                        
                        // Calculate current speed
                        if (lastLocationTime > 0) {
                            val timeDiff = (currentTime - lastLocationTime) / 1000f // seconds
                            if (timeDiff > 0) {
                                currentSpeed = (distanceFromLast / timeDiff) * 3.6f // m/s to km/h
                            }
                        }
                        lastLocationTime = currentTime
                        
                        addLog("移动距离: ${String.format("%.1f", distanceFromLast)}m, 当前速度: ${String.format("%.1f", currentSpeed)}km/h")
                    } else {
                        // If not moving significantly, speed is 0
                        currentSpeed = 0f
                    }
                } else {
                    lastLocationTime = currentTime
                }
                
                currentLocation = location
                userPath.add(currentLatLng)
                
                // Update user path visualization
                updateUserPath()
                
                addLog("用户位置更新: ${location.latitude}, ${location.longitude}")
                addLog("已记录路径点数: ${userPath.size}, 实际走过距离: ${String.format("%.1f", actualWalkedDistance)}m")
            }
        }
    }
    
    private fun updateUserPath() {
        if (userPath.size >= 2) {
            // Clear previous user path route
            userPathRoute?.let {
                mapService.removeRoute(it)
            }
            
            // Draw new user path in green color
            userPathRoute = mapService.drawRoute(
                userPath,
                ContextCompat.getColor(this, android.R.color.holo_green_dark),
                6f
            )
            
            addLog("用户路径已更新，当前路径点数: ${userPath.size}")
        }
    }

    private fun showTripSummary() {
        val tripDuration = System.currentTimeMillis() - tripStartTime
        
        // Use actual walked distance instead of planned route distance
        val actualDistance = if (actualWalkedDistance > 0) actualWalkedDistance else 0f
        
        val tripSummary = TripSummary(
            duration = tripDuration,
            distance = actualDistance,
            route = userPath // Use actual user path
        )
        
        addLog("=== 导航统计 ===")
        addLog("导航时长: ${tripSummary.getFormattedDuration()}")
        addLog("实际走过距离: ${tripSummary.getFormattedDistance()}")
        addLog("平均速度: ${tripSummary.getFormattedAverageSpeed()}")
        addLog("当前速度: ${String.format("%.1f", currentSpeed)} km/h")
        
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
                // Use the existing directionsHelper instance instead of creating a new one
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