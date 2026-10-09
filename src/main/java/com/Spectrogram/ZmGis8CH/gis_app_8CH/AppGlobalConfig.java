package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * lib/.config 软件全局功能配置管理器（AES 加密存储并自动隐藏）
 */
public final class AppGlobalConfig {
    private static final Logger LOGGER = Logger.getLogger(AppGlobalConfig.class.getName());

    public static final String CONFIG_DIR_NAME = "lib";
    public static final String CONFIG_FILE_NAME = ".config";

    public static final boolean DEFAULT_REPORT_DATA_ENABLED = false;
    public static final boolean DEFAULT_PERSISTENT_PEAK_ENABLED = false;
    public static final boolean DEFAULT_SPECTROGRAM_PEAK_FOLLOW_FILTERED = true;
    public static final double DEFAULT_DISPLAY_EPSILON_DB = 0.20;
    public static final double DEFAULT_DISPLAY_JITTER_DB = 0.0;
    public static final int DEFAULT_DISPLAY_DECIMALS = 0;
    public static final String DEFAULT_APP_VERSION = "Pro v2.2.8";
    public static final double DEFAULT_DISPLAY_MAX_STEP_DB = 0.3;
    public static final int DEFAULT_INITIAL_GROUPS = 3;
    public static final int DEFAULT_SWITCH_JUDGE_GROUPS = 3;
    public static final boolean DEFAULT_SWITCH_SHOW_DASH = false;
    public static final String DEFAULT_DEVICE_IP = "192.168.10.60";
    public static final boolean DEFAULT_SPECTROGRAM_ROLLING_ENABLED = false;

    // 采集速度配置默认值 (延时毫秒数, 0 表示 50Hz 全速实时)
    public static final int DEFAULT_ACQUISITION_SPEED_DELAY_MS = 0;
    public static final String DEFAULT_ACQUISITION_SPEED_MODE = "MAX"; // MAX(50Hz), FAST(25Hz), MEDIUM(10Hz), SLOW(2Hz), CUSTOM

    // IP 切换模式默认值 (0: 直接输入, 1: 3个预设按钮, 2: 并行并存模式)
    public static final int DEFAULT_IP_SWITCH_MODE = 2;
    public static final String DEFAULT_PRESET_IP_1 = "192.168.10.60";
    public static final String DEFAULT_PRESET_NAME_1 = "预设1";
    public static final String DEFAULT_PRESET_IP_2 = "192.168.10.61";
    public static final String DEFAULT_PRESET_NAME_2 = "预设2";
    public static final String DEFAULT_PRESET_IP_3 = "192.168.1.100";
    public static final String DEFAULT_PRESET_NAME_3 = "预设3";

    // 幅值报警默认值
    public static final boolean DEFAULT_ALARM_ENABLED = true;
    public static final double DEFAULT_ALARM_THRESHOLD_GLOBAL = -40.0;

    // AES-128 加密参数
    private static final String ENC_PREFIX = "#-- GRAPH8PRO ENCRYPTED CONFIG --#\nENC:";
    private static final byte[] AES_KEY = new byte[]{
            0x47, 0x72, 0x61, 0x70, 0x68, 0x38, 0x50, 0x72, 0x6f, 0x23, 0x53, 0x65, 0x63, 0x32, 0x31, 0x37
    }; // "Graph8Pro#Sec217"
    private static final byte[] AES_IV = new byte[]{
            0x47, 0x69, 0x73, 0x43, 0x6f, 0x6e, 0x66, 0x69, 0x67, 0x49, 0x76, 0x40, 0x32, 0x30, 0x32, 0x36
    }; // "GisConfigIv@2026"

    private static volatile boolean reportDataEnabled = DEFAULT_REPORT_DATA_ENABLED;
    private static volatile boolean persistentPeakEnabled = DEFAULT_PERSISTENT_PEAK_ENABLED;
    private static volatile boolean spectrogramPeakFollowFiltered = DEFAULT_SPECTROGRAM_PEAK_FOLLOW_FILTERED;
    private static volatile double displayEpsilonDb = DEFAULT_DISPLAY_EPSILON_DB;
    private static volatile double displayJitterDb = DEFAULT_DISPLAY_JITTER_DB;
    private static volatile int displayDecimals = DEFAULT_DISPLAY_DECIMALS;
    private static volatile String appVersion = DEFAULT_APP_VERSION;
    private static volatile double displayMaxStepDb = DEFAULT_DISPLAY_MAX_STEP_DB;
    private static volatile int initialGroups = DEFAULT_INITIAL_GROUPS;
    private static volatile int switchJudgeGroups = DEFAULT_SWITCH_JUDGE_GROUPS;
    private static volatile boolean switchShowDash = DEFAULT_SWITCH_SHOW_DASH;
    private static volatile String deviceIp = DEFAULT_DEVICE_IP;
    private static volatile boolean spectrogramRollingEnabled = DEFAULT_SPECTROGRAM_ROLLING_ENABLED;

