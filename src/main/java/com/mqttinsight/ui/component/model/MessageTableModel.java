package com.mqttinsight.ui.component.model;

import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.ReceivedMqttMessage;
import com.mqttinsight.mqtt.SizeLimitSynchronizedList;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.util.LangUtil;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * @author ptma
 * @see ReceivedMqttMessage
 */
public class MessageTableModel extends AbstractTableModel {

    public static final int COLUMN_TYPE = 0;
    public static final int COLUMN_TOPIC = 1;
    public static final int COLUMN_PAYLOAD = 2;
    public static final int COLUMN_QOS = 3;
    public static final int COLUMN_RETAINED = 4;
    public static final int COLUMN_TIME = 5;

    private final SizeLimitSynchronizedList<MqttMessage> messages;

    private MessageViewMode viewMode = MessageViewMode.TABLE;

    public MessageTableModel(final int maximum) {
        messages = new SizeLimitSynchronizedList<>(maximum);
    }

    public List<MqttMessage> getMessages() {
        return messages;
    }

    public void setViewMode(MessageViewMode viewMode) {
        this.viewMode = viewMode;
    }

    public MessageViewMode getViewMode() {
        return viewMode;
    }

    @Override
    public int getColumnCount() {
        if (viewMode == MessageViewMode.TABLE) {
            return 6;
        } else {
            return 1;
        }
    }

    @Override
    public int getRowCount() {
        return messages.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        // The return value is used for searching, not for rendering
        if (viewMode == MessageViewMode.TABLE) {
            switch (column) {
                case MessageTableModel.COLUMN_PAYLOAD:
                    return messages.get(row).getPayload();
                case MessageTableModel.COLUMN_TOPIC:
                    return messages.get(row).getTopic();
                default:
                    return "";
            }
        } else {
            return messages.get(row).getTopic() + " " + messages.get(row).getPayload();
        }

    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case COLUMN_TYPE:
                return "";
            case COLUMN_TOPIC:
                return LangUtil.getString("Topic");
            case COLUMN_PAYLOAD:
                return LangUtil.getString("Payload");
            case COLUMN_QOS:
                return LangUtil.getString("QoS");
            case COLUMN_RETAINED:
                return LangUtil.getString("Retained");
            case COLUMN_TIME:
                return LangUtil.getString("ReceivedTime");
            default:
                return "-";
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case COLUMN_RETAINED:
                return Boolean.class;
            default:
                return Object.class;
        }
    }

    public MqttMessage get(int index) {
        if (index < 0 || index >= messages.size()) {
            return null;
        }
        return messages.get(index);
    }

    public void add(MqttMessage message) {
        while (messages.isMaximum()) {
            messages.remove(0);
            fireTableRowsDeleted(0, 0);
        }
        messages.add(message);
        int lastIndex = messages.size() - 1;
        fireTableRowsInserted(lastIndex, lastIndex);
    }

    public void add(int index, MqttMessage message) {
        int insertIndex = index;
        while (messages.isMaximum()) {
            messages.remove(0);
            fireTableRowsDeleted(0, 0);
            insertIndex--;
        }
        if (insertIndex >= 0) {
            messages.add(index, message);
            fireTableRowsInserted(index, index);
        } else {
            messages.add(message);
            int lastIndex = messages.size() - 1;
            fireTableRowsInserted(lastIndex, lastIndex);
        }
    }

    public int lastIndexOf(MqttMessage message) {
        return messages.lastIndexOf(message);
    }

    public void clear() {
        if (messages.size() > 0) {
            int lastRow = messages.size() - 1;
            messages.clear();
            fireTableRowsDeleted(0, lastRow);
        }
    }

    public void remove(int index) {
        if (index < 0 || index >= messages.size()) {
            return;
        }
        messages.remove(index);
        fireTableRowsDeleted(index, index);
    }

    public void cleanMessages(Subscription subscription) {
        int len = messages.size();
        for (int i = len - 1; i >= 0; i--) {
            MqttMessage message = messages.get(i);
            if (message instanceof ReceivedMqttMessage && subscription.equals(((ReceivedMqttMessage) message).getSubscription())) {
                remove(i);
            }
        }
    }

}
