package com.example.foldable

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import androidx.core.util.Consumer
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 折叠屏设备帮助类
 * 用于检测折叠屏设备状态、监听折叠变化并提供布局建议
 */
class FoldableDeviceHelper private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: FoldableDeviceHelper? = null

        /**
         * 获取 FoldableDeviceHelper 单例实例
         */
        fun getInstance(context: Context): FoldableDeviceHelper {
            return instance ?: synchronized(this) {
                instance ?: FoldableDeviceHelper(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _foldableState = MutableStateFlow(FoldableState.UNKNOWN)
    private val _foldingFeatures = MutableStateFlow<List<FoldingFeature>>(emptyList())
    private val _layoutBounds = MutableStateFlow<Rect?>(null)

    /**
     * 当前折叠状态流
     */
    val foldableState: StateFlow<FoldableState> = _foldableState.asStateFlow()

    /**
     * 当前折叠特征列表流
     */
    val foldingFeatures: StateFlow<List<FoldingFeature>> = _foldingFeatures.asStateFlow()

    /**
     * 当前布局边界流
     */
    val layoutBounds: StateFlow<Rect?> = _layoutBounds.asStateFlow()

    /**
     * 开始监听 Activity 的折叠状态变化
     * @param activity 要监听的 Activity
     */
    fun startListening(activity: Activity) {
        mainScope.launch {
            try {
                WindowInfoTracker.getOrCreate(activity)
                    .windowLayoutInfo(activity)
                    .collect { layoutInfo ->
                        processLayoutInfo(layoutInfo)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                _foldableState.value = FoldableState.UNKNOWN
            }
        }
    }

    /**
     * 停止监听
     */
    fun stopListening() {
        mainScope.coroutineContext.cancelChildren()
    }

    /**
     * 释放资源
     */
    fun release() {
        stopListening()
        mainScope.cancel()
        instance = null
    }

    /**
     * 处理布局信息
     */
    private fun processLayoutInfo(layoutInfo: WindowLayoutInfo) {
        val foldingFeatures = layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>()
        _foldingFeatures.value = foldingFeatures

        if (foldingFeatures.isEmpty()) {
            _foldableState.value = FoldableState.FLAT
            return
        }

        // 获取第一个折叠特征的状态
        val feature = foldingFeatures.first()
        _layoutBounds.value = feature.bounds

        _foldableState.value = when (feature.state) {
            FoldingFeature.State.FLAT -> FoldableState.FLAT
            FoldingFeature.State.HALF_OPENED -> FoldableState.HALF_OPENED
            else -> FoldableState.UNKNOWN
        }
    }

    /**
     * 获取折叠铰链区域边界
     */
    fun getFoldingBounds(): Rect? {
        return _layoutBounds.value
    }

    /**
     * 判断是否为折叠屏设备
     */
    fun isFoldableDevice(): Boolean {
        return _foldableState.value != FoldableState.UNKNOWN
    }

    /**
     * 判断当前是否处于展开状态
     */
    fun isFlat(): Boolean {
        return _foldableState.value == FoldableState.FLAT
    }

    /**
     * 判断当前是否处于折叠状态
     */
    fun isFolded(): Boolean {
        return _foldableState.value == FoldableState.FOLDED
    }

    /**
     * 判断当前是否处于半开状态
     */
    fun isHalfOpened(): Boolean {
        return _foldableState.value == FoldableState.HALF_OPENED
    }
}
