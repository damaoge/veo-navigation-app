package com.veo.navigationapp.service

import android.location.Location
import com.amap.api.maps.model.LatLng

/**
 * Map service interface
 * 
 * @author Haisheng Wang
 * @email haislien@163.com
 * @description Defines common map operation interface, supporting different map provider implementations, providing core functions like map initialization, location, markers, routes, etc.
 */
interface IMapService {
    
    /**
     * Initialize map
     */
    fun initializeMap()
    
    /**
     * Enable my location display
     */
    fun enableMyLocation()
    
    /**
     * Move camera to specified position
     * @param latLng Target position
     * @param zoom Zoom level
     */
    fun moveCamera(latLng: LatLng, zoom: Float)
    
    /**
     * Add marker
     * @param latLng Marker position
     * @param title Marker title
     * @return Marker ID or object
     */
    fun addMarker(latLng: LatLng, title: String): Any?
    
    /**
     * Remove marker
     * @param marker Marker object
     */
    fun removeMarker(marker: Any?)
    
    /**
     * Draw route
     * @param points Route point collection
     * @param color Route color
     * @param width Route width
     * @return Route object
     */
    fun drawRoute(points: List<LatLng>, color: Int, width: Float): Any?
    
    /**
     * Remove route
     * @param route Route object
     */
    fun removeRoute(route: Any?)
    
    /**
     * Set map click listener
     * @param listener Click listener
     */
    fun setOnMapClickListener(listener: (LatLng) -> Unit)
    
    /**
     * Clean up resources
     */
    fun cleanup()
}