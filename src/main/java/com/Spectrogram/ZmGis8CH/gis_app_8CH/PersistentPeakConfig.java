package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 持久峰值平滑参数配置类
 */
public final class PersistentPeakConfig {
    private static final Logger LOGGER = Logger.getLogger(PersistentPeakConfig.class.getName());

    public static final int DEFAULT_GROUP_CYCLES = 80;
    public static final int DEFAULT_WARMUP_GROUPS = 0;
    public static final int DEFAULT_INITIAL_GROUPS = 3;
    public static final int DEFAULT_AVERAGE_GROUPS = 10;
    public static final int DEFAULT_TRIM_COUNT = 1;
    public static final double DEFAULT_ALPHA_RISE = 0.15;
    public static final double DEFAULT_ALPHA_FALL = 0.15;
    public static final double DEFAULT_MAX_STEP_RISE_DB = 0.3;
    public static final double DEFAULT_MAX_STEP_FALL_DB = 0.3;
    public static final double DEFAULT_DISPLAY_EPSILON_DB = 0.20;
    public static final double DEFAULT_DISPLAY_JITTER_DB = 0.0;
    public static final int DEFAULT_DISPLAY_DECIMALS = 1;
    public static final double DEFAULT_SIGNIFICANT_CHANGE_DB = 3.0;
    public static final int DEFAULT_CSV_FLUSH_INTERVAL_MS = 1000;
    public static final int DEFAULT_CSV_QUEUE_CAPACITY = 1024;
    public static final int DEFAULT_CSV_MAX_FILE_MB = 50;
    public static final int DEFAULT_SWITCH_JUDGE_GROUPS = 3;
    public static final boolean DEFAULT_SWITCH_SHOW_DASH = false;

    private final int groupCycles;
    private final int warmupGroups;
    private final int initialGroups;
    private final int averageGroups;
    private final int trimCount;
    private final double alphaRise;
    private final double alphaFall;
    private final double maxStepRiseDb;
    private final double maxStepFallDb;
    private final double displayEpsilonDb;
    private final double displayJitterDb;
    private final int displayDecimals;
    private final double significantChangeDb;
    private final int csvFlushIntervalMs;
    private final int csvQueueCapacity;
    private final int csvMaxFileMb;
    private final int switchJudgeGroups;
    private final boolean switchShowDash;

    public PersistentPeakConfig() {
        this(DEFAULT_GROUP_CYCLES, DEFAULT_WARMUP_GROUPS, DEFAULT_INITIAL_GROUPS,
                DEFAULT_AVERAGE_GROUPS, DEFAULT_TRIM_COUNT, DEFAULT_ALPHA_RISE,
                DEFAULT_ALPHA_FALL, DEFAULT_MAX_STEP_RISE_DB, DEFAULT_MAX_STEP_FALL_DB,
                DEFAULT_DISPLAY_EPSILON_DB, DEFAULT_DISPLAY_JITTER_DB, DEFAULT_DISPLAY_DECIMALS,
                DEFAULT_SIGNIFICANT_CHANGE_DB, DEFAULT_CSV_FLUSH_INTERVAL_MS,
                DEFAULT_CSV_QUEUE_CAPACITY, DEFAULT_CSV_MAX_FILE_MB);
    }

    public PersistentPeakConfig(int groupCycles, int warmupGroups, int initialGroups,
                                int averageGroups, int trimCount, double alphaRise,
                                double alphaFall, double displayEpsilonDb, double significantChangeDb,
                                int csvFlushIntervalMs, int csvQueueCapacity, int csvMaxFileMb) {
        this(groupCycles, warmupGroups, initialGroups, averageGroups, trimCount,
                alphaRise, alphaFall, DEFAULT_MAX_STEP_RISE_DB, DEFAULT_MAX_STEP_FALL_DB,
                displayEpsilonDb, DEFAULT_DISPLAY_JITTER_DB, DEFAULT_DISPLAY_DECIMALS,
                significantChangeDb, csvFlushIntervalMs, csvQueueCapacity, csvMaxFileMb);
    }

    public PersistentPeakConfig(int groupCycles, int warmupGroups, int initialGroups,
                                int averageGroups, int trimCount, double alphaRise,
                                double alphaFall, double maxStepRiseDb, double maxStepFallDb,
                                double displayEpsilonDb, double significantChangeDb,
                                int csvFlushIntervalMs, int csvQueueCapacity, int csvMaxFileMb) {
        this(groupCycles, warmupGroups, initialGroups, averageGroups, trimCount,
                alphaRise, alphaFall, maxStepRiseDb, maxStepFallDb,
                displayEpsilonDb, DEFAULT_DISPLAY_JITTER_DB, DEFAULT_DISPLAY_DECIMALS,
                significantChangeDb, csvFlushIntervalMs, csvQueueCapacity, csvMaxFileMb);
    }

