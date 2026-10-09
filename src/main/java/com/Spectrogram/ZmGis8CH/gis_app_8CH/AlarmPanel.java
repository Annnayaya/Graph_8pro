package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * 嵌入在主界面 fullPane 中的专属幅值报警页面（位于“数据浏览器”旁边）
 */
public class AlarmPanel extends JPanel implements AlarmService.AlarmListener {
    private static final Font FONT_TITLE = new Font("Microsoft YaHei", Font.BOLD, 15);
    private static final Font FONT_BOLD = new Font("Microsoft YaHei", Font.BOLD, 12);
    private static final Font FONT_REGULAR = new Font("Microsoft YaHei", Font.PLAIN, 12);
    private static final Font FONT_SMALL = new Font("Microsoft YaHei", Font.PLAIN, 11);

    private static final Color COLOR_OK = new Color(22, 163, 74);
    private static final Color COLOR_ALARM = new Color(220, 38, 38);
    private static final Color BG_CARD = new Color(248, 250, 252);
    private static final Color BORDER_CARD = new Color(226, 232, 240);

    private final JLabel systemStatusBadge;
    private final JPanel[] channelCards = new JPanel[8];
    private final JLabel[] cardStatusLabels = new JLabel[8];
    private final JLabel[] cardValueLabels = new JLabel[8];
    private final JLabel[] cardThresholdLabels = new JLabel[8];

    // 活动报警表格
    private DefaultTableModel activeTableModel;
    private JTable activeTable;

    // 历史报警表格
    private DefaultTableModel historyTableModel;
    private JTable historyTable;
    private JComboBox<String> channelFilterCombo;
    private JComboBox<String> statusFilterCombo;

    // 阈值配置输入控件
    private JTextField globalThresholdField;
    private final JTextField[] chThresholdFields = new JTextField[8];

    public static String formatDbm(double dbm) {
        if (Double.isNaN(dbm)) return "--";
        return String.valueOf(Math.round(dbm));
    }

    public static String formatOverDbm(double overDbm) {
        if (Double.isNaN(overDbm)) return "--";
        long val = Math.round(overDbm);
        return (val > 0 ? "+" : "") + val;
    }

