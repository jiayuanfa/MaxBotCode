# Foldable Device Helper

一个用于检测和适配折叠屏 Android 设备的 AAR 库。

## 功能特性

- ✅ 检测设备是否为折叠屏
- ✅ 获取折叠屏的折叠状态（展开/折叠/半折叠）
- ✅ 监听折叠状态变化
- ✅ 提供布局适配建议
- ✅ 支持 Jetpack WindowManager
- ✅ 兼容 Android 7.0+ (API 24+)

## 安装

### Gradle

```groovy
dependencies {
    implementation 'com.example:foldable-library:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>foldable-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 快速开始

### 1. 初始化

```kotlin
val foldableHelper = FoldableDeviceHelper.getInstance(context)
```

### 2. 在 Activity 中监听折叠状态

```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var foldableHelper: FoldableDeviceHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        foldableHelper = FoldableDeviceHelper.getInstance(this)
        
        // 收集折叠状态变化
        lifecycleScope.launch {
            foldableHelper.foldableState.collect { state ->
                when (state) {
                    FoldableState.FLAT -> handleFlatMode()
                    FoldableState.HALF_OPENED -> handleHalfOpenedMode()
                    FoldableState.FOLDED -> handleFoldedMode()
                    FoldableState.UNKNOWN -> handleNormalMode()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        foldableHelper.startListening(this)
    }
    
    override fun onPause() {
        super.onPause()
        foldableHelper.stopListening()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        foldableHelper.release()
    }
}
```

### 3. 使用布局帮助类

```kotlin
val layoutHelper = FoldableLayoutHelper(activity)

// 获取布局建议
val recommendations = layoutHelper.getLayoutRecommendations()
if (recommendations.shouldUseTwoPane) {
    // 使用双窗格布局
    showTwoPaneLayout()
}

// 让视图避开折叠区域
layoutHelper.positionViewAvoidingFold(myView, avoidFold = true)
```

## API 文档

### FoldableDeviceHelper

| 方法 | 说明 |
|------|------|
| `getInstance(context)` | 获取单例实例 |
| `startListening(activity)` | 开始监听折叠状态变化 |
| `stopListening()` | 停止监听 |
| `release()` | 释放资源 |
| `isFoldableDevice()` | 判断是否为折叠屏设备 |
| `isFlat()` | 判断是否完全展开 |
| `isFolded()` | 判断是否完全折叠 |
| `isHalfOpened()` | 判断是否半开 |
| `getFoldingBounds()` | 获取折叠区域边界 |

### FoldableState

- `FLAT` - 完全展开
- `HALF_OPENED` - 半开
- `FOLDED` - 完全折叠
- `UNKNOWN` - 未知/普通设备

## 示例应用

项目包含一个完整的示例应用，演示如何使用该库。运行示例：

```bash
git clone https://github.com/yourusername/foldable-aar.git
cd foldable-aar
./gradlew :demo-app:installDebug
```

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

```
Copyright 2024 Your Name

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
