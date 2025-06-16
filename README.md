# Veo Navigation App

An Android navigation application based on Amap SDK, supporting precise navigation mode with two-click setup for start and end points.

## Core Features

- 🗺️ **Two-Click Navigation**: First click sets start point, second click sets end point
- 🛣️ **Real-time Path Tracking**: Green line displays actual route traveled during navigation
- 📊 **Precise Statistics**: Shows actual distance traveled, time, and real-time speed
- 📍 **Location Services**: Precise positioning based on Amap SDK
- 🧭 **Route Planning**: Intelligent path planning and navigation guidance

## Tech Stack

- **Development Language**: Kotlin
- **Map Service**: Amap SDK
- **Minimum Version**: Android 7.0 (API 24)

## Configuration

### Amap API Configuration
1. Get API Key from [Amap Open Platform](https://lbs.amap.com/)
2. Configure in `AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.amap.api.v2.apikey"
       android:value="Your_Amap_API_Key" />
   ```

## Usage Instructions

### Two-Click Navigation Mode
1. **Launch App**: After granting location permissions, interface shows "Please click map to set start point"
2. **Set Start Point**: Click anywhere on the map to set start point (blue marker appears)
3. **Set End Point**: Interface prompts "Please click map to set end point", click map again to set end point (red marker appears)
4. **Start Navigation**: Click "Start Navigation" button, system will plan route and begin navigation
5. **Real-time Tracking**: During navigation, green line will show your actual path in real-time
6. **View Statistics**: Click "Stop Navigation" to view actual distance traveled, time, and speed statistics

### Feature Highlights
- **Precise Start Point Control**: Independent of GPS current location, freely set start point
- **Real-time Path Drawing**: Green line shows actual route traveled
- **Authentic Statistical Data**: Distance and speed calculated based on actual movement trajectory
- **Smart Speed Detection**: Speed shows 0 when stationary, real-time speed when moving

## Important Notes

- 📱 **Real Device Testing**: Location services require testing on actual devices
- 🔒 **Permission Handling**: Ensure location permissions are granted
- 🌐 **Network Connection**: Requires network connection to load maps and plan routes

## Logging System

### 📋 Logging Features
The app includes a comprehensive logging system to help developers debug and users understand app operation:

#### 🔍 **Real-time Location Logs**
- **Location Updates**: Records GPS location changes, including latitude, longitude, and accuracy
- **Movement Detection**: Records actual movement trajectory when distance exceeds 1 meter
- **Speed Calculation**: Real-time recording of current movement speed (shows 0 when stationary)

#### 🛣️ **Navigation State Logs**
- **Navigation Start**: Records start point, end point coordinates and navigation start time
- **Path Tracking**: Real-time recording of user's actual path points
- **Distance Statistics**: Records planned route distance vs actual distance traveled comparison

#### 📊 **Statistical Data Logs**
- **Trip Summary**: Outputs detailed statistics when navigation ends
  - Total navigation duration (formatted display)
  - Actual distance traveled (meters/kilometers)
  - Average movement speed
  - Current instantaneous speed
- **Path Analysis**: Records user path point count and trajectory completeness

#### 🔧 **Debug Information**
- **API Calls**: Records Amap API call status
- **Permission Checks**: Records location permission acquisition status
- **Error Handling**: Records exceptions and error information

### 📱 **Viewing Logs**

#### Android Studio Debugging
```bash
# Filter app logs
adb logcat | grep "VeoNavigation"

# View location update logs
adb logcat | grep "Location update"

# View navigation statistics logs
adb logcat | grep "Navigation"
```

#### Key Log Tags
- `VeoNavigation`: Main app functionality logs
- `Location update`: Location updates and movement detection
- `Navigation started`: Navigation start information
- `Navigation stopped`: Navigation end statistics
- `Trip Summary`: Detailed trip summary data

### 💡 **Log Examples**
```
Location update: Latitude=39.9042, Longitude=116.4074, Accuracy=5.0m
Movement detection: Distance=15.2m, Current speed=1.2m/s
Navigation started: Start(39.9042,116.4074) -> End(39.9100,116.4200)
Trip statistics: Duration=15min30s, Actual distance=1.2km, Average speed=4.8km/h
```

## FAQ

**Q: Map cannot load**  
A: Check Amap API key configuration and network connection

**Q: Cannot get location**  
A: Confirm location permissions are granted, check if device GPS is enabled

**Q: How to view detailed logs**  
A: Use Android Studio to connect device, filter "VeoNavigation" tag in Logcat

---

**Thank you for using Veo Navigation App!** 🚗✨