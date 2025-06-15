package com.veo.navigationapp.utils

import android.content.Context
import android.util.Log
import com.amap.api.maps.model.LatLng
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*
import javax.net.ssl.HttpsURLConnection

/**
 * Route planning helper utility class
 * 
 * @author Haisheng Wang
 * @email haislien@163.com
 * @description Provides route planning functionality based on AMap API, supporting driving route queries, path parsing and navigation services
 */
class DirectionsHelper(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val logFileManager = LogFileManager(context)
    
    // Helper method: output to both Logcat and file
    private fun logD(message: String) {
        Log.d(TAG, message)
        logFileManager.writeLog(TAG, "DEBUG", message)
    }
    
    private fun logE(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
        val fullMessage = if (throwable != null) {
            "$message\n${android.util.Log.getStackTraceString(throwable)}"
        } else {
            message
        }
        logFileManager.writeLog(TAG, "ERROR", fullMessage)
    }
    
    private fun logI(message: String) {
        Log.i(TAG, message)
        logFileManager.writeLog(TAG, "INFO", message)
    }
    
    private fun logW(message: String) {
        Log.w(TAG, message)
        logFileManager.writeLog(TAG, "WARN", message)
    }
    
    companion object {
        private const val TAG = "DirectionsHelper"
        private const val AMAP_API_KEY = "9d1ea83e095ca13e874ea4212f1f1556" // Replace with your AMap API key
        private const val BASE_URL = "https://restapi.amap.com/v3/direction/driving"
        private const val CONNECTION_TIMEOUT = 10000 // 10 seconds
        private const val READ_TIMEOUT = 15000 // 15 seconds
    }
    
    /**
     * Get route planning
     */
    fun getDirections(origin: LatLng, destination: LatLng, callback: (List<LatLng>?) -> Unit) {
        scope.launch {
            try {
                val route = withContext(Dispatchers.IO) {
                    fetchDirections(origin, destination)
                }
                if (route != null) {
                    logD("Route retrieved successfully, contains ${route.size} points")
                } else {
                    logE("Route retrieval failed: fetchDirections returned null")
            logE("Possible failure reasons:")
            logE("1. Network connection issues")
            logE("2. Invalid or expired API key")
            logE("3. Incorrect request parameters")
            logE("4. AMap service exception")
            logE("5. Response data parsing failed")
            logE("Please check network connection and API configuration")
                }
                callback(route)
            } catch (e: Exception) {
                logE("Exception occurred while getting route: ${e.javaClass.simpleName} - ${e.message}", e)
                callback(null)
            }
        }
    }
    
    /**
     * Test API connection
     */
    suspend fun testApiConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            logD("=== 🧪 API连接测试 (非导航请求) ===")
            logD("[🧪测试配置] 测试目的: 验证API密钥和网络连接")
            logD("[🧪测试配置] 注意: 这是测试请求，使用固定测试坐标，不影响实际导航")
            
            // 使用北京天安门到故宫的测试路线
            val testOrigin = LatLng(39.908823, 116.397470) // 天安门
            val testDestination = LatLng(39.916668, 116.397026) // 故宫
            
            logD("[🧪测试配置] 测试起点: 天安门 (${testOrigin.latitude}, ${testOrigin.longitude})")
            logD("[🧪测试配置] 测试终点: 故宫 (${testDestination.latitude}, ${testDestination.longitude})")
            logD("[🧪测试配置] 预期距离: 约1-2公里")
            
            val testStartTime = System.currentTimeMillis()
            logD("[🧪测试执行] 开始调用fetchDirections...")
            
            val result = fetchDirections(testOrigin, testDestination)
            
            val testEndTime = System.currentTimeMillis()
            val testDuration = testEndTime - testStartTime
            
            logD("[🧪测试结果] 测试耗时: ${testDuration}ms")
            
            val success = result != null && result.isNotEmpty()
            
            if (success) {
                logD("[🧪测试结果] ✅ API连接测试成功")
                logD("[🧪测试结果] 获得坐标点数量: ${result!!.size}")
                if (result.size >= 2) {
                    logD("[🧪测试结果] 路线起点: (${result.first().latitude}, ${result.first().longitude})")
                    logD("[🧪测试结果] 路线终点: (${result.last().latitude}, ${result.last().longitude})")
                }
                logD("[🧪测试结果] API密钥有效，网络连接正常，响应解析成功")
            } else {
                logE("[🧪测试结果] ❌ API连接测试失败")
                logE("[🧪测试结果] 可能原因:")
                logE("[🧪测试结果] 1. API密钥无效或过期")
                logE("[🧪测试结果] 2. 网络连接问题")
                logE("[🧪测试结果] 3. 高德地图服务不可用")
                logE("[🧪测试结果] 4. 请求参数格式错误")
                logE("[🧪测试结果] 5. 响应解析失败")
                logE("[🧪测试结果] 请查看上方详细日志进行排查")
            }
            
            logD("=== 🧪 高德地图API连接测试完成 ===")
            success
        }
    }
    
    /**
     * 获取路线数据
     */
    private suspend fun fetchDirections(origin: LatLng, destination: LatLng): List<LatLng>? {
        var connection: HttpURLConnection? = null
        return try {
            logD("=== 开始正常导航路线规划 ===")
            logD("[导航请求] 这是用户实际导航请求，非测试请求")
            
            // 1. 构建请求参数
            val originStr = "${origin.longitude},${origin.latitude}"
            val destinationStr = "${destination.longitude},${destination.latitude}"
            logD("[参数构建] 起点坐标: $originStr")
            logD("[参数构建] 终点坐标: $destinationStr")
            logD("[参数构建] 输出格式: json")
            logD("[参数构建] API密钥: ${AMAP_API_KEY.take(10)}...${AMAP_API_KEY.takeLast(4)}")
            
            // 2. 构建完整URL
            val urlString = "$BASE_URL?origin=$originStr&destination=$destinationStr&output=json&key=$AMAP_API_KEY"
            logD("[URL构建] 完整请求URL: $urlString")
            logD("[URL构建] 基础URL: $BASE_URL")
            logD("[URL构建] 查询参数: origin=$originStr&destination=$destinationStr&output=json&key=${AMAP_API_KEY.take(10)}...")
            
            // 3. 创建网络连接
            logD("[网络连接] 创建URL对象...")
            val url = URL(urlString)
            logD("[网络连接] 打开HTTP连接...")
            connection = url.openConnection() as HttpURLConnection
            
            // 4. 配置连接参数
            logD("[连接配置] 设置请求方法: GET")
            connection.requestMethod = "GET"
            logD("[连接配置] 设置连接超时: 15000ms")
            connection.connectTimeout = 15000
            logD("[连接配置] 设置读取超时: 15000ms")
            connection.readTimeout = 15000
            logD("[连接配置] 设置User-Agent: NavigationApp/1.0")
            connection.setRequestProperty("User-Agent", "NavigationApp/1.0")
            
            // 5. 发起网络请求
            logD("[网络请求] 开始连接到高德地图API服务器...")
            val startTime = System.currentTimeMillis()
            connection.connect()
            val endTime = System.currentTimeMillis()
            logD("[网络请求] 连接完成，耗时: ${endTime - startTime}ms")
            logD("[网络请求] HTTP响应码: ${connection.responseCode}")
            logD("[网络请求] 响应消息: ${connection.responseMessage}")
            logD("[网络请求] 内容类型: ${connection.contentType}")
            logD("[网络请求] 内容长度: ${connection.contentLength}")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logD("[响应处理] HTTP请求成功，开始读取响应数据...")
                
                // 6. 读取响应数据
                val readStartTime = System.currentTimeMillis()
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val response = reader.use { it.readText() }
                val readEndTime = System.currentTimeMillis()
                logD("[响应处理] 数据读取完成，耗时: ${readEndTime - readStartTime}ms")
                logD("[响应处理] 响应数据长度: ${response.length} 字符")
                logD("[响应处理] 响应数据类型: ${if (response.startsWith("{")) "JSON" else "未知格式"}")
                
                // 7. 输出响应内容（用于调试）
                if (response.length <= 1000) {
                    logD("[响应内容] 完整响应: $response")
                } else {
                    logD("[响应内容] 响应前500字符: ${response.take(500)}")
                    logD("[响应内容] 响应后500字符: ${response.takeLast(500)}")
                }
                
                if (response.isEmpty()) {
                    logE("[响应错误] API返回空响应")
                    return null
                }
                
                // 8. 解析响应数据
                logD("[数据解析] 开始解析API响应数据...")
                val parseResult = parseDirectionsResponse(response)
                logD("[数据解析] 解析完成，结果: ${if (parseResult != null) "成功获得" + parseResult.size + "个坐标点" else "解析失败"}")
                
                parseResult
            } else {
                logE("[错误处理] HTTP请求失败，响应码: $responseCode")
                logE("[错误处理] 响应消息: ${connection.responseMessage}")
                
                // 尝试读取错误响应
                try {
                    val errorStream = connection.errorStream
                    if (errorStream != null) {
                        val errorReader = BufferedReader(InputStreamReader(errorStream, "UTF-8"))
                        val errorResponse = errorReader.use { it.readText() }
                        logE("[错误响应] 错误响应内容: $errorResponse")
                    } else {
                        logE("[错误响应] 无法获取错误响应流")
                    }
                } catch (e: Exception) {
                    logE("[错误响应] 读取错误响应时发生异常", e)
                }
                
                // 根据HTTP状态码提供具体的错误分析
                when (responseCode) {
                    400 -> logE("[错误分析] 请求参数错误 (400) - 检查起点终点坐标格式")
                    401 -> logE("[错误分析] API密钥无效或未授权 (401) - 检查API密钥是否正确")
                    403 -> logE("[错误分析] 访问被禁止 (403) - 可能是配额不足或服务未开通")
                    404 -> logE("[错误分析] API端点不存在 (404) - 检查请求URL是否正确")
                    429 -> logE("[错误分析] 请求频率过高 (429) - 请降低请求频率")
                    500 -> logE("[错误分析] 服务器内部错误 (500) - 高德地图服务器问题")
                    502 -> logE("[错误分析] 网关错误 (502) - 服务器网关问题")
                    503 -> logE("[错误分析] 服务不可用 (503) - 服务器暂时不可用")
                    else -> logE("[错误分析] 未知HTTP错误: $responseCode")
                }
                
                logD("=== 路线规划SDK调用结束（失败） ===")
                null
            }
        } catch (e: SocketTimeoutException) {
            logE("[异常处理] 网络请求超时异常")
            logE("[异常详情] 连接超时: ${connection?.connectTimeout}ms, 读取超时: ${connection?.readTimeout}ms")
            logE("[异常建议] 请检查网络连接或增加超时时间", e)
            logD("=== 路线规划SDK调用结束（超时） ===")
            null
        } catch (e: UnknownHostException) {
            logE("[异常处理] 无法解析主机名异常")
            logE("[异常详情] 主机名: ${e.message}")
            logE("[异常建议] 请检查网络连接和DNS设置", e)
            logD("=== 路线规划SDK调用结束（DNS错误） ===")
            null
        } catch (e: ConnectException) {
            logE("[异常处理] 无法连接到服务器异常")
            logE("[异常详情] 连接异常: ${e.message}")
            logE("[异常建议] 请检查网络连接和防火墙设置", e)
            logD("=== 路线规划SDK调用结束（连接失败） ===")
            null
        } catch (e: java.io.IOException) {
            logE("[异常处理] IO异常")
            logE("[异常详情] IO错误: ${e.message}")
            logE("[异常建议] 请检查网络连接稳定性", e)
            logD("=== 路线规划SDK调用结束（IO错误） ===")
            null
        } catch (e: Exception) {
            logE("[异常处理] 未知异常")
            logE("[异常详情] 异常类型: ${e.javaClass.simpleName}")
            logE("[异常详情] 异常消息: ${e.message}")
            logE("[异常建议] 请联系开发者检查代码逻辑", e)
            logD("=== 路线规划SDK调用结束（未知错误） ===")
            null
        } finally {
            connection?.disconnect()
        }
    }
    
    /**
     * 解析高德地图API响应
     */
    private fun parseDirectionsResponse(response: String): List<LatLng>? {
        return try {
            logD("=== 开始解析API响应数据 ===")
            
            // 1. 创建JSON对象
            logD("[JSON解析] 创建JSON对象...")
            val jsonObject = JSONObject(response)
            logD("[JSON解析] JSON对象创建成功")
            
            // 2. 检查API调用状态
            logD("[状态检查] 检查status字段...")
            if (!jsonObject.has("status")) {
                logE("[状态检查] API响应缺少status字段")
                logE("[状态检查] 可用字段: ${jsonObject.keys().asSequence().toList()}")
                return null
            }
            
            val status = jsonObject.getString("status")
            logD("[状态检查] API状态码: $status")
            
            if (status != "1") {
                logE("[状态检查] API调用失败，状态码不为1")
                
                val info = if (jsonObject.has("info")) {
                    val infoValue = jsonObject.getString("info")
                    logD("[错误信息] info字段: $infoValue")
                    infoValue
                } else {
                    logD("[错误信息] 响应中无info字段")
                    "未知错误"
                }
                
                val infocode = if (jsonObject.has("infocode")) {
                    val infocodeValue = jsonObject.getString("infocode")
                    logD("[错误信息] infocode字段: $infocodeValue")
                    infocodeValue
                } else {
                    logD("[错误信息] 响应中无infocode字段")
                    "未知"
                }
                
                logE("[API错误] 高德路线规划API错误 - 状态: $status, 信息: $info, 错误码: $infocode")
                
                // 根据错误码提供详细说明
                when (infocode) {
                    "10000" -> logE("[错误说明] 请求正常")
                    "10001" -> logE("[错误说明] key不正确或过期")
                    "10002" -> logE("[错误说明] 没有权限使用相应的服务或者请求接口的路径拼写错误")
                    "10003" -> logE("[错误说明] 访问已超出日访问量")
                    "10004" -> logE("[错误说明] 单位时间内访问过于频繁")
                    "10005" -> logE("[错误说明] IP白名单出错，发送请求的服务器IP不在IP白名单内")
                    "20000" -> logE("[错误说明] 请求参数非法")
                    "20001" -> logE("[错误说明] 缺少必填参数")
                    "20002" -> logE("[错误说明] 请求协议非法")
                    "20003" -> logE("[错误说明] 其他未知错误")
                    "30000" -> logE("[错误说明] 请求服务响应错误")
                    "30001" -> logE("[错误说明] 引擎返回数据异常")
                    "30002" -> logE("[错误说明] 服务端请求链接超时")
                    "30003" -> logE("[错误说明] 读取服务端返回数据超时")
                    else -> logE("[错误说明] 未知错误码: $infocode")
                }
                
                return null
            }
            
            // 3. 获取路线数据
            logD("[路线解析] 检查route字段...")
            if (!jsonObject.has("route")) {
                logE("[路线解析] API响应缺少route字段")
                logE("[路线解析] 根对象可用字段: ${jsonObject.keys().asSequence().toList()}")
                return null
            }
            
            val route = jsonObject.getJSONObject("route")
            logD("[路线解析] route对象获取成功")
            logD("[路线解析] route对象字段: ${route.keys().asSequence().toList()}")
            
            // 4. 获取路径数组
            logD("[路径解析] 检查paths字段...")
            if (!route.has("paths")) {
                logE("[路径解析] route对象缺少paths字段")
                logE("[路径解析] route对象可用字段: ${route.keys().asSequence().toList()}")
                return null
            }
            
            val paths = route.getJSONArray("paths")
            logD("[路径解析] 找到 ${paths.length()} 条路径")
            
            if (paths.length() == 0) {
                logE("[路径解析] 没有找到可用路径")
                return null
            }
            
            // 5. 解析第一条路径
            logD("[路径解析] 开始解析第一条路径...")
            val path = paths.getJSONObject(0)
            logD("[路径解析] 第一条路径对象字段: ${path.keys().asSequence().toList()}")
            
            // 输出路径基本信息
            if (path.has("distance")) {
                val distance = path.getString("distance")
                logD("[路径信息] 路径距离: ${distance}米")
            }
            
            if (path.has("duration")) {
                val duration = path.getString("duration")
                logD("[路径信息] 预计时间: ${duration}秒")
            }
            
            if (path.has("strategy")) {
                val strategy = path.getString("strategy")
                logD("[路径信息] 路径策略: $strategy")
            }
            
            // 6. 获取步骤数组
            logD("[步骤解析] 检查steps字段...")
            if (!path.has("steps")) {
                logE("[步骤解析] path对象缺少steps字段")
                logE("[步骤解析] path对象可用字段: ${path.keys().asSequence().toList()}")
                return null
            }
            
            val steps = path.getJSONArray("steps")
            logD("[步骤解析] 第一条路径包含 ${steps.length()} 个步骤")
            
            // 7. 解析每个步骤的坐标
            logD("[坐标解析] 开始解析各步骤的坐标点...")
            val points = mutableListOf<LatLng>()
            var totalCoordCount = 0
            var validCoordCount = 0
            var invalidCoordCount = 0
            
            for (i in 0 until steps.length()) {
                logD("[坐标解析] 处理第 ${i + 1}/${steps.length()} 个步骤")
                val step = steps.getJSONObject(i)
                
                // 输出步骤信息
                if (step.has("instruction")) {
                    val instruction = step.getString("instruction")
                    logD("[步骤信息] 步骤 $i 指令: $instruction")
                }
                
                if (step.has("distance")) {
                    val distance = step.getString("distance")
                    logD("[步骤信息] 步骤 $i 距离: ${distance}米")
                }
                
                // 获取polyline坐标
                if (!step.has("polyline")) {
                    logW("[坐标解析] 步骤 $i 缺少polyline字段")
                    logW("[坐标解析] 步骤 $i 可用字段: ${step.keys().asSequence().toList()}")
                    continue
                }
                
                val polyline = step.getString("polyline")
                if (polyline.isEmpty()) {
                    logW("[坐标解析] 步骤 $i 的polyline为空")
                    continue
                }
                
                logD("[坐标解析] 步骤 $i polyline长度: ${polyline.length} 字符")
                
                // 解析坐标字符串
                val coords = polyline.split(";")
                logD("[坐标解析] 步骤 $i 包含 ${coords.size} 个坐标对")
                
                var stepValidCount = 0
                var stepInvalidCount = 0
                
                for (coord in coords) {
                    totalCoordCount++
                    
                    if (coord.trim().isEmpty()) {
                        stepInvalidCount++
                        invalidCoordCount++
                        continue
                    }
                    
                    try {
                        val latLng = coord.trim().split(",")
                        if (latLng.size == 2) {
                            val lng = latLng[0].toDouble()
                            val lat = latLng[1].toDouble()
                            
                            // 验证坐标范围
                            if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
                                points.add(LatLng(lat, lng))
                                stepValidCount++
                                validCoordCount++
                            } else {
                                logW("[坐标解析] 步骤 $i 坐标超出有效范围: $coord (lat: $lat, lng: $lng)")
                                stepInvalidCount++
                                invalidCoordCount++
                            }
                        } else {
                            logW("[坐标解析] 步骤 $i 坐标格式错误: $coord (分割后长度: ${latLng.size})")
                            stepInvalidCount++
                            invalidCoordCount++
                        }
                    } catch (e: NumberFormatException) {
                        logW("[坐标解析] 步骤 $i 无效坐标格式: $coord - ${e.message}")
                        stepInvalidCount++
                        invalidCoordCount++
                    }
                }
                
                logD("[坐标解析] 步骤 $i 完成: 有效坐标 $stepValidCount 个，无效坐标 $stepInvalidCount 个")
            }
            
            // 8. 输出解析统计信息
            logD("[解析统计] 总坐标对数: $totalCoordCount")
            logD("[解析统计] 有效坐标点: $validCoordCount")
            logD("[解析统计] 无效坐标点: $invalidCoordCount")
            logD("[解析统计] 最终坐标点列表大小: ${points.size}")
            
            if (points.isNotEmpty()) {
                logD("[解析统计] 起点坐标: (${points.first().latitude}, ${points.first().longitude})")
                logD("[解析统计] 终点坐标: (${points.last().latitude}, ${points.last().longitude})")
            }
            
            if (points.isEmpty()) {
                logE("[解析结果] 解析完成但没有获得任何有效坐标点")
                logD("=== API响应数据解析结束（失败） ===")
                return null
            }
            
            logD("[解析结果] 路线解析成功！")
            logD("=== API响应数据解析结束（成功） ===")
            points
        } catch (e: org.json.JSONException) {
            logE("[解析异常] JSON解析异常")
            logE("[解析异常] JSON错误: ${e.message}")
            logE("[解析异常] 可能的原因: 响应格式不是有效的JSON或字段结构不匹配", e)
            logD("=== API响应数据解析结束（JSON异常） ===")
            null
        } catch (e: Exception) {
            logE("[解析异常] 解析路线响应时发生未知错误")
            logE("[解析异常] 异常类型: ${e.javaClass.simpleName}")
            logE("[解析异常] 异常消息: ${e.message}")
            logE("[解析异常] 请联系开发者检查解析逻辑", e)
            logD("=== API响应数据解析结束（未知异常） ===")
            null
        }
    }
    
    /**
     * 获取简化路线（减少坐标点数量）
     */
    fun getSimpleRoute(points: List<LatLng>, maxPoints: Int = 50): List<LatLng> {
        if (points.size <= maxPoints) return points
        
        val step = points.size / maxPoints
        val result = mutableListOf<LatLng>()
        
        for (i in points.indices step step) {
            result.add(points[i])
        }
        
        // Ensure the last point is included
        if (result.last() != points.last()) {
            result.add(points.last())
        }
        
        return result
    }
    
    /**
     * 计算两点间距离（米）
     */
    fun calculateDistance(start: LatLng, end: LatLng): Double {
        val earthRadius = 6371000.0 // 地球半径（米）
        
        val lat1Rad = Math.toRadians(start.latitude)
        val lat2Rad = Math.toRadians(end.latitude)
        val deltaLatRad = Math.toRadians(end.latitude - start.latitude)
        val deltaLngRad = Math.toRadians(end.longitude - start.longitude)
        
        val a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Estimate travel time (minutes)
     */
    fun estimateTravelTime(distance: Double, averageSpeed: Double = 40.0): Double {
        // distance: distance (meters)
        // averageSpeed: average speed (km/h)
        val distanceKm = distance / 1000.0
        return (distanceKm / averageSpeed) * 60.0 // Convert to minutes
    }
    
    /**
     * 获取日志文件路径
     */
    fun getLogFilePath(): String {
        return logFileManager.getCurrentLogFilePath()
    }
    
    /**
     * 获取日志目录路径
     */
    fun getLogDirectoryPath(): String {
        return logFileManager.getLogDirectoryPath()
    }
    
    /**
     * 获取存储状态
     */
    fun getStorageStatus(): LogFileManager.StorageStatus {
        return logFileManager.checkStorageStatus()
    }
}