package com.Spectrogram.ZmGis8CH.gis_app_8CH;

import javax.swing.*;
import java.awt.*;

public class CheckboxScrollPaneDemo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 创建主窗口
            JFrame frame = new JFrame("复选框滚动面板示例");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 200);

            // 创建包含垂直布局的面板
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

            // 添加 20 个复选框到面板
            for (int i = 1; i <= 20; i++) {
                JCheckBox checkBox = new JCheckBox("选项 " + i);
                checkBox.setAlignmentX(Component.LEFT_ALIGNMENT); // 左对齐
                contentPanel.add(checkBox);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 5))); // 添加间距
            }

            // 创建滚动面板并添加内容面板
            JScrollPane scrollPane = new JScrollPane(contentPanel);
//            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            // 将滚动面板添加到窗口
            frame.add(scrollPane, BorderLayout.CENTER);

            // 居中显示窗口
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}