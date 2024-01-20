package com.mqttinsight.ui.form;

import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.component.StatePersistenceFrame;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.ui.form.panel.BasePreviewPanel;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Const;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author ptma
 */
public class MessagePreviewFrame extends StatePersistenceFrame {

    private static final Dimension MIN_DIMENSION = new Dimension(150, 100);
    private static final Dimension DEFAULT_DIMENSION = new Dimension(800, 600);

    protected final MqttInstance mqttInstance;
    private final BasePreviewPanel previewPanel;

    private InstanceEventAdapter instanceEventAdapter;

    public static void open(MqttInstance mqttInstance, MqttMessage mqttMessage) {
        JFrame dialog = new MessagePreviewFrame(mqttInstance, mqttMessage);
        dialog.setMinimumSize(MIN_DIMENSION);
        dialog.setPreferredSize(DEFAULT_DIMENSION);
        dialog.setResizable(true);
        dialog.pack();
        dialog.setLocationRelativeTo(MqttInsightApplication.frame);
        dialog.setVisible(true);
    }

    protected MessagePreviewFrame(MqttInstance mqttInstance, MqttMessage mqttMessage) {
        super();
        setIconImages(Icons.WINDOW_ICON);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(Const.APP_NAME + " - " + LangUtil.getString("MessagePreview"));
        this.mqttInstance = mqttInstance;
        this.previewPanel = new BasePreviewPanel(mqttInstance);
        setContentPane(this.previewPanel);
        previewPanel.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        initEventListeners();

        SwingUtilities.invokeLater(() -> {
            this.previewPanel.previewMessageDirectly(mqttMessage);
        });
    }

    @Override
    protected String getConfigKeyPrefix() {
        return "MessagePreviewFrame_";
    }

    @Override
    public void dispose() {
        mqttInstance.removeEventListener(instanceEventAdapter);
        super.dispose();
    }

    private void initEventListeners() {
        instanceEventAdapter = new InstanceEventAdapter() {
            @Override
            public void payloadFormatChanged() {
                previewPanel.updatePreviewMessage();
            }

            @Override
            public void onCodecsChanged() {
                previewPanel.reloadFormatCombo();
            }
        };
        mqttInstance.addEventListener(instanceEventAdapter);
        initWindowsListener();
    }

}
