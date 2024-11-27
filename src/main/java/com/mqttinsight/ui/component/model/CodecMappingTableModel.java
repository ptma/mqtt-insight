package com.mqttinsight.ui.component.model;

import com.mqttinsight.codec.proto.MappingField;
import com.mqttinsight.mqtt.Property;
import com.mqttinsight.mqtt.SecureMode;
import com.mqttinsight.util.LangUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * @author ptma
 */
public class CodecMappingTableModel extends AbstractTableModel {

    private static final List<MappingField> DEFAULT_FIELDS = List.of(MappingField.of("topic", "Topic", 200));

    @Getter
    private List<MappingField> fields = DEFAULT_FIELDS;

    @Getter
    private List<Map<String, String>> rows = new ArrayList<>();

    public CodecMappingTableModel() {
    }

    public void setFields(List<MappingField> fields) {
        if (fields != null && !fields.isEmpty()) {
            this.fields = fields;
        } else {
            this.fields = DEFAULT_FIELDS;
        }
        fireTableStructureChanged();
    }

    @Override
    public int getColumnCount() {
        return fields.size();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    public void clear() {
        rows.clear();
        fireTableDataChanged();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row >= 0 && row < rows.size() && column >= 0 && column < fields.size()) {
            return Optional.ofNullable(rows.get(row).get(fields.get(column).getKey()))
                .orElse("");
        } else {
            return "";
        }
    }

    @Override
    public String getColumnName(int column) {
        if (column >= 0 && column < fields.size()) {
            return LangUtil.getString(fields.get(column).getTitle());
        } else {
            return "-";
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public void setValueAt(Object newValue, int row, int column) {
        if (row >= 0 && row < rows.size() && column >= 0 && column < fields.size()) {
            rows.get(row).put(fields.get(column).getKey(), (String) newValue);
            fireTableRowsUpdated(row, row);
        }
    }

    public void addRow(Map<String, String> row) {
        this.rows.add(row);
        fireTableRowsInserted(rows.size(), rows.size());
    }

    public void addAll(List<Map<String, String>> rows) {
        int start = this.rows.size();
        this.rows.addAll(rows);
        fireTableRowsInserted(start, rows.size());
    }

    public void addEmptyRow() {
        Map<String, String> emptyRow = new HashMap<>();
        fields.forEach(field -> {
            emptyRow.put(field.getKey(), "");
        });
        this.addRow(emptyRow);
    }

    public void removeRow(int row) {
        rows.remove(row);
        fireTableRowsDeleted(row, row);
    }

}
