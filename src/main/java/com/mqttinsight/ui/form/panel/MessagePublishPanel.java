package com.mqttinsight.ui.form.panel;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
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
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaDefaultInputMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * @author ptma
 */
@Slf4j
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
    private JLabel tipsLabel;
    private JPanel bottomPanel;

    public MessagePublishPanel(MqttInstance mqttInstance) {
        this.mqttInstance = mqttInstance;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel = new JPanel();
        payloadPanel = new JPanel(new BorderLayout(0, 0));
        bottomPanel = new JPanel(new MigLayout(
            "insets 0 0 0 0,gap 5",
            "[grow][][]",
            "[]"
        ));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        add(topPanel, BorderLayout.NORTH);
        add(payloadPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

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
        bottomPanel.add(publishButton, "cell 2 0");
        tipsLabel = new JLabel();
        tipsLabel.setIcon(Icons.TIPS);
        tipsLabel.setToolTipText(LangUtil.getString("PublishTips"));
        bottomPanel.add(tipsLabel, "cell 1 0");

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
            try {
                String topic = topicComboBox.getSelectedItem().toString();
                String originalPayload = payloadEditor.getText();
                String replacedPayload = originalPayload;

                {
                    // replace variables
                    replacedPayload = StrUtil.replace(replacedPayload, "\\$\\{timestamp\\}", (p) -> System.currentTimeMillis() + "");
                    replacedPayload = StrUtil.replace(replacedPayload, "\\$\\{uuid\\}", (p) -> UUID.randomUUID().toString());
                    replacedPayload = StrUtil.replace(replacedPayload, "\\$\\{int(\\(\\s*(\\d*)\\s*,\\s*?(\\d*)\\s*\\))?\\}", (matcher) -> {
                        long min = NumberUtil.isLong(matcher.group(2)) ? Long.parseLong(matcher.group(2)) : 0;
                        long max = NumberUtil.isLong(matcher.group(3)) ? Long.parseLong(matcher.group(3)) : 100;
                        if (min > max) {
                            long temp = min;
                            min = max;
                            max = temp;
                        }
                        return RandomUtil.randomLong(min, max) + "";
                    });
                    replacedPayload = StrUtil.replace(replacedPayload, "\\$\\{float(\\(\\s*([\\-\\+]?\\d+(\\.?\\d+)?)\\s*,\\s*?([\\-\\+]?\\d+(\\.?\\d+)?)\\s*\\))?\\}", (matcher) -> {
                        float min = NumberUtil.isNumber(matcher.group(2)) ? NumberUtil.toBigDecimal(matcher.group(2)).floatValue() : 0;
                        float max = NumberUtil.isNumber(matcher.group(4)) ? NumberUtil.toBigDecimal(matcher.group(4)).floatValue() : 1;
                        if (min > max) {
                            float temp = min;
                            min = max;
                            max = temp;
                        }
                        return RandomUtil.randomFloat(min, max) + "";
                    });
                    replacedPayload = StrUtil.replace(replacedPayload, "\\$\\{string(\\((\\d+)\\))?\\}", (matcher) -> {
                        int length = NumberUtil.isInteger(matcher.group(2)) ? Integer.parseInt(matcher.group(2)) : 4;
                        if (length <= 0) {
                            length = 4;
                        }
                        return RandomUtil.randomString(length);
                    });
                }

                int qos = qosComboBox.getSelectedIndex();
                boolean retained = retainedCheckBox.isSelected();
                String format = (String) formatComboBox.getSelectedItem();
                byte[] payload = CodecSupports.instance().getByName(format).toPayload(topic, replacedPayload);
                mqttInstance.publishMessage(PublishedMqttMessage.of(
                    topic,
                    payload,
                    qos,
                    retained,
                    format
                ));
                addPublishedTopic(topic, originalPayload, qos, retained, format);
            } catch (Exception e) {
                Throwable throwable = Utils.getRootThrowable(e);
                Utils.Toast.error(throwable.getMessage());
                log.error(throwable.getMessage(), throwable);
            }
        });
    }

}
