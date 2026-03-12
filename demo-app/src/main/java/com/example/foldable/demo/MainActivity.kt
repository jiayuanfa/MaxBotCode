package com.example.foldable.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.foldable.demo.databinding.ActivityMainBinding
import com.example.foldable.FoldableDeviceHelper
import com.example.foldable.FoldableLayoutHelper
import com.example.foldable.FoldableState
import kotlinx.coroutines.launch

/**
 * Demo应用主界面
 * 功能：演示折叠屏设备检测、状态监听、布局适配功能
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding实例，用于访问布局中的控件
    private lateinit var binding: ActivityMainBinding
    // 折叠屏设备帮助类实例
    private lateinit var foldableHelper: FoldableDeviceHelper
    // 折叠屏布局帮助类实例
    private lateinit var layoutHelper: FoldableLayoutHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化折叠屏相关帮助类
        foldableHelper = FoldableDeviceHelper.getInstance(this)
        layoutHelper = FoldableLayoutHelper(this)

        // 初始化UI控件和点击事件
        setupUI()

        // 首次加载时检查设备类型
        checkDeviceType()

        // 启动协程监听折叠状态变化
        collectFoldableState()
    }

    /**
     * 初始化UI界面和点击事件
     */
    private fun setupUI() {
        // 刷新按钮点击事件
        binding.btnRefresh.setOnClickListener {
            refreshInfo()
        }

        // 布局建议按钮点击事件
        binding.btnTestLayout.setOnClickListener {
            testLayoutRecommendations()
        }
    }

    /**
     * 检查当前设备是否为折叠屏设备
     * 更新界面上的设备类型显示
     */
    private fun checkDeviceType() {
        val isFoldable = foldableHelper.isFoldableDevice()
        val deviceType = if (isFoldable) "折叠屏设备" else "普通设备"
        
        binding.tvDeviceType.text = "设备类型: $deviceType"
        // 根据设备类型设置不同的文字颜色
        binding.tvDeviceType.setTextColor(
            if (isFoldable) ContextCompat.getColor(this, android.R.color.holo_green_dark)
            else ContextCompat.getColor(this, android.R.color.darker_gray)
        )
    }

    /**
     * 启动协程收集折叠状态变化
     * 当折叠状态改变时自动更新UI
     */
    private fun collectFoldableState() {
        lifecycleScope.launch {
            // 订阅折叠状态流
            foldableHelper.foldableState.collect { state ->
                updateStateUI(state)
            }
        }
    }

    /**
     * 根据当前折叠状态更新界面显示
     * @param state 当前折叠状态
     */
    private fun updateStateUI(state: FoldableState) {
        // 根据状态获取对应的显示文字
        val stateText = when (state) {
            FoldableState.FLAT -> "完全展开 (FLAT)"
            FoldableState.HALF_OPENED -> "半开状态 (HALF_OPENED)"
            FoldableState.FOLDED -> "完全折叠 (FOLDED)"
            FoldableState.UNKNOWN -> "未知状态 (UNKNOWN)"
        }
        
        // 根据状态设置不同的文字颜色
        val stateColor = when (state) {
            FoldableState.FLAT -> android.R.color.holo_green_dark  // 绿色-展开
            FoldableState.HALF_OPENED -> android.R.color.holo_orange_dark // 橙色-半开
            FoldableState.FOLDED -> android.R.color.holo_red_dark  // 红色-折叠
            FoldableState.UNKNOWN -> android.R.color.darker_gray   // 灰色-未知
        }
        
        // 更新界面显示
        binding.tvFoldState.text = "折叠状态: $stateText"
        binding.tvFoldState.setTextColor(ContextCompat.getColor(this, stateColor))
    }

    /**
     * 刷新设备信息
     * 点击刷新按钮时调用
     */
    private fun refreshInfo() {
        checkDeviceType()
        Toast.makeText(this, "已刷新设备信息", Toast.LENGTH_SHORT).show()
    }

    /**
     * 显示布局适配建议弹窗
     * 点击布局建议按钮时调用
     */
    private fun testLayoutRecommendations() {
        val recommendations = layoutHelper.getLayoutRecommendations()
        val message = buildString {
            appendLine("布局建议:")
            appendLine("• 是否折叠屏设备: ${recommendations.isFoldable}")
            appendLine("• 是否使用双窗格布局: ${recommendations.shouldUseTwoPane}")
            appendLine("• 内容是否可跨越折叠区域: ${recommendations.shouldSpanContent}")
            appendLine("• 铰链宽度: ${recommendations.hingeWidth}px")
            appendLine("• 铰链高度: ${recommendations.hingeHeight}px")
            appendLine("• 折叠方向: ${recommendations.orientation?.name ?: "未知"}")
        }
        
        // 显示弹窗
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("布局适配建议")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // 页面恢复时开始监听折叠状态
        foldableHelper.startListening(this)
    }

    override fun onPause() {
        super.onPause()
        // 页面暂停时停止监听，节省资源
        foldableHelper.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 页面销毁时释放资源
        foldableHelper.release()
    }
}
