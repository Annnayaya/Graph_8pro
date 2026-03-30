package com.Spectrogram.ZmGis8CH.gis_app_8CH.ping;

import javax.swing.*;
import java.awt.*;

import com.Spectrogram.ZmGis8CH.gis_app_8CH.PRPSCell;

public class CheckBoxListRenderer extends JCheckBox implements ListCellRenderer<PRPSCell> {

 
//    @Override
//    public Component getListCellRendererComponent(JList<?> list, PRPSCell value, int index, boolean isSelected, boolean cellHasFocus) {
//        setText((value == null) ? "" : value.toString());
//        setSelected(isSelected);
//        setEnabled(list.isEnabled());
////        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
////        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
//
//        // 设置背景和前景色
//        if (isSelected) {
//            setBackground(list.getSelectionBackground());
//            setForeground(list.getSelectionForeground());
//        } else {
//            setBackground(list.getBackground());
//            setForeground(list.getForeground());
//        }
//
//        setFont(list.getFont());
//        setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : UIManager.getBorder("List.noFocusBorder"));
//        return this;
//    }


    @Override
    public Component getListCellRendererComponent(JList<? extends PRPSCell> list, PRPSCell value, int index, boolean isSelected, boolean cellHasFocus) {
        // 设置复选框状态
        setSelected(value.isSelected());
        // 设置文本
        setText(value.getStr());
//        // 设置背景和前景色
//        if (isSelected) {
//            setBackground(list.getSelectionBackground());
//            setForeground(list.getSelectionForeground());
//        } else {
//            setBackground(list.getBackground());
////            setForeground(list.getForeground());
//        }

        // 设置背景色
        if (value.isSelectedBackground()) {
            // 字符串选中状态：设置背景色
            setBackground(Color.YELLOW);
            setForeground(list.getForeground());
        } else {
            // 字符串未选中状态：恢复默认背景色
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}