    private static volatile int acquisitionSpeedDelayMs = DEFAULT_ACQUISITION_SPEED_DELAY_MS;
    private static volatile String acquisitionSpeedMode = DEFAULT_ACQUISITION_SPEED_MODE;

    private static volatile int ipSwitchMode = DEFAULT_IP_SWITCH_MODE;
    private static volatile String presetIp1 = DEFAULT_PRESET_IP_1;
    private static volatile String presetName1 = DEFAULT_PRESET_NAME_1;
    private static volatile String presetIp2 = DEFAULT_PRESET_IP_2;
    private static volatile String presetName2 = DEFAULT_PRESET_NAME_2;
    private static volatile String presetIp3 = DEFAULT_PRESET_IP_3;
    private static volatile String presetName3 = DEFAULT_PRESET_NAME_3;

    private static volatile boolean alarmEnabled = DEFAULT_ALARM_ENABLED;
    private static volatile double alarmThresholdGlobal = DEFAULT_ALARM_THRESHOLD_GLOBAL;
    private static final double[] alarmThresholdChannels = new double[8];
    static {
        for (int i = 0; i < 8; i++) {
            alarmThresholdChannels[i] = DEFAULT_ALARM_THRESHOLD_GLOBAL;
        }
    }

    static {
        loadConfig();
    }

    private AppGlobalConfig() {
    }

    /**
     * 获取当前配置文件路径（优先定位 lib/.config，兼顾根目录旧文件迁移）
     */
    public static File getConfigFile() {
        File libDir = new File(CONFIG_DIR_NAME);
        if (!libDir.exists()) {
            libDir.mkdirs();
        }
        File libCfg = new File(libDir, CONFIG_FILE_NAME);
        if (libCfg.exists()) {
            return libCfg;
        }
        // 兼容检查根目录旧 .config
        File rootCfg = new File(CONFIG_FILE_NAME);
        if (rootCfg.exists() && rootCfg.isFile()) {
            try {
                byte[] bytes = Files.readAllBytes(rootCfg.toPath());
                String raw = new String(bytes, StandardCharsets.UTF_8);
                String plain = decryptString(raw);
                saveEncryptedConfigString(libCfg, plain);
                hideFile(libCfg);
                rootCfg.delete(); // 彻底清理根目录旧明文文件，只保留 lib/.config
                return libCfg;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to migrate root .config to lib/.config", e);
                return rootCfg;
            }
        }
        return libCfg;
    }

