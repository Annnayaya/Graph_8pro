package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 持久峰值平滑核心计算与会话管理服务
 */
public class PersistentPeakService {
    private static final Logger LOGGER = Logger.getLogger(PersistentPeakService.class.getName());

    public interface SnapshotListener {
        void onSnapshotPublished(PersistentPeakSnapshot snapshot);
    }

    private static class ChannelState {
        int channelIndex;
        int currentGroupPeakRaw;
        int warmupGroupsRemaining;
        final List<Double> initialSamples = new ArrayList<>();
        final List<Double> recentSamples = new ArrayList<>();
        double publishedInternalDbm;
        double lockedBaselineDbm;
        boolean isTracking;
        double lastSignificantDbm;
        final List<Double> switchSamples = new ArrayList<>();
        boolean isSwitching = false;
        boolean ready;
        boolean enabled = true;
        int lastGain;
        int lastCalMin;
        int lastCalMax;
        boolean configInitialized = false;

        void reset(PersistentPeakConfig config) {
            currentGroupPeakRaw = 0;
            warmupGroupsRemaining = config.getWarmupGroups();
            initialSamples.clear();
            recentSamples.clear();
            switchSamples.clear();
            isSwitching = false;
            publishedInternalDbm = Double.NaN;
            lockedBaselineDbm = Double.NaN;
            isTracking = false;
            lastSignificantDbm = Double.NaN;
            ready = false;
            configInitialized = false;
        }
    }

    private final Object stateLock = new Object();
    private PersistentPeakConfig config;
    private PersistentPeakSession currentSession;
    private PersistentPeakCsvWriter csvWriter;
    private PeakSessionState sessionState = PeakSessionState.IDLE;
    private long sessionStartMillis = 0;
    private long sequence = 0;
    private int cyclesInCurrentGroup = 0;
    private final ChannelState[] channels = new ChannelState[8];
    private final List<SnapshotListener> listeners = new CopyOnWriteArrayList<>();
    private PersistentPeakSnapshot lastSnapshot;
    private String pendingEvent = "";

    public PersistentPeakService() {
        this(PersistentPeakConfig.loadOrDefault());
    }

    public PersistentPeakService(PersistentPeakConfig config) {
        this.config = (config != null) ? config : new PersistentPeakConfig();
        for (int i = 0; i < 8; i++) {
            channels[i] = new ChannelState();
            channels[i].channelIndex = i;
            channels[i].reset(this.config);
        }
    }

