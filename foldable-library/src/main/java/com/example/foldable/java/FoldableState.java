package com.example.foldable.java;

/**
 * 折叠屏设备折叠状态枚举
 * Java版本
 */
public enum FoldableState {
    /**
     * 设备完全展开状态
     */
    FLAT,
    
    /**
     * 设备半折叠状态（帐篷模式或半开）
     */
    HALF_OPENED,
    
    /**
     * 设备完全折叠状态
     */
    FOLDED,
    
    /**
     * 无法确定状态或设备不支持折叠
     */
    UNKNOWN
}
