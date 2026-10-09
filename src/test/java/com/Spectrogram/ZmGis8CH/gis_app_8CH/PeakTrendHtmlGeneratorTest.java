package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import junit.framework.TestCase;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class PeakTrendHtmlGeneratorTest extends TestCase {

    private File tempHtmlFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tempHtmlFile = File.createTempFile("peak-trend-test", ".html");
    }

    @Override
    protected void tearDown() throws Exception {
        if (tempHtmlFile != null && tempHtmlFile.exists()) {
            tempHtmlFile.delete();
        }
        super.tearDown();
    }

    public void testGenerateHtml() throws Exception {
        PersistentPeakConfig config = new PersistentPeakConfig();
        boolean generated = PeakTrendHtmlGenerator.generateHtml(tempHtmlFile, config);
        assertTrue(generated);
        assertTrue(tempHtmlFile.exists());
        assertTrue(tempHtmlFile.length() > 0);

        byte[] bytes = new byte[(int) tempHtmlFile.length()];
        try (FileInputStream in = new FileInputStream(tempHtmlFile)) {
            in.read(bytes);
        }
        String htmlContent = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(htmlContent.contains("GraphApp8Pro"));
        assertTrue(htmlContent.contains("peaks.csv"));
    }

    public void testUpdateHtmlWithCsv() throws Exception {
        PersistentPeakConfig config = new PersistentPeakConfig();
        File tempCsvFile = File.createTempFile("test-peaks", ".csv");
        tempCsvFile.deleteOnExit();
        String sampleCsv = "time,elapsed_seconds,UHF-1_dBm,UHF-2_dBm,AE-1_dBm,AE-2_dBm,HFCT-1_dBm,HFCT-2_dBm,Spare-1_dBm,Spare-2_dBm,event\r\n"
                + "2026-08-25 08:00:00.000,0.0,-56.8,-56.8,-56.8,-79.3,-79.3,-79.3,-79.3,-79.3,\r\n";
        java.nio.file.Files.write(tempCsvFile.toPath(), sampleCsv.getBytes(StandardCharsets.UTF_8));

        boolean updated = PeakTrendHtmlGenerator.updateHtmlWithCsv(tempHtmlFile, tempCsvFile, config);
        assertTrue(updated);
        assertTrue(tempHtmlFile.exists());

        byte[] bytes = new byte[(int) tempHtmlFile.length()];
        try (FileInputStream in = new FileInputStream(tempHtmlFile)) {
            in.read(bytes);
        }
        String htmlContent = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(htmlContent.contains("window.__INLINE_CSV__"));
        assertTrue(htmlContent.contains("-56.8"));
    }
}
