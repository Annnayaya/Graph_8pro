package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

/**
 * 谱图软件内置的采集速度配置弹窗
 */
public class SpeedConfigDialog extends JDialog {
    private static final Font FONT_TITLE = new Font("Microsoft YaHei", Font.BOLD, 14);
    private static final Font FONT_TEXT = new Font("Microsoft YaHei", Font.PLAIN, 12);

    private final JRadioButton rbMax;
    private final JRadioButton rbFast;
    private final JRadioButton rbMedium;
    private final JRadioButton rbSlow;
    private final JRadioButton rbCustom;
    private final JSpinner spinnerCustomDelay;
    private JButton btnOk;

    private boolean confirmed = false;

    public SpeedConfigDialog(Frame owner) {
        super(owner, "采集速度配置", true);
        setLayout(new BorderLayout(0, 10));

        // 1. 顶部说明面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 232)),
                new EmptyBorder(14, 18, 14, 18)
        ));
        JLabel titleLabel = new JLabel("采集速率与延时调节");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(new Color(30, 41, 59));
        JLabel tipLabel = new JLabel("调控 UDP 周波采集循环频率，设置即刻生效");
        tipLabel.setFont(FONT_TEXT);
        tipLabel.setForeground(new Color(100, 116, 139));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(tipLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // 2. 中间档位选择区
        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setBorder(new EmptyBorder(14, 28, 14, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.weightx = 1.0;

        ButtonGroup group = new ButtonGroup();
        rbMax = new JRadioButton("极速模式 (50Hz / 20ms 全速实时 · 默认推荐)");
        rbFast = new JRadioButton("快速模式 (约 25Hz / 40ms)");
        rbMedium = new JRadioButton("中速模式 (约 10Hz / 100ms)");
        rbSlow = new JRadioButton("慢速模式 (约 2Hz / 500ms 慢速观测)");
        rbCustom = new JRadioButton("自定义单周波延时毫秒数:");

        rbMax.setFont(FONT_TEXT);
        rbFast.setFont(FONT_TEXT);
        rbMedium.setFont(FONT_TEXT);
        rbSlow.setFont(FONT_TEXT);
        rbCustom.setFont(FONT_TEXT);

        group.add(rbMax);
        group.add(rbFast);
        group.add(rbMedium);
        group.add(rbSlow);
        group.add(rbCustom);

        spinnerCustomDelay = new JSpinner(new SpinnerNumberModel(100, 0, 5000, 10));
        spinnerCustomDelay.setFont(FONT_TEXT);
        spinnerCustomDelay.setEnabled(false);

        rbCustom.addActionListener(e -> spinnerCustomDelay.setEnabled(rbCustom.isSelected()));
        ActionListener radioListener = e -> {
            if (!rbCustom.isSelected()) {
                spinnerCustomDelay.setEnabled(false);
            }
        };
        rbMax.addActionListener(radioListener);
        rbFast.addActionListener(radioListener);
        rbMedium.addActionListener(radioListener);
        rbSlow.addActionListener(radioListener);

        gbc.gridy = 0; bodyPanel.add(rbMax, gbc);
        gbc.gridy = 1; bodyPanel.add(rbFast, gbc);
        gbc.gridy = 2; bodyPanel.add(rbMedium, gbc);
        gbc.gridy = 3; bodyPanel.add(rbSlow, gbc);

        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        customPanel.add(rbCustom);
        customPanel.add(spinnerCustomDelay);
        JLabel msLabel = new JLabel("ms");
        msLabel.setFont(FONT_TEXT);
        customPanel.add(msLabel);
        gbc.gridy = 4; bodyPanel.add(customPanel, gbc);

        add(bodyPanel, BorderLayout.CENTER);

        // 3. 底部按钮区
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 230, 238)));
        btnOk = new JButton("确定并应用");
        JButton btnCancel = new JButton("取消");
        btnOk.setFont(FONT_TEXT);
        btnCancel.setFont(FONT_TEXT);

        btnOk.addActionListener(e -> onConfirm());
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnOk);
        btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);

        // 载入当前配置
        loadCurrentSettings();

        setPreferredSize(new java.awt.Dimension(480, 420));
        setMinimumSize(new java.awt.Dimension(450, 390));
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void loadCurrentSettings() {
        if (!LicenseManager.isLicensed()) {
            rbMax.setSelected(true);
            rbFast.setEnabled(false);
            rbMedium.setEnabled(false);
            rbSlow.setEnabled(false);
            rbCustom.setEnabled(false);
            spinnerCustomDelay.setEnabled(false);
            if (btnOk != null) {
                btnOk.setEnabled(false);
                btnOk.setText("未授权已锁定");
            }
            return;
        }
        String mode = AppGlobalConfig.getAcquisitionSpeedMode();
        int delay = AppGlobalConfig.getAcquisitionSpeedDelayMs();

        if ("MAX".equalsIgnoreCase(mode) || (delay == 0 && !"CUSTOM".equalsIgnoreCase(mode))) {
            rbMax.setSelected(true);
        } else if ("FAST".equalsIgnoreCase(mode) || (delay == 20 && !"CUSTOM".equalsIgnoreCase(mode))) {
            rbFast.setSelected(true);
        } else if ("MEDIUM".equalsIgnoreCase(mode) || (delay == 80 && !"CUSTOM".equalsIgnoreCase(mode))) {
            rbMedium.setSelected(true);
        } else if ("SLOW".equalsIgnoreCase(mode) || (delay == 480 && !"CUSTOM".equalsIgnoreCase(mode))) {
            rbSlow.setSelected(true);
        } else {
            rbCustom.setSelected(true);
            spinnerCustomDelay.setEnabled(true);
            spinnerCustomDelay.setValue(delay);
        }
    }

    private void onConfirm() {
        String newMode;
        int newDelay;

        if (rbMax.isSelected()) {
            newMode = "MAX";
            newDelay = 0;
        } else if (rbFast.isSelected()) {
            newMode = "FAST";
            newDelay = 20;
        } else if (rbMedium.isSelected()) {
            newMode = "MEDIUM";
            newDelay = 80;
        } else if (rbSlow.isSelected()) {
            newMode = "SLOW";
            newDelay = 480;
        } else {
            newMode = "CUSTOM";
            newDelay = ((Number) spinnerCustomDelay.getValue()).intValue();
        }

        AppGlobalConfig.setAcquisitionSpeedMode(newMode);
        AppGlobalConfig.setAcquisitionSpeedDelayMs(newDelay);

        try {
            AppGlobalConfig.saveCurrentConfig();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "保存配置到 lib/.config 异常: " + ex.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
