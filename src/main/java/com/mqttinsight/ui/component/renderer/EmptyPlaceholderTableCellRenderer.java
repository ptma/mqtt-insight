package com.mqttinsight.ui.component.renderer;

import cn.hutool.core.util.ObjectUtil;
import com.mqttinsight.util.Utils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class EmptyPlaceholderTableCellRenderer extends DefaultTableCellRenderer {

    private static boolean isDarkTheme = UIManager.getBoolean("laf.dark");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (ObjectUtil.isEmpty(value)) {
            if (isSelected) {
                super.setForeground(Utils.darker(table.getSelectionForeground(), 0.7f));
            } else {
                if (isDarkTheme) {
                    super.setForeground(Utils.darker(table.getForeground(), 0.5f));
                } else {
                    super.setForeground(Utils.brighter(table.getForeground(), 0.5f));
                }
            }
            super.setText("<Empty>");
        } else {
            if (isSelected) {
                super.setForeground(table.getSelectionForeground());
            } else {
                super.setForeground(table.getForeground());
            }
        }

        return result;
    }
}
