package com.mqttinsight.ui.form.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.exception.VerificationException;
import com.mqttinsight.mqtt.PublishedItem;
import com.mqttinsight.mqtt.PublishedMqttMessage;
import com.mqttinsight.ui.component.SyntaxTextEditor;
import com.mqttinsight.ui.component.model.MessageViewMode;
import com.mqttinsight.ui.component.model.PayloadFormatComboBoxModel;
import com.mqttinsight.ui.component.renderer.TextableListRenderer;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.util.*;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaDefaultInputMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author ptma
 */
public class MessagePublishPanel extends JPanel {

    private final MqttInstance mqttInstance;
    private MigLayout topPanelLayout;
    private SyntaxTextEditor payloadEditor;
    private JPanel topPanel;
    private JComboBox<PublishedItem> topicComboBox;
    private JLabel qosLabel;
    private JComboBox<Integer> qosComboBox;
    private JCheckBox retainedCheckBox;
    private JLabel formatLabel;
    private JComboBox<String> formatComboBox;
    private JPanel payloadPanel;
    private JButton publishButton;
    private JPanel buttonPanel;

    public MessagePublishPanel(MqttInstance mqttInstance) {
        this.mqttInstance = mqttInstance;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel = new JPanel();
        payloadPanel = new JPanel(new BorderLayout(0, 0));
        buttonPanel = new JPanel(new BorderLayout(0, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        add(topPanel, BorderLayout.NORTH);
        add(payloadPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        topPanelLayout = new MigLayout(
            "insets 0 0 5 0,gap 5",
            "[grow][][][][][]",
            "[]"
        );
        topPanel.setLayout(topPanelLayout);

        topicComboBox = new JComboBox<>();
        topicComboBox.setEditable(true);
        topicComboBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, LangUtil.getString("Topic"));
        loadPublishedTopics();
        topicComboBox.setSelectedItem("");
        topicComboBox.setRenderer(new TextableListRenderer());
        topicComboBox.addActionListener(e -> {
            if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
                Object sel = topicComboBox.getSelectedItem();
                if (sel instanceof PublishedItem) {
                    PublishedItem item = (PublishedItem) sel;
                    payloadEditor.setText(item.getPayload());
                    qosComboBox.setSelectedItem(item.getQos());
                    formatComboBox.setSelectedItem(item.getPayloadFormat());
                }
            }
        });
        topPanel.add(topicComboBox, "growx");

        qosLabel = new JLabel("QoS");
        topPanel.add(qosLabel, "");
        qosComboBox = new JComboBox<>();
        qosComboBox.addItem(0);
        qosComboBox.addItem(1);
        qosComboBox.addItem(2);
        qosComboBox.setSelectedIndex(0);
        topPanel.add(qosComboBox, "wmax 50px");

        retainedCheckBox = new JCheckBox(LangUtil.getString("Retained"));
        topPanel.add(retainedCheckBox, "");

        formatLabel = new JLabel(LangUtil.getString("PayloadFormat"));
        topPanel.add(formatLabel, "right");
        formatComboBox = new JComboBox<>();
        formatComboBox.setModel(new PayloadFormatComboBoxModel(false, true));
        formatComboBox.setSelectedItem(mqttInstance.getPayloadFormat());
        formatComboBox.addActionListener(e -> {
            if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
                String format = (String) formatComboBox.getSelectedItem();
                CodecSupport codec = CodecSupports.instance().getByName(format);
                payloadEditor.setSyntax(codec.getSyntax());
            }
        });
        topPanel.add(formatComboBox, "");

        payloadEditor = new SyntaxTextEditor();
        payloadPanel.add(payloadEditor, BorderLayout.CENTER);

        publishButton = new JButton(LangUtil.getString("PublishMessage") + " (Ctrl + Enter)", Icons.SEND_GREEN);
        publishButton.addActionListener(e -> publishMessage());
        buttonPanel.add(publishButton, BorderLayout.WEST);

        // Register shortcut
        InputMap inputMap = payloadEditor.textArea().getInputMap();
        while (inputMap != null) {
            inputMap = inputMap.getParent();
            if (inputMap instanceof RSyntaxTextAreaDefaultInputMap) {
                // remove key binding
                inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK));
                break;
            }
        }
        publishButton.registerKeyboardAction(e -> {
                publishMessage();
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        mqttInstance.addEventListener(new InstanceEventAdapter() {
            @Override
            public void onCodecsChanged() {
                SwingUtilities.invokeLater(() -> {
                    formatComboBox.setModel(new PayloadFormatComboBoxModel(false, true));
                });
            }
        });
    }

    private void loadPublishedTopics() {
        List<PublishedItem> publishedHistory = mqttInstance.getProperties().getPublishedHistory();
        if (publishedHistory != null) {
            topicComboBox.removeAllItems();
            publishedHistory.sort(Comparator.comparing(PublishedItem::getTopic));
            publishedHistory.forEach(topic -> topicComboBox.addItem(topic));
        }
    }

    private void addPublishedTopic(String topic, String payload, int qos, boolean retained, String payloadFormat) {
        List<PublishedItem> publishedHistory = mqttInstance.getProperties().getPublishedHistory();
        if (publishedHistory == null) {
            publishedHistory = new ArrayList<>();
            mqttInstance.getProperties().setPublishedHistory(publishedHistory);
            Configuration.instance().changed();
        }
        publishedHistory.removeIf(t -> t.getTopic().equals(topic));
        PublishedItem newItem = new PublishedItem(topic, payload, qos, retained, payloadFormat);
        publishedHistory.add(newItem);
        topicComboBox.removeAllItems();
        publishedHistory.sort(Comparator.comparing(PublishedItem::getTopic));
        publishedHistory.forEach(item -> topicComboBox.addItem(item));
        topicComboBox.setSelectedItem(newItem);
    }

    public void toggleViewMode(MessageViewMode viewMode) {
        if (viewMode == MessageViewMode.TABLE) {
            topPanelLayout.setColumnConstraints("[grow][][][][][]");
            topPanelLayout.setRowConstraints("[]");
            topPanelLayout.setComponentConstraints(topicComboBox, "growx");
        } else {
            topPanelLayout.setColumnConstraints("[][][][grow][]");
            topPanelLayout.setRowConstraints("[][]");
            topPanelLayout.setComponentConstraints(topicComboBox, "growx, span, wrap");
        }
    }

    private void verifyFields() throws VerificationException {
        Validator.notEmpty(topicComboBox, () -> LangUtil.format("FieldRequiredValidation", LangUtil.getString("Topic")));
        TopicUtil.validate(topicComboBox.getSelectedItem().toString());
    }

    public void publishMessage() {
        try {
            verifyFields();
        } catch (VerificationException e) {
            Utils.Toast.warn(e.getMessage());
            return;
        }
        SwingUtilities.invokeLater(() -> {
            String topic = topicComboBox.getSelectedItem().toString();
            String payloadString = payloadEditor.getText();
            int qos = qosComboBox.getSelectedIndex();
            boolean retained = retainedCheckBox.isSelected();
            String format = (String) formatComboBox.getSelectedItem();
            byte[] payload = CodecSupports.instance().getByName(format).toPayload(payloadString);
            mqttInstance.publishMessage(PublishedMqttMessage.of(
                topic,
                payload,
                qos,
                retained,
                format
            ));
            addPublishedTopic(topic, payloadString, qos, retained, format);
        });
    }

}
