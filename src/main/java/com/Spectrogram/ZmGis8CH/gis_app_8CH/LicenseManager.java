package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/**
 * 软件授权管理器
 * <p>
 * 对接 tools/license-tools 授权体系：
 * 1. 授权文件固定存储在 lib/license.prop；
 * 2. 校验本机物理网卡 MAC 地址匹配；
 * 3. 校验 SHA-256 + CHAR_MAP 授权码（SALT = "ZustMonitor2025_Pro_Version_Salt!@#"）；
 * 4. 授权成功显示为 "Pro v2.2.x"，未授权显示为 "v2.2.x"。
 */
public final class LicenseManager {

    public static final String RELATIVE_LICENSE_PATH = "lib/license.prop";
    public static final String SALT = "ZustMonitor2025_Pro_Version_Salt!@#";
    public static final String CHAR_MAP = "23456789ABCDEFGHJKLMNPQRTVWXY";

    private static Boolean cachedLicensed = null;
    private static Boolean cachedSpecialLicense = null;
    private static String cachedMatchedMac = null;
    private static String cachedReason = "未检测";
    private static File customLicenseFile = null;
    private static Boolean mockLicensed = null;
    private static Boolean mockSpecialLicensed = null;

    private LicenseManager() {}

    /**
     * 设置测试模拟授权状态（设为 null 则恢复真实文件检测）
     */
    public static synchronized void setMockLicensed(Boolean mock) {
        mockLicensed = mock;
    }

    /**
     * 设置测试模拟特殊授权状态（设为 null 则恢复真实文件检测）
     */
    public static synchronized void setMockSpecialLicensed(Boolean mock) {
        mockSpecialLicensed = mock;
    }

    /**
     * 判断当前软件是否具备有效授权（正式授权或特殊调试授权均返回 true）
     */
    public static synchronized boolean isLicensed() {
        if (mockLicensed != null) {
            return mockLicensed.booleanValue();
        }
        if (mockSpecialLicensed != null && mockSpecialLicensed.booleanValue()) {
            return true;
        }
        if (cachedLicensed == null) {
            refreshLicenseStatus();
        }
        return cachedLicensed != null && cachedLicensed.booleanValue();
    }

    /**
     * 判断当前生效的授权是否为“特殊授权文件”（用于内部/调试使用）
     */
    public static synchronized boolean isSpecialLicense() {
        if (mockSpecialLicensed != null) {
            return mockSpecialLicensed.booleanValue();
        }
        if (cachedSpecialLicense == null) {
            refreshLicenseStatus();
        }
        return cachedSpecialLicense != null && cachedSpecialLicense.booleanValue();
    }

    /**
     * 判断授权文件是否存在（不作算法校验）
     */
    public static boolean hasLicenseFile() {
        if (mockLicensed != null) {
            return mockLicensed.booleanValue();
        }
        File f = getLicenseFile();
        return f != null && f.exists() && f.isFile();
    }

