package com.mqttinsight.ui.component.renderer;

import com.mqttinsight.ui.component.RendererLabel;
import com.mqttinsight.ui.component.RendererTextArea;
import com.mqttinsight.ui.component.RoundRendererLabel;
import com.mqttinsight.ui.component.RoundRendererPanel;
import com.mqttinsight.util.Utils;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.renderer.JRendererPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author ptma
 */
public class DialogueBubble extends JRendererPanel {

    private static final int ROUND_ARC = 12;

    private static boolean isDarkTheme = UIManager.getBoolean("laf.dark");

    private static final String BODY_CONSTRAINTS = "%s,width 90%%,wmax 90%%, wrap";

    private final int maxMessageRows;
    private final MigLayout layout;
    private final RoundRendererPanel bodyPanel;
    private final RendererLabel topicLabel;
    private final RendererLabel iconLabel;
    private final RoundRendererLabel qosLabel;
    private final RoundRendererLabel timeLabel;
    private final RendererTextArea payloadArea;

    public DialogueBubble(int maxMessageRows) {
        super();
        this.maxMessageRows = maxMessageRows;
        setDoubleBuffered(true);
        layout = new MigLayout(
            "fillx, insets 3 3 3 6, nocache",
            "[grow]",
            "[]0[]"
        );
        setOpaque(true);
        setLayout(layout);

        bodyPanel = new RoundRendererPanel(ROUND_ARC);
        bodyPanel.setLayout(new MigLayout(
            "fillx,insets 5 15 5 15,nocache",
            "[grow]5[]5[]5[]",
            "[]5[]"
        ));


        topicLabel = new RendererLabel();
        topicLabel.setOpaque(false);
        topicLabel.putClientProperty("FlatLaf.style", "font: $semibold.font");
        bodyPanel.add(topicLabel, "cell 0 0, growx, wmax 100%-250px");

        iconLabel = new RendererLabel();
        iconLabel.setOpaque(false);
        bodyPanel.add(iconLabel, "cell 1 0");

        qosLabel = new RoundRendererLabel(ROUND_ARC);
        qosLabel.setOpaque(false);
        qosLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 1, 6));
        qosLabel.putClientProperty("FlatLaf.styleClass", "small");
        bodyPanel.add(qosLabel, "cell 2 0");

        timeLabel = new RoundRendererLabel(ROUND_ARC);
        timeLabel.setOpaque(false);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 1, 6));
        timeLabel.putClientProperty("FlatLaf.styleClass", "small");
        bodyPanel.add(timeLabel, "cell 3 0, wrap");

        payloadArea = new RendererTextArea();
        payloadArea.setOpaque(false);
        payloadArea.setLineWrap(true);
        payloadArea.setBorder(BorderFactory.createEmptyBorder());
        payloadArea.setMargin(new Insets(0, 2, 0, 2));
        payloadArea.putClientProperty("FlatLaf.styleClass", "monospaced");
        if (maxMessageRows > 0) {
            payloadArea.setMaxDisplayRows(maxMessageRows);
        }
        bodyPanel.add(payloadArea, "cell 0 1, span, grow, wmax 100%-30px, wrap");

        add(bodyPanel, String.format(BODY_CONSTRAINTS, "left"));
    }

    public void setTopic(String topic) {
        topicLabel.setText(String.format("Topic: %s", topic));
    }

    public void setQos(int qos) {
        qosLabel.setText(String.format("QoS%d", qos));
    }

    public void setTime(String timeText) {
        timeLabel.setText(timeText);
    }

    public void setPayload(String payload) {
        payloadArea.setText(payload);
    }

    public void setBodyForeground(Color fg) {
        topicLabel.setForeground(fg);
        payloadArea.setForeground(fg);
        qosLabel.setForeground(fg);
        timeLabel.setForeground(fg);
    }

    public void setBodyBackground(Color bg) {
        bodyPanel.setBackground(bg);
        Color badgeColor;
        if (isDarkTheme) {
            badgeColor = Utils.brighter(bg, 0.7f);
        } else {
            badgeColor = Utils.darker(bg, 0.85f);
        }
        qosLabel.setBackground(badgeColor);
        timeLabel.setBackground(badgeColor);
    }

    public void setBodyAlignment(String alignment) {
        layout.setComponentConstraints(bodyPanel, String.format(BODY_CONSTRAINTS, alignment));
        bodyPanel.setAlignment(alignment);
    }

    public void setIcon(Icon icon) {
        iconLabel.setIcon(icon);
    }
}
