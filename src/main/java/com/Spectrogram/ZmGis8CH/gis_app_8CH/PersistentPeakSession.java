package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 持久峰值测试会话上下文
 */
public final class PersistentPeakSession {
    private static final Logger LOGGER = Logger.getLogger(PersistentPeakSession.class.getName());

    private final String sessionId;
    private final long startTimeMillis;
    private final File outputDirectory;
    private final File csvFile;
    private final File htmlFile;
    private final File propertiesFile;
    private final PersistentPeakConfig config;

    public PersistentPeakSession(String sessionId, long startTimeMillis, File outputDirectory,
                                 PersistentPeakConfig config) {
        this.sessionId = sessionId;
        this.startTimeMillis = startTimeMillis;
        this.outputDirectory = outputDirectory;
        this.csvFile = new File(outputDirectory, "peaks.csv");
        this.htmlFile = new File(outputDirectory, "peak-trend.html");
        this.propertiesFile = new File(outputDirectory, "session.properties");
        this.config = (config != null) ? config : new PersistentPeakConfig();
    }

    public static PersistentPeakSession create(PersistentPeakConfig config) {
        long now = System.currentTimeMillis();
        String id = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(now));
        File baseDir = getBaseOutputDirectory();
        File sessionDir = new File(baseDir, id);
        return new PersistentPeakSession(id, now, sessionDir, config);
    }

    public static File getBaseOutputDirectory() {
        return new File("持久测试");
    }

    public boolean initDirectory() {
        try {
            if (!outputDirectory.exists()) {
                if (!outputDirectory.mkdirs()) {
                    LOGGER.warning("Could not create session directory: " + outputDirectory.getAbsolutePath());
                    return false;
                }
            }
            saveSessionProperties();
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to initialize session directory", e);
            return false;
        }
    }

    public void saveSessionProperties() {
        try {
            Properties prop = config.toProperties();
            prop.setProperty("session.id", sessionId);
            prop.setProperty("start.time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date(startTimeMillis)));
            try (OutputStream out = new FileOutputStream(propertiesFile)) {
                prop.store(out, "GraphApp8Pro Persistent Peak Session Parameters");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to write session.properties", e);
        }
    }

    public String getSessionId() { return sessionId; }
    public long getStartTimeMillis() { return startTimeMillis; }
    public File getOutputDirectory() { return outputDirectory; }
    public File getCsvFile() { return csvFile; }
    public File getHtmlFile() { return htmlFile; }
    public File getPropertiesFile() { return propertiesFile; }
    public PersistentPeakConfig getConfig() { return config; }
}
