package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import junit.framework.TestCase;

public class PeakValueFormatterTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AppGlobalConfig.setDisplayDecimals(1);
    }

    @Override
    protected void tearDown() throws Exception {
        AppGlobalConfig.setDisplayDecimals(1);
        super.tearDown();
    }

    public void testRounding() {
        assertEquals(-34.2, PeakValueFormatter.roundForPublish(-34.24), 0.0001);
        assertEquals(-34.3, PeakValueFormatter.roundForPublish(-34.26), 0.0001);
        assertEquals(-34.3, PeakValueFormatter.roundForPublish(-34.25), 0.0001); // -34.25 HALF_UP -> -34.3
        assertEquals(0.0, PeakValueFormatter.roundForPublish(0.01), 0.0001);
        assertTrue(Double.isNaN(PeakValueFormatter.roundForPublish(Double.NaN)));
    }

    public void testZeroDecimalsRoundingAndFormatting() {
        // 测试 0 位小数（无小数点内容）
        AppGlobalConfig.setDisplayDecimals(0);
        assertEquals(-34.0, PeakValueFormatter.roundForPublish(-34.2), 0.0001);
        assertEquals(-35.0, PeakValueFormatter.roundForPublish(-34.6), 0.0001);

        assertEquals("-34", PeakValueFormatter.formatForCsv(-34.2));
        assertEquals("-35", PeakValueFormatter.formatForCsv(-34.6));
        assertEquals("-34", PeakValueFormatter.formatForUi(-34.2));
        assertEquals("-34 dBm", PeakValueFormatter.formatForUiWithUnit(-34.2, "dBm"));
        assertEquals("--", PeakValueFormatter.formatForUi(Double.NaN));
    }

    public void testTwoDecimalsRoundingAndFormatting() {
        AppGlobalConfig.setDisplayDecimals(2);
        assertEquals(-34.26, PeakValueFormatter.roundForPublish(-34.256), 0.0001);
        assertEquals("-34.26", PeakValueFormatter.formatForCsv(-34.256));
        assertEquals("-34.26 dBm", PeakValueFormatter.formatForUiWithUnit(-34.256, "dBm"));
    }

    public void testFormatForCsv() {
        AppGlobalConfig.setDisplayDecimals(1);
        assertEquals("-34.2", PeakValueFormatter.formatForCsv(-34.2));
        assertEquals("0.0", PeakValueFormatter.formatForCsv(0.0));
        assertEquals("", PeakValueFormatter.formatForCsv(Double.NaN));
    }

    public void testFormatForUi() {
        AppGlobalConfig.setDisplayDecimals(1);
        assertEquals("-34.2", PeakValueFormatter.formatForUi(-34.2));
        assertEquals("--", PeakValueFormatter.formatForUi(Double.NaN));
        assertEquals("-34.2 dBm", PeakValueFormatter.formatForUiWithUnit(-34.2, "dBm"));
        assertEquals("--", PeakValueFormatter.formatForUiWithUnit(Double.NaN, "dBm"));
    }
}

