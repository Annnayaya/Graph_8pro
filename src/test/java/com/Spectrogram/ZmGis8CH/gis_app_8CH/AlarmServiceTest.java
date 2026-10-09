package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import junit.framework.TestCase;
import java.util.List;

public class AlarmServiceTest extends TestCase {

    private AlarmService alarmService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LicenseManager.setMockLicensed(true);
        alarmService = AlarmService.getInstance();
        alarmService.clearHistory();
        alarmService.resetState();
        AppGlobalConfig.setAlarmEnabled(true);
    }

    @Override
    protected void tearDown() throws Exception {
        alarmService.resetState();
        AppGlobalConfig.setAlarmEnabled(false);
        LicenseManager.setMockLicensed(null);
        super.tearDown();
    }

    public void testAlarmRecordCsvSerialization() {
        long now = System.currentTimeMillis();
        AlarmRecord record = new AlarmRecord(1L, now, 0, "CH1", 23.0, 10.0, "触发报警");
        String csv = record.toCsvRow();
        assertNotNull(csv);
        assertTrue(csv.contains("23"));
        assertTrue(csv.contains("10"));
        assertTrue(csv.contains("触发报警"));

        AlarmRecord parsed = AlarmRecord.fromCsvRow(csv);
        assertNotNull(parsed);
        assertEquals(0, parsed.getChannelIndex());
        assertEquals(23.0, parsed.getAmplitudeDbm(), 0.01);
        assertEquals(10.0, parsed.getThresholdDbm(), 0.01);
        assertEquals("触发报警", parsed.getStatus());
    }

    public void testThresholdComparisonAndStateTransition() {
        // 配置通道 0 阈值为 10.0 dBm
        AppGlobalConfig.setAlarmThresholdChannel(0, 10.0);
        assertEquals(10.0, AppGlobalConfig.getAlarmThresholdChannel(0), 0.001);

        // 正常值（低于阈值），不应报警
        boolean alarmed = alarmService.checkChannelAlarm(0, 8.5);
        assertFalse(alarmed);
        assertFalse(alarmService.isChannelAlarming(0));

        // 超限值（高于阈值），触发报警
        alarmed = alarmService.checkChannelAlarm(0, 15.2);
        assertTrue(alarmed);
        assertTrue(alarmService.isChannelAlarming(0));

        // 持续超限
        alarmed = alarmService.checkChannelAlarm(0, 18.0);
        assertTrue(alarmed);
        assertTrue(alarmService.isChannelAlarming(0));

        // 恢复正常（低于阈值）
        alarmed = alarmService.checkChannelAlarm(0, 7.0);
        assertFalse(alarmed);
        assertFalse(alarmService.isChannelAlarming(0));
    }

    public void testAlarmDisabledBypassesChecking() {
        AppGlobalConfig.setAlarmEnabled(false);
        AppGlobalConfig.setAlarmThresholdChannel(1, 0.0);

        // 即使数值极大，关闭报警时不应触发
        boolean alarmed = alarmService.checkChannelAlarm(1, 99.0);
        assertFalse(alarmed);
        assertFalse(alarmService.isChannelAlarming(1));
    }

    public void testHistoryRecordsStorageAndClear() {
        alarmService.clearHistory();
        AppGlobalConfig.setAlarmThresholdChannel(3, 5.0);

        // 触发报警
        alarmService.checkChannelAlarm(3, 12.0);

        List<AlarmRecord> history = alarmService.getHistoryRecords();
        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertEquals(3, history.get(history.size() - 1).getChannelIndex());

        // 清空历史记录
        alarmService.clearHistory();
        history = alarmService.getHistoryRecords();
        assertTrue(history.isEmpty());
    }
}
