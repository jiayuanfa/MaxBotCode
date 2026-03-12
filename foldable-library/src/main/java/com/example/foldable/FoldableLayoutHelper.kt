package com.example.foldable

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.window.layout.FoldingFeature

/**
 * 折叠屏布局帮助类
 * 功能：提供折叠屏布局适配建议，帮助应用在不同折叠状态下自适应显示
 * 使用场景：当需要根据折叠状态调整布局、避开折叠铰链区域时使用
 */
class FoldableLayoutHelper(private val activity: Activity) {

    // 折叠屏设备帮助类实例
    private val foldableHelper = FoldableDeviceHelper.getInstance(activity)

    /**
     * 判断内容是否应该跨越折叠区域显示
     * @return Boolean true=可以跨越，false=不应该跨越
     */
    fun shouldSpanAcrossFold(): Boolean {
        // 只有设备完全展开时才可以跨越折叠区域
        return foldableHelper.isFlat()
    }

    /**
     * 获取折叠铰链区域的边界坐标
     * @return Rect? 折叠区域坐标，非折叠屏返回null
     */
    fun getFoldBounds(): Rect? {
        return foldableHelper.getFoldingBounds()
    }

    /**
     * 获取折叠铰链区域的宽度
     * @return Int 铰链宽度，非折叠屏返回0
     */
    fun getFoldWidth(): Int {
        return foldableHelper.getFoldingBounds()?.width() ?: 0
    }

    /**
     * 获取折叠铰链区域的高度
     * @return Int 铰链高度，非折叠屏返回0
     */
    fun getFoldHeight(): Int {
        return foldableHelper.getFoldingBounds()?.height() ?: 0
    }

    /**
     * 自动调整视图位置，避开折叠铰链区域
     * 当设备处于半开状态时，自动将视图移动到不被铰链遮挡的区域
     * @param view 要调整的视图对象
     * @param avoidFold 是否开启避开折叠区域功能，默认开启
     */
    fun positionViewAvoidingFold(view: View, avoidFold: Boolean = true) {
        // 如果不需要避开或者设备不是半开状态，重置视图边距
        if (!avoidFold || !foldableHelper.isHalfOpened()) {
            setViewMargins(view, 0, 0, 0, 0)
            return
        }

        // 获取折叠区域和父布局
        val foldBounds = foldableHelper.getFoldingBounds() ?: return
        val parent = view.parent as? ViewGroup ?: return

        // 获取折叠方向（水平/垂直）
        val foldOrientation = foldableHelper.foldingFeatures.value.firstOrNull()?.orientation

        when (foldOrientation) {
            FoldingFeature.Orientation.HORIZONTAL -> {
                // 水平折叠（上下折叠），调整上下边距
                if (view.top < foldBounds.centerY()) {
                    // 视图在折叠区域上方，底部边距增加到折叠区域顶部
                    setViewMargins(view, 0, 0, 0, parent.height - foldBounds.top)
                } else {
                    // 视图在折叠区域下方，顶部边距增加到折叠区域底部
                    setViewMargins(view, 0, foldBounds.bottom, 0, 0)
                }
            }
            FoldingFeature.Orientation.VERTICAL -> {
                // 垂直折叠（左右折叠），调整左右边距
                if (view.left < foldBounds.centerX()) {
                    // 视图在折叠区域左侧，右侧边距增加到折叠区域左侧
                    setViewMargins(view, 0, 0, parent.width - foldBounds.left, 0)
                } else {
                    // 视图在折叠区域右侧，左侧边距增加到折叠区域右侧
                    setViewMargins(view, foldBounds.right, 0, 0, 0)
                }
            }
            else -> {
                // 未知折叠方向，不做调整
            }
        }
    }

    /**
     * 内部方法：设置视图的边距
     * @param view 要设置边距的视图
     * @param left 左边距
     * @param top 顶部边距
     * @param right 右边距
     * @param bottom 底部边距
     */
    private fun setViewMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        val params = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        params.setMargins(left, top, right, bottom)
        view.layoutParams = params
    }

    /**
     * 获取窗口系统栏的内边距（状态栏、导航栏等）
     * 用于调整布局避免被系统栏遮挡
     * @return Insets? 系统栏内边距对象
     */
    fun getWindowInsets(): androidx.core.graphics.Insets? {
        val rootView = activity.window?.decorView?.rootView ?: return null
        val windowInsets = ViewCompat.getRootWindowInsets(rootView) ?: return null
        return windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * 获取完整的布局适配建议
     * 根据当前设备的折叠状态返回最合适的布局方案
     * @return LayoutRecommendations 布局建议对象
     */
    fun getLayoutRecommendations(): LayoutRecommendations {
        val foldBounds = foldableHelper.getFoldingBounds()
        val features = foldableHelper.foldingFeatures.value
        
        return LayoutRecommendations(
            isFoldable = foldableHelper.isFoldableDevice(),      // 是否为折叠屏设备
            isDualScreen = features.isNotEmpty(),                // 是否为双屏设备
            foldBounds = foldBounds,                              // 折叠区域坐标
            shouldUseTwoPane = foldableHelper.isFlat() && features.isNotEmpty(), // 是否应该使用双窗格布局
            shouldSpanContent = foldableHelper.isFlat(),          // 内容是否可以跨越折叠区域
            hingeWidth = foldBounds?.width() ?: 0,                // 铰链宽度
            hingeHeight = foldBounds?.height() ?: 0,              // 铰链高度
            orientation = features.firstOrNull()?.orientation     // 折叠方向
        )
    }

    /**
     * 释放资源
     * 建议在 Activity 的 onDestroy 方法中调用
     */
    fun release() {
        // FoldableDeviceHelper 的释放由调用者负责，这里不需要额外处理
    }
}

/**
 * 布局建议数据类
 * 封装了所有布局适配需要的信息
 */
data class LayoutRecommendations(
    val isFoldable: Boolean,               // 是否为折叠屏设备
    val isDualScreen: Boolean,             // 是否为双屏设备
    val foldBounds: Rect?,                 // 折叠区域边界
    val shouldUseTwoPane: Boolean,         // 是否应该使用双窗格布局
    val shouldSpanContent: Boolean,        // 内容是否可以跨越折叠区域
    val hingeWidth: Int,                   // 铰链宽度（像素）
    val hingeHeight: Int,                  // 铰链高度（像素）
    val orientation: FoldingFeature.Orientation? // 折叠方向
)