    /**
     * 重新从磁盘扫描并校验授权文件
     */
    public static synchronized boolean refreshLicenseStatus() {
        cachedSpecialLicense = Boolean.FALSE;
        File licenseFile = getLicenseFile();
        if (licenseFile == null || !licenseFile.exists() || !licenseFile.isFile()) {
            cachedLicensed = Boolean.FALSE;
            cachedMatchedMac = null;
            cachedReason = "未检测到授权文件 (" + RELATIVE_LICENSE_PATH + ")";
            return false;
        }

        Properties prop = new Properties();
        try (InputStream in = new FileInputStream(licenseFile)) {
            prop.load(in);
        } catch (Exception e) {
            cachedLicensed = Boolean.FALSE;
            cachedMatchedMac = null;
            cachedReason = "读取授权文件失败: " + e.getMessage();
            return false;
        }

        // 1. 特殊授权文件快速判定（满足其一即判定为特殊调试授权，开启全部功能）
        boolean isSpecial = false;
        String propSpecial = prop.getProperty("special");
        String propType = prop.getProperty("type");
        String propMode = prop.getProperty("mode");
        String propDebug = prop.getProperty("debug");
        String licMac = prop.getProperty("mac");
        String licKey = prop.getProperty("key");

        if ("true".equalsIgnoreCase(propSpecial) || "1".equals(propSpecial)
                || "special".equalsIgnoreCase(propType) || "debug".equalsIgnoreCase(propType)
                || "special".equalsIgnoreCase(propMode) || "debug".equalsIgnoreCase(propMode)
                || "true".equalsIgnoreCase(propDebug)) {
            isSpecial = true;
        } else if (licMac != null && (licMac.equalsIgnoreCase("SPECIAL")
                || licMac.equalsIgnoreCase("DEBUG")
                || licMac.equalsIgnoreCase("*")
                || licMac.equalsIgnoreCase("00:00:00:00:00:00")
                || licMac.equalsIgnoreCase("FF:FF:FF:FF:FF:FF")
                || licMac.equalsIgnoreCase("000000000000")
                || licMac.equalsIgnoreCase("FFFFFFFFFFFF"))) {
            isSpecial = true;
        } else if (licKey != null && (licKey.equalsIgnoreCase("SPECIAL") || licKey.equalsIgnoreCase("DEBUG"))) {
            isSpecial = true;
        } else if (licMac != null && licKey != null) {
            String clean = cleanMac(licMac);
            if ("020000000000".equals(clean)) {
                try {
                    if (computeKey(clean).equalsIgnoreCase(licKey.trim())) {
                        isSpecial = true;
                    }
                } catch (Exception ignored) {}
            }
        }

        if (isSpecial) {
            cachedLicensed = Boolean.TRUE;
            cachedSpecialLicense = Boolean.TRUE;
            cachedMatchedMac = (licMac != null ? licMac.trim() : "SPECIAL");
            cachedReason = "特殊授权文件有效 (仅供调试使用)";
            return true;
        }

        // 2. 正式授权文件匹配与校验
        if (licMac == null || licMac.trim().isEmpty() || licKey == null || licKey.trim().isEmpty()) {
            cachedLicensed = Boolean.FALSE;
            cachedMatchedMac = null;
            cachedReason = "授权文件内容不完整 (缺少 mac 或 key)";
            return false;
        }

        String cleanLicMac = cleanMac(licMac);
        if (cleanLicMac == null || cleanLicMac.length() != 12) {
            cachedLicensed = Boolean.FALSE;
            cachedMatchedMac = null;
            cachedReason = "授权文件中的 MAC 地址格式无效: " + licMac;
            return false;
        }

        Set<String> hostMacs = getHostCleanMacs();
        if (!hostMacs.contains(cleanLicMac)) {
            cachedLicensed = Boolean.FALSE;
            cachedMatchedMac = null;
            cachedReason = "授权文件 MAC (" + formatMacColon(cleanLicMac) + ") 与本机网卡不匹配";
            return false;
        }

        String expectedKey;
        try {
            expectedKey = computeKey(cleanLicMac);
        } catch (Exception e) {
            cachedLicensed = Boolean.FALSE;
            cachedMatchedMac = null;
            cachedReason = "计算授权签名异常: " + e.getMessage();
            return false;
        }

        if (!expectedKey.equalsIgnoreCase(licKey.trim())) {
            cachedLicensed = Boolean.FALSE;
            cachedMatchedMac = null;
            cachedReason = "授权码校验失败 (Key 不匹配)";
            return false;
        }

        cachedLicensed = Boolean.TRUE;
        cachedSpecialLicense = Boolean.FALSE;
        cachedMatchedMac = cleanLicMac;
        cachedReason = "授权有效 (绑定网卡: " + formatMacColon(cleanLicMac) + ")";
        return true;
    }

    /**
     * 重置缓存状态（用于导入新授权文件后或单元测试）
     */
    public static synchronized void resetCache() {
        cachedLicensed = null;
        cachedSpecialLicense = null;
        cachedMatchedMac = null;
        cachedReason = "未检测";
    }

    /**
     * 指定自定义授权文件路径（主要用于单元测试）
     */
    public static synchronized void setCustomLicenseFile(File file) {
        customLicenseFile = file;
        resetCache();
    }

    /**
     * 获取最近一次授权状态判定原因
     */
    public static synchronized String getLicenseStatusReason() {
        if (cachedLicensed == null) {
            refreshLicenseStatus();
        }
        return cachedReason;
    }