    public PersistentPeakConfig(int groupCycles, int warmupGroups, int initialGroups,
                                int averageGroups, int trimCount, double alphaRise,
                                double alphaFall, double maxStepRiseDb, double maxStepFallDb,
                                double displayEpsilonDb, double displayJitterDb, double significantChangeDb,
                                int csvFlushIntervalMs, int csvQueueCapacity, int csvMaxFileMb) {
        this(groupCycles, warmupGroups, initialGroups, averageGroups, trimCount,
                alphaRise, alphaFall, maxStepRiseDb, maxStepFallDb,
                displayEpsilonDb, displayJitterDb, DEFAULT_DISPLAY_DECIMALS,
                significantChangeDb, csvFlushIntervalMs, csvQueueCapacity, csvMaxFileMb);
    }

    public PersistentPeakConfig(int groupCycles, int warmupGroups, int initialGroups,
                                int averageGroups, int trimCount, double alphaRise,
                                double alphaFall, double maxStepRiseDb, double maxStepFallDb,
                                double displayEpsilonDb, double displayJitterDb, int displayDecimals,
                                double significantChangeDb, int csvFlushIntervalMs,
                                int csvQueueCapacity, int csvMaxFileMb) {
        this(groupCycles, warmupGroups, initialGroups, averageGroups, trimCount,
                alphaRise, alphaFall, maxStepRiseDb, maxStepFallDb,
                displayEpsilonDb, displayJitterDb, displayDecimals,
                significantChangeDb, csvFlushIntervalMs, csvQueueCapacity, csvMaxFileMb,
                DEFAULT_SWITCH_JUDGE_GROUPS, DEFAULT_SWITCH_SHOW_DASH);
    }

