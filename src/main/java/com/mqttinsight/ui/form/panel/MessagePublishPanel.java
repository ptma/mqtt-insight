package com.mqttinsight.ui.form.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.exception.VerificationException;
import com.mqttinsight.mqtt.PublishedMqttMessage;
import com.mqttinsight.ui.component.SyntaxTextEditor;
import com.mqttinsight.ui.component.model.MessageViewMode;
import com.mqttinsight.ui.component.model.PayloadFormatComboBoxModel;
import com.mqttinsight.ui.component.renderer.TextableListRenderer;
import com.mqttinsight.util.*;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaDefaultInputMap;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ptma
 */
public class MessagePublishPanel extends JPanel {

    private final MqttInstance mqttInstance;
    private MigLayout topPanelLayout;
    private SyntaxTextEditor payloadEditor;
    private JPanel topPanel;
    private JComboBox<String> topicComboBox;
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
        AutoCompleteDecorator.decorate(topicComboBox);
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
        formatComboBox.setModel(new PayloadFormatComboBoxModel(false));
        formatComboBox.setSelectedItem(mqttInstance.getPayloadFormat());
        topPanel.add(formatComboBox, "");

        payloadEditor = new SyntaxTextEditor();
        payloadPanel.add(payloadEditor, BorderLayout.CENTER);

        publishButton = new JButton(Icons.SEND_GREEN);
        LangUtil.buttonText(publishButton, "PublishMessage");
        publishButton.addActionListener(e -> publishMessage());
        buttonPanel.add(publishButton, BorderLayout.WEST);

        // Register shortcut
        InputMap inputMap = payloadEditor.textArea().getInputMap();
        while (inputMap != null) {
            inputMap = inputMap.getParent();
            if (inputMap != null && inputMap instanceof RSyntaxTextAreaDefaultInputMap) {
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
    }
    
    private void loadPublishedTopics() {
        List<String> publishedTopics = mqttInstance.getProperties().getPublishedTopics();
        if (publishedTopics != null) {
            topicComboBox.removeAllItems();
            publishedTopics.sort(String::compareTo);
            publishedTopics.forEach(topic -> topicComboBox.addItem(topic));
        }
    }
    
    private void addPublishedTopic(String newTopic) {
        List<String> publishedTopics = mqttInstance.getProperties().getPublishedTopics();
        if (publishedTopics == null) {
            publishedTopics = new ArrayList<>();
            mqttInstance.getProperties().setPublishedTopics(publishedTopics);
            Configuration.instance().changed();
        }
        if (!publishedTopics.contains(newTopic)) {
            publishedTopics.add(newTopic);
            topicComboBox.removeAllItems();
            publishedTopics.sort(String::compareTo);
            publishedTopics.forEach(topic -> topicComboBox.addItem(topic));
            topicComboBox.setSelectedItem(newTopic);
        }
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
        TopicUtil.validate((String) topicComboBox.getSelectedItem());
    }

    public void publishMessage() {
        try {
            verifyFields();
        } catch (VerificationException e) {
            Utils.Toast.warn(e.getMessage());
            return;
        }
        SwingUtilities.invokeLater(() -> {
            String topic = (String) topicComboBox.getSelectedItem();
            String payloadString = payloadEditor.getText();
            String format = (String) formatComboBox.getSelectedItem();
            byte[] payload = CodecSupports.instance().getByName(format).toPayload(payloadString);
            mqttInstance.publishMessage(PublishedMqttMessage.of(
                topic,
                payload,
                qosComboBox.getSelectedIndex(),
                retainedCheckBox.isSelected(),
                format
            ));
            addPublishedTopic(topic);
        });
    }

}
