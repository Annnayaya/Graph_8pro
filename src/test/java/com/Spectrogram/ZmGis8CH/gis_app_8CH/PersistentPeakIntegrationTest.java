package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import junit.framework.TestCase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PersistentPeakIntegrationTest extends TestCase {

    private File tempDir;
    private PersistentPeakConfig config;
    private PersistentPeakService service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LicenseManager.setMockLicensed(true);
        tempDir = new File(System.getProperty("java.io.tmpdir"), "integration_test_" + System.currentTimeMillis());
        tempDir.mkdirs();
        config = new PersistentPeakConfig(
                10, // 10 cycles per group for fast test
                1,  // 1 warmup group
                5,  // 5 initial groups
                5,  // 5 average groups
                1,  // 1 trim count
                0.35, 0.15, 1.0, 1.0, 0.05, 3.0, 1000, 1024, 50
        );
        service = new PersistentPeakService(config);
    }

    @Override
    protected void tearDown() throws Exception {
        service.stopSession();
        if (tempDir != null && tempDir.exists()) {
            deleteDir(tempDir);
        }
        LicenseManager.setMockLicensed(null);
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

    private int dbmToRaw(double dbm) {
        double raw = ((dbm - (-80.0)) / 80.0) * 4095.0;
        return (int) Math.round(Math.max(0, Math.min(4095, raw)));
    }

    public void testEndToEndPipelineAndConsistency() throws Exception {
        PersistentPeakSession session = new PersistentPeakSession("integ_session", System.currentTimeMillis(), tempDir, config);
        service.startSession(session);

        PeakMeasurementContext context = new PeakMeasurementContext(
                -80, 0, new int[8], new boolean[]{true, true, true, true, true, true, true, true},
                50.0f, false, System.currentTimeMillis()
        );

        final List<PersistentPeakSnapshot> receivedSnapshots = new ArrayList<>();
        service.addListener(receivedSnapshots::add);

        int raw40 = dbmToRaw(-40.0);
        int raw38 = dbmToRaw(-38.0); // +2.0 dB step

        int[] cycleData = new int[8];
        for (int i = 0; i < 8; i++) {
            cycleData[i] = raw40;
        }

        // 1. 预热 (1 组 = 10 cycles) + 初始化 (5 组 = 50 cycles)
        for (int c = 0; c < 60; c++) {
            service.acceptCyclePeaks(cycleData, context);
        }

        PersistentPeakSnapshot initSnap = service.getLastSnapshot();
        assertNotNull(initSnap);
        assertEquals(PeakSessionState.RUNNING, initSnap.getSessionState());
        assertEquals(-40.0, initSnap.getPublishedDbm(0), 0.1);

        // 2. 持续输入 +2 dB 阶跃 (10 组 = 100 cycles)
        for (int i = 0; i < 8; i++) {
            cycleData[i] = raw38;
        }
        for (int c = 0; c < 100; c++) {
            service.acceptCyclePeaks(cycleData, context);
        }

        PersistentPeakSnapshot stepSnap = service.getLastSnapshot();
        assertTrue(stepSnap.getPublishedDbm(0) > -40.0);
        assertEquals(-38.0, stepSnap.getPublishedDbm(0), 0.2);

        // 3. 停止会话并等待 CSV 刷新
        service.stopSession();
        Thread.sleep(100);

        File csvFile = session.getCsvFile();
        assertTrue(csvFile.exists());
        assertTrue(csvFile.length() > 0);

        // 4. 读取 CSV 并比对数据一致性
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 移除 BOM
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        }

        // 验证表头
        assertTrue(lines.size() > 1);
        assertEquals("time,elapsed_seconds,UHF-1_dBm,UHF-2_dBm,AE-1_dBm,AE-2_dBm,HFCT-1_dBm,HFCT-2_dBm,Spare-1_dBm,Spare-2_dBm,event", lines.get(0));

        // 验证每一行的 8 通道数值与对应格式化一致
        for (int i = 1; i < lines.size(); i++) {
            String[] cols = lines.get(i).split(",", -1);
            assertTrue(cols.length >= 10);
            for (int ch = 0; ch < 8; ch++) {
                String valStr = cols[2 + ch];
                if (!valStr.isEmpty()) {
                    double val = Double.parseDouble(valStr);
                    // 确保是 1 位小数
                    assertEquals(valStr, PeakValueFormatter.formatForCsv(val));
                }
            }
        }
    }

    public void testPersistentPeakDisabledDisplaysRawMax() {
        GisGraph graph = new GisGraph(64, 80);
        graph.amxdBm = -42.0;

        // 1. 模拟开启了谱图跟随，但 persistent.peak.enabled = false 的场景 (即用户反馈的导致永远显示--的场景)
        AppGlobalConfig.setPersistentPeakEnabled(false);
        AppGlobalConfig.setSpectrogramPeakFollowFiltered(true);

        // 未就绪或未启动时，必须返回原始最大值 -42.0 dBm，绝不能卡死在 NaN 或显示 --
        assertEquals(-42.0, graph.getDisplayedPeakDbm(), 0.001);

        // 2. 当 persistent.peak.enabled = true 时
        AppGlobalConfig.setPersistentPeakEnabled(true);
        // 如果平滑引擎未就绪，同样回退到原始值 -42.0
        graph.clearPersistentPeak();
        assertEquals(-42.0, graph.getDisplayedPeakDbm(), 0.001);

        // 一旦平滑引擎产生有效快照，正常展示平滑值
        double[] vals = new double[]{-35.5, -40.0, -50.0, -60.0, -70.0, -75.0, -80.0, -80.0};
        boolean[] valids = new boolean[]{true, true, true, true, true, true, true, true};
        PersistentPeakSnapshot snap = new PersistentPeakSnapshot(1, System.currentTimeMillis(), 1000, PeakSessionState.RUNNING, vals, valids, "TEST");
        graph.applyPersistentPeak(snap, 0);

        assertEquals(-35.5, graph.getDisplayedPeakDbm(), 0.001);
    }
}
