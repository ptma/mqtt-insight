package com.mqttinsight.ui.component.renderer;

import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.PublishedMqttMessage;
import com.mqttinsight.ui.component.model.MessageTableModel;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.Utils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.JRendererLabel;
import org.jdesktop.swingx.renderer.StringValue;

import javax.swing.*;
import java.awt.*;

/**
 * @author ptma
 */
public class TableViewRendererProvider extends ComponentProvider<JLabel> {

    private static final Color PUBLISH_BG = UIManager.getBoolean("laf.dark") ? Color.decode("#133918") : Color.decode("#C5EBCA");

    private MessageTableModel tableModel;

    public TableViewRendererProvider(MessageTableModel tableModel) {
        this(tableModel, null);
    }

    public TableViewRendererProvider(MessageTableModel tableModel, StringValue converter) {
        this(tableModel, converter, JLabel.LEADING);
    }

    public TableViewRendererProvider(MessageTableModel tableModel, int alignment) {
        this(tableModel, null, alignment);
    }

    public TableViewRendererProvider(MessageTableModel tableModel, StringValue converter, int alignment) {
        super(converter, alignment);
        this.tableModel = tableModel;
    }

    @Override
    protected JLabel createRendererComponent() {
        JRendererLabel label = new JRendererLabel();
        label.setOpaque(true);
        label.setHorizontalAlignment(JLabel.LEFT);
        label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 10));
        return label;
    }

    @Override
    protected void configureState(CellContext context) {
        rendererComponent.setHorizontalAlignment(getHorizontalAlignment());
    }

    @Override
    protected void format(CellContext context) {
        JXTable table = (JXTable) context.getComponent();
        int modelIndex = table.convertRowIndexToModel(context.getRow());
        MqttMessage message = tableModel.get(modelIndex);
        if (context.isSelected()) {
            rendererComponent.setForeground(table.getSelectionForeground());
            rendererComponent.setBackground(table.getSelectionBackground());
        } else {
            Color bgColor = message.getColor();
            if (message instanceof PublishedMqttMessage) {
                Color fgColor = Utils.getReverseForegroundColor(PUBLISH_BG);
                rendererComponent.setBackground(PUBLISH_BG);
                rendererComponent.setForeground(fgColor);
            } else if (bgColor != null) {
                rendererComponent.setForeground(Utils.getReverseForegroundColor(bgColor));
                rendererComponent.setBackground(bgColor);
            }
        }
        int columnIndex = (Integer) table.getColumnExt(context.getColumn()).getClientProperty("columnIndex");
        switch (columnIndex) {
            case MessageTableModel.COLUMN_TYPE:
                rendererComponent.setText("");
                rendererComponent.setIcon(message.getMessageType().getIcon());
                rendererComponent.setHorizontalAlignment(JLabel.CENTER);
                break;
            case MessageTableModel.COLUMN_TOPIC:
                rendererComponent.setText(message.getTopic());
                rendererComponent.setIcon(null);
                rendererComponent.setHorizontalAlignment(JLabel.LEFT);
                break;
            case MessageTableModel.COLUMN_PAYLOAD:
                rendererComponent.setText(message.payloadAsString(false));
                rendererComponent.setIcon(null);
                rendererComponent.setHorizontalAlignment(JLabel.LEFT);
                break;
            case MessageTableModel.COLUMN_QOS:
                rendererComponent.setText(String.valueOf(message.getQos()));
                rendererComponent.setIcon(null);
                rendererComponent.setHorizontalAlignment(JLabel.CENTER);
                break;
            case MessageTableModel.COLUMN_RETAINED:
                rendererComponent.setText("");
                rendererComponent.setIcon(message.isRetained() ? Icons.CHECKBOX_CHECKED : Icons.CHECKBOX);
                rendererComponent.setHorizontalAlignment(JLabel.CENTER);
                break;
            case MessageTableModel.COLUMN_TIME:
                rendererComponent.setText(message.getTime());
                rendererComponent.setIcon(null);
                rendererComponent.setHorizontalAlignment(JLabel.CENTER);
                break;
            default:
                rendererComponent.setIcon(null);
                rendererComponent.setText("");
        }
    }

}
