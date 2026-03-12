package com.example.foldable.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.foldable.demo.databinding.ActivityMainBinding
import com.example.foldable.library.FoldableDeviceHelper
import com.example.foldable.library.FoldableLayoutHelper
import com.example.foldable.library.FoldableState
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var foldableHelper: FoldableDeviceHelper
    private lateinit var layoutHelper: FoldableLayoutHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化折叠屏帮助类
        foldableHelper = FoldableDeviceHelper.getInstance(this)
        layoutHelper = FoldableLayoutHelper(this)

        // 设置UI
        setupUI()

        // 检查设备类型
        checkDeviceType()

        // 收集折叠状态
        collectFoldableState()
    }

    private fun setupUI() {
        binding.btnRefresh.setOnClickListener {
            refreshInfo()
        }

        binding.btnTestLayout.setOnClickListener {
            testLayoutRecommendations()
        }
    }

    private fun checkDeviceType() {
        val isFoldable = foldableHelper.isFoldableDevice()
        val deviceType = if (isFoldable) "折叠屏设备" else "普通设备"
        
        binding.tvDeviceType.text = "设备类型: $deviceType"
        binding.tvDeviceType.setTextColor(
            if (isFoldable) ContextCompat.getColor(this, android.R.color.holo_green_dark)
            else ContextCompat.getColor(this, android.R.color.darker_gray)
        )
    }

    private fun collectFoldableState() {
        lifecycleScope.launch {
            foldableHelper.foldableState.collect { state ->
                updateStateUI(state)
            }
        }
    }

    private fun updateStateUI(state: FoldableState) {
        val stateText = when (state) {
            FoldableState.FLAT -> "完全展开 (FLAT)"
            FoldableState.HALF_OPENED -> "半开状态 (HALF_OPENED)"
            FoldableState.FOLDED -> "完全折叠 (FOLDED)"
            FoldableState.UNKNOWN -> "未知状态 (UNKNOWN)"
        }
        
        val stateColor = when (state) {
            FoldableState.FLAT -> android.R.color.holo_green_dark
            FoldableState.HALF_OPENED -> android.R.color.holo_orange_dark
            FoldableState.FOLDED -> android.R.color.holo_red_dark
            FoldableState.UNKNOWN -> android.R.color.darker_gray
        }
        
        binding.tvFoldState.text = "折叠状态: $stateText"
        binding.tvFoldState.setTextColor(ContextCompat.getColor(this, stateColor))
    }

    private fun refreshInfo() {
        checkDeviceType()
        Toast.makeText(this, "已刷新设备信息", Toast.LENGTH_SHORT).show()
    }

    private fun testLayoutRecommendations() {
        val recommendations = layoutHelper.getLayoutRecommendations()
        val message = buildString {
            appendLine("布局建议:")
            appendLine("• 是否使用双窗格: ${recommendations.shouldUseTwoPane}")
            appendLine("• 建议使用屏幕宽度: ${recommendations.preferredScreenWidthDp}dp")
            appendLine("• 当前屏幕状态: ${recommendations.currentScreenState}")
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("布局适配建议")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
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
        layoutHelper.release()
    }
}
