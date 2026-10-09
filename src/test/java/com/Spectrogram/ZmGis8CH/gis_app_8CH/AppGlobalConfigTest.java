package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import junit.framework.TestCase;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class AppGlobalConfigTest extends TestCase {
    private String originalConfigContent = null;
    private File cfg = AppGlobalConfig.getConfigFile();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LicenseManager.setMockLicensed(true);
        if (cfg.exists()) {
            originalConfigContent = new String(Files.readAllBytes(cfg.toPath()), StandardCharsets.UTF_8);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (originalConfigContent != null) {
            AppGlobalConfig.unhideFile(cfg);
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(cfg), StandardCharsets.UTF_8)) {
                writer.write(originalConfigContent);
            }
        }
        AppGlobalConfig.loadConfig();
        AppGlobalConfig.setDisplayDecimals(0);
        AppGlobalConfig.setAppVersion(AppGlobalConfig.DEFAULT_APP_VERSION);
        AppGlobalConfig.setDisplayMaxStepDb(AppGlobalConfig.DEFAULT_DISPLAY_MAX_STEP_DB);
        AppGlobalConfig.setInitialGroups(AppGlobalConfig.DEFAULT_INITIAL_GROUPS);
        AppGlobalConfig.setSwitchJudgeGroups(AppGlobalConfig.DEFAULT_SWITCH_JUDGE_GROUPS);
        AppGlobalConfig.setSwitchShowDash(AppGlobalConfig.DEFAULT_SWITCH_SHOW_DASH);
        AppGlobalConfig.setSpectrogramRollingEnabled(AppGlobalConfig.DEFAULT_SPECTROGRAM_ROLLING_ENABLED);
        AppGlobalConfig.setAcquisitionSpeedDelayMs(AppGlobalConfig.DEFAULT_ACQUISITION_SPEED_DELAY_MS);
        AppGlobalConfig.setAcquisitionSpeedMode(AppGlobalConfig.DEFAULT_ACQUISITION_SPEED_MODE);
        AppGlobalConfig.setIpSwitchMode(AppGlobalConfig.DEFAULT_IP_SWITCH_MODE);
        AppGlobalConfig.setPresetIp1(AppGlobalConfig.DEFAULT_PRESET_IP_1);
        AppGlobalConfig.setPresetName1(AppGlobalConfig.DEFAULT_PRESET_NAME_1);
        AppGlobalConfig.setPresetIp2(AppGlobalConfig.DEFAULT_PRESET_IP_2);
        AppGlobalConfig.setPresetName2(AppGlobalConfig.DEFAULT_PRESET_NAME_2);
        AppGlobalConfig.setPresetIp3(AppGlobalConfig.DEFAULT_PRESET_IP_3);
        AppGlobalConfig.setPresetName3(AppGlobalConfig.DEFAULT_PRESET_NAME_3);
        AppGlobalConfig.setAlarmEnabled(AppGlobalConfig.DEFAULT_ALARM_ENABLED);
        AppGlobalConfig.setAlarmThresholdGlobal(AppGlobalConfig.DEFAULT_ALARM_THRESHOLD_GLOBAL);
        for (int i = 0; i < 8; i++) {
            AppGlobalConfig.setAlarmThresholdChannel(i, AppGlobalConfig.DEFAULT_ALARM_THRESHOLD_GLOBAL);
        }
        LicenseManager.setMockLicensed(null);
        super.tearDown();
    }

    public void testDefaultConfig() {
        if (cfg.exists()) {
            AppGlobalConfig.unhideFile(cfg);
            cfg.delete();
        }
        AppGlobalConfig.loadConfig();
        // 默认写入 reportdata 功能关闭
        assertFalse(AppGlobalConfig.isReportDataEnabled());
        // 默认持久测试功能关闭
        assertFalse(AppGlobalConfig.isPersistentPeakEnabled());
        // 默认谱图跟随配置为开启，但由于平滑总开关关闭，运行时实际生效为 false
        assertTrue(AppGlobalConfig.isSpectrogramPeakFollowFilteredConfig());
        assertFalse(AppGlobalConfig.isSpectrogramPeakFollowFiltered());
        // 开启平滑总开关后，跟随平滑滤波生效为 true
        AppGlobalConfig.setPersistentPeakEnabled(true);
        assertTrue(AppGlobalConfig.isSpectrogramPeakFollowFiltered());
        AppGlobalConfig.setPersistentPeakEnabled(false);
        // 默认死区门限 0.2 dB
        assertEquals(0.20, AppGlobalConfig.getDisplayEpsilonDb(), 0.0001);
        // 默认抖动 0.0 dB
        assertEquals(0.0, AppGlobalConfig.getDisplayJitterDb(), 0.0001);
        // 默认小数位数 0 位（默认关闭）
        assertEquals(0, AppGlobalConfig.getDisplayDecimals());
        // 默认版本号 Pro v2.2.8
        assertEquals("Pro v2.2.8", AppGlobalConfig.getAppVersion());
        // 默认变动幅值上限 0.3 dB
        assertEquals(0.3, AppGlobalConfig.getDisplayMaxStepDb(), 0.0001);
        // 默认初始建立组数 3 组
        assertEquals(3, AppGlobalConfig.getInitialGroups());
        // 默认切换判断组数 3 组
        assertEquals(3, AppGlobalConfig.getSwitchJudgeGroups());
        // 默认切换显示--关闭
        assertFalse(AppGlobalConfig.isSwitchShowDash());
        // 默认设备 IP
        assertEquals("192.168.10.60", AppGlobalConfig.getDeviceIp());
        // 默认刷新机制为整组模式 (false)
        assertFalse(AppGlobalConfig.isSpectrogramRollingEnabled());
    }

    public void testCustomConfigFile() throws Exception {
        AppGlobalConfig.unhideFile(cfg);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(cfg), StandardCharsets.UTF_8)) {
            writer.write("reportdata.enabled=true\n");
            writer.write("persistent.peak.enabled=false\n");
            writer.write("spectrogram.peak.follow.filtered=false\n");
            writer.write("display.epsilon.db=0.35\n");
            writer.write("display.jitter.db=2.0\n");
            writer.write("display.decimals=0\n");
            writer.write("app.version=Custom v3.0.0\n");
            writer.write("display.max.step.db=10.0\n");
            writer.write("initial.groups=5\n");
            writer.write("switch.judge.groups=4\n");
            writer.write("switch.show.dash=true\n");
            writer.write("default.ip=192.168.10.88\n");
            writer.write("spectrogram.rolling.enabled=false\n");
        }

        AppGlobalConfig.loadConfig();
        assertTrue(AppGlobalConfig.isReportDataEnabled());
        assertFalse(AppGlobalConfig.isPersistentPeakEnabled());
        assertFalse(AppGlobalConfig.isPersistentPeakUiEnabled());
        assertFalse(AppGlobalConfig.isSpectrogramPeakFollowFiltered());
        assertEquals(0.35, AppGlobalConfig.getDisplayEpsilonDb(), 0.0001);
        assertEquals(2.0, AppGlobalConfig.getDisplayJitterDb(), 0.0001);
        assertEquals(0, AppGlobalConfig.getDisplayDecimals());
        assertEquals("Custom v3.0.0", AppGlobalConfig.getAppVersion());
        assertEquals(10.0, AppGlobalConfig.getDisplayMaxStepDb(), 0.0001);
        assertEquals(5, AppGlobalConfig.getInitialGroups());
        assertEquals(4, AppGlobalConfig.getSwitchJudgeGroups());
        assertTrue(AppGlobalConfig.isSwitchShowDash());
        assertEquals("192.168.10.88", AppGlobalConfig.getDeviceIp());
        assertFalse(AppGlobalConfig.isSpectrogramRollingEnabled());
    }

    public void testEncryptionAndDecryption() throws Exception {
        String plain = "app.version=TestEnc\ndisplay.decimals=2\ninitial.groups=7";
        String cipher = AppGlobalConfig.encryptString(plain);
        assertTrue(cipher.contains("ENC:"));
        assertFalse(cipher.contains("TestEnc"));

        String decrypted = AppGlobalConfig.decryptString(cipher);
        assertEquals(plain, decrypted);

        // 测试加密保存
        AppGlobalConfig.saveEncryptedConfigString(cfg, plain);
        byte[] bytes = Files.readAllBytes(cfg.toPath());
        String onDisk = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(onDisk.contains("ENC:"));

        // loadConfig 能够正确解密加载
        AppGlobalConfig.loadConfig();
        assertEquals("TestEnc", AppGlobalConfig.getAppVersion());
        assertEquals(2, AppGlobalConfig.getDisplayDecimals());
        assertEquals(7, AppGlobalConfig.getInitialGroups());
    }

    public void testSpeedAndIpSwitchAndAlarmConfig() throws Exception {
        if (cfg.exists()) {
            AppGlobalConfig.unhideFile(cfg);
            cfg.delete();
        }
        AppGlobalConfig.loadConfig();

        // 验证新配置默认值
        assertEquals(AppGlobalConfig.DEFAULT_ACQUISITION_SPEED_DELAY_MS, AppGlobalConfig.getAcquisitionSpeedDelayMs());
        assertEquals(AppGlobalConfig.DEFAULT_ACQUISITION_SPEED_MODE, AppGlobalConfig.getAcquisitionSpeedMode());
        assertEquals(AppGlobalConfig.DEFAULT_IP_SWITCH_MODE, AppGlobalConfig.getIpSwitchMode());
        assertEquals(AppGlobalConfig.DEFAULT_PRESET_IP_1, AppGlobalConfig.getPresetIp1());
        assertEquals(AppGlobalConfig.DEFAULT_PRESET_NAME_1, AppGlobalConfig.getPresetName1());
        assertEquals(AppGlobalConfig.DEFAULT_PRESET_IP_2, AppGlobalConfig.getPresetIp2());
        assertEquals(AppGlobalConfig.DEFAULT_PRESET_NAME_2, AppGlobalConfig.getPresetName2());
        assertEquals(AppGlobalConfig.DEFAULT_PRESET_IP_3, AppGlobalConfig.getPresetIp3());
        assertEquals(AppGlobalConfig.DEFAULT_PRESET_NAME_3, AppGlobalConfig.getPresetName3());
        assertEquals(AppGlobalConfig.DEFAULT_ALARM_ENABLED, AppGlobalConfig.isAlarmEnabled());
        assertEquals(AppGlobalConfig.DEFAULT_ALARM_THRESHOLD_GLOBAL, AppGlobalConfig.getAlarmThresholdGlobal(), 0.001);

        // 修改配置并保存
        AppGlobalConfig.setSpeedPreset("MEDIUM"); // 10Hz, 80ms
        AppGlobalConfig.setIpSwitchMode(1);
        AppGlobalConfig.setPresetIp1("192.168.1.11");
        AppGlobalConfig.setPresetName1("主变1");
        AppGlobalConfig.setPresetIp2("192.168.1.22");
        AppGlobalConfig.setPresetName2("主变2");
        AppGlobalConfig.setPresetIp3("192.168.1.33");
        AppGlobalConfig.setPresetName3("GIS室");
        AppGlobalConfig.setAlarmEnabled(false);
        AppGlobalConfig.setAlarmThresholdGlobal(16.0);
        AppGlobalConfig.setAlarmThresholdChannel(2, 20.0);

        AppGlobalConfig.saveCurrentConfig();

        // 重新加载并验证
        AppGlobalConfig.loadConfig();
        assertEquals(80, AppGlobalConfig.getAcquisitionSpeedDelayMs());
        assertEquals("MEDIUM", AppGlobalConfig.getAcquisitionSpeedMode());
        assertEquals(1, AppGlobalConfig.getIpSwitchMode());
        assertEquals("192.168.1.11", AppGlobalConfig.getPresetIp1());
        assertEquals("主变1", AppGlobalConfig.getPresetName1());
        assertEquals("192.168.1.22", AppGlobalConfig.getPresetIp2());
        assertEquals("主变2", AppGlobalConfig.getPresetName2());
        assertEquals("192.168.1.33", AppGlobalConfig.getPresetIp3());
        assertEquals("GIS室", AppGlobalConfig.getPresetName3());
        assertFalse(AppGlobalConfig.isAlarmEnabled());
        assertEquals(16.0, AppGlobalConfig.getAlarmThresholdGlobal(), 0.001);
        assertEquals(20.0, AppGlobalConfig.getAlarmThresholdChannel(2), 0.001);
        assertEquals(16.0, AppGlobalConfig.getAlarmThresholdChannel(0), 0.001);
    }

    public void testUnlicensedGating() {
        LicenseManager.setMockLicensed(false);
        try {
            // 未授权状态下，版本号自动去除 Pro
            AppGlobalConfig.setAppVersion("Pro v2.2.8");
            assertEquals("v2.2.8", AppGlobalConfig.getAppVersion());
            assertEquals("Pro v2.2.8", AppGlobalConfig.getRawAppVersion());

            // 无论配置中如何开启，高级功能强制返回 false
            AppGlobalConfig.setPersistentPeakEnabled(true);
            assertFalse(AppGlobalConfig.isPersistentPeakEnabled());
            assertFalse(AppGlobalConfig.isSpectrogramPeakFollowFiltered());

            AppGlobalConfig.setSpectrogramRollingEnabled(true);
            assertFalse(AppGlobalConfig.isSpectrogramRollingEnabled());

            AppGlobalConfig.setAlarmEnabled(true);
            assertFalse(AppGlobalConfig.isAlarmEnabled());

            // 预设模式锁死为 0（无预设，仅直接输入），即使配置为 1 或 2
            AppGlobalConfig.setIpSwitchMode(2);
            assertEquals(0, AppGlobalConfig.getIpSwitchMode());
            assertEquals(2, AppGlobalConfig.getRawIpSwitchMode());

            // 采集速度锁死为默认极速（0ms delay / "MAX"），即使配置为慢速
            AppGlobalConfig.setSpeedPreset("SLOW");
            assertEquals(0, AppGlobalConfig.getAcquisitionSpeedDelayMs());
            assertEquals(AppGlobalConfig.DEFAULT_ACQUISITION_SPEED_MODE, AppGlobalConfig.getAcquisitionSpeedMode());
            assertEquals(480, AppGlobalConfig.getRawAcquisitionSpeedDelayMs());
            assertEquals("SLOW", AppGlobalConfig.getRawAcquisitionSpeedMode());
        } finally {
            LicenseManager.setMockLicensed(true);
        }
    }
}
