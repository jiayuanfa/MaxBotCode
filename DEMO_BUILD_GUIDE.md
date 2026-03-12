# Foldable AAR Demo 构建指南

## 项目结构

```
foldable-aar/
├── foldable-library/          # AAR 库模块
│   └── src/main/java/com/example/foldable/
│       ├── FoldableDeviceHelper.kt
│       ├── FoldableLayoutHelper.kt
│       └── FoldableState.kt
├── demo-app/                  # Demo 应用模块
│   └── src/main/
│       ├── java/com/example/foldable/demo/MainActivity.kt
│       └── res/layout/activity_main.xml
└── build.gradle               # 根项目配置
```

## 构建步骤

### 1. 导入项目

使用 Android Studio (推荐 Arctic Fox 或更高版本):
```
File -> Open -> 选择 foldable-aar 目录
```

### 2. 同步 Gradle

点击 "Sync Now" 或运行:
```bash
./gradlew build
```

### 3. 构建 APK

**调试版本:**
```bash
./gradlew :demo-app:assembleDebug
```
APK 输出路径: `demo-app/build/outputs/apk/debug/demo-app-debug.apk`

**发布版本:**
```bash
./gradlew :demo-app:assembleRelease
```
APK 输出路径: `demo-app/build/outputs/apk/release/demo-app-release-unsigned.apk`

### 4. 安装到设备

```bash
# 连接设备后
./gradlew :demo-app:installDebug

# 或直接使用 adb
adb install demo-app/build/outputs/apk/debug/demo-app-debug.apk
```

## Demo 功能

1. **设备类型检测** - 自动检测当前设备是否为折叠屏
2. **折叠状态监听** - 实时显示折叠状态变化:
   - FLAT (完全展开) - 绿色
   - HALF_OPENED (半开) - 橙色
   - FOLDED (完全折叠) - 红色
   - UNKNOWN (未知) - 灰色
3. **布局建议** - 点击"布局建议"按钮查看双窗格布局推荐

## 测试建议

**真机测试:**
- 三星 Galaxy Z Fold 系列
- 华为 Mate X 系列
- OPPO Find N 系列
- 小米 MIX Fold 系列

**模拟器测试:**
Android Studio 模拟器支持可折叠设备预设:
```
Device Manager -> Create Device -> Tablet -> Foldable devices
```

## 依赖版本

| 组件 | 版本 |
|------|------|
| Android Gradle Plugin | 8.1.0 |
| Kotlin | 1.9.0 |
| compileSdk | 34 |
| minSdk | 24 |
| targetSdk | 34 |
| Material Design | 3 |
| Jetpack WindowManager | 1.2.0 |

## 常见问题

**Q: 构建失败提示 "SDK not found"?**
A: 确保设置了 ANDROID_HOME 环境变量指向 Android SDK 目录。

**Q: 在普通手机上测试有什么效果?**
A: Demo 会显示 "普通设备" 和 "未知状态"，折叠状态监听功能不会触发。

**Q: 如何发布 AAR 到 Maven?**
A: 项目已配置 Maven Central 发布，修改 `gradle.properties` 添加签名密钥后运行 `./gradlew publish`。
