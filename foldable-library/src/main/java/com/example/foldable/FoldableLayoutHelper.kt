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
 * 用于帮助应用在折叠屏设备上进行自适应布局
 */
class FoldableLayoutHelper(private val activity: Activity) {

    private val foldableHelper = FoldableDeviceHelper.getInstance(activity)

    /**
     * 判断内容是否应该跨越折叠区域
     */
    fun shouldSpanAcrossFold(): Boolean {
        return foldableHelper.isFlat()
    }

    /**
     * 获取折叠区域边界
     */
    fun getFoldBounds(): Rect? {
        return foldableHelper.getFoldingBounds()
    }

    /**
     * 获取折叠区域的宽度
     */
    fun getFoldWidth(): Int {
        return foldableHelper.getFoldingBounds()?.width() ?: 0
    }

    /**
     * 获取折叠区域的高度
     */
    fun getFoldHeight(): Int {
        return foldableHelper.getFoldingBounds()?.height() ?: 0
    }

    /**
     * 将视图避开折叠区域放置
     * @param view 要调整的视图
     * @param avoidFold 是否避开折叠区域
     */
    fun positionViewAvoidingFold(view: View, avoidFold: Boolean = true) {
        if (!avoidFold || !foldableHelper.isHalfOpened()) {
            // 重置所有边距
            setViewMargins(view, 0, 0, 0, 0)
            return
        }

        val foldBounds = foldableHelper.getFoldingBounds() ?: return
        val parent = view.parent as? ViewGroup ?: return

        val foldOrientation = foldableHelper.foldingFeatures.value.firstOrNull()?.orientation

        when (foldOrientation) {
            FoldingFeature.Orientation.HORIZONTAL -> {
                // 水平折叠，调整上下边距
                if (view.top < foldBounds.centerY()) {
                    // 视图在折叠区域上方
                    setViewMargins(view, 0, 0, 0, parent.height - foldBounds.top)
                } else {
                    // 视图在折叠区域下方
                    setViewMargins(view, 0, foldBounds.bottom, 0, 0)
                }
            }
            FoldingFeature.Orientation.VERTICAL -> {
                // 垂直折叠，调整左右边距
                if (view.left < foldBounds.centerX()) {
                    // 视图在折叠区域左侧
                    setViewMargins(view, 0, 0, parent.width - foldBounds.left, 0)
                } else {
                    // 视图在折叠区域右侧
                    setViewMargins(view, foldBounds.right, 0, 0, 0)
                }
            }
            else -> {
                // 未知方向，不调整
            }
        }
    }

    /**
     * 设置视图边距
     */
    private fun setViewMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        val params = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        params.setMargins(left, top, right, bottom)
        view.layoutParams = params
    }

    /**
     * 获取窗口内边距（考虑系统栏）
     */
    fun getWindowInsets(): androidx.core.graphics.Insets? {
        val rootView = activity.window?.decorView?.rootView ?: return null
        val windowInsets = ViewCompat.getRootWindowInsets(rootView) ?: return null
        return windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * 为折叠屏优化的通用布局建议
     */
    fun getLayoutRecommendations(): LayoutRecommendations {
        val foldBounds = foldableHelper.getFoldingBounds()
        val features = foldableHelper.foldingFeatures.value
        
        return LayoutRecommendations(
            isFoldable = foldableHelper.isFoldableDevice(),
            isDualScreen = features.isNotEmpty(),
            foldBounds = foldBounds,
            shouldUseTwoPane = foldableHelper.isFlat() && features.isNotEmpty(),
            shouldSpanContent = foldableHelper.isFlat(),
            hingeWidth = foldBounds?.width() ?: 0,
            hingeHeight = foldBounds?.height() ?: 0,
            orientation = features.firstOrNull()?.orientation
        )
    }
}

/**
 * 布局建议数据类
 */
data class LayoutRecommendations(
    val isFoldable: Boolean,
    val isDualScreen: Boolean,
    val foldBounds: Rect?,
    val shouldUseTwoPane: Boolean,
    val shouldSpanContent: Boolean,
    val hingeWidth: Int,
    val hingeHeight: Int,
    val orientation: FoldingFeature.Orientation?
)