    public AlarmPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(8, 10, 8, 10));

        // 1. 顶部状态概览看板
        JPanel topHeader = new JPanel(new BorderLayout(10, 6));
        topHeader.setBackground(Color.WHITE);
        topHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(6, 8, 10, 8)
        ));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);
        JLabel pageTitle = new JLabel("幅值报警监控中心");
        pageTitle.setFont(FONT_TITLE);
        pageTitle.setForeground(new Color(15, 23, 42));
        titleRow.add(pageTitle);

        systemStatusBadge = new JLabel(" 系统状态: 正常运行 · 无活动报警 ");
        systemStatusBadge.setFont(FONT_BOLD);
        systemStatusBadge.setOpaque(true);
        systemStatusBadge.setBackground(new Color(240, 253, 244));
        systemStatusBadge.setForeground(COLOR_OK);
        systemStatusBadge.setBorder(BorderFactory.createLineBorder(new Color(187, 247, 208), 1));
        titleRow.add(systemStatusBadge);
        topHeader.add(titleRow, BorderLayout.NORTH);

        // 8通道微型状态卡片看板
        JPanel cardsGrid = new JPanel(new GridLayout(1, 8, 6, 0));
        cardsGrid.setOpaque(false);
        for (int i = 0; i < 8; i++) {
            channelCards[i] = createChannelCard(i);
            cardsGrid.add(channelCards[i]);
        }
        topHeader.add(cardsGrid, BorderLayout.CENTER);
        add(topHeader, BorderLayout.NORTH);

        // 2. 中间核心功能选项卡
        JTabbedPane centerTabs = new JTabbedPane();
        centerTabs.setFont(FONT_BOLD);

        // Tab 1: 实时活动报警
        centerTabs.addTab(" 实时活动报警 ", createActiveAlarmsPanel());
        // Tab 2: 报警历史数据
        centerTabs.addTab(" 报警历史记录 ", createHistoryAlarmsPanel());
        // Tab 3: 幅值阈值配置
        centerTabs.addTab(" 报警阈值设置 ", createThresholdSettingsPanel());

        add(centerTabs, BorderLayout.CENTER);

        // 注册到 AlarmService
        AlarmService.getInstance().addListener(this);

        // 刷新一次历史数据
        refreshHistoryTable();
    }

    private JPanel createChannelCard(int ch) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CARD, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));

        JLabel chName = new JLabel("CH" + (ch + 1));
        chName.setFont(FONT_BOLD);
        chName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLbl = new JLabel("● 正常");
        statusLbl.setFont(FONT_SMALL);
        statusLbl.setForeground(COLOR_OK);
        statusLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardStatusLabels[ch] = statusLbl;

        JLabel valLbl = new JLabel("-- dBm");
        valLbl.setFont(FONT_BOLD);
        valLbl.setForeground(new Color(30, 41, 59));
        valLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardValueLabels[ch] = valLbl;

        double th = AppGlobalConfig.getAlarmThresholdChannel(ch);
        JLabel thLbl = new JLabel("阈值: " + formatDbm(th) + " dBm");
        thLbl.setFont(FONT_SMALL);
        thLbl.setForeground(new Color(100, 116, 139));
        thLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardThresholdLabels[ch] = thLbl;

        card.add(chName);
        card.add(Box.createVerticalStrut(2));
        card.add(statusLbl);
        card.add(Box.createVerticalStrut(2));
        card.add(valLbl);
        card.add(Box.createVerticalStrut(2));
        card.add(thLbl);

        return card;
    }

    private JPanel createActiveAlarmsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] cols = {"通道", "通道名称", "实时幅值(dBm)", "报警阈值(dBm)", "超标差值(dBm)", "报警类型", "状态"};
        activeTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        activeTable = new JTable(activeTableModel);
        activeTable.setFont(FONT_REGULAR);
        activeTable.setRowHeight(24);
        activeTable.getTableHeader().setFont(FONT_BOLD);
        centerAlignTableColumns(activeTable);

        panel.add(new JScrollPane(activeTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryAlarmsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // 顶部工具栏
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.add(new JLabel("通道筛选:"));
        channelFilterCombo = new JComboBox<>(new String[]{"全部通道", "CH1", "CH2", "CH3", "CH4", "CH5", "CH6", "CH7", "CH8"});
        channelFilterCombo.setFont(FONT_REGULAR);
        channelFilterCombo.addActionListener(e -> refreshHistoryTable());
        bar.add(channelFilterCombo);

        bar.add(new JLabel("状态筛选:"));
        statusFilterCombo = new JComboBox<>(new String[]{"全部状态", "触发报警", "持续报警", "报警恢复"});
        statusFilterCombo.setFont(FONT_REGULAR);
        statusFilterCombo.addActionListener(e -> refreshHistoryTable());
        bar.add(statusFilterCombo);

        JButton btnRefresh = new JButton("刷新列表");
        JButton btnExport = new JButton("导出 CSV");
        JButton btnClear = new JButton("清空记录");

        btnRefresh.setFont(FONT_REGULAR);
        btnExport.setFont(FONT_REGULAR);
        btnClear.setFont(FONT_REGULAR);

        btnRefresh.addActionListener(e -> refreshHistoryTable());
        btnExport.addActionListener(e -> exportToCsv());
        btnClear.addActionListener(e -> {
            int ret = JOptionPane.showConfirmDialog(this, "确认清空所有历史报警记录吗？该操作不可逆。", "提示", JOptionPane.YES_NO_OPTION);
            if (ret == JOptionPane.YES_OPTION) {
                AlarmService.getInstance().clearHistory();
                refreshHistoryTable();
            }
        });

        bar.add(btnRefresh);
        bar.add(btnExport);
        bar.add(btnClear);
        panel.add(bar, BorderLayout.NORTH);

        // 表格
        String[] cols = {"序号", "时间", "通道", "通道名称", "实测幅值(dBm)", "报警阈值(dBm)", "超标幅值(dBm)", "报警类型", "状态"};
        historyTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setFont(FONT_REGULAR);
        historyTable.setRowHeight(24);
        historyTable.getTableHeader().setFont(FONT_BOLD);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        centerAlignTableColumns(historyTable);

        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createThresholdSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 全局阈值
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblGlobal = new JLabel("全局默认幅值报警阈值 (dBm):");
        lblGlobal.setFont(FONT_BOLD);
        form.add(lblGlobal, gbc);

        gbc.gridx = 1;
        globalThresholdField = new JTextField(formatDbm(AppGlobalConfig.getAlarmThresholdGlobal()), 10);
        globalThresholdField.setFont(FONT_REGULAR);
        form.add(globalThresholdField, gbc);

        gbc.gridx = 2;
        JButton btnApplyGlobal = new JButton("批量应用到所有通道");
        btnApplyGlobal.setFont(FONT_REGULAR);
        btnApplyGlobal.addActionListener(e -> {
            try {
                double val = Double.parseDouble(globalThresholdField.getText().trim());
                for (JTextField f : chThresholdFields) {
                    f.setText(formatDbm(val));
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "请输入合法的全局阈值数值！", "格式错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        form.add(btnApplyGlobal, gbc);

        // 8个通道独立阈值
        JPanel chGrid = new JPanel(new GridLayout(4, 2, 16, 8));
        chGrid.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "各通道独立幅值报警阈值 (单位: dBm)",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD
        ));

        for (int i = 0; i < 8; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            JLabel lblCh = new JLabel(String.format(Locale.ROOT, "通道 %d (CH%d) 阈值:", i + 1, i + 1));
            lblCh.setFont(FONT_REGULAR);
            lblCh.setPreferredSize(new Dimension(130, 24));
            double chTh = AppGlobalConfig.getAlarmThresholdChannel(i);
            JTextField tf = new JTextField(formatDbm(chTh), 8);
            tf.setFont(FONT_REGULAR);
            chThresholdFields[i] = tf;

            row.add(lblCh);
            row.add(tf);
            row.add(new JLabel("dBm"));
            chGrid.add(row);
        }

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        form.add(chGrid, gbc);

        panel.add(form, BorderLayout.CENTER);

        // 底部保存按钮
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        JButton btnSave = new JButton("保存阈值配置");
        btnSave.setFont(FONT_BOLD);
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveThresholdConfig());
        btnRow.add(btnSave);
        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    private void saveThresholdConfig() {
        try {
            double globalVal = Double.parseDouble(globalThresholdField.getText().trim());
            AppGlobalConfig.setAlarmThresholdGlobal(globalVal);

            for (int i = 0; i < 8; i++) {
                double chVal = Double.parseDouble(chThresholdFields[i].getText().trim());
                AppGlobalConfig.setAlarmThresholdChannel(i, chVal);
                cardThresholdLabels[i].setText("阈值: " + formatDbm(chVal) + " dBm");
            }

            AppGlobalConfig.saveCurrentConfig();
            JOptionPane.showMessageDialog(this, "幅值报警阈值保存成功并已即刻生效！", "保存成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "保存失败，请检查数值格式是否合法: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void centerAlignTableColumns(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    public void refreshHistoryTable() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshHistoryTable);
            return;
        }
        if (historyTableModel == null) return;

        int chFilterIndex = channelFilterCombo != null ? channelFilterCombo.getSelectedIndex() : 0;
        int statusFilterIndex = statusFilterCombo != null ? statusFilterCombo.getSelectedIndex() : 0;

        historyTableModel.setRowCount(0);
        List<AlarmRecord> list = AlarmService.getInstance().getHistoryRecords();
        // 倒序展示（最新在最前）
        for (int i = list.size() - 1; i >= 0; i--) {
            AlarmRecord r = list.get(i);
            if (chFilterIndex > 0 && r.getChannelIndex() != (chFilterIndex - 1)) {
                continue;
            }
            if (statusFilterIndex > 0) {
                String expected = statusFilterCombo.getItemAt(statusFilterIndex);
                if (!expected.equals(r.getStatus())) {
                    continue;
                }
            }
            historyTableModel.addRow(new Object[]{
                    r.getId(),
                    r.getTimeStr(),
                    "CH" + (r.getChannelIndex() + 1),
                    r.getChannelName(),
                    formatDbm(r.getAmplitudeDbm()),
                    formatDbm(r.getThresholdDbm()),
                    formatOverDbm(r.getOverDbm()),
                    r.getAlarmType(),
                    r.getStatus()
            });
        }
    }

    public void refreshActiveTable() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshActiveTable);
            return;
        }
        if (activeTableModel == null) return;

        activeTableModel.setRowCount(0);
        AlarmRecord[] active = AlarmService.getInstance().getActiveAlarms();
        int activeCount = 0;
        for (AlarmRecord r : active) {
            if (r != null) {
                activeCount++;
                activeTableModel.addRow(new Object[]{
                        "CH" + (r.getChannelIndex() + 1),
                        r.getChannelName(),
                        formatDbm(r.getAmplitudeDbm()),
                        formatDbm(r.getThresholdDbm()),
                        formatOverDbm(r.getOverDbm()),
                        r.getAlarmType(),
                        r.getStatus()
                });
            }
        }

        if (activeCount > 0) {
            systemStatusBadge.setText(" ⚠️ 系统状态: 报警中 (" + activeCount + " 个通道超标) ");
            systemStatusBadge.setBackground(new Color(254, 242, 242));
            systemStatusBadge.setForeground(COLOR_ALARM);
            systemStatusBadge.setBorder(BorderFactory.createLineBorder(new Color(254, 202, 202), 1));
        } else {
            systemStatusBadge.setText(" 系统状态: 正常运行 · 无活动报警 ");
            systemStatusBadge.setBackground(new Color(240, 253, 244));
            systemStatusBadge.setForeground(COLOR_OK);
            systemStatusBadge.setBorder(BorderFactory.createLineBorder(new Color(187, 247, 208), 1));
        }
    }

    private void exportToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("幅值报警历史导出.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(f)) {
                fw.write(AlarmRecord.getCsvHeader() + "\n");
                List<AlarmRecord> list = AlarmService.getInstance().getHistoryRecords();
                for (AlarmRecord r : list) {
                    fw.write(r.toCsvRow() + "\n");
                }
                JOptionPane.showMessageDialog(this, "导出完成: " + f.getAbsolutePath(), "导出成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "导出异常: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onAlarmStateChanged(boolean[] channelAlarming, AlarmRecord newRecord) {
        SwingUtilities.invokeLater(() -> {
            refreshActiveTable();
            refreshHistoryTable();
            updateChannelCards(channelAlarming, false);
        });
    }

    @Override
    public void onFlashTick(boolean flashOn, boolean[] channelAlarming) {
        SwingUtilities.invokeLater(() -> {
            updateChannelCards(channelAlarming, flashOn);
        });
    }

    private void updateChannelCards(boolean[] channelAlarming, boolean flashOn) {
        for (int i = 0; i < 8; i++) {
            boolean isOver = channelAlarming != null && i < channelAlarming.length && channelAlarming[i];
            if (isOver) {
                cardStatusLabels[i].setText("● 报警中");
                cardStatusLabels[i].setForeground(flashOn ? COLOR_ALARM : Color.GRAY);
                channelCards[i].setBackground(flashOn ? new Color(254, 226, 226) : BG_CARD);
            } else {
                cardStatusLabels[i].setText("● 正常");
                cardStatusLabels[i].setForeground(COLOR_OK);
                channelCards[i].setBackground(BG_CARD);
            }
        }
    }

    public void updateChannelCurrentValue(int ch, double ampDbm) {
        if (ch >= 0 && ch < 8 && cardValueLabels[ch] != null) {
            SwingUtilities.invokeLater(() -> {
                if (Double.isNaN(ampDbm)) {
                    cardValueLabels[ch].setText("-- dBm");
                } else {
                    cardValueLabels[ch].setText(formatDbm(ampDbm) + " dBm");
                }
            });
        }
    }
}