    /**
     * 在 Windows 系统下设置文件隐藏属性
     */
    public static void hideFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                Files.setAttribute(file.toPath(), "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 清除文件的隐藏属性（避免 Windows 下覆盖写入隐藏文件报拒绝访问）
     */
    public static void unhideFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                Files.setAttribute(file.toPath(), "dos:hidden", Boolean.FALSE, LinkOption.NOFOLLOW_LINKS);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 使用 AES-128 加密配置字符串
     */
    public static String encryptString(String plainText) throws Exception {
        if (plainText == null) {
            return "";
        }
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(AES_IV);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return ENC_PREFIX + Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 解密配置字符串（如果不是加密格式则原样返回，兼容明文）
     */
    public static String decryptString(String cipherText) throws Exception {
        if (cipherText == null) {
            return "";
        }
        String trimmed = cipherText.trim();
        if (!trimmed.contains("ENC:")) {
            return cipherText;
        }
        int idx = trimmed.indexOf("ENC:");
        String b64 = trimmed.substring(idx + 4).trim();
        byte[] cipherBytes = Base64.getDecoder().decode(b64);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(AES_IV);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(cipherBytes);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 将明文字符串加密保存至目标文件并设置隐藏属性
     */
    public static void saveEncryptedConfigString(File target, String plainText) throws Exception {
        if (target.getParentFile() != null && !target.getParentFile().exists()) {
            target.getParentFile().mkdirs();
        }
        if (target.exists()) {
            unhideFile(target);
        }
        String encrypted = encryptString(plainText);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
            writer.write(encrypted);
            writer.flush();
        }
        hideFile(target);
    }

    /**
     * 加载全局配置
     */
    public static synchronized void loadConfig() {
        File file = getConfigFile();
        if (!file.exists()) {
            reportDataEnabled = DEFAULT_REPORT_DATA_ENABLED;
            persistentPeakEnabled = DEFAULT_PERSISTENT_PEAK_ENABLED;
            spectrogramPeakFollowFiltered = DEFAULT_SPECTROGRAM_PEAK_FOLLOW_FILTERED;
            displayEpsilonDb = DEFAULT_DISPLAY_EPSILON_DB;
            displayJitterDb = DEFAULT_DISPLAY_JITTER_DB;
            displayDecimals = DEFAULT_DISPLAY_DECIMALS;
            appVersion = DEFAULT_APP_VERSION;
            displayMaxStepDb = DEFAULT_DISPLAY_MAX_STEP_DB;
            initialGroups = DEFAULT_INITIAL_GROUPS;
            switchJudgeGroups = DEFAULT_SWITCH_JUDGE_GROUPS;
            switchShowDash = DEFAULT_SWITCH_SHOW_DASH;
            deviceIp = DEFAULT_DEVICE_IP;
            spectrogramRollingEnabled = DEFAULT_SPECTROGRAM_ROLLING_ENABLED;
            acquisitionSpeedDelayMs = DEFAULT_ACQUISITION_SPEED_DELAY_MS;
            acquisitionSpeedMode = DEFAULT_ACQUISITION_SPEED_MODE;
            ipSwitchMode = DEFAULT_IP_SWITCH_MODE;
            presetIp1 = DEFAULT_PRESET_IP_1;
            presetIp2 = DEFAULT_PRESET_IP_2;
            presetIp3 = DEFAULT_PRESET_IP_3;
            alarmEnabled = DEFAULT_ALARM_ENABLED;
            alarmThresholdGlobal = DEFAULT_ALARM_THRESHOLD_GLOBAL;
            for (int i = 0; i < 8; i++) {
                alarmThresholdChannels[i] = DEFAULT_ALARM_THRESHOLD_GLOBAL;
            }
            try {
                saveCurrentConfig();
            } catch (Exception ignored) {
            }
            return;
        }

        Properties prop = new Properties();
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String rawContent = new String(bytes, StandardCharsets.UTF_8);
            String plainContent = decryptString(rawContent);

            // 如果读取的是明文文件，自动升级保存为加密格式并设置隐藏
            if (!rawContent.trim().contains("ENC:")) {
                try {
                    saveEncryptedConfigString(file, plainContent);
                } catch (Exception ignored) {
                }
            } else {
                hideFile(file);
            }

            try (StringReader sr = new StringReader(plainContent)) {
                prop.load(sr);
            }

            reportDataEnabled = parseBoolean(prop, String.valueOf(DEFAULT_REPORT_DATA_ENABLED),
                    "reportdata.enabled", "enable.reportdata", "reportdata");
            persistentPeakEnabled = parseBoolean(prop, String.valueOf(DEFAULT_PERSISTENT_PEAK_ENABLED),
                    "persistent.peak.enabled", "enable.persistent.peak",
                    "persistent.peak.ui.enabled", "persistent.peak", "show.persistent.buttons");
            spectrogramPeakFollowFiltered = parseBoolean(prop, String.valueOf(DEFAULT_SPECTROGRAM_PEAK_FOLLOW_FILTERED),
                    "spectrogram.peak.follow.filtered", "spectrogram.follow.persistent.peak",
                    "spectrogram.peak.follow.persistent", "spectrogram.follow.peak",
                    "spectrum.peak.follow.filtered", "spectrogram.peak.follow.smoothed",
                    "spectrogram.follow.filtered.peak");
            displayEpsilonDb = parseDouble(prop, DEFAULT_DISPLAY_EPSILON_DB,
                    "display.epsilon.db", "display.epsilon", "display_epsilon_db",
                    "display.deadband.db", "deadband.db", "epsilon.db");
            displayJitterDb = parseDouble(prop, DEFAULT_DISPLAY_JITTER_DB,
                    "display.jitter.db", "display_jitter_db", "display.jitter",
                    "display.fluctuation.db", "jitter.db");
            displayDecimals = parseInt(prop, DEFAULT_DISPLAY_DECIMALS, 0, 6,
                    "display.decimals", "display_decimals", "persistent.decimals", "decimals");
            appVersion = parseString(prop, DEFAULT_APP_VERSION,
                    "app.version", "version", "app_version");
            displayMaxStepDb = parseDouble(prop, DEFAULT_DISPLAY_MAX_STEP_DB,
                    "display.max.step.db", "display_max_step_db", "max.step.db",
                    "persistent.max.step.db", "max.step", "max_step");
            initialGroups = parseInt(prop, DEFAULT_INITIAL_GROUPS, 1, 100,
                    "initial.groups", "initial_groups", "init.groups", "initial.group.count", "init_groups");
            switchJudgeGroups = parseInt(prop, DEFAULT_SWITCH_JUDGE_GROUPS, 1, 100,
                    "switch.judge.groups", "switch_judge_groups", "signal.switch.groups",
                    "signal_switch_groups", "switch.groups", "transition.groups");
            switchShowDash = parseBoolean(prop, String.valueOf(DEFAULT_SWITCH_SHOW_DASH),
                    "switch.show.dash", "switch_show_dash", "signal.switch.show.dash",
                    "signal_switch_show_dash", "display.dash.on.switch", "switch.dash.enabled");
            deviceIp = parseString(prop, DEFAULT_DEVICE_IP,
                    "default.ip", "device.ip", "default.device.ip", "device.addr", "default_ip", "ip");
            spectrogramRollingEnabled = parseBoolean(prop, String.valueOf(DEFAULT_SPECTROGRAM_ROLLING_ENABLED),
                    "spectrogram.rolling.enabled", "spectrogram.rolling", "rolling.enabled",
                    "waterfall.enabled", "display.rolling.mode");

            acquisitionSpeedDelayMs = parseInt(prop, DEFAULT_ACQUISITION_SPEED_DELAY_MS, 0, 10000,
                    "acquisition.speed.delay.ms", "acq.speed.delay.ms", "speed.delay.ms");
            acquisitionSpeedMode = parseString(prop, DEFAULT_ACQUISITION_SPEED_MODE,
                    "acquisition.speed.mode", "acq.speed.mode", "speed.mode");

            ipSwitchMode = parseInt(prop, DEFAULT_IP_SWITCH_MODE, 0, 2,
                    "ip.switch.mode", "ip_switch_mode", "switch.mode");
            presetIp1 = parseString(prop, DEFAULT_PRESET_IP_1, "preset.ip.1", "preset_ip_1");
            presetName1 = parseString(prop, DEFAULT_PRESET_NAME_1, "preset.name.1", "preset_name_1");
            presetIp2 = parseString(prop, DEFAULT_PRESET_IP_2, "preset.ip.2", "preset_ip_2");
            presetName2 = parseString(prop, DEFAULT_PRESET_NAME_2, "preset.name.2", "preset_name_2");
            presetIp3 = parseString(prop, DEFAULT_PRESET_IP_3, "preset.ip.3", "preset_ip_3");
            presetName3 = parseString(prop, DEFAULT_PRESET_NAME_3, "preset.name.3", "preset_name_3");

            alarmEnabled = parseBoolean(prop, String.valueOf(DEFAULT_ALARM_ENABLED),
                    "alarm.enabled", "enable.alarm", "alarm");
            alarmThresholdGlobal = Math.round(parseDouble(prop, DEFAULT_ALARM_THRESHOLD_GLOBAL, -150.0, 100.0,
                    "alarm.threshold.global", "alarm.threshold", "alarm_threshold_global"));
            for (int i = 0; i < 8; i++) {
                alarmThresholdChannels[i] = Math.round(parseDouble(prop, alarmThresholdGlobal, -150.0, 100.0,
                        "alarm.threshold.ch" + (i + 1), "alarm_threshold_ch" + (i + 1)));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load .config file, using safe defaults", e);
            reportDataEnabled = DEFAULT_REPORT_DATA_ENABLED;
            persistentPeakEnabled = DEFAULT_PERSISTENT_PEAK_ENABLED;
            spectrogramPeakFollowFiltered = DEFAULT_SPECTROGRAM_PEAK_FOLLOW_FILTERED;
            displayEpsilonDb = DEFAULT_DISPLAY_EPSILON_DB;
            displayJitterDb = DEFAULT_DISPLAY_JITTER_DB;
            displayDecimals = DEFAULT_DISPLAY_DECIMALS;
            appVersion = DEFAULT_APP_VERSION;
            displayMaxStepDb = DEFAULT_DISPLAY_MAX_STEP_DB;
            initialGroups = DEFAULT_INITIAL_GROUPS;
            switchJudgeGroups = DEFAULT_SWITCH_JUDGE_GROUPS;
            switchShowDash = DEFAULT_SWITCH_SHOW_DASH;
            deviceIp = DEFAULT_DEVICE_IP;
            spectrogramRollingEnabled = DEFAULT_SPECTROGRAM_ROLLING_ENABLED;
            acquisitionSpeedDelayMs = DEFAULT_ACQUISITION_SPEED_DELAY_MS;
            acquisitionSpeedMode = DEFAULT_ACQUISITION_SPEED_MODE;
            ipSwitchMode = DEFAULT_IP_SWITCH_MODE;
            presetIp1 = DEFAULT_PRESET_IP_1;
            presetName1 = DEFAULT_PRESET_NAME_1;
            presetIp2 = DEFAULT_PRESET_IP_2;
            presetName2 = DEFAULT_PRESET_NAME_2;
            presetIp3 = DEFAULT_PRESET_IP_3;
            presetName3 = DEFAULT_PRESET_NAME_3;
            alarmEnabled = DEFAULT_ALARM_ENABLED;
            alarmThresholdGlobal = DEFAULT_ALARM_THRESHOLD_GLOBAL;
            for (int i = 0; i < 8; i++) {
                alarmThresholdChannels[i] = DEFAULT_ALARM_THRESHOLD_GLOBAL;
            }
        }
    }

    /**
     * 将当前配置加密保存到 lib/.config 并设置隐藏
     */
    public static synchronized void saveCurrentConfig() throws Exception {
        saveConfig(
                reportDataEnabled,
                persistentPeakEnabled,
                spectrogramPeakFollowFiltered,
                displayEpsilonDb,
                displayJitterDb,
                displayDecimals,
                appVersion,
                displayMaxStepDb,
                initialGroups,
                switchJudgeGroups,
                switchShowDash,
                deviceIp,
                spectrogramRollingEnabled
        );
    }

    /**
     * 保存指定参数，更新内存变量并加密保存到 lib/.config (包含设备 IP 及刷新模式)
     */
    public static synchronized void saveConfig(
            boolean reportData,
            boolean persistentPeak,
            boolean followFiltered,
            double epsilonDb,
            double jitterDb,
            int decimals,
            String version,
            double maxStepDb,
            int initGroups,
            int switchJudge,
            boolean switchDash,
            String ip,
            boolean rollingEnabled
    ) throws Exception {
        reportDataEnabled = reportData;
        persistentPeakEnabled = persistentPeak;
        spectrogramPeakFollowFiltered = followFiltered;
        setDisplayEpsilonDb(epsilonDb);
        setDisplayJitterDb(jitterDb);
        setDisplayDecimals(decimals);
        setAppVersion(version);
        setDisplayMaxStepDb(maxStepDb);
        setInitialGroups(initGroups);
        setSwitchJudgeGroups(switchJudge);
        switchShowDash = switchDash;
        setDeviceIp(ip);
        setSpectrogramRollingEnabled(rollingEnabled);

        StringBuilder sb = new StringBuilder();
        sb.append("# ===================================================================\n");
        sb.append("# Graph_8Pro 系统全局功能配置 (.config)\n");
        sb.append("# ===================================================================\n\n");
        sb.append("# 1. 是否开启原始UDP报文写入reportdata目录 (true/false, 默认关闭)\n");
        sb.append("reportdata.enabled=").append(reportDataEnabled).append("\n\n");
        sb.append("# 2. 是否开启持久峰值平滑滤波功能 (true/false, 默认开启)\n");
        sb.append("persistent.peak.enabled=").append(persistentPeakEnabled).append("\n\n");
        sb.append("# 3. 持久峰值界面数值更新最小波动门限/死区 (单位: dB, 默认 0.2)\n");
        sb.append("display.epsilon.db=").append(displayEpsilonDb).append("\n\n");
        sb.append("# 4. 持久峰值最大值微波动/微抖动范围 (单位: dB, 默认 0 表示不开启, 设置为如 2 则在稳定值基础上计算 ±(0-2) 随机波动)\n");
        sb.append("display.jitter.db=").append(displayJitterDb).append("\n\n");
        sb.append("# 5. 谱图峰值是否跟随平滑滤波峰值 (true/false, 默认开启)\n");
        sb.append("spectrogram.peak.follow.filtered=").append(spectrogramPeakFollowFiltered).append("\n\n");
        sb.append("# 6. 持久性测试数值显示与记录的小数点后位数 (0-6, 默认 1; 0 表示无小数点后内容/整数显示)\n");
        sb.append("display.decimals=").append(displayDecimals).append("\n\n");
        sb.append("# 7. 软件版本号显示 (若配置则覆盖默认版本号)\n");
        sb.append("app.version=").append(appVersion).append("\n\n");
        sb.append("# 8. 持久峰值每次变动幅值上限/步进上限 (单位: dB, 默认 0.3; 若设置为如 10，则在输入大幅突变且均值稳定后以 10dBm 为单位快速变动)\n");
        sb.append("display.max.step.db=").append(displayMaxStepDb).append("\n\n");
        sb.append("# 9. 持久峰值初始建立所需的组数 (默认 3，即第 3 组/约 4.8 秒后输出初始最大值)\n");
        sb.append("initial.groups=").append(initialGroups).append("\n\n");
        sb.append("# 10. 中途切换输入信号强度的判断组数 (默认 3，即检测到信号大幅切换后，需连续采集几组来判定并稳定新信号强度)\n");
        sb.append("switch.judge.groups=").append(switchJudgeGroups).append("\n\n");
        sb.append("# 11. 中途切换信号强度时是否先将最大值显示为--，待稳定后再显示数据 (true/false, 默认 false)\n");
        sb.append("switch.show.dash=").append(switchShowDash).append("\n\n");
        sb.append("# 12. 默认设备 IP 地址 (默认 192.168.10.60)\n");
        sb.append("default.ip=").append(deviceIp).append("\n\n");
        sb.append("# 13. 谱图动态刷新机制 (true: 逐周波平滑滚动(瀑布流), false: 整组80周波跳变(传统模式), 默认 true)\n");
        sb.append("spectrogram.rolling.enabled=").append(spectrogramRollingEnabled).append("\n\n");
        sb.append("# 14. 采集速度控制延时毫秒数 (0 为 50Hz 全速, 20 为 25Hz 等) 与模式\n");
        sb.append("acquisition.speed.delay.ms=").append(acquisitionSpeedDelayMs).append("\n");
        sb.append("acquisition.speed.mode=").append(acquisitionSpeedMode).append("\n\n");
        sb.append("# 15. IP 切换方式 (0: 直接输入, 1: 3个预设按钮, 2: 并行并存模式) 与 3 个预设 IP 及按钮文本\n");
        sb.append("ip.switch.mode=").append(ipSwitchMode).append("\n");
        sb.append("preset.ip.1=").append(presetIp1).append("\n");
        sb.append("preset.name.1=").append(presetName1).append("\n");
        sb.append("preset.ip.2=").append(presetIp2).append("\n");
        sb.append("preset.name.2=").append(presetName2).append("\n");
        sb.append("preset.ip.3=").append(presetIp3).append("\n");
        sb.append("preset.name.3=").append(presetName3).append("\n\n");
        sb.append("# 16. 幅值报警开关与阈值 (dBm)\n");
        sb.append("alarm.enabled=").append(alarmEnabled).append("\n");
        sb.append("alarm.threshold.global=").append(Math.round(alarmThresholdGlobal)).append("\n");
        for (int i = 0; i < 8; i++) {
            sb.append("alarm.threshold.ch").append(i + 1).append("=").append(Math.round(alarmThresholdChannels[i])).append("\n");
        }

        File file = getConfigFile();
        saveEncryptedConfigString(file, sb.toString());
    }

    /**
     * 重载 saveConfig (兼容包含 IP 的旧版调用)
     */
    public static synchronized void saveConfig(
            boolean reportData,
            boolean persistentPeak,
            boolean followFiltered,
            double epsilonDb,
            double jitterDb,
            int decimals,
            String version,
            double maxStepDb,
            int initGroups,
            int switchJudge,
            boolean switchDash,
            String ip
    ) throws Exception {
        saveConfig(
                reportData,
                persistentPeak,
                followFiltered,
                epsilonDb,
                jitterDb,
                decimals,
                version,
                maxStepDb,
                initGroups,
                switchJudge,
                switchDash,
                ip,
                spectrogramRollingEnabled
        );
    }

    /**
     * 重载 saveConfig (兼容旧版调用)
     */
    public static synchronized void saveConfig(
            boolean reportData,
            boolean persistentPeak,
            boolean followFiltered,
            double epsilonDb,
            double jitterDb,
            int decimals,
            String version,
            double maxStepDb,
            int initGroups,
            int switchJudge,
            boolean switchDash
    ) throws Exception {
        saveConfig(
                reportData,
                persistentPeak,
                followFiltered,
                epsilonDb,
                jitterDb,
                decimals,
                version,
                maxStepDb,
                initGroups,
                switchJudge,
                switchDash,
                deviceIp
        );
    }

    private static boolean parseBoolean(Properties prop, String defaultValue, String... keys) {
        for (String k : keys) {
            String val = prop.getProperty(k);
            if (val != null && !val.trim().isEmpty()) {
                val = val.trim().toLowerCase();
                return "true".equals(val) || "1".equals(val) || "yes".equals(val) || "on".equals(val);
            }
        }
        return "true".equalsIgnoreCase(defaultValue);
    }

    private static double parseDouble(Properties prop, double defaultValue, String... keys) {
        return parseDouble(prop, defaultValue, 0.0, 50.0, keys);
    }

    private static double parseDouble(Properties prop, double defaultValue, double min, double max, String... keys) {
        for (String k : keys) {
            String val = prop.getProperty(k);
            if (val != null && !val.trim().isEmpty()) {
                try {
                    double d = Double.parseDouble(val.trim());
                    if (d >= min && d <= max) {
                        return d;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return defaultValue;
    }

    private static int parseInt(Properties prop, int defaultValue, int min, int max, String... keys) {
        for (String k : keys) {
            String val = prop.getProperty(k);
            if (val != null && !val.trim().isEmpty()) {
                try {
                    int v = Integer.parseInt(val.trim());
                    if (v >= min && v <= max) {
                        return v;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return defaultValue;
    }

    private static String parseString(Properties prop, String defaultValue, String... keys) {
        for (String k : keys) {
            String val = prop.getProperty(k);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return defaultValue;
    }

    public static boolean isReportDataEnabled() {
        return reportDataEnabled;
    }

    public static void setReportDataEnabled(boolean enabled) {
        reportDataEnabled = enabled;
    }

    public static boolean isPersistentPeakEnabled() {
        if (!LicenseManager.isLicensed()) {
            return false;
        }
        return persistentPeakEnabled;
    }

    public static void setPersistentPeakEnabled(boolean enabled) {
        persistentPeakEnabled = enabled;
    }

    /**
     * 谱图峰值是否实时跟随平滑滤波值。
     * 注意：作为持久测试平滑滤波的子功能，只有在平滑滤波总开关 (persistentPeakEnabled)
     * 同时开启时才会真正生效；若总开关未开启，则谱图峰值强制使用原始实时峰值。
     */
    public static boolean isSpectrogramPeakFollowFiltered() {
        return isPersistentPeakEnabled() && spectrogramPeakFollowFiltered;
    }

    /**
     * 获取配置文件中存储的谱图峰值跟随原始配置值（不受总开关状态影响，供配置界面回显使用）
     */
    public static boolean isSpectrogramPeakFollowFilteredConfig() {
        return spectrogramPeakFollowFiltered;
    }

    public static void setSpectrogramPeakFollowFiltered(boolean enabled) {
        spectrogramPeakFollowFiltered = enabled;
    }

    public static boolean isPersistentPeakUiEnabled() {
        return persistentPeakEnabled;
    }

    public static void setPersistentPeakUiEnabled(boolean enabled) {
        persistentPeakEnabled = enabled;
    }

    public static double getDisplayEpsilonDb() {
        return displayEpsilonDb;
    }

    public static void setDisplayEpsilonDb(double epsilon) {
        if (epsilon >= 0.0 && epsilon <= 50.0) {
            displayEpsilonDb = epsilon;
        }
    }

    public static double getDisplayJitterDb() {
        return displayJitterDb;
    }

    public static void setDisplayJitterDb(double jitter) {
        if (jitter >= 0.0 && jitter <= 50.0) {
            displayJitterDb = jitter;
        }
    }

    public static int getDisplayDecimals() {
        return displayDecimals;
    }

    public static void setDisplayDecimals(int decimals) {
        if (decimals >= 0 && decimals <= 6) {
            displayDecimals = decimals;
        }
    }

    public static String getAppVersion() {
        return LicenseManager.formatDisplayVersion(appVersion);
    }

    public static String getRawAppVersion() {
        return appVersion;
    }

    public static void setAppVersion(String version) {
        if (version != null && !version.trim().isEmpty()) {
            appVersion = version.trim();
        }
    }

    public static double getDisplayMaxStepDb() {
        return displayMaxStepDb;
    }

    public static void setDisplayMaxStepDb(double maxStep) {
        if (maxStep > 0.0 && maxStep <= 50.0) {
            displayMaxStepDb = maxStep;
        }
    }

    public static int getInitialGroups() {
        return initialGroups;
    }

    public static void setInitialGroups(int groups) {
        if (groups >= 1 && groups <= 100) {
            initialGroups = groups;
        }
    }

    public static int getSwitchJudgeGroups() {
        return switchJudgeGroups;
    }

    public static void setSwitchJudgeGroups(int groups) {
        if (groups >= 1 && groups <= 100) {
            switchJudgeGroups = groups;
        }
    }

    public static boolean isSwitchShowDash() {
        return switchShowDash;
    }

    public static void setSwitchShowDash(boolean showDash) {
        switchShowDash = showDash;
    }

    public static String getDeviceIp() {
        return deviceIp;
    }

    public static void setDeviceIp(String ip) {
        if (ip != null && !ip.trim().isEmpty()) {
            deviceIp = ip.trim();
        }
    }

    public static boolean isSpectrogramRollingEnabled() {
        if (!LicenseManager.isLicensed()) {
            return false;
        }
        return spectrogramRollingEnabled;
    }

    public static void setSpectrogramRollingEnabled(boolean enabled) {
        spectrogramRollingEnabled = enabled;
    }

    // ---------------- 采集速度配置 ----------------
    public static int getAcquisitionSpeedDelayMs() {
        if (!LicenseManager.isLicensed()) {
            return DEFAULT_ACQUISITION_SPEED_DELAY_MS;
        }
        return acquisitionSpeedDelayMs;
    }

    public static int getRawAcquisitionSpeedDelayMs() {
        return acquisitionSpeedDelayMs;
    }

    public static void setAcquisitionSpeedDelayMs(int delayMs) {
        if (delayMs >= 0 && delayMs <= 10000) {
            acquisitionSpeedDelayMs = delayMs;
        }
    }

    public static String getAcquisitionSpeedMode() {
        if (!LicenseManager.isLicensed()) {
            return DEFAULT_ACQUISITION_SPEED_MODE;
        }
        return acquisitionSpeedMode;
    }

    public static String getRawAcquisitionSpeedMode() {
        return acquisitionSpeedMode;
    }

    public static void setAcquisitionSpeedMode(String mode) {
        if (mode != null && !mode.trim().isEmpty()) {
            acquisitionSpeedMode = mode.trim();
        }
    }

    public static void setSpeedPreset(String mode) {
        if (mode == null) return;
        switch (mode.toUpperCase(java.util.Locale.ROOT)) {
            case "MAX":
                acquisitionSpeedMode = "MAX";
                acquisitionSpeedDelayMs = 0; // 50Hz 全速
                break;
            case "FAST":
                acquisitionSpeedMode = "FAST";
                acquisitionSpeedDelayMs = 20; // 约 25Hz
                break;
            case "MEDIUM":
                acquisitionSpeedMode = "MEDIUM";
                acquisitionSpeedDelayMs = 80; // 约 10Hz
                break;
            case "SLOW":
                acquisitionSpeedMode = "SLOW";
                acquisitionSpeedDelayMs = 480; // 约 2Hz
                break;
            default:
                acquisitionSpeedMode = "CUSTOM";
                break;
        }
    }

    // ---------------- IP 切换模式与 3 预设 IP ----------------
    public static int getIpSwitchMode() {
        if (!LicenseManager.isLicensed()) {
            return 0; // 未授权强制无预设功能（仅直接输入）
        }
        return ipSwitchMode;
    }

    public static int getRawIpSwitchMode() {
        return ipSwitchMode;
    }

    public static void setIpSwitchMode(int mode) {
        if (mode >= 0 && mode <= 2) {
            ipSwitchMode = mode;
        }
    }

    public static String getPresetIp1() {
        return presetIp1;
    }

    public static void setPresetIp1(String ip) {
        if (ip != null && !ip.trim().isEmpty()) {
            presetIp1 = ip.trim();
        }
    }

    public static String getPresetIp2() {
        return presetIp2;
    }

    public static void setPresetIp2(String ip) {
        if (ip != null && !ip.trim().isEmpty()) {
            presetIp2 = ip.trim();
        }
    }

    public static String getPresetIp3() {
        return presetIp3;
    }

    public static void setPresetIp3(String ip) {
        if (ip != null && !ip.trim().isEmpty()) {
            presetIp3 = ip.trim();
        }
    }

    public static String getPresetIp(int index) {
        switch (index) {
            case 1: return presetIp1;
            case 2: return presetIp2;
            case 3: return presetIp3;
            default: return deviceIp;
        }
    }

    public static void setPresetIp(int index, String ip) {
        if (ip == null || ip.trim().isEmpty()) return;
        switch (index) {
            case 1: presetIp1 = ip.trim(); break;
            case 2: presetIp2 = ip.trim(); break;
            case 3: presetIp3 = ip.trim(); break;
        }
    }

    public static String getPresetName1() {
        return presetName1;
    }

    public static void setPresetName1(String name) {
        if (name != null && !name.trim().isEmpty()) {
            presetName1 = name.trim();
        }
    }

    public static String getPresetName2() {
        return presetName2;
    }

    public static void setPresetName2(String name) {
        if (name != null && !name.trim().isEmpty()) {
            presetName2 = name.trim();
        }
    }

    public static String getPresetName3() {
        return presetName3;
    }

    public static void setPresetName3(String name) {
        if (name != null && !name.trim().isEmpty()) {
            presetName3 = name.trim();
        }
    }

    public static String getPresetName(int index) {
        switch (index) {
            case 1: return presetName1;
            case 2: return presetName2;
            case 3: return presetName3;
            default: return "预设" + index;
        }
    }

    public static void setPresetName(int index, String name) {
        if (name == null || name.trim().isEmpty()) return;
        switch (index) {
            case 1: presetName1 = name.trim(); break;
            case 2: presetName2 = name.trim(); break;
            case 3: presetName3 = name.trim(); break;
        }
    }

    // ---------------- 幅值报警配置 ----------------
    public static boolean isAlarmEnabled() {
        if (!LicenseManager.isLicensed()) {
            return false;
        }
        return alarmEnabled;
    }

    public static void setAlarmEnabled(boolean enabled) {
        alarmEnabled = enabled;
    }

    public static double getAlarmThresholdGlobal() {
        return alarmThresholdGlobal;
    }

    public static void setAlarmThresholdGlobal(double threshold) {
        double oldGlobal = alarmThresholdGlobal;
        alarmThresholdGlobal = Math.round(threshold);
        for (int i = 0; i < 8; i++) {
            if (alarmThresholdChannels[i] == oldGlobal) {
                alarmThresholdChannels[i] = alarmThresholdGlobal;
            }
        }
    }

    public static double getAlarmThresholdChannel(int ch) {
        if (ch >= 0 && ch < 8) {
            return alarmThresholdChannels[ch];
        }
        return alarmThresholdGlobal;
    }

    public static void setAlarmThresholdChannel(int ch, double threshold) {
        if (ch >= 0 && ch < 8) {
            alarmThresholdChannels[ch] = Math.round(threshold);
        }
    }

    public static double[] getAlarmThresholdChannels() {
        double[] copy = new double[8];
        System.arraycopy(alarmThresholdChannels, 0, copy, 0, 8);
        return copy;
    }

    public static void main(String[] args) {
        loadConfig();
        setDisplayDecimals(0);
        setAppVersion(DEFAULT_APP_VERSION);
        try {
            saveCurrentConfig();
            System.out.println("Config successfully updated: version=" + getAppVersion() + ", decimals=" + getDisplayDecimals());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
