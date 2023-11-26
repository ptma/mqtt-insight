package com.mqttinsight.ui.component.model;

import com.mqttinsight.mqtt.SecureMode;
import com.mqttinsight.ui.chart.series.MessageSeriesDefinition;
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
public class CountSeriesTableModel extends AbstractTableModel {

    private List<MessageSeriesDefinition> series;

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public int getRowCount() {
        return getSeries().size();
    }

    public void clear() {
        getSeries().clear();
    }

    @Override
    public Object getValueAt(int row, int column) {
        return switch (column) {
            case 0 -> getSeries().get(row).getSeriesName();
            case 1 -> getSeries().get(row).isDynamic();
            case 2 -> getSeries().get(row).getMatch().getText();
            case 3 -> getSeries().get(row).getMatchType().getText();
            case 4 -> getSeries().get(row).getMatchExpression().toString();
            default -> "";
        };
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> LangUtil.getString("SeriesName");
            case 1 -> LangUtil.getString("Dynamic");
            case 2 -> LangUtil.getString("Match");
            case 3 -> LangUtil.getString("Type");
            case 4 -> LangUtil.getString("Expression");
            default -> "-";
        };
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 1) {
            return Boolean.class;
        }
        return Object.class;
    }

    public void addRow(MessageSeriesDefinition series) {
        getSeries().add(series);
        fireTableDataChanged();
    }

    public void removeRow(int row) {
        getSeries().remove(row);
        fireTableRowsDeleted(row, row);
    }

    public void removeAll() {
        int count = getSeries().size();
        if (count > 0) {
            getSeries().clear();
            fireTableRowsDeleted(0, count - 1);
        }
    }

    public MessageSeriesDefinition getRow(int row) {
        return getSeries().get(row);
    }

    public List<MessageSeriesDefinition> getSeries() {
        if (series == null) {
            series = new ArrayList<>();
        }
        return series;
    }
}
