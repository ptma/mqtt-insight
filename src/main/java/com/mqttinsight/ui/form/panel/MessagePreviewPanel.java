package com.mqttinsight.ui.form.panel;

import cn.hutool.core.thread.ThreadUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.component.SingleLineBorder;
import com.mqttinsight.ui.component.SyntaxTextEditor;
import com.mqttinsight.ui.component.TextSearchToolbar;
import com.mqttinsight.ui.component.model.MessageViewMode;
import com.mqttinsight.ui.component.model.PayloadFormatComboBoxModel;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.ui.form.MessagePreviewFrame;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.painter.RectanglePainter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ptma
 */
public class MessagePreviewPanel extends BasePreviewPanel {

    private final ExecutorService previewExecutorService = ThreadUtil.newFixedExecutor(1, 2, "Preview", new ThreadPoolExecutor.DiscardOldestPolicy());

    private JMenuItem separateWindowMenuitem;

    public MessagePreviewPanel(final MqttInstance mqttInstance) {
        super(mqttInstance);
        initEventListeners();
        separateWindowMenuitem = Utils.UI.createMenuItem(LangUtil.getString("PreviewSeparateWindow"), (e) -> {
            MessagePreviewFrame.open(this.mqttInstance, this.previewedMessage);
        });

        // Append a menu item to the editor pop-up menu
        payloadEditor.textArea().getPopupMenu().addSeparator();
        payloadEditor.textArea().getPopupMenu().add(separateWindowMenuitem).setEnabled(false);

        // Load the check status from the option and save it when it changes
        prettyCheckbox.setSelected(mqttInstance.getProperties().isPrettyDuringPreview());
        prettyCheckbox.addActionListener(e -> {
            mqttInstance.getProperties().setPrettyDuringPreview(prettyCheckbox.isSelected());
            Configuration.instance().changed();
        });
    }

    private void initEventListeners() {
        mqttInstance.addEventListener(new InstanceEventAdapter() {
            @Override
            public void payloadFormatChanged() {
                updatePreviewMessage();
            }

            @Override
            public void clearAllMessages() {
                previewMessage(null);
            }

            @Override
            public void tableSelectionChanged(MqttMessage message) {
                previewMessage(message);
            }

            @Override
            public void onCodecsChanged() {
                reloadFormatCombo();
            }
        });
    }

    private void previewMessage(final MqttMessage message) {
        separateWindowMenuitem.setEnabled(message != null);
        previewExecutorService.execute(() -> {
            previewMessageDirectly(message);
        });

    }

}
