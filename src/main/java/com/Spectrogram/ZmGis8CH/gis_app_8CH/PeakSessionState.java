package com.Spectrogram.ZmGis8CH.gis_app_8CH;

/**
 * 持久峰值平滑测试会话状态枚举
 */
public enum PeakSessionState {
    IDLE,
    STARTING,
    WAITING_FOR_ACQUISITION,
    WARMING_UP,
    INITIALIZING,
    RUNNING,
    FINAL_HOLD,
    ERROR
}
