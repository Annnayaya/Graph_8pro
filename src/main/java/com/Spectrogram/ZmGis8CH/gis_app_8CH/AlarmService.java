package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * 幅值报警核心服务类
 * 负责实时报警判定、数据落盘存储 (CSV)、通道红光交替闪烁驱动及状态事件分发
 */
public class AlarmService {
    private static final String ALARM_DIR = "alarm";
    private static final String ALARM_FILE = ALARM_DIR + File.separator + "amplitude_alarms.csv";
    private static final int MAX_MEMORY_RECORDS = 2000;
    private static final Border NORMAL_BORDER = BorderFactory.createEtchedBorder();
    private static final Border ALARM_BORDER = BorderFactory.createLineBorder(new Color(220, 38, 38), 3);
    private static final Color ALARM_COLOR = new Color(220, 38, 38);
    private static final Color NORMAL_BTN_BG = new Color(240, 240, 240);

    private static final AlarmService INSTANCE = new AlarmService();

    private final boolean[] channelAlarming = new boolean[8];
    private final AlarmRecord[] activeAlarms = new AlarmRecord[8];
    private final List<AlarmRecord> historyRecords = new ArrayList<>();
    private final AtomicLong recordIdGenerator = new AtomicLong(1);
    private final long[] lastLogTime = new long[8];
    private final double[] lastLoggedAmp = new double[8];

    private final List<AlarmListener> listeners = new CopyOnWriteArrayList<>();

    private JComponent[] registeredPrpsFrames;
    private JComponent[] registeredPrpdFrames;
    private AbstractButton[] registeredChannelButtons;
    private JComponent[] registeredPrpsControlPanels;
    private JComponent[] registeredPrpdControlPanels;
    private JComponent[] registeredChCPanels;
    private GisGraph[] registeredPrpsGraphs;
    private PRPDGraph[] registeredPrpdGraphs;

    private Timer flashTimer;
    private boolean flashState = false;

    public interface AlarmListener {
        void onAlarmStateChanged(boolean[] channelAlarming, AlarmRecord newRecord);
        void onFlashTick(boolean flashOn, boolean[] channelAlarming);
    }

    private AlarmService() {
        initStorage();
        initFlashTimer();
    }

    public static AlarmService getInstance() {
        return INSTANCE;
    }

