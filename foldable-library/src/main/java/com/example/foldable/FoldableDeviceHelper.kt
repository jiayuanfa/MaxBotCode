package com.example.foldable

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 折叠屏设备帮助类
 * 功能：检测折叠屏设备状态、监听折叠状态变化、获取折叠区域信息
 * 兼容性：支持 Android 7.0+ (API 24)
 * 依赖：Jetpack WindowManager
 */
class FoldableDeviceHelper private constructor(private val context: Context) {

    companion object {
        // 单例实例，使用volatile保证线程安全
        @Volatile
        private var instance: FoldableDeviceHelper? = null

        /**
         * 获取 FoldableDeviceHelper 单例实例
         * @param context 上下文，自动使用ApplicationContext避免内存泄漏
         * @return FoldableDeviceHelper 实例
         */
        fun getInstance(context: Context): FoldableDeviceHelper {
            return instance ?: synchronized(this) {
                instance ?: FoldableDeviceHelper(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    // 主线程协程作用域，用于监听折叠状态变化
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    // 折叠状态的可观察流
    private val _foldableState = MutableStateFlow(FoldableState.UNKNOWN)
    // 折叠特征列表的可观察流
    private val _foldingFeatures = MutableStateFlow<List<FoldingFeature>>(emptyList())
    // 折叠区域边界的可观察流
    private val _layoutBounds = MutableStateFlow<Rect?>(null)

    /**
     * 当前折叠状态可观察流
     * 使用方式：lifecycleScope.launch { foldableState.collect { 状态变化处理 } }
     */
    val foldableState: StateFlow<FoldableState> = _foldableState.asStateFlow()

    /**
     * 当前折叠特征列表可观察流
     * 包含所有折叠铰链的信息
     */
    val foldingFeatures: StateFlow<List<FoldingFeature>> = _foldingFeatures.asStateFlow()

    /**
     * 当前折叠铰链区域边界可观察流
     * 返回折叠区域的坐标范围
     */
    val layoutBounds: StateFlow<Rect?> = _layoutBounds.asStateFlow()

    /**
     * 开始监听 Activity 的折叠状态变化
     * 建议在 Activity 的 onResume 方法中调用
     * @param activity 要监听的Activity实例
     */
    fun startListening(activity: Activity) {
        mainScope.launch {
            try {
                // 从WindowInfoTracker获取布局信息流
                WindowInfoTracker.getOrCreate(activity)
                    .windowLayoutInfo(activity)
                    .collect { layoutInfo ->
                        // 处理布局信息，更新折叠状态
                        processLayoutInfo(layoutInfo)
                    }
            } catch (e: Exception) {
                // 发生异常时设置为未知状态
                e.printStackTrace()
                _foldableState.value = FoldableState.UNKNOWN
            }
        }
    }

    /**
     * 停止监听折叠状态变化
     * 建议在 Activity 的 onPause 方法中调用
     */
    fun stopListening() {
        // 取消所有子协程，停止监听
        mainScope.coroutineContext.cancelChildren()
    }

    /**
     * 释放所有资源
     * 建议在 Activity 的 onDestroy 方法中调用
     */
    fun release() {
        stopListening()
        // 取消整个协程作用域
        mainScope.cancel()
        // 重置单例实例
        instance = null
    }

    /**
     * 内部方法：处理窗口布局信息，更新折叠状态
     * @param layoutInfo 窗口布局信息
     */
    private fun processLayoutInfo(layoutInfo: androidx.window.layout.WindowLayoutInfo) {
        // 过滤出所有折叠特征
        val foldingFeatures = layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>()
        _foldingFeatures.value = foldingFeatures

        // 没有折叠特征，说明是普通设备或设备完全展开
        if (foldingFeatures.isEmpty()) {
            _foldableState.value = FoldableState.FLAT
            return
        }

        // 目前只处理第一个折叠铰链（大部分折叠屏只有一个铰链）
        val feature = foldingFeatures.first()
        // 保存折叠区域边界
        _layoutBounds.value = feature.bounds

        // 根据折叠特征状态更新当前状态
        _foldableState.value = when (feature.state) {
            FoldingFeature.State.FLAT -> FoldableState.FLAT        // 完全展开
            FoldingFeature.State.HALF_OPENED -> FoldableState.HALF_OPENED // 半开状态
            else -> FoldableState.UNKNOWN                          // 未知状态
        }
    }

    /**
     * 获取折叠铰链区域的边界坐标
     * @return Rect? 折叠区域的坐标，非折叠屏设备返回null
     */
    fun getFoldingBounds(): Rect? {
        return _layoutBounds.value
    }

    /**
     * 判断当前设备是否为折叠屏设备
     * @return Boolean true=折叠屏设备，false=普通设备
     */
    fun isFoldableDevice(): Boolean {
        return _foldableState.value != FoldableState.UNKNOWN
    }

    /**
     * 判断当前是否处于完全展开状态
     * @return Boolean true=完全展开
     */
    fun isFlat(): Boolean {
        return _foldableState.value == FoldableState.FLAT
    }

    /**
     * 判断当前是否处于完全折叠状态
     * @return Boolean true=完全折叠
     */
    fun isFolded(): Boolean {
        return _foldableState.value == FoldableState.FOLDED
    }

    /**
     * 判断当前是否处于半开状态（帐篷模式）
     * @return Boolean true=半开状态
     */
    fun isHalfOpened(): Boolean {
        return _foldableState.value == FoldableState.HALF_OPENED
    }
}
