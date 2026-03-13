package com.example.foldable.java;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.window.layout.FoldingFeature;

/**
 * 折叠屏布局帮助类（Java版本）
 * 功能：提供折叠屏布局适配建议，帮助应用在不同折叠状态下自适应显示
 * 使用场景：当需要根据折叠状态调整布局、避开折叠铰链区域时使用
 */
public class FoldableLayoutHelper {

    private final Activity activity;
    private final FoldableDeviceHelper foldableHelper;

    /**
     * 构造方法
     * @param activity Activity实例
     */
    public FoldableLayoutHelper(@NonNull Activity activity) {
        this.activity = activity;
        this.foldableHelper = FoldableDeviceHelper.getInstance(activity);
    }

    /**
     * 判断内容是否应该跨越折叠区域显示
     * @return Boolean true=可以跨越，false=不应该跨越
     */
    public boolean shouldSpanAcrossFold() {
        // 只有设备完全展开时才可以跨越折叠区域
        return foldableHelper.isFlat();
    }

    /**
     * 获取折叠铰链区域的边界坐标
     * @return Rect? 折叠区域坐标，非折叠屏返回null
     */
    @Nullable
    public Rect getFoldBounds() {
        return foldableHelper.getFoldingBounds();
    }

    /**
     * 获取折叠铰链区域的宽度
     * @return Int 铰链宽度，非折叠屏返回0
     */
    public int getFoldWidth() {
        Rect bounds = foldableHelper.getFoldingBounds();
        return bounds != null ? bounds.width() : 0;
    }

    /**
     * 获取折叠铰链区域的高度
     * @return Int 铰链高度，非折叠屏返回0
     */
    public int getFoldHeight() {
        Rect bounds = foldableHelper.getFoldingBounds();
        return bounds != null ? bounds.height() : 0;
    }

    /**
     * 自动调整视图位置，避开折叠铰链区域
     * 当设备处于半开状态时，自动将视图移动到不被铰链遮挡的区域
     * @param view 要调整的视图对象
     * @param avoidFold 是否开启避开折叠区域功能，默认开启
     */
    public void positionViewAvoidingFold(@NonNull View view, boolean avoidFold) {
        // 如果不需要避开或者设备不是半开状态，重置视图边距
        if (!avoidFold || !foldableHelper.isHalfOpened()) {
            setViewMargins(view, 0, 0, 0, 0);
            return;
        }

        // 获取折叠区域和父布局
        Rect foldBounds = foldableHelper.getFoldingBounds();
        if (foldBounds == null) {
            return;
        }
        
        if (!(view.getParent() instanceof ViewGroup)) {
            return;
        }
        ViewGroup parent = (ViewGroup) view.getParent();

        // 获取折叠方向（水平/垂直）
        FoldingFeature.Orientation orientation = null;
        if (!foldableHelper.getFoldingFeatures().isEmpty()) {
            orientation = foldableHelper.getFoldingFeatures().get(0).getOrientation();
        }

        if (orientation == FoldingFeature.Orientation.HORIZONTAL) {
            // 水平折叠（上下折叠），调整上下边距
            if (view.getTop() < foldBounds.centerY()) {
                // 视图在折叠区域上方，底部边距增加到折叠区域顶部
                setViewMargins(view, 0, 0, 0, parent.getHeight() - foldBounds.top);
            } else {
                // 视图在折叠区域下方，顶部边距增加到折叠区域底部
                setViewMargins(view, 0, foldBounds.bottom, 0, 0);
            }
        } else if (orientation == FoldingFeature.Orientation.VERTICAL) {
            // 垂直折叠（左右折叠），调整左右边距
            if (view.getLeft() < foldBounds.centerX()) {
                // 视图在折叠区域左侧，右侧边距增加到折叠区域左侧
                setViewMargins(view, 0, 0, parent.getWidth() - foldBounds.left, 0);
            } else {
                // 视图在折叠区域右侧，左侧边距增加到折叠区域右侧
                setViewMargins(view, foldBounds.right, 0, 0, 0);
            }
        }
    }

    /**
     * 自动调整视图位置，避开折叠铰链区域（默认开启避开功能）
     * @param view 要调整的视图对象
     */
    public void positionViewAvoidingFold(@NonNull View view) {
        positionViewAvoidingFold(view, true);
    }

