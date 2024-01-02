package com.mqttinsight.ui.component.model;


import com.mqttinsight.codec.CodecSupports;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ptma
 */
public class DynaminCodecComboModel extends AbstractListModel<String> implements ComboBoxModel<String>, ActionListener {

    public static final String UPDATE = "update";
    protected final List<String> data;
    protected String selected;

    public DynaminCodecComboModel() {
        data = new ArrayList<>();
        data.addAll(CodecSupports.instance().getDynamicCodecNames());
        if (data.size() > 0) {
            selected = data.get(0);
        }
    }

    @Override
    public void setSelectedItem(Object item) {
        if ((selected != null && !selected.equals(item))
            || selected == null && item != null) {
            selected = (String) item;
            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public String getSelectedItem() {
        return this.selected;
    }

    @Override
    public String getElementAt(int index) {
        return data.get(index);
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals(UPDATE)) {
            this.fireContentsChanged(this, 0, getSize() - 1);
        }
    }
}
