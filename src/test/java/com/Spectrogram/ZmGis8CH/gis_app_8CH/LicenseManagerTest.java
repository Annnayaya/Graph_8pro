package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import junit.framework.TestCase;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class LicenseManagerTest extends TestCase {

    private File tempLicFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LicenseManager.setMockLicensed(null);
        LicenseManager.setCustomLicenseFile(null);
        LicenseManager.resetCache();
        tempLicFile = File.createTempFile("test_lic_", ".prop");
        tempLicFile.delete();
    }

    @Override
    protected void tearDown() throws Exception {
        LicenseManager.setMockLicensed(null);
        LicenseManager.setCustomLicenseFile(null);
        LicenseManager.resetCache();
        if (tempLicFile != null && tempLicFile.exists()) {
            tempLicFile.delete();
        }
        super.tearDown();
    }

    public void testVersionFormatting() {
        // 未授权状态下：去除 Pro 前缀
        LicenseManager.setMockLicensed(false);
        assertEquals("v2.2.8", LicenseManager.formatDisplayVersion("Pro v2.2.8"));
        assertEquals("v2.2.8", LicenseManager.formatDisplayVersion("v2.2.8"));
        assertEquals("v2.2.8", LicenseManager.formatDisplayVersion(null));
        assertEquals("v2.2.1", LicenseManager.formatDisplayVersion("Pro v2.2.1"));
        assertEquals("v1.0.0", LicenseManager.formatDisplayVersion("PRO v1.0.0"));

        // 授权状态下：显示 Pro 前缀
        LicenseManager.setMockLicensed(true);
        assertEquals("Pro v2.2.8", LicenseManager.formatDisplayVersion("Pro v2.2.8"));
        assertEquals("Pro v2.2.8", LicenseManager.formatDisplayVersion("v2.2.8"));
        assertEquals("Pro v2.2.8", LicenseManager.formatDisplayVersion(null));
        assertEquals("Pro v2.2.1", LicenseManager.formatDisplayVersion("v2.2.1"));
    }

    public void testComputeKeyAlgorithm() throws Exception {
        String testMac = "02:AB:CD:12:34:56";
        String cleanMac = LicenseManager.cleanMac(testMac);
        assertEquals("02ABCD123456", cleanMac);

        String key = LicenseManager.computeKey(cleanMac);
        assertNotNull(key);
        assertEquals(19, key.length()); // 16 chars + 3 dashes = 19
        assertTrue(key.matches("^[23456789ABCDEFGHJKLMNPQRTVWXY]{4}(-[23456789ABCDEFGHJKLMNPQRTVWXY]{4}){3}$"));
    }

    public void testMissingLicenseFile() {
        LicenseManager.setCustomLicenseFile(new File("non_existent_path_test_123.prop"));
        assertFalse(LicenseManager.isLicensed());
        assertFalse(LicenseManager.hasLicenseFile());
        assertTrue(LicenseManager.getLicenseStatusReason().contains("未检测到授权文件"));
    }

    public void testInvalidMacLicenseFile() throws Exception {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempLicFile), StandardCharsets.UTF_8)) {
            writer.write("mac=00:11:22:33:44:55\n");
            writer.write("key=AAAA-BBBB-CCCC-DDDD\n");
        }
        LicenseManager.setCustomLicenseFile(tempLicFile);
        assertTrue(LicenseManager.hasLicenseFile());
        // 外部 MAC 不匹配本机网卡
        assertFalse(LicenseManager.isLicensed());
        assertTrue(LicenseManager.getLicenseStatusReason().contains("不匹配") || LicenseManager.getLicenseStatusReason().contains("失败"));
    }

    public void testValidLocalMacLicenseFile() throws Exception {
        Set<String> hostMacs = LicenseManager.getHostCleanMacs();
        if (hostMacs.isEmpty()) {
            // 无活动物理网卡环境跳过此子项
            return;
        }
        String localCleanMac = hostMacs.iterator().next();
        String validKey = LicenseManager.computeKey(localCleanMac);

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempLicFile), StandardCharsets.UTF_8)) {
            writer.write("mac=" + LicenseManager.formatMacColon(localCleanMac) + "\n");
            writer.write("key=" + validKey + "\n");
        }

        LicenseManager.setCustomLicenseFile(tempLicFile);
        assertTrue(LicenseManager.hasLicenseFile());
        assertTrue(LicenseManager.isLicensed());
        assertTrue(LicenseManager.getLicenseStatusReason().contains("授权有效"));

        // 测试篡改 key 导致校验失败
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempLicFile), StandardCharsets.UTF_8)) {
            writer.write("mac=" + LicenseManager.formatMacColon(localCleanMac) + "\n");
            writer.write("key=WRONG-KEY0-TEST-1234\n");
        }
        LicenseManager.resetCache();
        assertFalse(LicenseManager.isLicensed());
        assertTrue(LicenseManager.getLicenseStatusReason().contains("校验失败"));
    }

    public void testSpecialLicenseDetection() throws Exception {
        // 1. 验证 special=true 判定为特殊授权文件并开启全部功能
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempLicFile), StandardCharsets.UTF_8)) {
            writer.write("special=true\n");
            writer.write("mac=SPECIAL\n");
            writer.write("key=SPECIAL-DEBUG-INTERNAL-USE-ONLY\n");
        }
        LicenseManager.setCustomLicenseFile(tempLicFile);
        assertTrue(LicenseManager.hasLicenseFile());
        assertTrue(LicenseManager.isLicensed());
        assertTrue(LicenseManager.isSpecialLicense());
        assertEquals("Pro v2.2.8", LicenseManager.formatDisplayVersion("v2.2.8"));
        assertTrue(LicenseManager.getLicenseStatusReason().contains("特殊授权文件有效"));

        // 2. 验证仅包含 mac=SPECIAL 时判定为特殊授权
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempLicFile), StandardCharsets.UTF_8)) {
            writer.write("mac=SPECIAL\n");
            writer.write("key=TEST\n");
        }
        LicenseManager.resetCache();
        assertTrue(LicenseManager.isLicensed());
        assertTrue(LicenseManager.isSpecialLicense());

        // 3. 验证普通无效授权文件不是特殊授权
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempLicFile), StandardCharsets.UTF_8)) {
            writer.write("mac=00:11:22:33:44:55\n");
            writer.write("key=AAAA-BBBB-CCCC-DDDD\n");
        }
        LicenseManager.resetCache();
        assertFalse(LicenseManager.isSpecialLicense());
        assertFalse(LicenseManager.isLicensed());
    }
}