    /**
     * 内部方法：设置视图的边距
     * @param view 要设置边距的视图
     * @param left 左边距
     * @param top 顶部边距
     * @param right 右边距
     * @param bottom 底部边距
     */
    private void setViewMargins(@NonNull View view, int left, int top, int right, int bottom) {
        if (!(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.setMargins(left, top, right, bottom);
        view.setLayoutParams(params);
    }

    /**
     * 获取窗口系统栏的内边距（状态栏、导航栏等）
     * 用于调整布局避免被系统栏遮挡
     * @return Insets? 系统栏内边距对象
     */
    @Nullable
    public Insets getWindowInsets() {
        View rootView = activity.getWindow() != null ? activity.getWindow().getDecorView().getRootView() : null;
        if (rootView == null) {
            return null;
        }
        
        WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(rootView);
        if (windowInsets == null) {
            return null;
        }
        
        return windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
    }

    /**
     * 获取完整的布局适配建议
     * 根据当前设备的折叠状态返回最合适的布局方案
     * @return LayoutRecommendations 布局建议对象
     */
    @NonNull
    public LayoutRecommendations getLayoutRecommendations() {
        Rect foldBounds = foldableHelper.getFoldingBounds();
        List<FoldingFeature> features = foldableHelper.getFoldingFeatures();
        FoldingFeature.Orientation orientation = null;
        
        if (!features.isEmpty()) {
            orientation = features.get(0).getOrientation();
        }
        
        return new LayoutRecommendations(
            foldableHelper.isFoldableDevice(),      // 是否为折叠屏设备
            !features.isEmpty(),                    // 是否为双屏设备
            foldBounds,                              // 折叠区域坐标
            foldableHelper.isFlat() && !features.isEmpty(), // 是否应该使用双窗格布局
            foldableHelper.isFlat(),          // 内容是否可以跨越折叠区域
            foldBounds != null ? foldBounds.width() : 0,                // 铰链宽度
            foldBounds != null ? foldBounds.height() : 0,              // 铰链高度
            orientation     // 折叠方向
        );
    }

    /**
     * 释放资源
     * 建议在 Activity 的 onDestroy 方法中调用
     */
    public void release() {
        // FoldableDeviceHelper 的释放由调用者负责，这里不需要额外处理
    }

    /**
     * 布局建议数据类
     * 封装了所有布局适配需要的信息
     */
    public static class LayoutRecommendations {
        private final boolean isFoldable;               // 是否为折叠屏设备
        private final boolean isDualScreen;             // 是否为双屏设备
        private final Rect foldBounds;                 // 折叠区域边界
        private final boolean shouldUseTwoPane;         // 是否应该使用双窗格布局
        private final boolean shouldSpanContent;        // 内容是否可以跨越折叠区域
        private final int hingeWidth;                   // 铰链宽度（像素）
        private final int hingeHeight;                  // 铰链高度（像素）
        private final FoldingFeature.Orientation orientation; // 折叠方向

        public LayoutRecommendations(boolean isFoldable, boolean isDualScreen, 
                                   @Nullable Rect foldBounds, boolean shouldUseTwoPane,
                                   boolean shouldSpanContent, int hingeWidth, 
                                   int hingeHeight, @Nullable FoldingFeature.Orientation orientation) {
            this.isFoldable = isFoldable;
            this.isDualScreen = isDualScreen;
            this.foldBounds = foldBounds != null ? new Rect(foldBounds) : null;
            this.shouldUseTwoPane = shouldUseTwoPane;
            this.shouldSpanContent = shouldSpanContent;
            this.hingeWidth = hingeWidth;
            this.hingeHeight = hingeHeight;
            this.orientation = orientation;
        }

        public boolean isFoldable() {
            return isFoldable;
        }

        public boolean isDualScreen() {
            return isDualScreen;
        }

        @Nullable
        public Rect getFoldBounds() {
            return foldBounds != null ? new Rect(foldBounds) : null;
        }

        public boolean shouldUseTwoPane() {
            return shouldUseTwoPane;
        }

        public boolean shouldSpanContent() {
            return shouldSpanContent;
        }

        public int getHingeWidth() {
            return hingeWidth;
        }

        public int getHingeHeight() {
            return hingeHeight;
        }

        @Nullable
        public FoldingFeature.Orientation getOrientation() {
            return orientation;
        }
    }
}
