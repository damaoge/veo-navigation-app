package com.veo.navigationapp.model

import com.amap.api.maps.model.LatLng

/**
 * Trip summary data model
 * 
 * @author Haisheng Wang
 * @email haislien@163.com
 * @description Encapsulates statistical data of navigation trip, including trip duration, distance, route and other information, with formatted display methods
 */
data class TripSummary(
    val duration: Long, // Trip duration (milliseconds)
    val distance: Float, // Total distance (meters)
    val route: List<LatLng> // Travel route
) {
    // Get formatted trip duration
    fun getFormattedDuration(): String {
        val totalSeconds = duration / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return when {
            hours > 0 -> String.format("%d hours %d minutes %d seconds", hours, minutes, seconds)
        minutes > 0 -> String.format("%d minutes %d seconds", minutes, seconds)
        else -> String.format("%d seconds", seconds)
        }
    }
    
    // Get formatted distance
    fun getFormattedDistance(): String {
        return when {
            distance >= 1000 -> String.format("%.2f km", distance / 1000)
            else -> String.format("%.0f m", distance)
        }
    }
    
    // Calculate average speed (km/h)
    fun getAverageSpeed(): Float {
        if (duration == 0L) return 0f
        // If distance is too small (less than 10 meters), consider as not moving
        if (distance < 10f) return 0f
        val distanceInKm = distance / 1000f
        val timeInHours = duration / (1000f * 3600f)
        return distanceInKm / timeInHours
    }
    
    // Get formatted average speed
    fun getFormattedAverageSpeed(): String {
        return String.format("%.1f km/h", getAverageSpeed())
    }
}