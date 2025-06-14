package com.veo.navigationapp.service

import android.content.Context
import com.veo.navigationapp.config.MapConfig
import com.veo.navigationapp.config.MapProvider

/**
 * Map service factory class
 * 
 * @author Haisheng Wang
 * @email haislien@163.com
 * @description Uses factory pattern to create map service instances, currently only supports AMap service creation and management
 */
object MapServiceFactory {
    
    /**
     * Create map service instance
     * @param context Context
     * @return AMap service instance
     */
    fun createMapService(context: Context): IMapService {
        return AMapService(context, null)
    }
    
    /**
     * Get current map provider name
     */
    fun getCurrentProviderName(): String {
        return "AMap"
    }
}