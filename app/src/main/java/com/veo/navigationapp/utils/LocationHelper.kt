package com.veo.navigationapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat

/**
 * Location service helper utility class
 * 
 * @author Haisheng Wang
 * @email haislien@163.com
 * @description Provides location acquisition functionality based on AMap location SDK, supporting high-precision positioning, location monitoring and permission management
 */
class LocationHelper(
    private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var locationListener: LocationListener? = null
    private var isRequestingLocationUpdates = false

    fun getCurrentLocation(callback: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            callback(null)
            return
        }

        // Try to get last known location
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        
        for (provider in providers) {
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null && (bestLocation == null || location.accuracy < bestLocation.accuracy)) {
                bestLocation = location
            }
        }
        
        if (bestLocation != null) {
            callback(bestLocation)
        } else {
            // If no cached location, request new location
            requestSingleLocationUpdate(callback)
        }
    }

    private fun requestSingleLocationUpdate(callback: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            callback(null)
            return
        }

        val singleUpdateListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                callback(location)
                locationManager.removeUpdates(this)
            }
            
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // Set timeout handling
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            locationManager.removeUpdates(singleUpdateListener)
            callback(null)
        }, 10000L) // 10 seconds timeout

        // Prefer GPS, use network location if GPS unavailable
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> {
                callback(null)
                return
            }
        }

        locationManager.requestLocationUpdates(
            provider,
            5000L, // 5 seconds interval
            10f,   // 10 meters distance
            singleUpdateListener,
            Looper.getMainLooper()
        )
    }

    fun startLocationUpdates(onLocationUpdate: (Location) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onLocationUpdate(location)
            }
            
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // Prefer GPS, use network location if GPS unavailable
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return
        }

        locationManager.requestLocationUpdates(
            provider,
            3000L, // 3 seconds interval, navigation requires more frequent updates
            5f,    // Minimum 5 meters update distance
            locationListener!!,
            Looper.getMainLooper()
        )
        
        isRequestingLocationUpdates = true
    }

    fun stopLocationUpdates() {
        if (isRequestingLocationUpdates && locationListener != null) {
            locationManager.removeUpdates(locationListener!!)
            isRequestingLocationUpdates = false
            locationListener = null
        }
    }

    fun isLocationUpdateActive(): Boolean {
        return isRequestingLocationUpdates
    }
}