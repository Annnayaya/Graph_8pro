package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * lib/.config 系统参数配置图形化管理界面
 */
public class ConfigManagerGUI extends JDialog {

    private static final Font FONT_TITLE = new Font("Microsoft YaHei", Font.BOLD, 15);
    private static final Font FONT_SECTION = new Font("Microsoft YaHei", Font.BOLD, 12);
    private static final Font FONT_LABEL = new Font("Microsoft YaHei", Font.PLAIN, 12);

    // 系统基础设置
    private JTextField versionField;
    private JTextField deviceIpField;
    private JComboBox<String> refreshModeCombo;
    private JCheckBox reportDataCheckBox;

    // 持久峰值与图谱联动
    private JCheckBox persistentPeakCheckBox;
    private JCheckBox spectrogramFollowCheckBox;
    private JSpinner epsilonSpinner;
    private JSpinner jitterSpinner;
    private JComboBox<String> decimalsCombo;
    private JSpinner maxStepSpinner;

    // 初始建立与切换保护
    private JSpinner initialGroupsSpinner;
    private JSpinner switchJudgeGroupsSpinner;
    private JCheckBox switchShowDashCheckBox;

    // IP 切换与报警配置
    private JComboBox<String> ipSwitchModeCombo;
    private JTextField presetName1Field;
    private JTextField presetIp1Field;
    private JTextField presetName2Field;
    private JTextField presetIp2Field;
    private JTextField presetName3Field;
    private JTextField presetIp3Field;
    private JCheckBox alarmEnabledCheckBox;

    // 状态与路径标签
    private JLabel statusLabel;
    private JLabel licenseStatusLabel;
    private JButton importLicenseBtn;

    public ConfigManagerGUI(Window owner) {
        super(owner, "Graph_8Pro 系统参数配置管理器", ModalityType.APPLICATION_MODAL);
        try {
            Image icon = loadGearIcon();
            if (icon != null) {
                setIconImage(icon);
            }
        } catch (Exception ignored) {}
        initComponents();
        loadCurrentValues();
        setPreferredSize(new Dimension(860, 840));
        setMinimumSize(new Dimension(820, 700));
        pack();
        setLocationRelativeTo(owner);
    }

