# Veo Navigation App

一个功能完整的Android导航应用，基于高德地图提供实时导航服务。

## 功能特性

### 核心功能
- ✅ **高德地图集成**: 基于高德地图SDK提供地图服务
- ✅ **地图显示**: 支持多种地图类型和样式
- ✅ **位置服务**: 实时获取用户当前位置
- ✅ **目的地选择**: 点击地图选择目的地
- ✅ **路线规划**: 基于高德路线规划API
- ✅ **实时导航**: 提供逐步导航指引
- ✅ **行程总结**: 显示行程时长、距离和平均速度

### 用户体验
- 🎨 **现代化UI**: 采用Material Design 3设计规范
- 📱 **响应式布局**: 适配不同屏幕尺寸
- 🔄 **实时更新**: 导航过程中实时更新位置和路线
- 📊 **详细统计**: 完整的行程数据分析
- 🔗 **分享功能**: 支持分享行程总结

## 技术栈

- **开发语言**: Kotlin
- **最低SDK版本**: Android 7.0 (API 24)
- **目标SDK版本**: Android 14 (API 34)
- **架构模式**: MVVM
- **地图服务**: 高德地图SDK
- **位置服务**: 高德定位SDK
- **UI框架**: Material Design 3
- **权限管理**: EasyPermissions

## 项目结构

```
NavigationApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/veo/navigationapp/
│   │   │   ├── MainActivityNew.kt          # 主Activity
│   │   │   ├── TripSummaryDialog.kt        # 行程总结对话框
│   │   │   ├── config/
│   │   │   │   └── MapProvider.kt          # 地图提供商配置
│   │   │   ├── factory/
│   │   │   │   └── MapServiceFactory.kt    # 地图服务工厂
│   │   │   ├── service/
│   │   │   │   ├── IMapService.kt          # 地图服务接口
│   │   │   │   └── AMapService.kt          # 高德地图服务实现
│   │   │   ├── model/
│   │   │   │   └── TripSummary.kt          # 行程数据模型
│   │   │   └── utils/
│   │   │       ├── LocationHelper.kt       # 位置服务工具类
│   │   │       └── DirectionsHelper.kt     # 路线规划工具类
│   │   ├── res/
│   │   │   ├── layout/                     # 布局文件
│   │   │   ├── drawable/                   # 图标资源
│   │   │   ├── values/                     # 字符串、颜色、样式
│   │   │   └── ...
│   │   └── AndroidManifest.xml             # 应用清单
│   └── build.gradle                        # 应用级构建配置
├── build.gradle                            # 项目级构建配置
├── settings.gradle                         # 项目设置
├── local.properties                        # 本地配置（API密钥）
└── README.md                              # 项目说明
```

## 安装和配置

### 高德地图API配置
1. 访问 [高德开放平台](https://lbs.amap.com/)
2. 注册开发者账号并创建应用
3. 获取Android平台的API Key
4. 在 `local.properties` 文件中添加高德API密钥：
   ```
   AMAP_API_KEY=你的高德地图API密钥
   ```
5. 在 `AndroidManifest.xml` 中配置高德API密钥：
   ```xml
   <meta-data
       android:name="com.amap.api.v2.apikey"
       android:value="你的高德地图API密钥" />
   ```

## 使用说明

### 基本操作
1. **启动应用**: 应用会自动请求位置权限
2. **查看当前位置**: 点击右下角的定位按钮
3. **选择目的地**: 在地图上点击任意位置设置目的地
4. **开始导航**: 点击"开始导航"按钮
5. **查看行程总结**: 导航结束后查看详细统计信息

### 权限要求
- **位置权限**: 用于获取当前位置和导航
- **网络权限**: 用于加载地图和获取路线信息

## 开发指南

### 添加新功能
1. 在相应的包中创建新的类文件
2. 更新布局文件（如需要）
3. 添加必要的字符串资源
4. 更新权限（如需要）

### 自定义样式
- 修改 `colors.xml` 中的颜色定义
- 更新 `themes.xml` 中的主题样式
- 调整 `styles.xml` 中的组件样式

### API集成
- 位置服务相关代码在 `LocationHelper.kt`
- 路线规划相关代码在 `DirectionsHelper.kt`
- 地图服务抽象接口在 `IMapService.kt`
- 高德地图实现在 `AMapService.kt`
- 根据需要扩展这些工具类

## 注意事项

### 安全性
- ⚠️ **API密钥安全**: 不要将API密钥提交到版本控制系统
- 🔒 **权限处理**: 确保正确处理用户权限请求
- 🛡️ **数据保护**: 不要记录或存储敏感的位置数据

### 性能优化
- 📍 **位置更新频率**: 根据使用场景调整位置更新间隔
- 🗺️ **地图渲染**: 避免频繁的地图操作
- 🔋 **电池优化**: 在不需要时停止位置服务

### 测试建议
- 🧪 **真机测试**: 位置服务需要在真实设备上测试
- 🌐 **网络环境**: 测试不同网络条件下的应用表现
- 📱 **多设备测试**: 确保在不同屏幕尺寸上正常工作

## 故障排除

### 常见问题

**Q: 地图无法加载**
A: 检查高德地图API密钥是否正确配置，确保API密钥已在AndroidManifest.xml中正确配置

**Q: 无法获取当前位置**
A: 确认已授予位置权限，检查设备GPS是否开启

**Q: 路线规划失败**
A: 检查网络连接，确认高德API密钥有效

**Q: 应用崩溃**
A: 查看Logcat输出，检查是否有权限或API相关错误

## 版本历史

### v1.0.0 (当前版本)
- ✨ 初始版本发布
- 🗺️ 基础地图功能
- 🧭 导航功能
- 📊 行程总结
- 🎨 Material Design 3 UI

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目维护者: Veo Team
- 邮箱: haisilen@163.com

---

**感谢使用 Veo 导航应用！** 🚗✨