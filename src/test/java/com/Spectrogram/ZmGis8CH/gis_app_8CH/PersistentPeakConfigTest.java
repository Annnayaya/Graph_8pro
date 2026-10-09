package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import junit.framework.TestCase;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

public class PersistentPeakConfigTest extends TestCase {

    public void testDefaultConfig() {
        PersistentPeakConfig config = new PersistentPeakConfig();
        assertEquals(80, config.getGroupCycles());
        assertEquals(0, config.getWarmupGroups());
        assertEquals(3, config.getInitialGroups());
        assertEquals(10, config.getAverageGroups());
        assertEquals(1, config.getTrimCount());
        assertEquals(0.15, config.getAlphaRise(), 0.0001);
        assertEquals(0.15, config.getAlphaFall(), 0.0001);
        assertEquals(0.3, config.getMaxStepRiseDb(), 0.0001);
        assertEquals(0.3, config.getMaxStepFallDb(), 0.0001);
        assertEquals(0.20, config.getDisplayEpsilonDb(), 0.0001);
        assertEquals(0.0, config.getDisplayJitterDb(), 0.0001);
        assertEquals(1, config.getDisplayDecimals());
        assertEquals(3.0, config.getSignificantChangeDb(), 0.0001);
        assertEquals(1000, config.getCsvFlushIntervalMs());
        assertEquals(1024, config.getCsvQueueCapacity());
        assertEquals(50, config.getCsvMaxFileMb());
        assertEquals(3, config.getSwitchJudgeGroups());
        assertFalse(config.isSwitchShowDash());
    }

    public void testInvalidParametersFallback() {
        // 传入非法参数，期望自动校验并回退到安全默认值
        PersistentPeakConfig config = new PersistentPeakConfig(
                -1, -5, 0, 0, 15, -0.5, 2.0, -1.0, -1.0, -1.0, -1.0, 99, -3.0, 5, 2, 0
        );
        assertEquals(80, config.getGroupCycles());
        assertEquals(0, config.getWarmupGroups());
        assertEquals(3, config.getInitialGroups());
        assertEquals(10, config.getAverageGroups());
        assertEquals(1, config.getTrimCount());
        assertEquals(0.15, config.getAlphaRise(), 0.0001);
        assertEquals(0.15, config.getAlphaFall(), 0.0001);
        assertEquals(0.3, config.getMaxStepRiseDb(), 0.0001);
        assertEquals(0.3, config.getMaxStepFallDb(), 0.0001);
        assertEquals(0.20, config.getDisplayEpsilonDb(), 0.0001);
        assertEquals(0.0, config.getDisplayJitterDb(), 0.0001);
        assertEquals(1, config.getDisplayDecimals());
        assertEquals(3.0, config.getSignificantChangeDb(), 0.0001);
        assertEquals(1000, config.getCsvFlushIntervalMs());
        assertEquals(1024, config.getCsvQueueCapacity());
        assertEquals(50, config.getCsvMaxFileMb());
        assertEquals(3, config.getSwitchJudgeGroups());
        assertFalse(config.isSwitchShowDash());
    }

    public void testLegacyConstructorDelegatesToFull() {
        // 旧版 12 参数构造函数应自动使用默认的 maxStep 和 displayJitter 和 displayDecimals 值
        PersistentPeakConfig config = new PersistentPeakConfig(
                80, 0, 15, 10, 1, 0.15, 0.15, 0.20, 3.0, 1000, 1024, 50
        );
        assertEquals(0.3, config.getMaxStepRiseDb(), 0.0001);
        assertEquals(0.3, config.getMaxStepFallDb(), 0.0001);
        assertEquals(0.0, config.getDisplayJitterDb(), 0.0001);
        assertEquals(1, config.getDisplayDecimals());
        assertEquals(3, config.getSwitchJudgeGroups());
        assertFalse(config.isSwitchShowDash());
    }

    public void testPropertiesSerialization() throws Exception {
        PersistentPeakConfig config = new PersistentPeakConfig();
        Properties prop = config.toProperties();
        assertEquals("80", prop.getProperty("group.cycles"));
        assertEquals("3", prop.getProperty("initial.groups"));
        assertEquals("10", prop.getProperty("average.groups"));
        assertEquals("0.15", prop.getProperty("alpha.rise"));
        assertEquals("0.15", prop.getProperty("alpha.fall"));
        assertEquals("0.3", prop.getProperty("max.step.rise.db"));
        assertEquals("0.3", prop.getProperty("max.step.fall.db"));
        assertEquals("0.3", prop.getProperty("display.max.step.db"));
        assertEquals("0.2", prop.getProperty("display.epsilon.db"));
        assertEquals("0.0", prop.getProperty("display.jitter.db"));
        assertEquals("1", prop.getProperty("display.decimals"));
        assertEquals("3", prop.getProperty("switch.judge.groups"));
        assertEquals("false", prop.getProperty("switch.show.dash"));

        File tmpFile = File.createTempFile("peak-cfg-test", ".properties");
        tmpFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tmpFile)) {
            prop.store(out, "test");
        }

        PersistentPeakConfig loaded = PersistentPeakConfig.loadFromFile(tmpFile);
        assertEquals(config.getGroupCycles(), loaded.getGroupCycles());
        assertEquals(config.getInitialGroups(), loaded.getInitialGroups());
        assertEquals(config.getAverageGroups(), loaded.getAverageGroups());
        assertEquals(config.getAlphaRise(), loaded.getAlphaRise(), 0.0001);
        assertEquals(config.getMaxStepRiseDb(), loaded.getMaxStepRiseDb(), 0.0001);
        assertEquals(config.getMaxStepFallDb(), loaded.getMaxStepFallDb(), 0.0001);
        assertEquals(config.getDisplayMaxStepDb(), loaded.getDisplayMaxStepDb(), 0.0001);
        assertEquals(config.getDisplayEpsilonDb(), loaded.getDisplayEpsilonDb(), 0.0001);
        assertEquals(config.getDisplayJitterDb(), loaded.getDisplayJitterDb(), 0.0001);
        assertEquals(config.getDisplayDecimals(), loaded.getDisplayDecimals());
        assertEquals(config.getSwitchJudgeGroups(), loaded.getSwitchJudgeGroups());
        assertEquals(config.isSwitchShowDash(), loaded.isSwitchShowDash());
    }
}