    public PersistentPeakConfig(int groupCycles, int warmupGroups, int initialGroups,
                                int averageGroups, int trimCount, double alphaRise,
                                double alphaFall, double maxStepRiseDb, double maxStepFallDb,
                                double displayEpsilonDb, double displayJitterDb, int displayDecimals,
                                double significantChangeDb, int csvFlushIntervalMs,
                                int csvQueueCapacity, int csvMaxFileMb,
                                int switchJudgeGroups, boolean switchShowDash) {
        // 校验与回退
        if (groupCycles < 1) {
            LOGGER.warning("Invalid group.cycles: " + groupCycles + ", fallback to " + DEFAULT_GROUP_CYCLES);
            groupCycles = DEFAULT_GROUP_CYCLES;
        }
        if (warmupGroups < 0) {
            LOGGER.warning("Invalid warmup.groups: " + warmupGroups + ", fallback to " + DEFAULT_WARMUP_GROUPS);
            warmupGroups = DEFAULT_WARMUP_GROUPS;
        }
        if (initialGroups < 1) {
            LOGGER.warning("Invalid initial.groups: " + initialGroups + ", fallback to " + DEFAULT_INITIAL_GROUPS);
            initialGroups = DEFAULT_INITIAL_GROUPS;
        }
        if (averageGroups < 1) {
            LOGGER.warning("Invalid average.groups: " + averageGroups + ", fallback to " + DEFAULT_AVERAGE_GROUPS);
            averageGroups = DEFAULT_AVERAGE_GROUPS;
        }
        if (trimCount < 0 || (initialGroups > 2 && 2 * trimCount >= initialGroups) || (averageGroups > 2 && 2 * trimCount >= averageGroups)) {
            LOGGER.warning("Invalid trim.count: " + trimCount + ", fallback to " + DEFAULT_TRIM_COUNT);
            trimCount = DEFAULT_TRIM_COUNT;
        }
        if (alphaRise <= 0 || alphaRise > 1.0) {
            LOGGER.warning("Invalid alpha.rise: " + alphaRise + ", fallback to " + DEFAULT_ALPHA_RISE);
            alphaRise = DEFAULT_ALPHA_RISE;
        }
        if (alphaFall <= 0 || alphaFall > 1.0 || alphaFall > alphaRise) {
            LOGGER.warning("Invalid alpha.fall: " + alphaFall + ", fallback to " + Math.min(alphaRise, DEFAULT_ALPHA_FALL));
            alphaFall = Math.min(alphaRise, DEFAULT_ALPHA_FALL);
        }
        if (maxStepRiseDb <= 0) {
            LOGGER.warning("Invalid max.step.rise.db: " + maxStepRiseDb + ", fallback to " + DEFAULT_MAX_STEP_RISE_DB);
            maxStepRiseDb = DEFAULT_MAX_STEP_RISE_DB;
        }
        if (maxStepFallDb <= 0) {
            LOGGER.warning("Invalid max.step.fall.db: " + maxStepFallDb + ", fallback to " + DEFAULT_MAX_STEP_FALL_DB);
            maxStepFallDb = DEFAULT_MAX_STEP_FALL_DB;
        }
        if (displayEpsilonDb < 0 || displayEpsilonDb > 50.0) {
            LOGGER.warning("Invalid display.epsilon.db: " + displayEpsilonDb + ", fallback to " + DEFAULT_DISPLAY_EPSILON_DB);
            displayEpsilonDb = DEFAULT_DISPLAY_EPSILON_DB;
        }
        if (displayJitterDb < 0 || displayJitterDb > 50.0) {
            LOGGER.warning("Invalid display.jitter.db: " + displayJitterDb + ", fallback to " + DEFAULT_DISPLAY_JITTER_DB);
            displayJitterDb = DEFAULT_DISPLAY_JITTER_DB;
        }
        if (displayDecimals < 0 || displayDecimals > 6) {
            LOGGER.warning("Invalid display.decimals: " + displayDecimals + ", fallback to " + DEFAULT_DISPLAY_DECIMALS);
            displayDecimals = DEFAULT_DISPLAY_DECIMALS;
        }
        if (significantChangeDb <= 0) {
            LOGGER.warning("Invalid significant.change.db: " + significantChangeDb + ", fallback to " + DEFAULT_SIGNIFICANT_CHANGE_DB);
            significantChangeDb = DEFAULT_SIGNIFICANT_CHANGE_DB;
        }
        if (csvFlushIntervalMs < 100) {
            LOGGER.warning("Invalid csv.flush.interval.ms: " + csvFlushIntervalMs + ", fallback to " + DEFAULT_CSV_FLUSH_INTERVAL_MS);
            csvFlushIntervalMs = DEFAULT_CSV_FLUSH_INTERVAL_MS;
        }
        if (csvQueueCapacity < 16) {
            LOGGER.warning("Invalid csv.queue.capacity: " + csvQueueCapacity + ", fallback to " + DEFAULT_CSV_QUEUE_CAPACITY);
            csvQueueCapacity = DEFAULT_CSV_QUEUE_CAPACITY;
        }
        if (csvMaxFileMb < 1) {
            LOGGER.warning("Invalid csv.max.file.mb: " + csvMaxFileMb + ", fallback to " + DEFAULT_CSV_MAX_FILE_MB);
            csvMaxFileMb = DEFAULT_CSV_MAX_FILE_MB;
        }
        if (switchJudgeGroups < 1) {
            LOGGER.warning("Invalid switch.judge.groups: " + switchJudgeGroups + ", fallback to " + DEFAULT_SWITCH_JUDGE_GROUPS);
            switchJudgeGroups = DEFAULT_SWITCH_JUDGE_GROUPS;
        }

        this.groupCycles = groupCycles;
        this.warmupGroups = warmupGroups;
        this.initialGroups = initialGroups;
        this.averageGroups = averageGroups;
        this.trimCount = trimCount;
        this.alphaRise = alphaRise;
        this.alphaFall = alphaFall;
        this.maxStepRiseDb = maxStepRiseDb;
        this.maxStepFallDb = maxStepFallDb;
        this.displayEpsilonDb = displayEpsilonDb;
        this.displayJitterDb = displayJitterDb;
        this.displayDecimals = displayDecimals;
        this.significantChangeDb = significantChangeDb;
        this.csvFlushIntervalMs = csvFlushIntervalMs;
        this.csvQueueCapacity = csvQueueCapacity;
        this.csvMaxFileMb = csvMaxFileMb;
        this.switchJudgeGroups = switchJudgeGroups;
        this.switchShowDash = switchShowDash;
    }

