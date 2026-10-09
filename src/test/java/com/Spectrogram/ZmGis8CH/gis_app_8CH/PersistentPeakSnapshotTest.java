package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import junit.framework.TestCase;

public class PersistentPeakSnapshotTest extends TestCase {

    public void testSnapshotImmutabilityAndRounding() {
        double[] raw = new double[]{-34.24, -35.16, -42.61, -41.88, -38.04, -37.55, -50.22, -49.81};
        boolean[] valid = new boolean[]{true, true, true, true, true, true, true, true};

        PersistentPeakSnapshot snapshot = new PersistentPeakSnapshot(
                1, 100000L, 5000L, PeakSessionState.RUNNING, raw, valid, "TEST_EVENT"
        );

        assertEquals(1, snapshot.getSequence());
        assertEquals(100000L, snapshot.getTimestampMillis());
        assertEquals(5000L, snapshot.getElapsedMillis());
        assertEquals(5.0, snapshot.getElapsedSeconds(), 0.0001);
        assertEquals(PeakSessionState.RUNNING, snapshot.getSessionState());
        assertEquals("TEST_EVENT", snapshot.getEvent());

        // 验证 1 位小数舍入结果
        assertEquals(-34.2, snapshot.getPublishedDbm(0), 0.0001);
        assertEquals(-35.2, snapshot.getPublishedDbm(1), 0.0001);
        assertEquals(-42.6, snapshot.getPublishedDbm(2), 0.0001);
        assertEquals(-41.9, snapshot.getPublishedDbm(3), 0.0001);

        // 验证不可变防御性拷贝
        double[] copy = snapshot.getPublishedDbmCopy();
        copy[0] = 999.0;
        assertEquals(-34.2, snapshot.getPublishedDbm(0), 0.0001);
    }

    public void testInvalidChannels() {
        double[] raw = new double[]{-30.0, -30.0, -30.0, -30.0, -30.0, -30.0, -30.0, -30.0};
        boolean[] valid = new boolean[]{true, false, true, false, true, false, true, false};

        PersistentPeakSnapshot snapshot = new PersistentPeakSnapshot(
                2, 200000L, 10000L, PeakSessionState.INITIALIZING, raw, valid, ""
        );

        assertTrue(snapshot.isValid(0));
        assertFalse(snapshot.isValid(1));
        assertEquals(-30.0, snapshot.getPublishedDbm(0), 0.0001);
        assertTrue(Double.isNaN(snapshot.getPublishedDbm(1)));
    }
}