    private Image loadGearIcon() {
        try {
            File[] candidates = new File[] {
                new File("gear.png"),
                new File("tools/ConfigManager/gear.png"),
                new File("output/gear.png")
            };
            for (File f : candidates) {
                if (f.exists()) {
                    return ImageIO.read(f);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 8));

        // 1. 顶部标头
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(215, 220, 228)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLabel = new JLabel("Graph_8Pro 系统参数配置管理");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(new Color(30, 41, 59));

        JPanel licPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        licPanel.setOpaque(false);
        licenseStatusLabel = new JLabel("授权文件: 检测中...");
        licenseStatusLabel.setFont(FONT_LABEL);
        importLicenseBtn = new JButton("导入授权文件");
        importLicenseBtn.setFont(FONT_LABEL);
        importLicenseBtn.setFocusPainted(false);
        importLicenseBtn.addActionListener(e -> importLicenseAction());
        licPanel.add(licenseStatusLabel);
        licPanel.add(importLicenseBtn);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(licPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. 中间表单区域
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBorder(new EmptyBorder(10, 16, 6, 16));

        // --- 分组 1: 系统与版本 ---
        JPanel sysPanel = createTitledPanel("1. 系统通用设置");
        GridBagLayout gbl1 = new GridBagLayout();
        sysPanel.setLayout(gbl1);

        versionField = new JTextField(15);
        versionField.setFont(FONT_LABEL);
        deviceIpField = new JTextField(15);
        deviceIpField.setFont(FONT_LABEL);
        String[] refreshOptions = {
                "整组80周波跳变 (每1.6秒整屏切换，传统工业对照模式 · 默认)",
                "逐周波平滑滚动 (每20ms连续流动，丝滑瀑布流)"
        };
        refreshModeCombo = new JComboBox<>(refreshOptions);
        refreshModeCombo.setFont(FONT_LABEL);
        reportDataCheckBox = new JCheckBox("开启原始 UDP 报文保存至 reportdata 目录");
        reportDataCheckBox.setFont(FONT_LABEL);

        addFormField(sysPanel, gbl1, 0, "软件版本号 (app.version):", versionField, "界面右下角状态栏展示的版本号");
        addFormField(sysPanel, gbl1, 1, "默认设备 IP (default.ip):", deviceIpField, "采集通信默认目标 IP 地址，启动时自动填入输入框");
        addFormField(sysPanel, gbl1, 2, "谱图刷新模式 (spectrogram.rolling.enabled):", refreshModeCombo, "控制 3D 谱图是以 50Hz 连续流水滚动，还是攒满 80 周波整屏切换");
        addFormField(sysPanel, gbl1, 3, "原始报文记录 (reportdata.enabled):", reportDataCheckBox, "默认关闭，开启后会将每帧原始报文落盘");

        formContainer.add(sysPanel);
        formContainer.add(Box.createVerticalStrut(8));

        // --- 分组 2: 持久峰值滤波与图谱联动 ---
        JPanel peakPanel = createTitledPanel("2. 持久峰值平滑滤波设置");
        GridBagLayout gbl2 = new GridBagLayout();
        peakPanel.setLayout(gbl2);

        persistentPeakCheckBox = new JCheckBox("启用持久测试峰值平滑滤波引擎");
        persistentPeakCheckBox.setFont(FONT_LABEL);
        persistentPeakCheckBox.addActionListener(e -> updatePeakControlsState());
        spectrogramFollowCheckBox = new JCheckBox("谱图峰值实时跟随平滑滤波值");
        spectrogramFollowCheckBox.setFont(FONT_LABEL);

        epsilonSpinner = new JSpinner(new SpinnerNumberModel(0.20, 0.0, 50.0, 0.1));
        epsilonSpinner.setFont(FONT_LABEL);
        jitterSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10.0, 0.1));
        jitterSpinner.setFont(FONT_LABEL);

        String[] decimalsOptions = {
                "0 (纯整数格式，无小数点后内容)",
                "1 (保留 1 位小数，默认)",
                "2 (保留 2 位小数)",
                "3 (保留 3 位小数)",
                "4 (保留 4 位小数)",
                "5 (保留 5 位小数)",
                "6 (保留 6 位小数)"
        };
        decimalsCombo = new JComboBox<>(decimalsOptions);
        decimalsCombo.setFont(FONT_LABEL);

        maxStepSpinner = new JSpinner(new SpinnerNumberModel(0.3, 0.1, 50.0, 0.1));
        maxStepSpinner.setFont(FONT_LABEL);

        addFormField(peakPanel, gbl2, 0, "平滑滤波总开关 (persistent.peak.enabled):", persistentPeakCheckBox, "全局持久峰值算法总开关");
        addFormField(peakPanel, gbl2, 1, "谱图峰值跟随 (spectrogram.peak.follow.filtered):", spectrogramFollowCheckBox, "开启后各通道谱图最大值跟随平滑值");
        addFormField(peakPanel, gbl2, 2, "变动死区门限 (display.epsilon.db, 单位: dB):", epsilonSpinner, "小于该门限的微小抖动将被平滑吸收，默认 0.2 或 10");
        addFormField(peakPanel, gbl2, 3, "稳定值微抖动 (display.jitter.db, 单位: dB):", jitterSpinner, "稳定数值上叠加的微小仿真抖动，0 表示关闭");
        addFormField(peakPanel, gbl2, 4, "数值显示小数位 (display.decimals):", decimalsCombo, "设置持久测试数值显示与记录的小数点后位数");
        addFormField(peakPanel, gbl2, 5, "变动步进上限 (display.max.step.db, 单位: dB):", maxStepSpinner, "每次变动的最大步长，输入大突变时以该单位快速步进");

        formContainer.add(peakPanel);
        formContainer.add(Box.createVerticalStrut(8));

        // --- 分组 3: 初始建立与切换保护 ---
        JPanel switchPanel = createTitledPanel("3. 初始建立与信号切换保护");
        GridBagLayout gbl3 = new GridBagLayout();
        switchPanel.setLayout(gbl3);

        initialGroupsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
        initialGroupsSpinner.setFont(FONT_LABEL);
        switchJudgeGroupsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
        switchJudgeGroupsSpinner.setFont(FONT_LABEL);
        switchShowDashCheckBox = new JCheckBox("中途切换信号强度时先显示 --，待稳定后再显示新数据");
        switchShowDashCheckBox.setFont(FONT_LABEL);

        addFormField(switchPanel, gbl3, 0, "初始建立组数 (initial.groups, 组):", initialGroupsSpinner, "初始最大值建立所需组数 (默认3组/约4.8秒)");
        addFormField(switchPanel, gbl3, 1, "切换判断组数 (switch.judge.groups, 组):", switchJudgeGroupsSpinner, "中途信号大幅突变后判定新稳定强度的组数 (默认3组)");
        addFormField(switchPanel, gbl3, 2, "切换过渡保护 (switch.show.dash):", switchShowDashCheckBox, "开启后切换过程先显示--，稳定后再出新值，避免过渡跳动");

        formContainer.add(switchPanel);
        formContainer.add(Box.createVerticalStrut(8));

        // --- 分组 4: IP 切换模式与预设地址 ---
        JPanel ipPanel = createTitledPanel("4. IP 切换模式与预设地址配置");
        GridBagLayout gbl4 = new GridBagLayout();
        ipPanel.setLayout(gbl4);

        String[] ipSwitchOptions = {
                "0: 仅直接输入 (保留原有文本框手动输入)",
                "1: 仅 3 个预设按钮 (点击快捷按钮切换 IP)",
                "2: 两种方式并行并存 (输入框 + 3 个预设按钮 · 默认)"
        };
        ipSwitchModeCombo = new JComboBox<>(ipSwitchOptions);
        ipSwitchModeCombo.setFont(FONT_LABEL);

        presetName1Field = new JTextField(15);
        presetName1Field.setFont(FONT_LABEL);
        presetIp1Field = new JTextField(15);
        presetIp1Field.setFont(FONT_LABEL);
        presetName2Field = new JTextField(15);
        presetName2Field.setFont(FONT_LABEL);
        presetIp2Field = new JTextField(15);
        presetIp2Field.setFont(FONT_LABEL);
        presetName3Field = new JTextField(15);
        presetName3Field.setFont(FONT_LABEL);
        presetIp3Field = new JTextField(15);
        presetIp3Field.setFont(FONT_LABEL);

        addFormField(ipPanel, gbl4, 0, "IP 切换方式 (ip.switch.mode):", ipSwitchModeCombo, "控制谱图软件顶部工具栏的 IP 切换显示方式");
        addFormField(ipPanel, gbl4, 1, "预设 1 按钮文本 (preset.name.1):", presetName1Field, "快捷预设按钮 1 界面显示文本");
        addFormField(ipPanel, gbl4, 2, "预设 IP 1 (preset.ip.1):", presetIp1Field, "预设按钮 1 对应的设备 IP 地址");
        addFormField(ipPanel, gbl4, 3, "预设 2 按钮文本 (preset.name.2):", presetName2Field, "快捷预设按钮 2 界面显示文本");
        addFormField(ipPanel, gbl4, 4, "预设 IP 2 (preset.ip.2):", presetIp2Field, "预设按钮 2 对应的设备 IP 地址");
        addFormField(ipPanel, gbl4, 5, "预设 3 按钮文本 (preset.name.3):", presetName3Field, "快捷预设按钮 3 界面显示文本");
        addFormField(ipPanel, gbl4, 6, "预设 IP 3 (preset.ip.3):", presetIp3Field, "预设按钮 3 对应的设备 IP 地址");

        formContainer.add(ipPanel);
        formContainer.add(Box.createVerticalStrut(8));

        // --- 分组 5: 幅值报警监控配置 ---
        JPanel alarmPanel = createTitledPanel("5. 幅值报警监控配置");
        GridBagLayout gbl5 = new GridBagLayout();
        alarmPanel.setLayout(gbl5);

        alarmEnabledCheckBox = new JCheckBox("启用幅值超限报警监控与专用查看页面 (alarm.enabled)");
        alarmEnabledCheckBox.setFont(FONT_LABEL);

        addFormField(alarmPanel, gbl5, 0, "幅值报警功能开关:", alarmEnabledCheckBox, "开启后主界面显示“幅值报警”独立页面，并对超限通道红光闪烁及存储记录");

        formContainer.add(alarmPanel);

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        add(scrollPane, BorderLayout.CENTER);

        // 3. 底部按钮与状态栏
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(215, 220, 228)),
                new EmptyBorder(8, 16, 8, 16)
        ));

        statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(22, 101, 52));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton saveBtn = new JButton("保存");
        saveBtn.setFont(FONT_LABEL);
        saveBtn.setBackground(new Color(37, 99, 235));
        saveBtn.setForeground(Color.BLACK);

        JButton resetBtn = new JButton("恢复默认值");
        resetBtn.setFont(FONT_LABEL);

        JButton cancelBtn = new JButton("关闭");
        cancelBtn.setFont(FONT_LABEL);

        buttonPanel.add(saveBtn);
        buttonPanel.add(resetBtn);
        buttonPanel.add(cancelBtn);

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // 事件绑定
        saveBtn.addActionListener(e -> saveConfigAction());
        resetBtn.addActionListener(e -> resetToDefaultsAction());
        cancelBtn.addActionListener(e -> dispose());

        // ESC 关闭快捷键
        JRootPane root = getRootPane();
        root.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        updatePeakControlsState();
    }

    private void updateLicenseStatus() {
        if (licenseStatusLabel == null) return;
        boolean hasLic = LicenseManager.hasLicenseFile();
        if (hasLic) {
            if (LicenseManager.isSpecialLicense()) {
                licenseStatusLabel.setText("授权文件: 已加载 (特殊调试授权，仅供调试使用)");
                licenseStatusLabel.setForeground(new Color(217, 119, 6));
            } else {
                licenseStatusLabel.setText("授权文件: 已加载 (lib/license.prop)");
                licenseStatusLabel.setForeground(new Color(22, 101, 52));
            }
        } else {
            licenseStatusLabel.setText("授权文件: 未检测到授权文件");
            licenseStatusLabel.setForeground(new Color(185, 28, 28));
        }
        updatePeakControlsState();
    }

    private void updatePeakControlsState() {
        boolean hasLic = LicenseManager.hasLicenseFile();

        if (refreshModeCombo != null) refreshModeCombo.setEnabled(hasLic);
        if (persistentPeakCheckBox != null) persistentPeakCheckBox.setEnabled(hasLic);
        if (alarmEnabledCheckBox != null) alarmEnabledCheckBox.setEnabled(hasLic);

        boolean enabled = hasLic && persistentPeakCheckBox != null && persistentPeakCheckBox.isSelected();
        if (spectrogramFollowCheckBox != null) spectrogramFollowCheckBox.setEnabled(enabled);
        if (epsilonSpinner != null) epsilonSpinner.setEnabled(enabled);
        if (jitterSpinner != null) jitterSpinner.setEnabled(enabled);
        if (decimalsCombo != null) decimalsCombo.setEnabled(enabled);
        if (maxStepSpinner != null) maxStepSpinner.setEnabled(enabled);

        if (initialGroupsSpinner != null) initialGroupsSpinner.setEnabled(enabled);
        if (switchJudgeGroupsSpinner != null) switchJudgeGroupsSpinner.setEnabled(enabled);
        if (switchShowDashCheckBox != null) switchShowDashCheckBox.setEnabled(enabled);

        if (ipSwitchModeCombo != null) ipSwitchModeCombo.setEnabled(hasLic);
        if (presetName1Field != null) presetName1Field.setEnabled(hasLic);
        if (presetIp1Field != null) presetIp1Field.setEnabled(hasLic);
        if (presetName2Field != null) presetName2Field.setEnabled(hasLic);
        if (presetIp2Field != null) presetIp2Field.setEnabled(hasLic);
        if (presetName3Field != null) presetName3Field.setEnabled(hasLic);
        if (presetIp3Field != null) presetIp3Field.setEnabled(hasLic);
    }

    private void importLicenseAction() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("选择授权文件 (.prop / .properties)");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("授权文件 (*.prop, *.properties)", "prop", "properties"));
        int res = chooser.showOpenDialog(this);
        if (res == javax.swing.JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            if (selected == null || !selected.exists()) return;
            try {
                File target = LicenseManager.getLicenseFile();
                if (target.getParentFile() != null && !target.getParentFile().exists()) {
                    target.getParentFile().mkdirs();
                }
                java.nio.file.Files.copy(selected.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                LicenseManager.resetCache();
                updateLicenseStatus();
                JOptionPane.showMessageDialog(this, "授权文件导入成功！\n文件已保存至: " + target.getAbsolutePath(), "导入成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "导入授权文件失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createTitledPanel(String title) {
        JPanel p = new JPanel();
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                title
        );
        border.setTitleFont(FONT_SECTION);
        border.setTitleColor(new Color(51, 65, 85));
        p.setBorder(new CompoundBorder(border, new EmptyBorder(6, 10, 8, 10)));
        return p;
    }

    private void addFormField(JPanel panel, GridBagLayout gbl, int row, String labelText, JComponent comp, String tip) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 4, 3, 4);

        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0.45;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setToolTipText(tip);
        comp.setToolTipText(tip);
        panel.add(lbl, c);

        c.gridx = 1;
        c.weightx = 0.55;
        panel.add(comp, c);
    }

    private void loadCurrentValues() {
        AppGlobalConfig.loadConfig();

        versionField.setText(AppGlobalConfig.getRawAppVersion());
        deviceIpField.setText(AppGlobalConfig.getDeviceIp());
        reportDataCheckBox.setSelected(AppGlobalConfig.isReportDataEnabled());

        persistentPeakCheckBox.setSelected(AppGlobalConfig.isPersistentPeakEnabled());
        spectrogramFollowCheckBox.setSelected(AppGlobalConfig.isSpectrogramPeakFollowFilteredConfig());
        epsilonSpinner.setValue(AppGlobalConfig.getDisplayEpsilonDb());
        jitterSpinner.setValue(AppGlobalConfig.getDisplayJitterDb());

        int dec = AppGlobalConfig.getDisplayDecimals();
        if (dec >= 0 && dec < decimalsCombo.getItemCount()) {
            decimalsCombo.setSelectedIndex(dec);
        } else {
            decimalsCombo.setSelectedIndex(1);
        }

        maxStepSpinner.setValue(AppGlobalConfig.getDisplayMaxStepDb());
        initialGroupsSpinner.setValue(AppGlobalConfig.getInitialGroups());
        switchJudgeGroupsSpinner.setValue(AppGlobalConfig.getSwitchJudgeGroups());
        switchShowDashCheckBox.setSelected(AppGlobalConfig.isSwitchShowDash());

        ipSwitchModeCombo.setSelectedIndex(Math.max(0, Math.min(2, AppGlobalConfig.getRawIpSwitchMode())));
        presetName1Field.setText(AppGlobalConfig.getPresetName1());
        presetIp1Field.setText(AppGlobalConfig.getPresetIp1());
        presetName2Field.setText(AppGlobalConfig.getPresetName2());
        presetIp2Field.setText(AppGlobalConfig.getPresetIp2());
        presetName3Field.setText(AppGlobalConfig.getPresetName3());
        presetIp3Field.setText(AppGlobalConfig.getPresetIp3());
        alarmEnabledCheckBox.setSelected(AppGlobalConfig.isAlarmEnabled());

        File cfg = AppGlobalConfig.getConfigFile();
        statusLabel.setText("文件: " + cfg.getPath() + " | AES-128 加密已生效");
        setRollingModeSelected(AppGlobalConfig.isSpectrogramRollingEnabled());
        updateLicenseStatus();
    }

    private void resetToDefaultsAction() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定将所有参数重置为系统默认值吗？",
                "确认重置",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        versionField.setText(AppGlobalConfig.DEFAULT_APP_VERSION);
        deviceIpField.setText(AppGlobalConfig.DEFAULT_DEVICE_IP);
        setRollingModeSelected(AppGlobalConfig.DEFAULT_SPECTROGRAM_ROLLING_ENABLED);
        reportDataCheckBox.setSelected(AppGlobalConfig.DEFAULT_REPORT_DATA_ENABLED);
        persistentPeakCheckBox.setSelected(AppGlobalConfig.DEFAULT_PERSISTENT_PEAK_ENABLED);
        spectrogramFollowCheckBox.setSelected(AppGlobalConfig.DEFAULT_SPECTROGRAM_PEAK_FOLLOW_FILTERED);
        epsilonSpinner.setValue(AppGlobalConfig.DEFAULT_DISPLAY_EPSILON_DB);
        jitterSpinner.setValue(AppGlobalConfig.DEFAULT_DISPLAY_JITTER_DB);
        decimalsCombo.setSelectedIndex(AppGlobalConfig.DEFAULT_DISPLAY_DECIMALS);
        maxStepSpinner.setValue(AppGlobalConfig.DEFAULT_DISPLAY_MAX_STEP_DB);
        initialGroupsSpinner.setValue(AppGlobalConfig.DEFAULT_INITIAL_GROUPS);
        switchJudgeGroupsSpinner.setValue(AppGlobalConfig.DEFAULT_SWITCH_JUDGE_GROUPS);
        switchShowDashCheckBox.setSelected(AppGlobalConfig.DEFAULT_SWITCH_SHOW_DASH);
        ipSwitchModeCombo.setSelectedIndex(AppGlobalConfig.DEFAULT_IP_SWITCH_MODE);
        presetName1Field.setText(AppGlobalConfig.DEFAULT_PRESET_NAME_1);
        presetIp1Field.setText(AppGlobalConfig.DEFAULT_PRESET_IP_1);
        presetName2Field.setText(AppGlobalConfig.DEFAULT_PRESET_NAME_2);
        presetIp2Field.setText(AppGlobalConfig.DEFAULT_PRESET_IP_2);
        presetName3Field.setText(AppGlobalConfig.DEFAULT_PRESET_NAME_3);
        presetIp3Field.setText(AppGlobalConfig.DEFAULT_PRESET_IP_3);
        alarmEnabledCheckBox.setSelected(AppGlobalConfig.DEFAULT_ALARM_ENABLED);
        updatePeakControlsState();
    }

    private void saveConfigAction() {
        try {
            String ver = versionField.getText().trim();
            if (ver.isEmpty()) {
                ver = AppGlobalConfig.DEFAULT_APP_VERSION;
            }
            String ip = deviceIpField.getText().trim();
            if (ip.isEmpty()) {
                ip = AppGlobalConfig.DEFAULT_DEVICE_IP;
            }
            boolean rolling = isRollingEnabledSelected();
            boolean reportData = reportDataCheckBox.isSelected();
            boolean persistentPeak = persistentPeakCheckBox.isSelected();
            boolean followFiltered = spectrogramFollowCheckBox.isSelected();
            double epsilon = ((Number) epsilonSpinner.getValue()).doubleValue();
            double jitter = ((Number) jitterSpinner.getValue()).doubleValue();
            int decimals = decimalsCombo.getSelectedIndex();
            double maxStep = ((Number) maxStepSpinner.getValue()).doubleValue();
            int initGroups = ((Number) initialGroupsSpinner.getValue()).intValue();
            int switchJudge = ((Number) switchJudgeGroupsSpinner.getValue()).intValue();
            boolean switchDash = switchShowDashCheckBox.isSelected();

            AppGlobalConfig.setIpSwitchMode(ipSwitchModeCombo.getSelectedIndex());
            String n1 = presetName1Field.getText().trim();
            if (!n1.isEmpty()) AppGlobalConfig.setPresetName1(n1);
            String p1 = presetIp1Field.getText().trim();
            if (!p1.isEmpty()) AppGlobalConfig.setPresetIp1(p1);
            String n2 = presetName2Field.getText().trim();
            if (!n2.isEmpty()) AppGlobalConfig.setPresetName2(n2);
            String p2 = presetIp2Field.getText().trim();
            if (!p2.isEmpty()) AppGlobalConfig.setPresetIp2(p2);
            String n3 = presetName3Field.getText().trim();
            if (!n3.isEmpty()) AppGlobalConfig.setPresetName3(n3);
            String p3 = presetIp3Field.getText().trim();
            if (!p3.isEmpty()) AppGlobalConfig.setPresetIp3(p3);
            AppGlobalConfig.setAlarmEnabled(alarmEnabledCheckBox.isSelected());

            AppGlobalConfig.saveConfig(
                    reportData,
                    persistentPeak,
                    followFiltered,
                    epsilon,
                    jitter,
                    decimals,
                    ver,
                    maxStep,
                    initGroups,
                    switchJudge,
                    switchDash,
                    ip,
                    rolling
            );

            JOptionPane.showMessageDialog(this,
                    "参数配置已成功保存！",
                    "保存成功",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "保存配置失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isRollingEnabledSelected() {
        if (refreshModeCombo == null) return AppGlobalConfig.DEFAULT_SPECTROGRAM_ROLLING_ENABLED;
        return refreshModeCombo.getSelectedIndex() == 1;
    }

    private void setRollingModeSelected(boolean rolling) {
        if (refreshModeCombo == null) return;
        refreshModeCombo.setSelectedIndex(rolling ? 1 : 0);
    }

    /**
     * 在指定父窗口中显示配置管理界面
     */
    public static void showDialog(Component parent) {
        Window window = null;
        if (parent instanceof Window) {
            window = (Window) parent;
        } else if (parent != null) {
            window = SwingUtilities.getWindowAncestor(parent);
        }
        ConfigManagerGUI gui = new ConfigManagerGUI(window);
        gui.setVisible(true);
    }

    /**
     * 独立启动入口（可双击批处理或通过 java 命令行直接打开参数配置工具）
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            JFrame dummyFrame = new JFrame();
            dummyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            dummyFrame.setLocationRelativeTo(null);
            showDialog(dummyFrame);
            dummyFrame.dispose();
            System.exit(0);
        });
    }
}
