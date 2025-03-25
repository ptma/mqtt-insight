package com.mqttinsight.ui.component.model;

import com.mqttinsight.mqtt.FavoriteSubscription;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * @author ptma
 */
@Getter
public class SubscriptionTableModel extends AbstractTableModel {

    private final List<FavoriteSubscription> subscriptions;

    public SubscriptionTableModel(List<FavoriteSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public int appendRow() {
        subscriptions.add(new FavoriteSubscription("", 0));
        int row = subscriptions.size() - 1;
        fireTableRowsInserted(row, row);
        return row;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getRowCount() {
        return subscriptions.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        return switch (column) {
            case 0 -> subscriptions.get(row).getTopic();
            case 1 -> subscriptions.get(row).getQos();
            case 2 -> subscriptions.get(row).getPayloadFormat();
            default -> "";
        };
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> LangUtil.getString("Topic");
            case 1 -> LangUtil.getString("QoS");
            case 2 -> LangUtil.getString("PayloadFormat");
            default -> "-";
        };
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return String.class;
    }

    public FavoriteSubscription getRow(int row) {
        return subscriptions.get(row);
    }

    public void addRow(FavoriteSubscription subscription) {
        subscriptions.add(subscription);
        int insertedRow = subscriptions.size() - 1;
        fireTableRowsInserted(insertedRow, insertedRow);
    }

    public void removeRow(int row) {
        subscriptions.remove(row);
        fireTableRowsDeleted(row, row);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            subscriptions.get(rowIndex).setTopic((String) aValue);
        } else if (columnIndex == 1) {
            subscriptions.get(rowIndex).setQos((Integer) aValue);
        } else if (columnIndex == 2) {
            subscriptions.get(rowIndex).setPayloadFormat((String) aValue);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
}
