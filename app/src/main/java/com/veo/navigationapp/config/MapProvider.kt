package com.veo.navigationapp.config

/**
 * Map provider enumeration
 * 
 * @author Haisheng Wang
 * @email haislien@163.com
 * @description Defines map provider enumeration, currently only supports AMap service
 */
enum class MapProvider {
    AMAP          // AMap
}

/**
 * Map configuration class
 * 
 * @author Haisheng Wang
 * @email haislien@163.com
 * @description Map configuration management object, providing map provider configuration and judgment functions
 */
object MapConfig {
    // Fixed to use AMap, switching functionality removed
    val currentProvider: MapProvider = MapProvider.AMAP
    
    /**
     * Check if currently using AMap
     * Always returns true, as only AMap is supported
     */
    fun isAMap(): Boolean {
        return true
    }
}