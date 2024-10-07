package com.mqttinsight.ui.form.panel;

import cn.hutool.core.thread.ThreadUtil;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.ui.form.MessagePreviewFrame;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;

import javax.swing.*;
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
            MessagePreviewFrame.open(this.mqttInstance, this.previewedMessage, this.getCurrentPreviewFormat());
        });

        // Append a menu item to the editor pop-up menu
        payloadEditor.textArea().getPopupMenu().add(new JPopupMenu.Separator(), 0);
        payloadEditor.textArea().getPopupMenu().add(separateWindowMenuitem, 0).setEnabled(false);

        // Load the check status from the option and save it when it changes
        prettyCheckbox.setSelected(mqttInstance.getProperties().isPrettyDuringPreview());
        prettyCheckbox.addActionListener(e -> {
            mqttInstance.getProperties().setPrettyDuringPreview(prettyCheckbox.isSelected());
            Configuration.instance().changed();
        });
        syntaxEnableCheckbox.setSelected(mqttInstance.getProperties().isSyntaxHighlighting());
        syntaxEnableCheckbox.addActionListener(e -> {
            mqttInstance.getProperties().setSyntaxHighlighting(syntaxEnableCheckbox.isSelected());
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
