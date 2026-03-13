package com.example.foldable.java;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;

import com.example.foldable.FoldableDeviceHelper;
import com.example.foldable.FoldableState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;

/**
 * 折叠屏设备帮助类（Java版本）
 * 功能：检测折叠屏设备状态、监听折叠状态变化、获取折叠区域信息
 * 兼容性：支持 Android 7.0+ (API 24)
 * 依赖：Jetpack WindowManager
 */
public class FoldableDeviceHelper {

    // 单例实例，使用volatile保证线程安全
    private static volatile FoldableDeviceHelper instance;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private FoldableState currentState = FoldableState.UNKNOWN;
    private List<FoldingFeature> foldingFeatures = new ArrayList<>();
    private Rect layoutBounds = null;
    private final List<FoldableStateListener> listeners = new ArrayList<>();
    private boolean isListening = false;

    /**
     * 折叠状态变化监听器接口
     */
    public interface FoldableStateListener {
        /**
         * 当折叠状态发生变化时回调
         * @param newState 新的折叠状态
         * @param foldingFeatures 折叠特征列表
         * @param layoutBounds 折叠区域边界
         */
        void onFoldableStateChanged(@NonNull FoldableState newState, 
                                  @NonNull List<FoldingFeature> foldingFeatures, 
                                  @Nullable Rect layoutBounds);
    }

    /**
     * 私有构造方法，防止外部实例化
     * @param context 上下文
     */
    private FoldableDeviceHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 获取 FoldableDeviceHelper 单例实例
     * @param context 上下文，自动使用ApplicationContext避免内存泄漏
     * @return FoldableDeviceHelper 实例
     */
    public static FoldableDeviceHelper getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (FoldableDeviceHelper.class) {
                if (instance == null) {
                    instance = new FoldableDeviceHelper(context);
                }
            }
        }
        return instance;
    }

    /**
     * 添加折叠状态变化监听器
     * @param listener 监听器实例
     */
    public void addListener(@NonNull FoldableStateListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    /**
     * 移除折叠状态变化监听器
     * @param listener 监听器实例
     */
    public void removeListener(@NonNull FoldableStateListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * 开始监听 Activity 的折叠状态变化
     * 建议在 Activity 的 onResume 方法中调用
     * @param activity 要监听的Activity实例
     */
    public void startListening(@NonNull Activity activity) {
        if (isListening) {
            return;
        }
        isListening = true;

        executorService.submit(() -> {
            try {
                WindowInfoTracker windowInfoTracker = WindowInfoTracker.getOrCreate(activity);
                Flow<WindowLayoutInfo> flow = windowInfoTracker.windowLayoutInfo(activity);
                
                flow.collect(new FlowCollector<WindowLayoutInfo>() {
                    @Override
                    public void emit(WindowLayoutInfo layoutInfo) {
                        processLayoutInfo(layoutInfo);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                updateState(FoldableState.UNKNOWN, new ArrayList<>(), null);
            }
        });
    }

    /**
     * 停止监听折叠状态变化
     * 建议在 Activity 的 onPause 方法中调用
     */
    public void stopListening() {
        isListening = false;
        executorService.shutdownNow();
    }

    /**
     * 释放所有资源
     * 建议在 Activity 的 onDestroy 方法中调用
     */
    public void release() {
        stopListening();
        synchronized (listeners) {
            listeners.clear();
        }
        instance = null;
    }

    /**
     * 内部方法：处理窗口布局信息，更新折叠状态
     * @param layoutInfo 窗口布局信息
     */
    private void processLayoutInfo(@NonNull WindowLayoutInfo layoutInfo) {
        List<FoldingFeature> features = new ArrayList<>();
        for (Object displayFeature : layoutInfo.getDisplayFeatures()) {
            if (displayFeature instanceof FoldingFeature) {
                features.add((FoldingFeature) displayFeature);
            }
        }

        Rect bounds = null;
        FoldableState state = FoldableState.FLAT;

        if (!features.isEmpty()) {
            FoldingFeature feature = features.get(0);
            bounds = feature.getBounds();
            
            if (feature.getState() == FoldingFeature.State.FLAT) {
                state = FoldableState.FLAT;
            } else if (feature.getState() == FoldingFeature.State.HALF_OPENED) {
                state = FoldableState.HALF_OPENED;
            } else {
                state = FoldableState.UNKNOWN;
            }
        }

        updateState(state, features, bounds);
    }

    /**
     * 内部方法：更新状态并通知所有监听器
     * @param newState 新状态
     * @param features 折叠特征列表
     * @param bounds 折叠区域边界
     */
    private void updateState(@NonNull FoldableState newState, 
                           @NonNull List<FoldingFeature> features, 
                           @Nullable Rect bounds) {
        this.currentState = newState;
        this.foldingFeatures = features;
        this.layoutBounds = bounds;

        // 在主线程通知所有监听器
        mainHandler.post(() -> {
            synchronized (listeners) {
                for (FoldableStateListener listener : listeners) {
                    listener.onFoldableStateChanged(newState, new ArrayList<>(features), 
                            bounds != null ? new Rect(bounds) : null);
                }
            }
        });
    }

    /**
     * 获取折叠铰链区域的边界坐标
     * @return Rect? 折叠区域的坐标，非折叠屏设备返回null
     */
    @Nullable
    public Rect getFoldingBounds() {
        return layoutBounds != null ? new Rect(layoutBounds) : null;
    }

    /**
     * 判断当前设备是否为折叠屏设备
     * @return Boolean true=折叠屏设备，false=普通设备
     */
    public boolean isFoldableDevice() {
        return currentState != FoldableState.UNKNOWN;
    }

    /**
     * 判断当前是否处于完全展开状态
     * @return Boolean true=完全展开
     */
    public boolean isFlat() {
        return currentState == FoldableState.FLAT;
    }

    /**
     * 判断当前是否处于完全折叠状态
     * @return Boolean true=完全折叠
     */
    public boolean isFolded() {
        return currentState == FoldableState.FOLDED;
    }

    /**
     * 判断当前是否处于半开状态（帐篷模式）
     * @return Boolean true=半开状态
     */
    public boolean isHalfOpened() {
        return currentState == FoldableState.HALF_OPENED;
    }

    /**
     * 获取当前折叠状态
     * @return FoldableState 当前状态
     */
    @NonNull
    public FoldableState getCurrentState() {
        return currentState;
    }

    /**
     * 获取当前折叠特征列表
     * @return List<FoldingFeature> 折叠特征列表
     */
    @NonNull
    public List<FoldingFeature> getFoldingFeatures() {
        return new ArrayList<>(foldingFeatures);
    }
}
