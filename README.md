# Veo 导航应用

基于高德地图SDK的Android导航应用，支持两次点击设置起点和终点的精确导航模式。

## 核心功能

- 🗺️ **两次点击导航**: 第一次点击设置起点，第二次点击设置终点
- 🛣️ **实时路径跟踪**: 绿色线条显示导航过程中的实际行驶路线
- 📊 **精确统计**: 显示实际行驶距离、时间和实时速度
- 📍 **定位服务**: 基于高德地图SDK的精确定位
- 🧭 **路线规划**: 智能路径规划和导航指引

## 技术栈

- **开发语言**: Kotlin
- **地图服务**: 高德地图SDK
- **最低版本**: Android 7.0 (API 24)

## 配置说明

### 高德地图API配置
1. 从[高德开放平台](https://lbs.amap.com/)获取API密钥
2. 在`local.properties`中配置:
   ```properties
   AMAP_API_KEY=your_amap_api_key_here
   ```
3. API密钥将在构建过程中自动注入到`AndroidManifest.xml`


## 项目架构

### 📁 项目结构
```
app/src/main/java/com/veo/navigationapp/
├── MainActivityNew.kt          # 主活动（重构版）
├── extensions/                 # 扩展方法代码组织
│   ├── LocationExtensions.kt   # 位置相关功能
│   ├── MapExtensions.kt        # 地图操作和交互
│   ├── NavigationExtensions.kt # 导航逻辑和路线规划
│   ├── LogExtensions.kt        # 日志和调试工具
│   └── UIExtensions.kt         # UI更新和用户交互
├── service/                    # 地图服务抽象层
│   ├── IMapService.kt          # 地图服务接口
│   ├── AMapService.kt          # 高德地图实现
│   └── MapServiceFactory.kt    # 服务工厂
└── utils/                      # 工具类
    ├── LocationHelper.kt       # 位置管理
    └── DirectionsHelper.kt     # 路线计算
```

### 🏗️ 架构模式
项目采用**模块化扩展方法架构**:

- **MainActivityNew**: 核心活动，职责最小化
- **扩展方法**: 按领域分组的功能（位置、地图、导航等）
- **服务层**: 不同地图提供商的抽象
- **工具类**: 位置和路线的可重用组件

## API使用指南

### 🗺️ 地图初始化
```kotlin
// 初始化地图服务
val mapService = MapServiceFactory.getMapService(MapServiceFactory.MapProvider.AMAP)
(mapService as AMapService).setAMap(aMap)
mapService.initializeMap()

// 基本地图操作
mapService.setOnMapClickListener { latLng -> handleMapClick(latLng) }
val marker = mapService.addMarker(latLng, "标题", markerType)
mapService.displayRoute(routePoints, color)
```

### 📍 位置与导航
```kotlin
// 位置更新
startLocationUpdates()
stopLocationUpdates()

// 路线计算和导航
calculateRoute(originLocation, destinationLocation) { route ->
    if (route != null) {
        displayRoute(route)
        startNavigation()
    }
}

// 导航控制
startNavigation()
stopNavigation()
```

## 开发指南

### 🚀 快速开始

1. **克隆仓库**
   ```bash
   git clone <repository-url>
   ```

## 使用说明

### 两次点击导航模式
1. **启动应用**: 授予位置权限后，界面显示"请点击地图设置起点"
2. **设置起点**: 在地图任意位置点击设置起点（出现蓝色标记）
3. **设置终点**: 界面提示"请点击地图设置终点"，再次点击地图设置终点（出现红色标记）
4. **开始导航**: 点击"开始导航"按钮，系统将规划路线并开始导航
5. **实时跟踪**: 导航过程中，绿色线条将实时显示你的实际路径
6. **查看统计**: 点击"停止导航"查看实际行驶距离、时间和速度统计

### 功能亮点
- **精确起点控制**: 独立于GPS当前位置，自由设置起点
- **实时路径绘制**: 绿色线条显示实际行驶路线
- **真实统计数据**: 基于实际移动轨迹计算距离和速度
- **智能速度检测**: 静止时显示速度为0，移动时显示实时速度

## 重要提示

- 📱 **真机测试**: 位置服务需要在真实设备上测试
- 🔒 **权限处理**: 确保已授予位置权限
- 🌐 **网络连接**: 需要网络连接来加载地图和规划路线

## 日志系统

### 📋 日志功能
应用包含完整的日志系统，帮助开发者调试和用户了解应用运行情况:

#### 🔍 **实时位置日志**
- **位置更新**: 记录GPS位置变化，包括经纬度和精度
- **移动检测**: 当距离超过1米时记录实际移动轨迹
- **速度计算**: 实时记录当前移动速度（静止时显示0）

#### 🛣️ **导航状态日志**
- **导航开始**: 记录起点、终点坐标和导航开始时间
- **路径跟踪**: 实时记录用户的实际路径点
- **距离统计**: 记录规划路线距离与实际行驶距离对比

#### 📊 **统计数据日志**
- **行程总结**: 导航结束时输出详细统计
  - 总导航时长（格式化显示）
  - 实际行驶距离（米/公里）
  - 平均移动速度
  - 当前瞬时速度
- **Path Analysis**: Records user path point count and trajectory completeness

#### 🔧 **调试信息**
- **API调用**: 记录高德地图API调用状态
- **权限检查**: 记录位置权限获取状态
- **错误处理**: 记录异常和错误信息

### 📱 **查看日志**

#### Android Studio调试
```bash
# 过滤应用日志
adb logcat | grep "VeoNavigation"

# 查看位置更新日志
adb logcat | grep "Location update"

# 查看导航统计日志
adb logcat | grep "Navigation"
```

#### 关键日志标签
- `VeoNavigation`: 主要应用功能日志
- `Location update`: 位置更新和移动检测
- `Navigation started`: 导航开始信息
- `Navigation stopped`: 导航结束统计
- `Trip Summary`: 详细行程总结数据

### 💡 **日志示例**
```
Location update: Latitude=39.9042, Longitude=116.4074, Accuracy=5.0m
Movement detection: Distance=15.2m, Current speed=1.2m/s
Navigation started: Start(39.9042,116.4074) -> End(39.9100,116.4200)
Trip statistics: Duration=15min30s, Actual distance=1.2km, Average speed=4.8km/h
```

## 常见问题

**问: 地图无法加载**  
答: 检查高德地图API密钥配置和网络连接

**问: 无法获取位置**  
答: 确认已授予位置权限，检查设备GPS是否开启

**问: 如何查看详细日志**  
答: 使用Android Studio连接设备，在Logcat中过滤"VeoNavigation"标签

---

**感谢使用Veo导航应用!** 🚗✨
