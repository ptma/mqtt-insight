package com.mqttinsight.ui.component.model;

import com.mqttinsight.codec.DynamicCodec;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ptma
 */
@Getter
@Setter
@NoArgsConstructor
public class CodecTableModel extends AbstractTableModel {

    private List<DynamicCodec> codecs = new ArrayList<>();

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getRowCount() {
        return codecs.size();
    }

    public void clear() {
        codecs.clear();
    }

    @Override
    public Object getValueAt(int row, int column) {
        return switch (column) {
            case 0 -> codecs.get(row).getName();
            case 1 -> codecs.get(row).getType();
            case 2 -> codecs.get(row).getSchemaFile();
            default -> "";
        };
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> LangUtil.getString("Name");
            case 1 -> LangUtil.getString("Type");
            case 2 -> LangUtil.getString("SchemaFile");
            default -> "-";
        };
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return String.class;
    }

    public DynamicCodec getRow(int row) {
        return codecs.get(row);
    }

    public void addRow(DynamicCodec codec) {
        codecs.add(codec);
        int insertedRow = codecs.size() - 1;
        fireTableRowsInserted(insertedRow, insertedRow);
    }

    public void removeRow(int row) {
        codecs.remove(row);
        fireTableRowsDeleted(row, row);
    }
}
