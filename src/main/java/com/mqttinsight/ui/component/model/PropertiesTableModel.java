package com.mqttinsight.ui.component.model;

import com.mqttinsight.mqtt.Property;
import com.mqttinsight.mqtt.SecureMode;
import com.mqttinsight.util.LangUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ptma
 * @see SecureMode
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PropertiesTableModel extends AbstractTableModel {

    private List<Property> properties;

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        return getProperties().size();
    }

    public void clear() {
        getProperties().clear();
    }

    @Override
    public Object getValueAt(int row, int column) {
        switch (column) {
            case 0:
                return getProperties().get(row).getKey();
            case 1:
                return getProperties().get(row).getValue();
            default:
                return "";
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return LangUtil.getString("Property");
            case 1:
                return LangUtil.getString("Value");
            default:
                return "-";
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return this.getValueAt(0, column).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public void setValueAt(Object newValue, int row, int column) {
        if (row > getProperties().size()) {
            return;
        }
        switch (column) {
            case 0:
                getProperties().get(row).setKey((String) newValue);
                return;
            case 1:
                getProperties().get(row).setValue((String) newValue);
                return;
            default:
        }
    }

    public void addRow(Property property) {
        getProperties().add(property);
        fireTableDataChanged();
    }

    public void removeRow(int row) {
        getProperties().remove(row);
        fireTableDataChanged();
    }

    public List<Property> getProperties() {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        return properties;
    }
}