    public static PersistentPeakConfig loadOrDefault() {
        AppGlobalConfig.loadConfig();
        Properties merged = new Properties();
        File configFile = getDefaultConfigFile();
        if (configFile.exists() && configFile.isFile()) {
            try (InputStream in = new FileInputStream(configFile)) {
                merged.load(in);
            } catch (Exception ignored) {
            }
        }
        // 读取 lib/.config (或根目录 .config) 并最高优先级覆盖
        File dotConfig = AppGlobalConfig.getConfigFile();
        if (dotConfig != null && dotConfig.exists() && dotConfig.isFile()) {
            try {
                byte[] bytes = java.nio.file.Files.readAllBytes(dotConfig.toPath());
                String raw = new String(bytes, StandardCharsets.UTF_8);
                String plain = AppGlobalConfig.decryptString(raw);
                Properties dotProp = new Properties();
                try (java.io.StringReader sr = new java.io.StringReader(plain)) {
                    dotProp.load(sr);
                }
                for (String name : dotProp.stringPropertyNames()) {
                    merged.setProperty(name, dotProp.getProperty(name));
                }
            } catch (Exception ignored) {
            }
            merged.setProperty("display.epsilon.db", String.valueOf(AppGlobalConfig.getDisplayEpsilonDb()));
            merged.setProperty("display.jitter.db", String.valueOf(AppGlobalConfig.getDisplayJitterDb()));
            merged.setProperty("display.decimals", String.valueOf(AppGlobalConfig.getDisplayDecimals()));
            merged.setProperty("display.max.step.db", String.valueOf(AppGlobalConfig.getDisplayMaxStepDb()));
            merged.setProperty("max.step.rise.db", String.valueOf(AppGlobalConfig.getDisplayMaxStepDb()));
            merged.setProperty("max.step.fall.db", String.valueOf(AppGlobalConfig.getDisplayMaxStepDb()));
            merged.setProperty("initial.groups", String.valueOf(AppGlobalConfig.getInitialGroups()));
            merged.setProperty("switch.judge.groups", String.valueOf(AppGlobalConfig.getSwitchJudgeGroups()));
            merged.setProperty("switch.show.dash", String.valueOf(AppGlobalConfig.isSwitchShowDash()));
        }
        if (merged.isEmpty()) {
            return new PersistentPeakConfig();
        }
        return fromProperties(merged);
    }