    public void addListener(SnapshotListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(SnapshotListener listener) {
        listeners.remove(listener);
    }

    /**
     * 启动新的持久测试会话
     */
    public void startSession(PersistentPeakSession session) {
        synchronized (stateLock) {
            stopSessionInternal(); // 停止并清理前一个会话

            this.currentSession = session != null ? session : PersistentPeakSession.create(this.config);
            this.config = this.currentSession.getConfig();
            this.sessionState = PeakSessionState.STARTING;
            this.sessionStartMillis = System.currentTimeMillis();
            this.sequence = 0;
            this.cyclesInCurrentGroup = 0;
            this.pendingEvent = "";

            for (int i = 0; i < 8; i++) {
                channels[i].reset(this.config);
            }

            // 初始化会话输出目录和 HTML 模板
            boolean dirOk = this.currentSession.initDirectory();
            if (dirOk) {
                PeakTrendHtmlGenerator.generateHtml(this.currentSession.getHtmlFile(), this.config);
                this.csvWriter = new PersistentPeakCsvWriter(this.currentSession);
                this.csvWriter.start();
                this.sessionState = PeakSessionState.WAITING_FOR_ACQUISITION;
            } else {
                this.sessionState = PeakSessionState.ERROR;
                this.pendingEvent = "SESSION_DIR_ERROR";
            }

            // 生成初始空快照
            double[] rawInit = new double[8];
            boolean[] validInit = new boolean[8];
            for (int i = 0; i < 8; i++) {
                rawInit[i] = Double.NaN;
                validInit[i] = false;
            }
            this.lastSnapshot = new PersistentPeakSnapshot(0, sessionStartMillis, 0,
                    this.sessionState, rawInit, validInit, pendingEvent);
            notifyListeners(this.lastSnapshot);
        }
    }

    /**
     * 停止当前持久测试会话，进入最终保持状态
     */
    public void stopSession() {
        synchronized (stateLock) {
            stopSessionInternal();
        }
    }

    private void stopSessionInternal() {
        if (sessionState == PeakSessionState.IDLE || sessionState == PeakSessionState.FINAL_HOLD) {
            return;
        }
        if (csvWriter != null) {
            try {
                csvWriter.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing CSV writer", e);
            }
            csvWriter = null;
        }
        this.sessionState = PeakSessionState.FINAL_HOLD;
        if (lastSnapshot != null) {
            // 生成保持状态快照
            this.lastSnapshot = new PersistentPeakSnapshot(
                    lastSnapshot.getSequence(),
                    System.currentTimeMillis(),
                    sessionStartMillis > 0 ? System.currentTimeMillis() - sessionStartMillis : 0,
                    PeakSessionState.FINAL_HOLD,
                    lastSnapshot.getPublishedDbmCopy(),
                    lastSnapshot.getValidCopy(),
                    "FINAL_HOLD"
            );
            notifyListeners(this.lastSnapshot);
        }
    }

    /**
     * 接收每周波原始峰值（在采集线程同步调用）
     *
     * @param rawCyclePeaks 8 通道当前周波最大 ADC 原始值 (0~4095)
     * @param context       校准、增益、通道状态上下文
     */
    public void acceptCyclePeaks(int[] rawCyclePeaks, PeakMeasurementContext context) {
        if (rawCyclePeaks == null || context == null) {
            return;
        }

        PersistentPeakSnapshot snapshotToPublish = null;
        PersistentPeakCsvWriter writer = null;

        synchronized (stateLock) {
            if (sessionState == PeakSessionState.IDLE || sessionState == PeakSessionState.FINAL_HOLD) {
                return;
            }

            // 处理数据跳变 / 丢包（仅记录事件，不中断累加计算）
            if (context.isPacketGap()) {
                appendEvent("DATA_GAP");
            }

            // 检查通道配置变更（增益、校准）
            for (int i = 0; i < 8; i++) {
                int gain = context.getGainDbm(i);
                int calMin = context.getCalMinDbm();
                int calMax = context.getCalMaxDbm();
                boolean enabled = context.isEnabled(i);
                channels[i].enabled = enabled;

                if (!channels[i].configInitialized) {
                    channels[i].lastGain = gain;
                    channels[i].lastCalMin = calMin;
                    channels[i].lastCalMax = calMax;
                    channels[i].configInitialized = true;
                } else if (channels[i].lastGain != gain || channels[i].lastCalMin != calMin || channels[i].lastCalMax != calMax) {
                    // 配置改变，更新增益与校准，不重置已就绪状态和已收集样本，避免界面频繁打回 --
                    channels[i].lastGain = gain;
                    channels[i].lastCalMin = calMin;
                    channels[i].lastCalMax = calMax;
                    appendEvent("CONFIG_CHANGE:CH" + (i + 1));
                }

                // 累积该周波最大 ADC 值
                if (i < rawCyclePeaks.length) {
                    channels[i].currentGroupPeakRaw = Math.max(channels[i].currentGroupPeakRaw, rawCyclePeaks[i]);
                }
            }

            cyclesInCurrentGroup++;

            // 判断是否达到一组
            if (cyclesInCurrentGroup >= config.getGroupCycles()) {
                long now = context.getTimestampMillis();
                long elapsed = (sessionStartMillis > 0) ? (now - sessionStartMillis) : 0;
                sequence++;

                double[] rawGroupDbm = new double[8];
                double[] currentPublishedDbm = new double[8];
                boolean[] validArr = new boolean[8];

                boolean allReady = true;
                boolean anyInitializing = false;
                boolean anyWarmingUp = false;

                for (int i = 0; i < 8; i++) {
                    ChannelState cs = channels[i];
                    if (!cs.enabled) {
                        currentPublishedDbm[i] = Double.NaN;
                        validArr[i] = false;
                        cs.currentGroupPeakRaw = 0;
                        continue;
                    }

                    // 计算当前组的 dBm 值（严格限制在 -80.0 到 -20.0 之间）
                    double groupDbm = (double) (cs.currentGroupPeakRaw * (cs.lastCalMax - cs.lastCalMin)) / 4095.0D
                            + cs.lastCalMin + cs.lastGain;
                    groupDbm = Math.max(-80.0, Math.min(-20.0, groupDbm));
                    rawGroupDbm[i] = groupDbm;
                    cs.currentGroupPeakRaw = 0; // 重置下一组累加

                    // 1. 预热阶段
                    if (cs.warmupGroupsRemaining > 0) {
                        cs.warmupGroupsRemaining--;
                        anyWarmingUp = true;
                        allReady = false;
                        currentPublishedDbm[i] = Double.NaN;
                        validArr[i] = false;
                        continue;
                    }

                    // 2. 初始化阶段 (收集 initial.groups 组)
                    if (!cs.ready) {
                        cs.initialSamples.add(groupDbm);
                        if (cs.initialSamples.size() >= config.getInitialGroups()) {
                            // 排序并去极值平均
                            List<Double> sorted = new ArrayList<>(cs.initialSamples);
                            Collections.sort(sorted);
                            int trim = config.getTrimCount();
                            double sum = 0.0;
                            int count = 0;
                            if (sorted.size() > 2 * trim && trim > 0) {
                                for (int s = trim; s < sorted.size() - trim; s++) {
                                    sum += sorted.get(s);
                                    count++;
                                }
                            } else {
                                for (Double d : sorted) {
                                    sum += d;
                                    count++;
                                }
                            }
                            double initAvg = count > 0 ? (sum / count) : sorted.get(sorted.size() / 2);
                            cs.publishedInternalDbm = Math.max(-80.0, Math.min(-20.0, initAvg));
                            cs.lockedBaselineDbm = cs.publishedInternalDbm;
                            cs.isTracking = false;
                            cs.lastSignificantDbm = cs.publishedInternalDbm;
                            cs.ready = true;

                            // 初始化滑动窗口
                            cs.recentSamples.clear();
                            int startIdx = Math.max(0, cs.initialSamples.size() - config.getAverageGroups());
                            for (int k = startIdx; k < cs.initialSamples.size(); k++) {
                                cs.recentSamples.add(cs.initialSamples.get(k));
                            }

                            currentPublishedDbm[i] = applyDisplayJitter(cs.publishedInternalDbm, config.getDisplayJitterDb());
                            validArr[i] = true;
                        } else {
                            anyInitializing = true;
                            allReady = false;
                            // 初始化阶段最大值显示 --
                            currentPublishedDbm[i] = Double.NaN;
                            validArr[i] = false;
                        }
                    } else {
                        // 3. 正常运行阶段
                        double triggerDeadband = Math.max(0.05, config.getDisplayEpsilonDb());
                        if (Double.isNaN(cs.lockedBaselineDbm)) {
                            cs.lockedBaselineDbm = cs.publishedInternalDbm;
                        }

                        // 如果开启了中途切换信号强度先显示--，稳定后再显示数据功能
                        if (config.isSwitchShowDash()) {
                            double errorFromLocked = groupDbm - cs.lockedBaselineDbm;
                            boolean switchDetected = Math.abs(errorFromLocked) >= triggerDeadband;

                            if (switchDetected || cs.isSwitching) {
                                if (!cs.isSwitching) {
                                    // 首次触发切换判断，清空切换样本并进入切换状态
                                    cs.isSwitching = true;
                                    cs.switchSamples.clear();
                                } else if (!switchDetected) {
                                    // 正在切换中但当前组又回落到原锁定基准死区内，判定为单次孤立毛刺，取消切换并恢复原基准显示
                                    cs.isSwitching = false;
                                    cs.switchSamples.clear();
                                    currentPublishedDbm[i] = applyDisplayJitter(cs.publishedInternalDbm, config.getDisplayJitterDb());
                                    validArr[i] = true;
                                    continue;
                                }

                                cs.switchSamples.add(groupDbm);
                                int judgeCount = Math.max(1, config.getSwitchJudgeGroups());
                                if (cs.switchSamples.size() < judgeCount) {
                                    // 切换过渡与判断阶段：显示为 --
                                    currentPublishedDbm[i] = Double.NaN;
                                    validArr[i] = false;
                                    continue;
                                } else {
                                    // 已经收集满 switchJudgeGroups 组新信号，计算稳定值
                                    double sum = 0.0;
                                    for (Double sVal : cs.switchSamples) {
                                        sum += sVal;
                                    }
                                    double stableDbm = sum / cs.switchSamples.size();
                                    stableDbm = Math.max(-80.0, Math.min(-20.0, stableDbm));

                                    cs.publishedInternalDbm = stableDbm;
                                    cs.lockedBaselineDbm = stableDbm;
                                    cs.isTracking = false;
                                    cs.isSwitching = false;
                                    cs.switchSamples.clear();

                                    // 同步更新滑动窗口样本
                                    cs.recentSamples.clear();
                                    for (int k = 0; k < config.getAverageGroups(); k++) {
                                        cs.recentSamples.add(stableDbm);
                                    }

                                    // 显著变化标记
                                    if (Double.isNaN(cs.lastSignificantDbm)) {
                                        cs.lastSignificantDbm = cs.publishedInternalDbm;
                                    } else if (Math.abs(cs.publishedInternalDbm - cs.lastSignificantDbm) >= config.getSignificantChangeDb()) {
                                        String ev = (cs.publishedInternalDbm > cs.lastSignificantDbm)
                                                 ? "SIGNIFICANT_RISE:CH" + (i + 1)
                                                 : "SIGNIFICANT_FALL:CH" + (i + 1);
                                        appendEvent(ev);
                                        cs.lastSignificantDbm = cs.publishedInternalDbm;
                                    }

                                    currentPublishedDbm[i] = applyDisplayJitter(cs.publishedInternalDbm, config.getDisplayJitterDb());
                                    validArr[i] = true;
                                    continue;
                                }
                            }
                        }

                        // 未开启 switch.show.dash 时的常规平滑与步进收敛逻辑
                        cs.recentSamples.add(groupDbm);
                        int judgeWindow = Math.max(1, config.getSwitchJudgeGroups());
                        while (cs.recentSamples.size() > Math.max(judgeWindow, config.getAverageGroups())) {
                            cs.recentSamples.remove(0);
                        }

                        double targetDbm;
                        int avgCount = Math.max(judgeWindow, config.getAverageGroups());
                        if (cs.recentSamples.size() >= avgCount) {
                            List<Double> sorted = new ArrayList<>(cs.recentSamples);
                            Collections.sort(sorted);
                            int trim = (avgCount > 2) ? config.getTrimCount() : 0;
                            double sum = 0.0;
                            int count = 0;
                            for (int s = trim; s < sorted.size() - trim; s++) {
                                sum += sorted.get(s);
                                count++;
                            }
                            targetDbm = count > 0 ? (sum / count) : sorted.get(sorted.size() / 2);
                        } else {
                            targetDbm = groupDbm;
                        }
                        targetDbm = Math.max(-80.0, Math.min(-20.0, targetDbm));

                        // 滞环触发 + 全程平滑收敛跟踪逻辑（突破死区后忽略大死区阻断，完全过渡到新目标值）
                        double errorFromLocked = targetDbm - cs.lockedBaselineDbm;

                        if (!cs.isTracking) {
                            if (Math.abs(errorFromLocked) >= triggerDeadband) {
                                cs.isTracking = true; // 超过死区门限，启动全程平滑跟踪
                            }
                        }

                        if (cs.isTracking) {
                            double currentError = targetDbm - cs.publishedInternalDbm;
                            if (Math.abs(currentError) >= 0.05) {
                                double maxStep = currentError > 0 ? config.getMaxStepRiseDb() : config.getMaxStepFallDb();
                                double step;
                                if (maxStep > 0) {
                                    step = Math.min(Math.abs(currentError), maxStep);
                                } else {
                                    double alpha = currentError > 0 ? config.getAlphaRise() : config.getAlphaFall();
                                    step = Math.abs(alpha * currentError);
                                }
                                if (step >= Math.abs(currentError)) {
                                    // 已达到或超过目标值，直接收敛至目标值，锁定新基准
                                    cs.publishedInternalDbm = targetDbm;
                                    cs.lockedBaselineDbm = targetDbm;
                                    cs.isTracking = false;
                                } else {
                                    cs.publishedInternalDbm += Math.signum(currentError) * step;
                                }
                            } else {
                                // 已完全平滑收敛至目标值，锁定新基准
                                cs.publishedInternalDbm = targetDbm;
                                cs.lockedBaselineDbm = targetDbm;
                                cs.isTracking = false;
                            }
                            cs.publishedInternalDbm = Math.max(-80.0, Math.min(-20.0, cs.publishedInternalDbm));
                        }

                        // 显著变化标记
                        if (Double.isNaN(cs.lastSignificantDbm)) {
                            cs.lastSignificantDbm = cs.publishedInternalDbm;
                        } else if (Math.abs(cs.publishedInternalDbm - cs.lastSignificantDbm) >= config.getSignificantChangeDb()) {
                            String ev = (cs.publishedInternalDbm > cs.lastSignificantDbm)
                                     ? "SIGNIFICANT_RISE:CH" + (i + 1)
                                     : "SIGNIFICANT_FALL:CH" + (i + 1);
                            appendEvent(ev);
                            cs.lastSignificantDbm = cs.publishedInternalDbm;
                        }

                        currentPublishedDbm[i] = applyDisplayJitter(cs.publishedInternalDbm, config.getDisplayJitterDb());
                        validArr[i] = true;
                    }
                }

                // 综合会话状态判定
                if (allReady) {
                    sessionState = PeakSessionState.RUNNING;
                } else if (anyInitializing) {
                    sessionState = PeakSessionState.INITIALIZING;
                } else if (anyWarmingUp) {
                    sessionState = PeakSessionState.WARMING_UP;
                }

                String ev = pendingEvent;
                pendingEvent = "";

                // 生成单一不可变快照
                snapshotToPublish = new PersistentPeakSnapshot(
                        sequence, now, elapsed, sessionState, currentPublishedDbm, validArr, ev
                );
                this.lastSnapshot = snapshotToPublish;
                writer = this.csvWriter;
                cyclesInCurrentGroup = 0;
            }
        }

        // 在锁外部投递给 CSV 队列与通知 UI 监听器，避免阻塞 UDP 采集
        if (snapshotToPublish != null) {
            if (writer != null) {
                writer.offerSnapshot(snapshotToPublish);
            }
            notifyListeners(snapshotToPublish);
        }
    }

    private void appendEvent(String ev) {
        if (ev == null || ev.isEmpty()) return;
        if (pendingEvent.isEmpty()) {
            pendingEvent = ev;
        } else {
            pendingEvent = pendingEvent + ";" + ev;
        }
    }

    private void notifyListeners(PersistentPeakSnapshot snapshot) {
        for (SnapshotListener listener : listeners) {
            try {
                listener.onSnapshotPublished(snapshot);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error in snapshot listener", e);
            }
        }
    }

    public void resetChannel(int channel, String reason) {
        synchronized (stateLock) {
            if (channel >= 0 && channel < 8) {
                channels[channel].reset(config);
                appendEvent("RESET:CH" + (channel + 1) + (reason != null ? "(" + reason + ")" : ""));
            }
        }
    }

    public PeakSessionState getState() {
        synchronized (stateLock) {
            return sessionState;
        }
    }

    public PersistentPeakSnapshot getLastSnapshot() {
        synchronized (stateLock) {
            return lastSnapshot;
        }
    }

    public PersistentPeakSession getCurrentSession() {
        synchronized (stateLock) {
            return currentSession;
        }
    }

    public PersistentPeakConfig getConfig() {
        synchronized (stateLock) {
            return config;
        }
    }

    public void setConfig(PersistentPeakConfig config) {
        synchronized (stateLock) {
            if (config != null) {
                this.config = config;
            }
        }
    }

    private double applyDisplayJitter(double baseDbm, double jitterDb) {
        if (Double.isNaN(baseDbm)) {
            return Double.NaN;
        }
        // 严格限幅在 -80.0 到 -20.0 之间
        double clamped = Math.max(-80.0, Math.min(-20.0, baseDbm));
        // 如果本身的最大值平均值达到了 -80.0 或是 -20.0（精确到 0.1dB 达到边界），则不做微波动
        double roundedBase = Math.round(clamped * 10.0) / 10.0;
        if (roundedBase <= -80.0 || roundedBase >= -20.0 || jitterDb <= 0.0) {
            return roundedBase;
        }
        double rand = (java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0) * jitterDb;
        rand = Math.round(rand * 10.0) / 10.0;
        double val = clamped + rand;
        // 波动后的值同样严格限制在 [-80.0, -20.0] 范围内
        return Math.max(-80.0, Math.min(-20.0, val));
    }

    public void shutdown() {
        stopSession();
    }
}
