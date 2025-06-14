package com.veo.navigationapp.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions

/**
 * AMap service implementation class
 * 
 * @author Haisheng Wang
 * @email haislien@163.com
 * @description Implementation of IMapService interface, providing specific AMap functionality including map initialization, marker management, route drawing, etc.
 */
class AMapService(
    private val context: Context,
    private var aMap: AMap?
) : IMapService {
    
    override fun initializeMap() {
        // 高德地图初始化
        aMap?.let { map ->
            map.uiSettings?.isZoomControlsEnabled = false
            map.uiSettings?.isMyLocationButtonEnabled = false
            map.uiSettings?.isScaleControlsEnabled = true
            map.uiSettings?.isCompassEnabled = true
            map.uiSettings?.isRotateGesturesEnabled = true
            map.uiSettings?.isTiltGesturesEnabled = true
            map.mapType = AMap.MAP_TYPE_NORMAL
            map.showMapText(true)
        }
    }
    
    override fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            aMap?.isMyLocationEnabled = true
            aMap?.myLocationStyle?.myLocationType(com.amap.api.maps.model.MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
        }
    }
    
    override fun moveCamera(latLng: LatLng, zoom: Float) {
        aMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, zoom)
        )
    }
    
    override fun addMarker(latLng: LatLng, title: String): Any? {
        return aMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
        )
    }
    
    override fun removeMarker(marker: Any?) {
        (marker as? Marker)?.remove()
    }
    
    override fun drawRoute(points: List<LatLng>, color: Int, width: Float): Any? {
        return aMap?.addPolyline(
            PolylineOptions()
                .addAll(points)
                .color(color)
                .width(width)
        )
    }
    
    override fun removeRoute(route: Any?) {
        (route as? Polyline)?.remove()
    }
    
    override fun setOnMapClickListener(listener: (LatLng) -> Unit) {
        aMap?.setOnMapClickListener { aMapLatLng ->
            val latLng = LatLng(aMapLatLng.latitude, aMapLatLng.longitude)
            listener(latLng)
        }
    }
    
    override fun cleanup() {
        aMap = null
    }
    
    /**
     * 设置AMap实例
     */
    fun setAMap(map: AMap) {
        this.aMap = map
    }
}