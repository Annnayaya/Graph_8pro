package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 幅值报警数据记录实体
 */
public class AlarmRecord {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    private long id;
    private long timestamp;
    private String timeStr;
    private int channelIndex; // 0..7
    private String channelName;
    private double amplitudeDbm;
    private double thresholdDbm;
    private double overDbm;
    private String alarmType;
    private String status; // 触发报警, 持续报警, 报警恢复

    public AlarmRecord() {
    }

    public AlarmRecord(long id, long timestamp, int channelIndex, String channelName,
                       double amplitudeDbm, double thresholdDbm, String status) {
        this.id = id;
        this.timestamp = timestamp;
        synchronized (SDF) {
            this.timeStr = SDF.format(new Date(timestamp));
        }
        this.channelIndex = channelIndex;
        this.channelName = channelName;
        this.amplitudeDbm = amplitudeDbm;
        this.thresholdDbm = thresholdDbm;
        this.overDbm = amplitudeDbm - thresholdDbm;
        this.alarmType = "幅值超限";
        this.status = status;
    }

    public static String getCsvHeader() {
        return "ID,时间,通道,通道名称,实测幅值(dBm),报警阈值(dBm),超标幅值(dBm),报警类型,状态";
    }

    public String toCsvRow() {
        return String.format(Locale.ROOT, "%d,%s,CH%d,%s,%d,%d,%d,%s,%s",
                id,
                timeStr,
                channelIndex + 1,
                channelName != null ? channelName.replace(",", " ") : ("CH" + (channelIndex + 1)),
                Math.round(amplitudeDbm),
                Math.round(thresholdDbm),
                Math.round(overDbm),
                alarmType,
                status);
    }

    public static AlarmRecord fromCsvRow(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("ID,")) {
            return null;
        }
        String[] parts = line.split(",");
        if (parts.length < 9) {
            return null;
        }
        try {
            AlarmRecord r = new AlarmRecord();
            r.id = Long.parseLong(parts[0].trim());
            r.timeStr = parts[1].trim();
            try {
                synchronized (SDF) {
                    r.timestamp = SDF.parse(r.timeStr).getTime();
                }
            } catch (Exception ignored) {
                r.timestamp = System.currentTimeMillis();
            }
            String chStr = parts[2].trim().toUpperCase(Locale.ROOT).replace("CH", "");
            r.channelIndex = Integer.parseInt(chStr) - 1;
            r.channelName = parts[3].trim();
            r.amplitudeDbm = Double.parseDouble(parts[4].trim());
            r.thresholdDbm = Double.parseDouble(parts[5].trim());
            r.overDbm = Double.parseDouble(parts[6].trim());
            r.alarmType = parts[7].trim();
            r.status = parts[8].trim();
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public int getChannelIndex() {
        return channelIndex;
    }

    public void setChannelIndex(int channelIndex) {
        this.channelIndex = channelIndex;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public double getAmplitudeDbm() {
        return amplitudeDbm;
    }

    public void setAmplitudeDbm(double amplitudeDbm) {
        this.amplitudeDbm = amplitudeDbm;
    }

    public double getThresholdDbm() {
        return thresholdDbm;
    }

    public void setThresholdDbm(double thresholdDbm) {
        this.thresholdDbm = thresholdDbm;
    }

    public double getOverDbm() {
        return overDbm;
    }

    public void setOverDbm(double overDbm) {
        this.overDbm = overDbm;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
