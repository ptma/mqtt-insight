package com.mqttinsight.ui.form.panel;

import cn.hutool.core.io.unit.DataSizeUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.component.SingleLineBorder;
import com.mqttinsight.ui.component.SyntaxTextEditor;
import com.mqttinsight.ui.component.TextSearchToolbar;
import com.mqttinsight.ui.component.model.MessageViewMode;
import com.mqttinsight.ui.component.model.PayloadFormatComboBoxModel;
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
public class BasePreviewPanel extends JPanel {

    private static final KeyStroke FIND_HOT_KEY = KeyStroke.getKeyStroke("ctrl F");
    private static final KeyStroke ESC_KEY = KeyStroke.getKeyStroke("ESCAPE");

    protected final MqttInstance mqttInstance;
    protected MqttMessage previewedMessage;

    private MigLayout topPanelLayout;
    protected SyntaxTextEditor payloadEditor;
    private JPanel toolbarPanel;
    private TextSearchToolbar textSearchToolbar;
    private JPanel topPanel;
    private JTextField topicField;

    private JXLabel timeLabel;
    private JXLabel qosLabel;
    private JXLabel retainedLabel;
    private JXLabel sizeLabel;
    private JLabel formatLabel;
    private JComboBox<String> formatComboBox;
    protected JCheckBox prettyCheckbox;
    private JPanel payloadPanel;

    public BasePreviewPanel(MqttInstance mqttInstance) {
        this.mqttInstance = mqttInstance;
        initComponents();
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
            "[grow][][][][][][][]",
            "[]"
        );
        topPanel.setLayout(topPanelLayout);
        topicField = new JTextField();
        topicField.setEditable(false);
        topicField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, LangUtil.getString("Topic"));
        topPanel.add(topicField, "growx");

        RectanglePainter badgePainter = new RectanglePainter();
        badgePainter.setRounded(true);
        badgePainter.setRoundWidth(20);
        badgePainter.setRoundHeight(20);
        boolean isDarkTheme = UIManager.getBoolean("laf.dark");
        Color bgColor = UIManager.getColor("Panel.background");
        Color badgeColor;
        if (isDarkTheme) {
            badgeColor = Utils.brighter(bgColor, 0.7f);
        } else {
            badgeColor = Utils.darker(bgColor, 0.9f);
        }
        badgePainter.setFillPaint(badgeColor);
        badgePainter.setBorderPaint(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 128));
        Border badgeBorder = BorderFactory.createEmptyBorder(2, 8, 2, 8);

        timeLabel = new JXLabel(" ");
        timeLabel.setBackgroundPainter(badgePainter);
        timeLabel.setOpaque(false);
        timeLabel.setBorder(badgeBorder);
        topPanel.add(timeLabel, "span 2,wmin 162px");

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
        topPanel.add(retainedLabel, "hidemode 3");

        sizeLabel = new JXLabel(" ");
        sizeLabel.setBackgroundPainter(badgePainter);
        sizeLabel.setOpaque(false);
        sizeLabel.setBorder(badgeBorder);
        sizeLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        topPanel.add(sizeLabel, "");

        formatLabel = new JLabel(LangUtil.getString("PayloadFormat"));
        topPanel.add(formatLabel, "right");
        formatComboBox = new JComboBox<>();
        formatComboBox.setModel(new PayloadFormatComboBoxModel(true, false));
        formatComboBox.setSelectedItem(CodecSupport.DEFAULT);
        formatComboBox.addActionListener(e -> this.updatePreviewMessage());
        topPanel.add(formatComboBox, "");

        prettyCheckbox = new JCheckBox(LangUtil.getString("Pretty"), null, true);
        prettyCheckbox.addActionListener(e -> {
            this.updatePreviewMessage();
        });
        topPanel.add(prettyCheckbox, "");

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
    }

    public void toggleViewMode(MessageViewMode viewMode) {
        if (viewMode == MessageViewMode.TABLE) {
            topPanelLayout.setColumnConstraints("[grow][][][][][][right][]");
            topPanelLayout.setRowConstraints("[]");
            topPanelLayout.setComponentConstraints(topicField, "growx");
            topPanelLayout.setComponentConstraints(formatLabel, "");
        } else {
            topPanelLayout.setColumnConstraints("[][][][][][][grow,right][]");
            topPanelLayout.setRowConstraints("[][]");
            topPanelLayout.setComponentConstraints(topicField, "growx, span, wrap");
            topPanelLayout.setComponentConstraints(formatLabel, "newline");
        }
    }

    public String getCurrentPreviewFormat() {
        return (String) formatComboBox.getSelectedItem();
    }

    public void setCurrentPreviewFormat(String format) {
        formatComboBox.setSelectedItem(format);
    }

    public void updatePreviewMessage() {
        boolean pretty = prettyCheckbox.isSelected();
        if (previewedMessage != null) {
            String format = (String) formatComboBox.getSelectedItem();
            if (CodecSupport.DEFAULT.equals(format)) {
                payloadEditor.setText(previewedMessage.payloadAsString(pretty));
                CodecSupport codec = CodecSupports.instance().getByName(previewedMessage.getPayloadFormat());
                payloadEditor.setSyntax(codec.getSyntax());
            } else {
                CodecSupport codec = CodecSupports.instance().getByName(format);
                payloadEditor.setText(previewedMessage.decodePayload(codec, pretty));
                payloadEditor.setSyntax(codec.getSyntax());
            }
        }
    }

    public void previewMessageDirectly(final MqttMessage message) {
        this.previewedMessage = message;
        if (previewedMessage == null) {
            SwingUtilities.invokeLater(() -> {
                topicField.setText(" ");
                retainedLabel.setVisible(false);
                qosLabel.setText(" ");
                timeLabel.setText(" ");
                sizeLabel.setText(" ");
                payloadEditor.setText("");
            });
        } else {
            String format = (String) formatComboBox.getSelectedItem();
            if (CodecSupport.DEFAULT.equals(format)) {
                format = previewedMessage.getPayloadFormat();
            }
            CodecSupport codec = CodecSupports.instance().getByName(format);
            String previewText = previewedMessage.decodePayload(codec, prettyCheckbox.isSelected());

            SwingUtilities.invokeLater(() -> {
                if (previewedMessage != null) {
                    topicField.setText(previewedMessage.getTopic());
                    retainedLabel.setVisible(previewedMessage.isRetained());
                    qosLabel.setText(String.format("QoS %d", previewedMessage.getQos()));
                    timeLabel.setText(previewedMessage.getTime());
                    sizeLabel.setText(DataSizeUtil.format(previewedMessage.payloadSize()));
                    payloadEditor.setText(previewText);
                    payloadEditor.setSyntax(codec.getSyntax());

                    if (toolbarPanel.isVisible()) {
                        textSearchToolbar.find(true);
                    }
                }
            });
        }
    }

    public void reloadFormatCombo() {
        SwingUtilities.invokeLater(() -> {
            formatComboBox.setModel(new PayloadFormatComboBoxModel(true, false));
        });
    }

    private void activeFindToolbar() {
        toolbarPanel.setVisible(true);
        textSearchToolbar.focusSearch(payloadEditor.textArea().getSelectedText());
    }

    public void closeFindToolbar() {
        toolbarPanel.setVisible(false);
    }

}
