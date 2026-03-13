package com.example.foldable.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.foldable.demo.databinding.ActivityDemoBinding;
import com.example.foldable.FoldableState;
import com.example.foldable.java.FoldableDeviceHelper;
import com.example.foldable.java.FoldableLayoutHelper;
import com.example.foldable.java.FoldableLayoutHelper.LayoutRecommendations;

import java.util.List;

import androidx.window.layout.FoldingFeature;
import android.graphics.Rect;

/**
 * Java版本演示界面
 * 功能：演示折叠屏设备检测、状态监听、布局适配功能
 */
public class JavaDemoActivity extends AppCompatActivity implements FoldableDeviceHelper.FoldableStateListener {

    // ViewBinding实例，用于访问布局中的控件
    private ActivityDemoBinding binding;
    // 折叠屏设备帮助类实例
    private FoldableDeviceHelper foldableHelper;
    // 折叠屏布局帮助类实例
    private FoldableLayoutHelper layoutHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化ViewBinding
        binding = ActivityDemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置标题
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Java 版本演示");
        }

        // 初始化折叠屏相关帮助类
        foldableHelper = FoldableDeviceHelper.getInstance(this);
        layoutHelper = new FoldableLayoutHelper(this);

        // 初始化UI控件和点击事件
        setupUI();

        // 首次加载时检查设备类型
        checkDeviceType();

        // 添加折叠状态监听器
        foldableHelper.addListener(this);
    }

    /**
     * 初始化UI界面和点击事件
     */
    private void setupUI() {
        // 刷新按钮点击事件
        binding.btnRefresh.setOnClickListener(v -> refreshInfo());

        // 布局建议按钮点击事件
        binding.btnTestLayout.setOnClickListener(v -> testLayoutRecommendations());

        // 返回按钮
        binding.btnBack.setOnClickListener(v -> finish());
    }

    /**
     * 检查当前设备是否为折叠屏设备
     * 更新界面上的设备类型显示
     */
    private void checkDeviceType() {
        boolean isFoldable = foldableHelper.isFoldableDevice();
        String deviceType = isFoldable ? "折叠屏设备" : "普通设备";
        
        binding.tvDeviceType.setText("设备类型: " + deviceType);
        // 根据设备类型设置不同的文字颜色
        binding.tvDeviceType.setTextColor(
            ContextCompat.getColor(this, isFoldable ? 
                android.R.color.holo_green_dark : android.R.color.darker_gray)
        );
    }

    /**
     * 折叠状态变化回调
     * @param newState 新的折叠状态
     * @param foldingFeatures 折叠特征列表
     * @param layoutBounds 折叠区域边界
     */
    @Override
    public void onFoldableStateChanged(@NonNull FoldableState newState, 
                                     @NonNull List<FoldingFeature> foldingFeatures, 
                                     @Nullable Rect layoutBounds) {
        updateStateUI(newState);
    }

    /**
     * 根据当前折叠状态更新界面显示
     * @param state 当前折叠状态
     */
    private void updateStateUI(FoldableState state) {
        // 根据状态获取对应的显示文字
        String stateText;
        int stateColor;
        
        if (state == FoldableState.FLAT) {
            stateText = "完全展开 (FLAT)";
            stateColor = android.R.color.holo_green_dark;  // 绿色-展开
        } else if (state == FoldableState.HALF_OPENED) {
            stateText = "半开状态 (HALF_OPENED)";
            stateColor = android.R.color.holo_orange_dark; // 橙色-半开
        } else if (state == FoldableState.FOLDED) {
            stateText = "完全折叠 (FOLDED)";
            stateColor = android.R.color.holo_red_dark;  // 红色-折叠
        } else {
            stateText = "未知状态 (UNKNOWN)";
            stateColor = android.R.color.darker_gray;   // 灰色-未知
        }
        
        // 更新界面显示
        binding.tvFoldState.setText("折叠状态: " + stateText);
        binding.tvFoldState.setTextColor(ContextCompat.getColor(this, stateColor));
    }

    /**
     * 刷新设备信息
     * 点击刷新按钮时调用
     */
    private void refreshInfo() {
        checkDeviceType();
        Toast.makeText(this, "已刷新设备信息", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示布局适配建议弹窗
     * 点击布局建议按钮时调用
     */
    private void testLayoutRecommendations() {
        LayoutRecommendations recommendations = layoutHelper.getLayoutRecommendations();
        StringBuilder message = new StringBuilder();
        
        message.append("布局建议:\n");
        message.append("• 是否折叠屏设备: ").append(recommendations.isFoldable()).append("\n");
        message.append("• 是否使用双窗格布局: ").append(recommendations.shouldUseTwoPane()).append("\n");
        message.append("• 内容是否可跨越折叠区域: ").append(recommendations.shouldSpanContent()).append("\n");
        message.append("• 铰链宽度: ").append(recommendations.getHingeWidth()).append("px\n");
        message.append("• 铰链高度: ").append(recommendations.getHingeHeight()).append("px\n");
        message.append("• 折叠方向: ").append(recommendations.getOrientation() != null ? 
            recommendations.getOrientation().name() : "未知");
        
        // 显示弹窗
        new AlertDialog.Builder(this)
            .setTitle("布局适配建议")
            .setMessage(message.toString())
            .setPositiveButton("确定", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 页面恢复时开始监听折叠状态
        foldableHelper.startListening(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 页面暂停时停止监听，节省资源
        foldableHelper.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除监听器并释放资源
        foldableHelper.removeListener(this);
        foldableHelper.release();
        binding = null;
    }
}