    private void initStorage() {
        try {
            File dir = new File(ALARM_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(ALARM_FILE);
            // 兼容旧路径 data/alarm/amplitude_alarms.csv 自动平滑迁移
            File oldFile = new File("data" + File.separator + "alarm" + File.separator + "amplitude_alarms.csv");
            if (oldFile.exists() && !file.exists()) {
                try {
                    java.nio.file.Files.copy(oldFile.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ignored) {}
            }
            if (oldFile.exists()) {
                try {
                    oldFile.delete();
                    File oldDir = new File("data" + File.separator + "alarm");
                    oldDir.delete();
                } catch (Exception ignored) {}
            }
            if (!file.exists()) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                    bw.write(AlarmRecord.getCsvHeader());
                    bw.newLine();
                }
            } else {
                loadRecentRecords(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRecentRecords(File file) {
        List<AlarmRecord> loaded = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            long maxId = 0;
            while ((line = br.readLine()) != null) {
                AlarmRecord r = AlarmRecord.fromCsvRow(line);
                if (r != null) {
                    loaded.add(r);
                    if (r.getId() > maxId) {
                        maxId = r.getId();
                    }
                }
            }
            recordIdGenerator.set(maxId + 1);
            int start = Math.max(0, loaded.size() - MAX_MEMORY_RECORDS);
            for (int i = start; i < loaded.size(); i++) {
                historyRecords.add(loaded.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initFlashTimer() {
        flashTimer = new Timer(500, e -> {
            boolean anyAlarm = false;
            for (boolean b : channelAlarming) {
                if (b) {
                    anyAlarm = true;
                    break;
                }
            }
            if (!anyAlarm) {
                flashState = false;
                flashTimer.stop();
                updateVisuals(false);
                notifyFlashTick(false);
                return;
            }

            flashState = !flashState;
            updateVisuals(flashState);
            notifyFlashTick(flashState);
        });
    }

    public void registerUiComponents(JComponent[] prpsFrames, JComponent[] prpdFrames, AbstractButton[] channelButtons) {
        registerUiComponents(prpsFrames, prpdFrames, channelButtons, null, null, null, null, null);
    }

    public void registerUiComponents(
            JComponent[] prpsFrames,
            JComponent[] prpdFrames,
            AbstractButton[] channelButtons,
            JComponent[] prpsControlPanels,
            JComponent[] prpdControlPanels,
            JComponent[] chCPanels,
            GisGraph[] prpsGraphs,
            PRPDGraph[] prpdGraphs) {
        this.registeredPrpsFrames = prpsFrames;
        this.registeredPrpdFrames = prpdFrames;
        this.registeredChannelButtons = channelButtons;
        this.registeredPrpsControlPanels = prpsControlPanels;
        this.registeredPrpdControlPanels = prpdControlPanels;
        this.registeredChCPanels = chCPanels;
        this.registeredPrpsGraphs = prpsGraphs;
        this.registeredPrpdGraphs = prpdGraphs;
    }

    public void addListener(AlarmListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(AlarmListener listener) {
        listeners.remove(listener);
    }

    private void notifyFlashTick(boolean flashOn) {
        for (AlarmListener l : listeners) {
            try {
                l.onFlashTick(flashOn, channelAlarming);
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyAlarmStateChanged(AlarmRecord newRecord) {
        for (AlarmListener l : listeners) {
            try {
                l.onAlarmStateChanged(channelAlarming, newRecord);
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void checkAlarms(double[] currentAmplitudes, String[] channelNames, boolean[] channelEnabled, boolean isAcquiring) {
        if (!AppGlobalConfig.isAlarmEnabled() || !isAcquiring) {
            boolean hadAlarm = false;
            for (int i = 0; i < 8; i++) {
                if (channelAlarming[i]) {
                    hadAlarm = true;
                    channelAlarming[i] = false;
                    activeAlarms[i] = null;
                }
            }
            if (hadAlarm) {
                if (flashTimer.isRunning()) {
                    flashTimer.stop();
                }
                flashState = false;
                SwingUtilities.invokeLater(() -> updateVisuals(false));
                notifyFlashTick(false);
                notifyAlarmStateChanged(null);
            }
            return;
        }

        long now = System.currentTimeMillis();
        boolean stateChanged = false;
        boolean anyAlarm = false;

        for (int ch = 0; ch < 8; ch++) {
            boolean enabled = channelEnabled == null || ch >= channelEnabled.length || channelEnabled[ch];
            double amp = (currentAmplitudes != null && ch < currentAmplitudes.length) ? currentAmplitudes[ch] : Double.NaN;
            double threshold = AppGlobalConfig.getAlarmThresholdChannel(ch);

            String chName = (channelNames != null && ch < channelNames.length && channelNames[ch] != null && !channelNames[ch].isEmpty())
                    ? channelNames[ch]
                    : ("CH" + (ch + 1));

            boolean isOver = enabled && !Double.isNaN(amp) && amp >= threshold;

            if (isOver) {
                anyAlarm = true;
                if (!channelAlarming[ch]) {
                    // 1. 触发新报警
                    channelAlarming[ch] = true;
                    AlarmRecord record = new AlarmRecord(
                            recordIdGenerator.getAndIncrement(),
                            now,
                            ch,
                            chName,
                            amp,
                            threshold,
                            "触发报警"
                    );
                    activeAlarms[ch] = record;
                    lastLogTime[ch] = now;
                    lastLoggedAmp[ch] = amp;
                    saveRecordAsync(record);
                    stateChanged = true;
                    notifyAlarmStateChanged(record);
                } else {
                    // 2. 持续报警中（防抖节流：每隔 3 秒或幅值增加 1dBm 以上时记录一次）
                    if (now - lastLogTime[ch] >= 3000 || (amp - lastLoggedAmp[ch]) >= 1.0) {
                        AlarmRecord record = new AlarmRecord(
                                recordIdGenerator.getAndIncrement(),
                                now,
                                ch,
                                chName,
                                amp,
                                threshold,
                                "持续报警"
                        );
                        activeAlarms[ch] = record;
                        lastLogTime[ch] = now;
                        lastLoggedAmp[ch] = amp;
                        saveRecordAsync(record);
                        stateChanged = true;
                        notifyAlarmStateChanged(record);
                    } else if (activeAlarms[ch] != null) {
                        activeAlarms[ch].setAmplitudeDbm(amp);
                        activeAlarms[ch].setOverDbm(amp - threshold);
                    }
                }
            } else {
                if (channelAlarming[ch]) {
                    // 3. 报警恢复
                    channelAlarming[ch] = false;
                    AlarmRecord record = new AlarmRecord(
                            recordIdGenerator.getAndIncrement(),
                            now,
                            ch,
                            chName,
                            Double.isNaN(amp) ? threshold : amp,
                            threshold,
                            "报警恢复"
                    );
                    activeAlarms[ch] = null;
                    saveRecordAsync(record);
                    stateChanged = true;
                    notifyAlarmStateChanged(record);
                }
            }
        }

        if (anyAlarm && !flashTimer.isRunning()) {
            flashTimer.start();
        } else if (!anyAlarm && flashTimer.isRunning()) {
            flashTimer.stop();
            flashState = false;
            SwingUtilities.invokeLater(() -> updateVisuals(false));
            notifyFlashTick(false);
        }

        if (stateChanged) {
            notifyAlarmStateChanged(null);
        }
    }

    private void updateVisuals(final boolean flashOn) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> updateVisuals(flashOn));
            return;
        }

        Color panelAlarmBg = new Color(220, 38, 38);
        Color panelDefaultBg = UIManager.getColor("Panel.background");
        if (panelDefaultBg == null) {
            panelDefaultBg = new Color(238, 238, 238);
        }

        for (int i = 0; i < 8; i++) {
            boolean alarming = channelAlarming[i];
            boolean doFlash = alarming && flashOn;
            Border borderToSet = doFlash ? ALARM_BORDER : NORMAL_BORDER;

            // 1. 谱图外部边框
            if (registeredPrpsFrames != null && i < registeredPrpsFrames.length && registeredPrpsFrames[i] != null) {
                registeredPrpsFrames[i].setBorder(borderToSet);
            }
            if (registeredPrpdFrames != null && i < registeredPrpdFrames.length && registeredPrpdFrames[i] != null) {
                registeredPrpdFrames[i].setBorder(borderToSet);
            }

            // 2. 顶部通道切换按钮（背景闪烁红色）
            if (registeredChannelButtons != null && i < registeredChannelButtons.length && registeredChannelButtons[i] != null) {
                AbstractButton btn = registeredChannelButtons[i];
                if (btn instanceof JRTogButton) {
                    ((JRTogButton) btn).setAlarmFlashRed(doFlash);
                } else {
                    if (doFlash) {
                        btn.setBackground(ALARM_COLOR);
                        btn.setForeground(Color.WHITE);
                    } else {
                        btn.setBackground(NORMAL_BTN_BG);
                        btn.setForeground(Color.BLACK);
                    }
                }
            }

            // 3. 通道控制面板区域红色底色闪烁（PRPS下方控制条、PRPD下方控制条、底部相位阈值控制面板）
            if (registeredPrpsControlPanels != null && i < registeredPrpsControlPanels.length && registeredPrpsControlPanels[i] != null) {
                setPanelAlarmColor(registeredPrpsControlPanels[i], doFlash, panelAlarmBg, panelDefaultBg);
            }
            if (registeredPrpdControlPanels != null && i < registeredPrpdControlPanels.length && registeredPrpdControlPanels[i] != null) {
                setPanelAlarmColor(registeredPrpdControlPanels[i], doFlash, panelAlarmBg, panelDefaultBg);
            }
            if (registeredChCPanels != null && i < registeredChCPanels.length && registeredChCPanels[i] != null) {
                setPanelAlarmColor(registeredChCPanels[i], doFlash, panelAlarmBg, panelDefaultBg);
            }

            // 4. 谱图放电类型下方显示红色的报警字样
            if (registeredPrpsGraphs != null && i < registeredPrpsGraphs.length && registeredPrpsGraphs[i] != null) {
                registeredPrpsGraphs[i].setAlarming(alarming);
                registeredPrpsGraphs[i].repaint();
            }
            if (registeredPrpdGraphs != null && i < registeredPrpdGraphs.length && registeredPrpdGraphs[i] != null) {
                registeredPrpdGraphs[i].setAlarming(alarming);
                registeredPrpdGraphs[i].repaint();
            }
        }
    }

    private void setPanelAlarmColor(Component comp, boolean doFlash, Color alarmBg, Color defaultBg) {
        if (comp == null) return;
        if (comp instanceof JScrollBar) {
            return;
        }
        if (doFlash) {
            comp.setBackground(alarmBg);
            if (comp instanceof JLabel || comp instanceof JCheckBox || comp instanceof JRadioButton) {
                comp.setForeground(Color.WHITE);
            }
        } else {
            comp.setBackground(defaultBg);
            if (comp instanceof JLabel || comp instanceof JCheckBox || comp instanceof JRadioButton) {
                comp.setForeground(Color.BLACK);
            }
        }
        if (comp instanceof Container) {
            Container container = (Container) comp;
            for (Component child : container.getComponents()) {
                setPanelAlarmColor(child, doFlash, alarmBg, defaultBg);
            }
        }
    }

    private void saveRecordAsync(final AlarmRecord record) {
        if (record == null) return;
        historyRecords.add(record);
        if (historyRecords.size() > MAX_MEMORY_RECORDS) {
            historyRecords.remove(0);
        }
        new Thread(() -> {
            synchronized (ALARM_FILE) {
                try {
                    File file = new File(ALARM_FILE);
                    if (file.getParentFile() != null && !file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                        bw.write(record.toCsvRow());
                        bw.newLine();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "AlarmCsvWriter").start();
    }

    public List<AlarmRecord> getHistoryRecords() {
        return Collections.unmodifiableList(historyRecords);
    }

    public AlarmRecord[] getActiveAlarms() {
        AlarmRecord[] copy = new AlarmRecord[8];
        System.arraycopy(activeAlarms, 0, copy, 0, 8);
        return copy;
    }

    public boolean[] getChannelAlarming() {
        boolean[] copy = new boolean[8];
        System.arraycopy(channelAlarming, 0, copy, 0, 8);
        return copy;
    }

    public boolean hasActiveAlarm() {
        for (boolean b : channelAlarming) {
            if (b) return true;
        }
        return false;
    }

    public boolean isChannelAlarming(int ch) {
        if (ch >= 0 && ch < 8) {
            return channelAlarming[ch];
        }
        return false;
    }

    public synchronized boolean checkChannelAlarm(int ch, double amplitude) {
        if (ch < 0 || ch >= 8) return false;
        double[] amps = new double[8];
        for (int i = 0; i < 8; i++) amps[i] = (i == ch ? amplitude : -999.0);
        boolean[] enabled = new boolean[8];
        enabled[ch] = true;
        checkAlarms(amps, null, enabled, true);
        return channelAlarming[ch];
    }

    public synchronized void clearHistory() {
        historyRecords.clear();
        synchronized (ALARM_FILE) {
            try {
                File file = new File(ALARM_FILE);
                if (file.getParentFile() != null && !file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
                    bw.write(AlarmRecord.getCsvHeader());
                    bw.newLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        notifyAlarmStateChanged(null);
    }

    public synchronized void resetState() {
        for (int i = 0; i < 8; i++) {
            channelAlarming[i] = false;
            activeAlarms[i] = null;
            lastLogTime[i] = 0;
            lastLoggedAmp[i] = Double.NEGATIVE_INFINITY;
        }
        if (flashTimer != null && flashTimer.isRunning()) {
            flashTimer.stop();
        }
        flashState = false;
        SwingUtilities.invokeLater(() -> updateVisuals(false));
        notifyFlashTick(false);
        notifyAlarmStateChanged(null);
    }
}