    public static File getDefaultConfigFile() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.trim().isEmpty()) {
            return new File(localAppData, "GraphApp8Pro" + File.separator + "persistent-peak" + File.separator + "persistent-peak.properties");
        }
        String userHome = System.getProperty("user.home", ".");
        return new File(userHome, ".graphapp8pro" + File.separator + "persistent-peak" + File.separator + "persistent-peak.properties");
    }

    public static PersistentPeakConfig loadFromFile(File file) {
        Properties prop = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            prop.load(in);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to read properties from " + file.getAbsolutePath() + ", using default.", e);
            return new PersistentPeakConfig();
        }
        return fromProperties(prop);
    }

    public static PersistentPeakConfig fromProperties(Properties prop) {
        int groupCycles = getIntProperty(prop, "group.cycles", DEFAULT_GROUP_CYCLES);
        int warmupGroups = getIntProperty(prop, "warmup.groups", DEFAULT_WARMUP_GROUPS);
        int initialGroups = getIntProperty(prop, "initial.groups", AppGlobalConfig.getInitialGroups());
        int averageGroups = getIntProperty(prop, "average.groups", DEFAULT_AVERAGE_GROUPS);
        int trimCount = getIntProperty(prop, "trim.count", DEFAULT_TRIM_COUNT);
        double alphaRise = getDoubleProperty(prop, "alpha.rise", DEFAULT_ALPHA_RISE);
        double alphaFall = getDoubleProperty(prop, "alpha.fall", DEFAULT_ALPHA_FALL);
        double defaultMaxStep = AppGlobalConfig.getDisplayMaxStepDb();
        double maxStepRiseDb = getDoubleProperty(prop, "display.max.step.db",
                getDoubleProperty(prop, "max.step.rise.db", defaultMaxStep));
        double maxStepFallDb = getDoubleProperty(prop, "display.max.step.db",
                getDoubleProperty(prop, "max.step.fall.db", defaultMaxStep));
        double displayEpsilonDb = getDoubleProperty(prop, "display.epsilon.db", AppGlobalConfig.getDisplayEpsilonDb());
        double displayJitterDb = getDoubleProperty(prop, "display.jitter.db", AppGlobalConfig.getDisplayJitterDb());
        int displayDecimals = getIntProperty(prop, "display.decimals", AppGlobalConfig.getDisplayDecimals());
        if (displayDecimals < 0 || displayDecimals > 6) {
            displayDecimals = AppGlobalConfig.getDisplayDecimals();
        }
        double significantChangeDb = getDoubleProperty(prop, "significant.change.db", DEFAULT_SIGNIFICANT_CHANGE_DB);
        int csvFlushIntervalMs = getIntProperty(prop, "csv.flush.interval.ms", DEFAULT_CSV_FLUSH_INTERVAL_MS);
        int csvQueueCapacity = getIntProperty(prop, "csv.queue.capacity", DEFAULT_CSV_QUEUE_CAPACITY);
        int csvMaxFileMb = getIntProperty(prop, "csv.max.file.mb", DEFAULT_CSV_MAX_FILE_MB);
        int switchJudgeGroups = getIntProperty(prop, "switch.judge.groups", AppGlobalConfig.getSwitchJudgeGroups());
        boolean switchShowDash = getBooleanProperty(prop, "switch.show.dash", AppGlobalConfig.isSwitchShowDash());

        return new PersistentPeakConfig(groupCycles, warmupGroups, initialGroups, averageGroups,
                trimCount, alphaRise, alphaFall, maxStepRiseDb, maxStepFallDb,
                displayEpsilonDb, displayJitterDb, displayDecimals, significantChangeDb,
                csvFlushIntervalMs, csvQueueCapacity, csvMaxFileMb,
                switchJudgeGroups, switchShowDash);
    }

    private static int getIntProperty(Properties prop, String key, int defaultValue) {
        String val = prop.getProperty(key);
        if (val != null) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static double getDoubleProperty(Properties prop, String key, double defaultValue) {
        String val = prop.getProperty(key);
        if (val != null) {
            try {
                return Double.parseDouble(val.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static boolean getBooleanProperty(Properties prop, String key, boolean defaultValue) {
        String val = prop.getProperty(key);
        if (val != null && !val.trim().isEmpty()) {
            val = val.trim().toLowerCase();
            return "true".equals(val) || "1".equals(val) || "yes".equals(val) || "on".equals(val);
        }
        return defaultValue;
    }

    public Properties toProperties() {
        Properties prop = new Properties();
        prop.setProperty("group.cycles", String.valueOf(groupCycles));
        prop.setProperty("warmup.groups", String.valueOf(warmupGroups));
        prop.setProperty("initial.groups", String.valueOf(initialGroups));
        prop.setProperty("average.groups", String.valueOf(averageGroups));
        prop.setProperty("trim.count", String.valueOf(trimCount));
        prop.setProperty("alpha.rise", String.valueOf(alphaRise));
        prop.setProperty("alpha.fall", String.valueOf(alphaFall));
        prop.setProperty("max.step.rise.db", String.valueOf(maxStepRiseDb));
        prop.setProperty("max.step.fall.db", String.valueOf(maxStepFallDb));
        prop.setProperty("display.max.step.db", String.valueOf(maxStepRiseDb));
        prop.setProperty("display.epsilon.db", String.valueOf(displayEpsilonDb));
        prop.setProperty("display.jitter.db", String.valueOf(displayJitterDb));
        prop.setProperty("display.decimals", String.valueOf(displayDecimals));
        prop.setProperty("significant.change.db", String.valueOf(significantChangeDb));
        prop.setProperty("csv.flush.interval.ms", String.valueOf(csvFlushIntervalMs));
        prop.setProperty("csv.queue.capacity", String.valueOf(csvQueueCapacity));
        prop.setProperty("csv.max.file.mb", String.valueOf(csvMaxFileMb));
        prop.setProperty("switch.judge.groups", String.valueOf(switchJudgeGroups));
        prop.setProperty("switch.show.dash", String.valueOf(switchShowDash));
        return prop;
    }

    public int getGroupCycles() { return groupCycles; }
    public int getWarmupGroups() { return warmupGroups; }
    public int getInitialGroups() { return initialGroups; }
    public int getAverageGroups() { return averageGroups; }
    public int getTrimCount() { return trimCount; }
    public double getAlphaRise() { return alphaRise; }
    public double getAlphaFall() { return alphaFall; }
    public double getMaxStepRiseDb() { return maxStepRiseDb; }
    public double getMaxStepFallDb() { return maxStepFallDb; }
    public double getDisplayMaxStepDb() { return maxStepRiseDb; }
    public double getMaxStepDb() { return maxStepRiseDb; }
    public double getDisplayEpsilonDb() { return displayEpsilonDb; }
    public double getDisplayJitterDb() { return displayJitterDb; }
    public int getDisplayDecimals() { return displayDecimals; }
    public double getSignificantChangeDb() { return significantChangeDb; }
    public int getCsvFlushIntervalMs() { return csvFlushIntervalMs; }
    public int getCsvQueueCapacity() { return csvQueueCapacity; }
    public int getCsvMaxFileMb() { return csvMaxFileMb; }
    public int getSwitchJudgeGroups() { return switchJudgeGroups; }
    public boolean isSwitchShowDash() { return switchShowDash; }
}
