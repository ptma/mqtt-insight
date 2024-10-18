package com.mqttinsight.ui.component.model;

import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.ReceivedMqttMessage;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.util.LangUtil;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.function.Consumer;

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
    public static final int COLUMN_SIZE = 6;

    private final int maximum;
    private final List<MqttMessage> messages;

    private MessageViewMode viewMode = MessageViewMode.TABLE;

    public MessageTableModel(final int maximum) {
        this.maximum = maximum;
        messages = Collections.synchronizedList(new ArrayList<>());
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

    public boolean isOverMaximum() {
        return maximum > 0 && messages.size() > maximum;
    }

    @Override
    public int getColumnCount() {
        if (viewMode == MessageViewMode.TABLE) {
            return 7;
        } else {
            return 1;
        }
    }

    public Optional<MqttMessage> getMessage(int index) {
        try {
            return Optional.ofNullable(messages.get(index));
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
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
                    return getMessage(row).map(MqttMessage::getPayload).orElse("");
                case MessageTableModel.COLUMN_TOPIC:
                    return getMessage(row).map(MqttMessage::getTopic).orElse("");
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
            case COLUMN_SIZE:
                return LangUtil.getString("MessageSize");
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
        if (index < 0 || index >= getRowCount()) {
            return null;
        }
        return messages.get(index);
    }

    public synchronized void add(MqttMessage message) {
        messages.add(message);
        int lastIndex = getRowCount() - 1;
        fireTableRowsInserted(lastIndex, lastIndex);
        while (isOverMaximum()) {
            messages.remove(0);
            fireTableRowsDeleted(0, 0);
        }
    }

    public synchronized void add(int index, MqttMessage message) {
        messages.add(index, message);
        fireTableRowsInserted(index, index);

        while (isOverMaximum()) {
            messages.remove(0);
            fireTableRowsDeleted(0, 0);
        }
    }

    public int lastIndexOf(MqttMessage message) {
        return messages.lastIndexOf(message);
    }

    public synchronized void clear() {
        if (messages.size() > 0) {
            messages.clear();
            fireTableDataChanged();
            System.gc();
        }
    }

    public synchronized void remove(int index) {
        if (index < 0 || index >= getRowCount()) {
            return;
        }
        messages.remove(index);
        fireTableRowsDeleted(index, index);
    }

    public synchronized void cleanMessages(Subscription subscription, Consumer<MqttMessage> removedConsumer) {
        Iterator<MqttMessage> itr = messages.iterator();
        boolean changed = false;
        while (itr.hasNext()) {
            MqttMessage message = itr.next();
            if (message instanceof ReceivedMqttMessage && subscription.equals(((ReceivedMqttMessage) message).getSubscription())) {
                itr.remove();
                if (removedConsumer != null) {
                    removedConsumer.accept(message);
                }
                changed = true;
            }
        }
        if (changed) {
            fireTableDataChanged();
        }
        System.gc();
    }

    public synchronized void cleanMessages(String topicPrefix, Consumer<MqttMessage> removedConsumer) {
        Iterator<MqttMessage> itr = messages.iterator();
        boolean changed = false;
        while (itr.hasNext()) {
            MqttMessage message = itr.next();
            if (message.getTopic().startsWith(topicPrefix)) {
                itr.remove();
                if (removedConsumer != null) {
                    removedConsumer.accept(message);
                }
                changed = true;
            }
        }
        if (changed) {
            fireTableDataChanged();
        }
        System.gc();
    }

    @Override
    public void fireTableRowsInserted(int firstRow, int lastRow) {
        try {
            super.fireTableRowsInserted(firstRow, lastRow);
        } catch (IndexOutOfBoundsException ignore) {
            super.fireTableDataChanged();
        }
    }

    @Override
    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        try {
            super.fireTableRowsUpdated(firstRow, lastRow);
        } catch (IndexOutOfBoundsException ignore) {
            super.fireTableDataChanged();
        }
    }

    @Override
    public void fireTableRowsDeleted(int firstRow, int lastRow) {
        try {
            super.fireTableRowsDeleted(firstRow, lastRow);
        } catch (IndexOutOfBoundsException ignore) {
            super.fireTableDataChanged();
        }
    }
}
