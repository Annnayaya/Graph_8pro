package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.util.Arrays;

/**
 * 不可变的持久峰值发布快照
 * 供 UI 显示与 CSV 异步写入消费，保证两者数据严格一致
 */
public final class PersistentPeakSnapshot {
    private final long sequence;
    private final long timestampMillis;
    private final long elapsedMillis;
    private final PeakSessionState sessionState;
    private final double[] publishedDbm; // 8 通道对外发布值（已统一 1 位小数舍入）
    private final boolean[] valid;        // 8 通道就绪/有效标记
    private final String event;

    public PersistentPeakSnapshot(long sequence, long timestampMillis, long elapsedMillis,
                                  PeakSessionState sessionState, double[] rawValues,
                                  boolean[] valid, String event) {
        this.sequence = sequence;
        this.timestampMillis = timestampMillis;
        this.elapsedMillis = elapsedMillis;
        this.sessionState = sessionState != null ? sessionState : PeakSessionState.IDLE;
        this.event = event != null ? event : "";

        int len = rawValues != null ? rawValues.length : 8;
        this.publishedDbm = new double[len];
        this.valid = new boolean[len];

        for (int i = 0; i < len; i++) {
            boolean v = (valid != null && i < valid.length && valid[i]);
            this.valid[i] = v;
            if (v && rawValues != null && i < rawValues.length) {
                this.publishedDbm[i] = PeakValueFormatter.roundForPublish(rawValues[i]);
            } else {
                this.publishedDbm[i] = Double.NaN;
            }
        }
    }

    public long getSequence() {
        return sequence;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public double getElapsedSeconds() {
        return elapsedMillis / 1000.0;
    }

    public PeakSessionState getSessionState() {
        return sessionState;
    }

    public double getPublishedDbm(int channel) {
        if (channel >= 0 && channel < publishedDbm.length) {
            return publishedDbm[channel];
        }
        return Double.NaN;
    }

    public double[] getPublishedDbmCopy() {
        return publishedDbm.clone();
    }

    public boolean isValid(int channel) {
        if (channel >= 0 && channel < valid.length) {
            return valid[channel];
        }
        return false;
    }

    public boolean[] getValidCopy() {
        return valid.clone();
    }

    public int getChannelCount() {
        return publishedDbm.length;
    }

    public String getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "PersistentPeakSnapshot{" +
                "seq=" + sequence +
                ", time=" + timestampMillis +
                ", elapsed=" + elapsedMillis + "ms" +
                ", state=" + sessionState +
                ", dbm=" + Arrays.toString(publishedDbm) +
                ", event='" + event + '\'' +
                '}';
    }
}
