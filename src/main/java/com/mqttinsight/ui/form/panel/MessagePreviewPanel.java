package com.mqttinsight.ui.form.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.component.SingleLineBorder;
import com.mqttinsight.ui.component.SyntaxTextEditor;
import com.mqttinsight.ui.component.TextSearchToolbar;
import com.mqttinsight.ui.component.model.MessageViewMode;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.painter.RectanglePainter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * @author ptma
 */
public class MessagePreviewPanel extends JPanel {

    private static final KeyStroke FIND_HOT_KEY = KeyStroke.getKeyStroke("ctrl F");
    private static final KeyStroke ESC_KEY = KeyStroke.getKeyStroke("ESCAPE");

    private final MqttInstance mqttInstance;
    private MqttMessage previewedMessage;

    private MigLayout topPanelLayout;
    private SyntaxTextEditor payloadEditor;
    private JPanel toolbarPanel;
    private TextSearchToolbar textSearchToolbar;
    private JPanel topPanel;
    private JTextField topicField;

    private JXLabel timeLabel;
    private JXLabel qosLabel;
    private JXLabel retainedLabel;
    private JCheckBox prettyCheckBox;
    private JPanel payloadPanel;

    public MessagePreviewPanel(MqttInstance mqttInstance) {
        this.mqttInstance = mqttInstance;
        initComponents();
        initEventListeners();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel = new JPanel();
        payloadPanel = new JPanel(new BorderLayout(0, 0));
        add(topPanel, BorderLayout.NORTH);
        add(payloadPanel, BorderLayout.CENTER);

        topPanelLayout = new MigLayout(
            "insets 0 0 5 0,gap 5",
            "[grow][][][][]",
            "[]"
        );
        topPanel.setLayout(topPanelLayout);
        topicField = new JTextField();
        topicField.setEditable(false);
        topicField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, LangUtil.getString("Topic"));
        topPanel.add(topicField, "growx");

        RectanglePainter badgePainter = new RectanglePainter();
        badgePainter.setRounded(true);
        badgePainter.setRoundWidth(16);
        badgePainter.setRoundHeight(16);
        boolean isDarkTheme = UIManager.getBoolean("laf.dark");
        Color bgColor = UIManager.getColor("Panel.background");
        Color badgeColor;
        if (isDarkTheme) {
            badgeColor = Utils.brighter(bgColor, 0.7f);
        } else {
            badgeColor = Utils.darker(bgColor, 0.85f);
        }
        badgePainter.setFillPaint(badgeColor);
        badgePainter.setBorderPaint(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 128));
        Border badgeBorder = BorderFactory.createEmptyBorder(0, 8, 0, 8);

        timeLabel = new JXLabel(" ");
        timeLabel.setBackgroundPainter(badgePainter);
        timeLabel.setOpaque(false);
        timeLabel.setBorder(badgeBorder);
        topPanel.add(timeLabel, "wmin 162px");

        qosLabel = new JXLabel(" ");
        qosLabel.setBackgroundPainter(badgePainter);
        qosLabel.setOpaque(false);
        qosLabel.setBorder(badgeBorder);
        topPanel.add(qosLabel, "wmin 52px");

        retainedLabel = new JXLabel(LangUtil.getString("Retained"));
        retainedLabel.setVisible(false);
        retainedLabel.setBackgroundPainter(badgePainter);
        retainedLabel.setOpaque(false);
        retainedLabel.setBorder(badgeBorder);
        topPanel.add(retainedLabel, "hidemode 2");

        prettyCheckBox = new JCheckBox(LangUtil.getString("Pretty"));
        topPanel.add(prettyCheckBox, "");


        payloadEditor = new SyntaxTextEditor();
        payloadEditor.textArea().setEditable(false);
        payloadPanel.add(payloadEditor, BorderLayout.CENTER);

        toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BorderLayout(0, 0));
        toolbarPanel.setBorder(new SingleLineBorder(UIManager.getColor("Component.borderColor"), true, true, false, true));
        textSearchToolbar = new TextSearchToolbar(this, payloadEditor.textArea());
        toolbarPanel.add(textSearchToolbar, BorderLayout.CENTER);
        payloadPanel.add(toolbarPanel, BorderLayout.NORTH);
        toolbarPanel.setVisible(false);

        payloadEditor.textArea().registerKeyboardAction(e -> {
            activeFindToolbar();
        }, FIND_HOT_KEY, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        payloadEditor.textArea().registerKeyboardAction(e -> {
            closeFindToolbar();
        }, ESC_KEY, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        prettyCheckBox.addActionListener(e -> {
            if (previewedMessage != null) {
                payloadEditor.setText(previewedMessage.payloadAsString(prettyCheckBox.isSelected()));
            }
        });
    }

    private void initEventListeners() {
        mqttInstance.addEventListeners(new InstanceEventAdapter() {
            @Override
            public void clearAllMessages() {
                previewMessage(null);
            }
        });
    }

    public void toggleViewMode(MessageViewMode viewMode) {
        if (viewMode == MessageViewMode.TABLE) {
            topPanelLayout.setColumnConstraints("[grow][][][][right]");
            topPanelLayout.setRowConstraints("[]");
            topPanelLayout.setComponentConstraints(topicField, "growx");
            topPanelLayout.setComponentConstraints(prettyCheckBox, "");
        } else {
            topPanelLayout.setColumnConstraints("[][][][grow,right]");
            topPanelLayout.setRowConstraints("[][]");
            topPanelLayout.setComponentConstraints(topicField, "growx, span, wrap");
            topPanelLayout.setComponentConstraints(prettyCheckBox, "newline");
        }
    }

    public void previewMessage(MqttMessage message) {
        SwingUtilities.invokeLater(() -> {
            previewedMessage = message;
            if (message == null) {
                topicField.setText(" ");
                retainedLabel.setVisible(false);
                qosLabel.setText(" ");
                timeLabel.setText(" ");
                payloadEditor.setText("");
            } else {
                topicField.setText(message.getTopic());
                retainedLabel.setVisible(message.isRetained());
                qosLabel.setText(String.format("QoS %d",message.getQos()));
                timeLabel.setText(message.getTime());
                payloadEditor.setText(message.payloadAsString(prettyCheckBox.isSelected()));
                CodecSupport codecSupport = CodecSupports.instance()
                    .getByName(message.getPayloadFormat());
                payloadEditor.setSyntax(codecSupport.getSyntax());
                if (toolbarPanel.isVisible()) {
                    textSearchToolbar.find(true);
                }
            }
        });
    }

    public void activeFindToolbar() {
        toolbarPanel.setVisible(true);
        textSearchToolbar.focusSearch();
    }

    public void closeFindToolbar() {
        toolbarPanel.setVisible(false);
    }

}
