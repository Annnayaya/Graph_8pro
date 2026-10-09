package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.util.Arrays;

/**
 * 采集上下文信息
 * 传递校准范围、增益、通道启用状态、帧中断标记与时间戳
 */
public final class PeakMeasurementContext {
    private final int calMinDbm;
    private final int calMaxDbm;
    private final int[] gainDbm;
    private final boolean[] enabled;
    private final float syncFreq;
    private final boolean packetGap;
    private final long timestampMillis;

    public PeakMeasurementContext(int calMinDbm, int calMaxDbm, int[] gainDbm,
                                  boolean[] enabled, float syncFreq, boolean packetGap,
                                  long timestampMillis) {
        this.calMinDbm = calMinDbm;
        this.calMaxDbm = calMaxDbm;
        this.gainDbm = (gainDbm != null) ? gainDbm.clone() : new int[8];
        this.enabled = (enabled != null) ? enabled.clone() : new boolean[8];
        this.syncFreq = syncFreq;
        this.packetGap = packetGap;
        this.timestampMillis = timestampMillis > 0 ? timestampMillis : System.currentTimeMillis();
    }

    public int getCalMinDbm() {
        return calMinDbm;
    }

    public int getCalMaxDbm() {
        return calMaxDbm;
    }

    public int getGainDbm(int channel) {
        if (channel >= 0 && channel < gainDbm.length) {
            return gainDbm[channel];
        }
        return 0;
    }

    public int[] getGainDbmCopy() {
        return gainDbm.clone();
    }

    public boolean isEnabled(int channel) {
        if (channel >= 0 && channel < enabled.length) {
            return enabled[channel];
        }
        return true;
    }

    public boolean[] getEnabledCopy() {
        return enabled.clone();
    }

    public float getSyncFreq() {
        return syncFreq;
    }

    public boolean isPacketGap() {
        return packetGap;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }
}
