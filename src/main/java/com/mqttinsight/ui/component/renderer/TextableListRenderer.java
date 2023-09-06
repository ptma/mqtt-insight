package com.mqttinsight.ui.component.renderer;

import com.mqttinsight.ui.component.Textable;

import javax.swing.*;
import java.awt.*;

/**
 * 枚举渲染， 用于 Combobox
 *
 * @author ptma
 */
public class TextableListRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            value = "";
        } else if (value instanceof Textable) {
            value = ((Textable) value).getText();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
