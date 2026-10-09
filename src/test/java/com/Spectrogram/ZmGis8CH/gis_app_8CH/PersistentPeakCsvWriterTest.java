package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import junit.framework.TestCase;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class PersistentPeakCsvWriterTest extends TestCase {

    private File tempDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tempDir = new File(System.getProperty("java.io.tmpdir"), "csv_test_" + System.currentTimeMillis());
        tempDir.mkdirs();
    }

    @Override
    protected void tearDown() throws Exception {
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

    public void testCsvWriterHeaderAndBOM() throws Exception {
        PersistentPeakConfig config = new PersistentPeakConfig();
        PersistentPeakSession session = new PersistentPeakSession("test_csv", System.currentTimeMillis(), tempDir, config);
        session.initDirectory();

        PersistentPeakCsvWriter writer = new PersistentPeakCsvWriter(session);
        writer.start();

        double[] vals = new double[]{-34.2, -35.1, -42.6, -41.8, -38.0, -37.5, -50.2, -49.8};
        boolean[] valids = new boolean[]{true, true, true, true, true, true, true, true};

        PersistentPeakSnapshot snapshot = new PersistentPeakSnapshot(
                1, System.currentTimeMillis(), 1200L, PeakSessionState.RUNNING, vals, valids, "TEST_EVENT"
        );

        writer.offerSnapshot(snapshot);
        writer.close();

        File csv = session.getCsvFile();
        assertTrue(csv.exists());
        assertTrue(csv.length() > 0);

        byte[] allBytes = new byte[(int) csv.length()];
        try (FileInputStream in = new FileInputStream(csv)) {
            in.read(allBytes);
        }

        // 验证 UTF-8 BOM
        assertEquals((byte) 0xEF, allBytes[0]);
        assertEquals((byte) 0xBB, allBytes[1]);
        assertEquals((byte) 0xBF, allBytes[2]);

        String content = new String(allBytes, 3, allBytes.length - 3, StandardCharsets.UTF_8);
        assertTrue(content.startsWith("time,elapsed_seconds,UHF-1_dBm,UHF-2_dBm,AE-1_dBm,AE-2_dBm,HFCT-1_dBm,HFCT-2_dBm,Spare-1_dBm,Spare-2_dBm,event"));
        assertTrue(content.contains("-34.2,-35.1,-42.6,-41.8,-38.0,-37.5,-50.2,-49.8,TEST_EVENT"));
    }
}
