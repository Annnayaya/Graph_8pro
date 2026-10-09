package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import junit.framework.TestCase;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PersistentPeakServiceTest extends TestCase {

    private PersistentPeakConfig config;
    private PersistentPeakService service;
    private File tempDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        config = new PersistentPeakConfig(
                10, // group.cycles = 10 (方便单元测试快速运行)
                1,  // warmup.groups = 1
                10, // initial.groups = 10
                5,  // average.groups = 5
                1,  // trim.count = 1
                0.35, 0.15, 1.0, 1.0, 0.05, 3.0, 1000, 1024, 50
        );
        service = new PersistentPeakService(config);
        tempDir = new File(System.getProperty("java.io.tmpdir"), "peak_test_" + System.currentTimeMillis());
        tempDir.mkdirs();
    }

    @Override
    protected void tearDown() throws Exception {
        service.stopSession();
        if (tempDir != null && tempDir.exists()) {
            deleteDir(tempDir);
        }
        super.tearDown();
    }

    private void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDir(f);
                else f.delete();
            }
        }
        dir.delete();
    }

    private void feedCycles(int channel, int rawValue, int cycleCount, PeakMeasurementContext context) {
        int[] raw = new int[8];
        for (int i = 0; i < 8; i++) {
            raw[i] = (i == channel) ? rawValue : 0;
        }
        for (int c = 0; c < cycleCount; c++) {
            service.acceptCyclePeaks(raw, context);
        }
    }

    // 将 dBm (-80 ~ 0) 转换为 raw ADC (0 ~ 4095), calMin=-80, calMax=0, gain=0
    private int dbmToRaw(double dbm) {
        double raw = ((dbm - (-80.0)) / 80.0) * 4095.0;
        return (int) Math.round(Math.max(0, Math.min(4095, raw)));
    }

    public void testConstantSignal() {
        PersistentPeakSession session = new PersistentPeakSession("test_session", System.currentTimeMillis(), tempDir, config);
        service.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        int raw40 = dbmToRaw(-40.0);

        // 预热 1 组 (10 cycles) + 初始化 10 组 (100 cycles)
        feedCycles(0, raw40, 110, context);

        PersistentPeakSnapshot snapshot = service.getLastSnapshot();
        assertNotNull(snapshot);
        assertTrue(snapshot.isValid(0));
        assertEquals(-40.0, snapshot.getPublishedDbm(0), 0.1);
        assertEquals(PeakSessionState.RUNNING, snapshot.getSessionState());

        // 继续喂 5 组，峰值保持稳定
        feedCycles(0, raw40, 50, context);
        snapshot = service.getLastSnapshot();
        assertEquals(-40.0, snapshot.getPublishedDbm(0), 0.1);
    }

    public void testInitializationTrimOutliers() {
        PersistentPeakSession session = new PersistentPeakSession("test_trim", System.currentTimeMillis(), tempDir, config);
        service.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        int raw40 = dbmToRaw(-40.0);
        int rawExtremeHigh = dbmToRaw(-10.0);
        int rawExtremeLow = dbmToRaw(-70.0);

        // 预热 1 组
        feedCycles(0, raw40, 10, context);

        // 初始化 10 组：8 组 -40dBm, 1 组 -10dBm (高), 1 组 -70dBm (低)
        feedCycles(0, rawExtremeHigh, 10, context);
        feedCycles(0, rawExtremeLow, 10, context);
        for (int g = 0; g < 8; g++) {
            feedCycles(0, raw40, 10, context);
        }

        PersistentPeakSnapshot snapshot = service.getLastSnapshot();
        assertNotNull(snapshot);
        assertTrue(snapshot.isValid(0));
        // 去掉最高和最低后，由 8 个 -40dBm 平均，结果依然为 -40.0 dBm
        assertEquals(-40.0, snapshot.getPublishedDbm(0), 0.1);
    }

    public void testSingleSpikeSuppression() {
        PersistentPeakSession session = new PersistentPeakSession("test_spike", System.currentTimeMillis(), tempDir, config);
        service.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        int raw40 = dbmToRaw(-40.0);
        int rawSpike = dbmToRaw(-20.0); // +20 dB 尖峰

        // 预热 + 初始化
        feedCycles(0, raw40, 110, context);
        double beforeSpike = service.getLastSnapshot().getPublishedDbm(0);

        // 插入单个周波/单组尖峰
        feedCycles(0, rawSpike, 10, context);

        double afterSpike = service.getLastSnapshot().getPublishedDbm(0);
        // 5 组去极值去掉了最高 1 个尖峰，发布值不应被抬高超过 0.1 dB
        assertEquals(beforeSpike, afterSpike, 0.1);
    }

    public void testContinuousRise1Db() {
        PersistentPeakSession session = new PersistentPeakSession("test_rise", System.currentTimeMillis(), tempDir, config);
        service.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        int raw40 = dbmToRaw(-40.0);
        int raw39 = dbmToRaw(-39.0); // 持续提升 1 dB

        // 预热 + 初始化
        feedCycles(0, raw40, 110, context);
        double initialPublished = service.getLastSnapshot().getPublishedDbm(0);

        // 持续输入 -39 dBm，多组观察缓慢爬升过程
        List<Double> progression = new ArrayList<>();
        progression.add(initialPublished);

        for (int g = 0; g < 15; g++) {
            feedCycles(0, raw39, 10, context);
            progression.add(service.getLastSnapshot().getPublishedDbm(0));
        }

        // 验证单调上升
        for (int i = 1; i < progression.size(); i++) {
            assertTrue(progression.get(i) >= progression.get(i - 1) - 0.001);
        }

        // 最终收敛至约 -39.0 dBm
        double finalPublished = progression.get(progression.size() - 1);
        assertEquals(-39.0, finalPublished, 0.15);
    }

    public void testSignalFallSlowerThanRise() {
        PersistentPeakSession session = new PersistentPeakSession("test_fall", System.currentTimeMillis(), tempDir, config);
        service.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        int raw40 = dbmToRaw(-40.0);
        int raw30 = dbmToRaw(-30.0); // +10 dB 阶跃

        // 初始化在 -40 dBm
        feedCycles(0, raw40, 110, context);

        // 上升 10 组
        for (int g = 0; g < 10; g++) {
            feedCycles(0, raw30, 10, context);
        }
        double highPeak = service.getLastSnapshot().getPublishedDbm(0);

        // 回落到 -40 dBm 5 组
        feedCycles(0, raw40, 50, context);
        double after5Fall = service.getLastSnapshot().getPublishedDbm(0);

        // 下降速率慢于上升速率（alphaFall=0.15 < alphaRise=0.35），保持在较高位置
        assertTrue(after5Fall > -40.0);
    }

    public void testChannelReset() {
        PersistentPeakSession session = new PersistentPeakSession("test_reset", System.currentTimeMillis(), tempDir, config);
        service.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        int raw40 = dbmToRaw(-40.0);
        feedCycles(0, raw40, 110, context);
        feedCycles(1, raw40, 110, context);

        assertTrue(service.getLastSnapshot().isValid(0));
        assertTrue(service.getLastSnapshot().isValid(1));

        // 重置通道 0
        service.resetChannel(0, "USER_TRIGGERED");

        feedCycles(1, raw40, 10, context);
        // 通道 0 应该处于重新初始化中（不可用），而通道 1 依然可用
        PersistentPeakSnapshot snap = service.getLastSnapshot();
        assertFalse(snap.isValid(0));
        assertTrue(snap.isValid(1));
    }

    public void testInitializationShowsDash() {
        PersistentPeakSession session = new PersistentPeakSession("test_init_dash", System.currentTimeMillis(), tempDir, config);
        service.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        int raw40 = dbmToRaw(-40.0);
        // 预热 1 组 + 初始化前 5 组 (未满 10 组)
        feedCycles(0, raw40, 60, context);

        PersistentPeakSnapshot snapshot = service.getLastSnapshot();
        assertNotNull(snapshot);
        assertEquals(PeakSessionState.INITIALIZING, snapshot.getSessionState());
        assertFalse(snapshot.isValid(0));
        assertTrue(Double.isNaN(snapshot.getPublishedDbm(0)));
    }

    public void testDeadbandTriggerWithFullConvergence() {
        // 配置 10 dB 大死区
        PersistentPeakConfig customCfg = new PersistentPeakConfig(
                10, 0, 5, 5, 0, 0.15, 0.15, 0.4, 0.4, 10.0, 3.0, 1000, 1024, 50
        );
        PersistentPeakService customService = new PersistentPeakService(customCfg);
        PersistentPeakSession session = new PersistentPeakSession("test_deadband_conv", System.currentTimeMillis(), tempDir, customCfg);
        customService.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        int raw56 = dbmToRaw(-56.8);
        int raw53 = dbmToRaw(-53.0); // 变化 3.8 dB < 10 dB
        int raw20 = dbmToRaw(-20.4); // 变化 36.4 dB > 10 dB

        // 1. 初始化 5 组 (-56.8 dBm)
        for (int i = 0; i < 50; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw56;
            customService.acceptCyclePeaks(cyc, context);
        }
        assertEquals(-56.8, customService.getLastSnapshot().getPublishedDbm(0), 0.1);

        // 2. 输入 -53.0 dBm（未突破 10dB 死区），观察 10 组后依然保持在 -56.8 dBm
        for (int i = 0; i < 100; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw53;
            customService.acceptCyclePeaks(cyc, context);
        }
        assertEquals(-56.8, customService.getLastSnapshot().getPublishedDbm(0), 0.1);

        // 3. 输入 -20.4 dBm（突破 10dB 死区），开始全程平滑爬升（需约 90 组爬升 36.4 dB）
        for (int i = 0; i < 1200; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw20;
            customService.acceptCyclePeaks(cyc, context);
        }
        // 验证已经完全收敛至 -20.4 dBm（而不会被 10dB 死区阻断停留在 -30 dBm）
        assertEquals(-20.4, customService.getLastSnapshot().getPublishedDbm(0), 0.1);
    }

    public void testDisplayJitterCalculations() {
        // 配置 2.0 dB 随机微抖动
        PersistentPeakConfig jitterCfg = new PersistentPeakConfig(
                10, 0, 5, 5, 0, 0.15, 0.15, 0.4, 0.4, 0.2, 2.0, 3.0, 1000, 1024, 50
        );
        PersistentPeakService jitterService = new PersistentPeakService(jitterCfg);
        PersistentPeakSession session = new PersistentPeakSession("test_jitter", System.currentTimeMillis(), tempDir, jitterCfg);
        jitterService.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        int raw50 = dbmToRaw(-50.0);
        // 初始化 5 组
        for (int i = 0; i < 50; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw50;
            jitterService.acceptCyclePeaks(cyc, context);
        }

        // 采集后续 20 组数据，检查发布值均在 -50 ± 2.0 dB (即 [-52.0, -48.0]) 范围内，且存在自然微波动
        double minSeen = Double.POSITIVE_INFINITY;
        double maxSeen = Double.NEGATIVE_INFINITY;
        for (int g = 0; g < 20; g++) {
            for (int i = 0; i < 10; i++) {
                int[] cyc = new int[8];
                cyc[0] = raw50;
                jitterService.acceptCyclePeaks(cyc, context);
            }
            double val = jitterService.getLastSnapshot().getPublishedDbm(0);
            assertTrue("Value " + val + " out of jitter range [-52.1, -47.9]", val >= -52.1 && val <= -47.9);
            minSeen = Math.min(minSeen, val);
            maxSeen = Math.max(maxSeen, val);
        }
        // 验证确实存在微波动，跨度大于 0.2 dB
        assertTrue("Expected display fluctuation across 20 groups", (maxSeen - minSeen) >= 0.2);
    }

    public void testBoundaryJitterSuppressionAtNegative80AndNegative20() {
        // 配置 2.0 dB 随机微抖动
        PersistentPeakConfig jitterCfg = new PersistentPeakConfig(
                10, 0, 5, 5, 0, 0.15, 0.15, 0.4, 0.4, 0.2, 2.0, 3.0, 1000, 1024, 50
        );
        PersistentPeakService jitterService = new PersistentPeakService(jitterCfg);
        PersistentPeakSession session = new PersistentPeakSession("test_boundary_jitter", System.currentTimeMillis(), tempDir, jitterCfg);
        jitterService.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        // 1. 测试 -80.0 dBm 边界：由于达到 -80.0 dBm，系统不产生微抖动，严格保持 -80.0
        int raw80 = dbmToRaw(-80.0);
        for (int i = 0; i < 50; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw80;
            jitterService.acceptCyclePeaks(cyc, context);
        }
        for (int g = 0; g < 20; g++) {
            for (int i = 0; i < 10; i++) {
                int[] cyc = new int[8];
                cyc[0] = raw80;
                jitterService.acceptCyclePeaks(cyc, context);
            }
            double val = jitterService.getLastSnapshot().getPublishedDbm(0);
            assertEquals(-80.0, val, 0.0001);
        }
        jitterService.stopSession();

        // 2. 测试 -20.0 dBm 边界：由于达到 -20.0 dBm，系统不产生微抖动，严格保持 -20.0
        PersistentPeakSession session2 = new PersistentPeakSession("test_boundary_jitter_20", System.currentTimeMillis(), tempDir, jitterCfg);
        jitterService.startSession(session2);
        int raw20 = dbmToRaw(-20.0);
        for (int i = 0; i < 50; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw20;
            jitterService.acceptCyclePeaks(cyc, context);
        }
        for (int g = 0; g < 20; g++) {
            for (int i = 0; i < 10; i++) {
                int[] cyc = new int[8];
                cyc[0] = raw20;
                jitterService.acceptCyclePeaks(cyc, context);
            }
            double val = jitterService.getLastSnapshot().getPublishedDbm(0);
            assertEquals(-20.0, val, 0.0001);
        }
    }

    public void testRapidStepConvergenceWithConfiguredMaxStep() {
        // 配置每次可以变动的幅值上限 maxStep = 10.0 dB, averageGroups = 1 (使新均值立即稳定，检验 10dBm 步进变动)
        PersistentPeakConfig step10Cfg = new PersistentPeakConfig(
                10, 0, 5, 1, 0, 0.15, 0.15, 10.0, 10.0, 0.2, 0.0, 1, 3.0, 1000, 1024, 50
        );
        PersistentPeakService stepService = new PersistentPeakService(step10Cfg);
        PersistentPeakSession session = new PersistentPeakSession("test_step10", System.currentTimeMillis(), tempDir, step10Cfg);
        stepService.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        int raw65 = dbmToRaw(-65.0);
        int raw30 = dbmToRaw(-30.0);

        // 1. 初始化 5 组 (-65.0 dBm)
        for (int i = 0; i < 50; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw65;
            stepService.acceptCyclePeaks(cyc, context);
        }
        assertEquals(-65.0, stepService.getLastSnapshot().getPublishedDbm(0), 0.1);

        // 2. 切换输入为 -30.0 dBm。由于配置了 maxStep=10，平均值稳定后以 10dBm 为上限快速步进变动
        List<Double> progression = new ArrayList<>();
        progression.add(stepService.getLastSnapshot().getPublishedDbm(0));
        for (int g = 0; g < 6; g++) {
            for (int i = 0; i < 10; i++) {
                int[] cyc = new int[8];
                cyc[0] = raw30;
                stepService.acceptCyclePeaks(cyc, context);
            }
            progression.add(stepService.getLastSnapshot().getPublishedDbm(0));
        }

        // 最终精准收敛至 -30.0 dBm
        assertEquals(-30.0, stepService.getLastSnapshot().getPublishedDbm(0), 0.1);

        // 验证变动序列：精确以 10dBm 为单位快速上升 (-65 -> -55 -> -45 -> -35 -> -30)
        assertEquals(-65.0, progression.get(0), 0.1);
        assertEquals(-55.0, progression.get(1), 0.1);
        assertEquals(-45.0, progression.get(2), 0.1);
        assertEquals(-35.0, progression.get(3), 0.1);
        assertEquals(-30.0, progression.get(4), 0.1);
        assertEquals(-30.0, progression.get(5), 0.1);
    }

    public void testSwitchShowDashOnSignalStep() {
        // 配置 switch.show.dash = true, switch.judge.groups = 3, initial.groups = 3
        PersistentPeakConfig dashConfig = new PersistentPeakConfig(
                10, 0, 3, 3, 0, 0.15, 0.15, 10.0, 10.0,
                0.20, 0.0, 1, 3.0, 1000, 1024, 50,
                3, true
        );
        PersistentPeakService dashService = new PersistentPeakService(dashConfig);
        PersistentPeakSession session = new PersistentPeakSession("test_switch_dash", System.currentTimeMillis(), tempDir, dashConfig);
        dashService.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        int raw65 = dbmToRaw(-65.0);
        int raw30 = dbmToRaw(-30.0);

        // 1. 初始化 3 组 (-65.0 dBm)
        for (int i = 0; i < 30; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw65;
            dashService.acceptCyclePeaks(cyc, context);
        }
        assertTrue(dashService.getLastSnapshot().isValid(0));
        assertEquals(-65.0, dashService.getLastSnapshot().getPublishedDbm(0), 0.1);

        // 2. 切换输入为 -30.0 dBm
        // 组 1：处于切换过渡中，应显示为 -- (isValid == false, publishedDbm == NaN)
        for (int i = 0; i < 10; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw30;
            dashService.acceptCyclePeaks(cyc, context);
        }
        assertFalse(dashService.getLastSnapshot().isValid(0));
        assertTrue(Double.isNaN(dashService.getLastSnapshot().getPublishedDbm(0)));

        // 组 2：继续处于切换过渡中，依然显示为 --
        for (int i = 0; i < 10; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw30;
            dashService.acceptCyclePeaks(cyc, context);
        }
        assertFalse(dashService.getLastSnapshot().isValid(0));
        assertTrue(Double.isNaN(dashService.getLastSnapshot().getPublishedDbm(0)));

        // 组 3：收集满 3 组判断样本，判定为稳定新信号，恢复显示为 -30.0 dBm
        for (int i = 0; i < 10; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw30;
            dashService.acceptCyclePeaks(cyc, context);
        }
        assertTrue(dashService.getLastSnapshot().isValid(0));
        assertEquals(-30.0, dashService.getLastSnapshot().getPublishedDbm(0), 0.1);

        // 3. 验证单次偶发毛刺能够被快速过滤并恢复原显示
        // 组 4 突发干扰 -55.0 dBm
        int raw55 = dbmToRaw(-55.0);
        for (int i = 0; i < 10; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw55;
            dashService.acceptCyclePeaks(cyc, context);
        }
        assertFalse(dashService.getLastSnapshot().isValid(0));

        // 组 5 干扰消失，信号恢复至 -30.0 dBm：应立即恢复原基准 -30.0 dBm 显示
        for (int i = 0; i < 10; i++) {
            int[] cyc = new int[8];
            cyc[0] = raw30;
            dashService.acceptCyclePeaks(cyc, context);
        }
        assertTrue(dashService.getLastSnapshot().isValid(0));
        assertEquals(-30.0, dashService.getLastSnapshot().getPublishedDbm(0), 0.1);
    }
}
