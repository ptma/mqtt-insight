package com.mqttinsight.ui.component.renderer;

import cn.hutool.core.date.DatePattern;
import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.MessageType;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.PublishedMqttMessage;
import com.mqttinsight.ui.component.MessageTable;
import com.mqttinsight.ui.component.model.MessageTableModel;
import com.mqttinsight.util.Utils;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.StringValue;

import javax.swing.*;
import java.awt.*;

/**
 * @author ptma
 */
public class DialogueViewRendererProvider extends ComponentProvider<DialogueBubble> {

    private static final boolean DARK_LAF = UIManager.getBoolean("laf.dark");
    private static final Color PUBLISH_BG = DARK_LAF ? Color.decode("#133918") : Color.decode("#C5EBCA");
    private static final String TIME_FORMAT = Configuration.instance().getString(ConfKeys.TIME_FORMAT, DatePattern.NORM_DATETIME_MS_PATTERN);

    private MessageTableModel messageModel;

    public DialogueViewRendererProvider(MessageTableModel messageModel) {
        this(messageModel, null);
    }

    public DialogueViewRendererProvider(MessageTableModel messageModel, StringValue converter) {
        super(converter);
        this.messageModel = messageModel;
    }

    @Override
    protected DialogueBubble createRendererComponent() {
        return new DialogueBubble(Configuration.instance().getInt(ConfKeys.MAX_MESSAGE_ROWS, 0));
    }

    @Override
    protected void configureState(CellContext context) {
        // do nothing
    }

    @Override
    public String getString(Object value) {
        if (value instanceof MqttMessage) {
            return ((MqttMessage) value).getPayload();
        } else {
            return super.getString(value);
        }
    }

    @Override
    protected void format(CellContext context) {
        MessageTable table = (MessageTable) context.getComponent();
        int modelIndex = table.convertRowIndexToModel(context.getRow());
        MqttMessage message = messageModel.get(modelIndex);
        if (message == null) {
            return;
        }
        if (rendererComponent instanceof DialogueBubble) {
            DialogueBubble itemComponent = (DialogueBubble) rendererComponent;
            itemComponent.setTopic(message.getTopic());
            itemComponent.setQos(message.getQos());
            itemComponent.setTime(message.timeWithFormat(TIME_FORMAT));
            itemComponent.setPayload(message.getPayload());
            if (message instanceof PublishedMqttMessage) {
                itemComponent.setBodyAlignment("right");
                Color fgColor = Utils.getReverseForegroundColor(PUBLISH_BG, DARK_LAF);
                itemComponent.setBodyBackground(PUBLISH_BG);
                itemComponent.setBodyForeground(fgColor);
            } else {
                itemComponent.setBodyAlignment("left");
            }
            if (MessageType.PUBLISHED_SCRIPT.equals(message.getMessageType()) || MessageType.RECEIVED_SCRIPT.equals(message.getMessageType())) {
                itemComponent.setIcon(message.getMessageType().getIcon());
            } else {
                itemComponent.setIcon(null);
            }
            Color bgColor = message.getColor();
            if (bgColor != null) {
                Color fgColor = Utils.getReverseForegroundColor(bgColor, DARK_LAF);
                itemComponent.setBodyBackground(bgColor);
                itemComponent.setBodyForeground(fgColor);
            }
            if (context.isSelected()) {
                rendererComponent.setBackground(table.getSelectionBackground());
            } else {
                rendererComponent.setBackground(table.getBackground());
            }
            int componentHeight = itemComponent.getPreferredSize().height;
            if (table.getRowHeight(context.getRow()) != componentHeight) {
                table.setRowHeight(context.getRow(), componentHeight);
                table.rowHeightChanged(context.getRow());
            }
        }
    }

}