    /**
     * 定位授权文件物理位置
     */
    public static File getLicenseFile() {
        if (customLicenseFile != null) {
            return customLicenseFile;
        }

        // 1. 优先尝试从 AppGlobalConfig 的配置目录获取 lib/license.prop 或 lib/license_special.prop
        try {
            File cfg = AppGlobalConfig.getConfigFile();
            if (cfg != null && cfg.getParentFile() != null) {
                File f = new File(cfg.getParentFile(), "license.prop");
                if (f.exists()) {
                    return f;
                }
                File fSpec = new File(cfg.getParentFile(), "license_special.prop");
                if (fSpec.exists()) {
                    return fSpec;
                }
            }
        } catch (Throwable ignored) {}

        // 2. 尝试当前工作目录下的 lib/license.prop 或 lib/license_special.prop
        File localFile = new File(RELATIVE_LICENSE_PATH);
        if (localFile.exists()) {
            return localFile;
        }
        File localSpec = new File("lib/license_special.prop");
        if (localSpec.exists()) {
            return localSpec;
        }

        // 3. 尝试类代码位置上级
        try {
            File codeSource = new File(LicenseManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File baseDir = codeSource.isFile() ? codeSource.getParentFile() : codeSource;
            if (baseDir != null) {
                File f = new File(baseDir, RELATIVE_LICENSE_PATH);
                if (f.exists()) {
                    return f;
                }
                if (baseDir.getParentFile() != null) {
                    File pf = new File(baseDir.getParentFile(), RELATIVE_LICENSE_PATH);
                    if (pf.exists()) {
                        return pf;
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 默认返回配置目录下的目标路径对象
        try {
            File cfg = AppGlobalConfig.getConfigFile();
            if (cfg != null && cfg.getParentFile() != null) {
                return new File(cfg.getParentFile(), "license.prop");
            }
        } catch (Throwable ignored) {}
        return localFile;
    }

    /**
     * 根据授权状态格式化版本号展示：
     * <ul>
     *   <li>已授权：显示为 "Pro v2.2.x"（保留或自动补全 Pro 前缀）</li>
     *   <li>未授权：显示为 "v2.2.x"（去除 Pro 前缀）</li>
     * </ul>
     */
    public static String formatDisplayVersion(String baseVersion) {
        if (baseVersion == null || baseVersion.trim().isEmpty()) {
            baseVersion = "v2.2.8";
        }
        String v = baseVersion.trim();
        if (isLicensed()) {
            if (v.toLowerCase(Locale.ROOT).startsWith("pro ")) {
                return v;
            } else if (v.equalsIgnoreCase("pro")) {
                return "Pro";
            } else if (v.matches("^(?i)v[0-9].*")) {
                return "Pro " + v;
            } else {
                return v;
            }
        } else {
            return v.replaceFirst("(?i)^pro\\s*", "");
        }
    }

    /**
     * 计算指定 12 位纯 MAC 的授权 Key (SHA-256 + CHAR_MAP)
     */
    public static String computeKey(String cleanMac) throws Exception {
        String input = cleanMac.toUpperCase(Locale.ROOT) + SALT;
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        int charMapLen = CHAR_MAP.length();
        for (int i = 0; i < 16; i++) {
            int b1 = hash[i] & 0xFF;
            int b2 = hash[i + 1] & 0xFF;
            int idx = (b1 + b2) % charMapLen;
            sb.append(CHAR_MAP.charAt(idx));
            if (i % 4 == 3 && i != 15) {
                sb.append('-');
            }
        }
        return sb.toString();
    }

    /**
     * 获取本机所有非 Loopback 网卡的 12 位纯 MAC 集合
     */
    public static Set<String> getHostCleanMacs() {
        Set<String> set = new HashSet<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                for (NetworkInterface ni : Collections.list(interfaces)) {
                    try {
                        if (ni.isLoopback()) {
                            continue;
                        }
                        byte[] mac = ni.getHardwareAddress();
                        if (mac == null || mac.length != 6) {
                            continue;
                        }
                        StringBuilder sb = new StringBuilder();
                        for (byte b : mac) {
                            sb.append(String.format(Locale.ROOT, "%02X", b));
                        }
                        String clean = sb.toString();
                        if (!"000000000000".equals(clean)) {
                            set.add(clean);
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        return set;
    }

    /**
     * 清理 MAC 字符串，提取 12 位纯十六进制
     */
    public static String cleanMac(String mac) {
        if (mac == null) return null;
        String s = mac.replaceAll("[:-]", "").trim().toUpperCase(Locale.ROOT);
        if (s.matches("^[0-9A-F]{12}$")) {
            return s;
        }
        return null;
    }

    /**
     * 格式化 12 位纯 MAC 为 XX:XX:XX:XX:XX:XX
     */
    public static String formatMacColon(String cleanMac) {
        if (cleanMac == null || cleanMac.length() != 12) return cleanMac;
        return String.join(":", cleanMac.split("(?<=\\G..)(?!$)"));
    }
}
