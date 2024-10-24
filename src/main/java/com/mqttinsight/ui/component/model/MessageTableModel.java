package com.mqttinsight.ui.component.model;

import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.ReceivedMqttMessage;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;
import lombok.Setter;

import javax.swing.table.AbstractTableModel;
import java.util.*;

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

    private final MqttInstance mqttInstance;
    private final int maximum;
    @Getter
    private final List<MqttMessage> messages;

    @Getter
    @Setter
    private MessageViewMode viewMode = MessageViewMode.TABLE;

    public MessageTableModel(MqttInstance mqttInstance, final int maximum) {
        this.mqttInstance = mqttInstance;
        this.maximum = maximum;
        messages = Collections.synchronizedList(new ArrayList<>());
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
            return switch (column) {
                case MessageTableModel.COLUMN_PAYLOAD -> getMessage(row).map(MqttMessage::getPayload).orElse("");
                case MessageTableModel.COLUMN_TOPIC -> getMessage(row).map(MqttMessage::getTopic).orElse("");
                default -> "";
            };
        } else {
            return messages.get(row).getTopic() + " " + messages.get(row).getPayload();
        }
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case COLUMN_TYPE -> "";
            case COLUMN_TOPIC -> LangUtil.getString("Topic");
            case COLUMN_PAYLOAD -> LangUtil.getString("Payload");
            case COLUMN_QOS -> LangUtil.getString("QoS");
            case COLUMN_RETAINED -> LangUtil.getString("Retained");
            case COLUMN_TIME -> LangUtil.getString("ReceivedTime");
            case COLUMN_SIZE -> LangUtil.getString("MessageSize");
            default -> "-";
        };
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == COLUMN_RETAINED) {
            return Boolean.class;
        }
        return Object.class;
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
            MqttMessage removed = messages.remove(0);
            mqttInstance.applyEvent(l -> l.onMessageRemoved(removed));
            fireTableRowsDeleted(0, 0);
        }
    }

    public synchronized void add(int index, MqttMessage message) {
        messages.add(index, message);
        fireTableRowsInserted(index, index);

        while (isOverMaximum()) {
            MqttMessage removed = messages.remove(0);
            mqttInstance.applyEvent(l -> l.onMessageRemoved(removed));
            fireTableRowsDeleted(0, 0);
        }
    }

    public int lastIndexOf(MqttMessage message) {
        return messages.lastIndexOf(message);
    }

    public synchronized void clear() {
        Iterator<MqttMessage> itr = messages.iterator();
        boolean changed = false;
        while (itr.hasNext()) {
            MqttMessage message = itr.next();
            itr.remove();
            mqttInstance.applyEvent(l -> l.onMessageRemoved(message));
            changed = true;
        }
        if (changed) {
            fireTableDataChanged();
            System.gc();
        }
    }

    public synchronized void remove(int index) {
        if (index < 0 || index >= getRowCount()) {
            return;
        }
        MqttMessage removed = messages.remove(index);
        mqttInstance.applyEvent(l -> l.onMessageRemoved(removed));
        fireTableRowsDeleted(index, index);
    }

    public synchronized void cleanMessages(Subscription subscription) {
        Iterator<MqttMessage> itr = messages.iterator();
        boolean changed = false;
        while (itr.hasNext()) {
            MqttMessage message = itr.next();
            if (message instanceof ReceivedMqttMessage && subscription.equals(((ReceivedMqttMessage) message).getSubscription())) {
                itr.remove();
                mqttInstance.applyEvent(l -> l.onMessageRemoved(message));
                changed = true;
            }
        }
        if (changed) {
            fireTableDataChanged();
            System.gc();
        }
    }

    public synchronized void cleanMessages(String topicPrefix) {
        Iterator<MqttMessage> itr = messages.iterator();
        boolean changed = false;
        while (itr.hasNext()) {
            MqttMessage message = itr.next();
            if (message.getTopic().startsWith(topicPrefix)) {
                itr.remove();
                mqttInstance.applyEvent(l -> l.onMessageRemoved(message));
                changed = true;
            }
        }
        if (changed) {
            fireTableDataChanged();
            System.gc();
        }
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
