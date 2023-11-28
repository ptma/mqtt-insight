package com.mqttinsight.ui.chart.series;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ptma
 */
public abstract class AbstractSeriesTableModel<T extends SeriesProperties> extends AbstractTableModel {

    private List<T> series;

    @Override
    public int getRowCount() {
        return getSeries().size();
    }

    public void clear() {
        removeAll();
    }

    public void addRow(T properties) {
        getSeries().add(properties);
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

    public T getRow(int row) {
        return getSeries().get(row);
    }

    public List<T> getSeries() {
        if (series == null) {
            series = new ArrayList<>();
        }
        return series;
    }
}
