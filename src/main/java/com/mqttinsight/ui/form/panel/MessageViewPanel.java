package com.mqttinsight.ui.form.panel;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.ReceivedMqttMessage;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.ui.component.MessageTable;
import com.mqttinsight.ui.component.SingleLineBorder;
import com.mqttinsight.ui.component.model.MessageTableModel;
import com.mqttinsight.ui.component.model.MessageViewMode;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.ui.event.InstanceEventListener;
import com.mqttinsight.util.Const;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * @author ptma
 */
public class MessageViewPanel {

    private final MqttInstance mqttInstance;
    protected MessageTableModel messageTableModel;

    private JPanel rootPanel;
    private JScrollPane scrollPanel;

    private MessageTable messageTable;
    private int lastSelectedRow = -1;

    public MessageViewPanel(final MqttInstance mqttInstance, MessageViewMode viewMode) {
        this.mqttInstance = mqttInstance;
        $$$setupUI$$$();
        initComponents(viewMode);
        initializeMessageTable();
        initEventListeners();
    }

    private void initComponents(MessageViewMode viewMode) {
        Integer maxMessageStored = mqttInstance.getProperties().getMaxMessageStored();
        messageTableModel = new MessageTableModel(maxMessageStored == null ? Const.MESSAGES_STORED_MAX_SIZE : maxMessageStored);
        messageTableModel.setViewMode(viewMode);
        Border scrollPanelBorder = new SingleLineBorder(UIManager.getColor("Component.borderColor"), true, true, true, true);
        scrollPanel.setBorder(scrollPanelBorder);
        scrollPanel.setMinimumSize(new Dimension(400, 250));
    }

    private void initializeMessageTable() {
        if (messageTable != null) {
            messageTable.resetKeyboardActions();
            scrollPanel.remove(messageTable);
            messageTable = null;
            System.gc();
        }
        messageTable = new MessageTable(mqttInstance, messageTableModel);
        messageTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    mqttInstance.getEventListeners().forEach(InstanceEventListener::requestFocusPreview);
                }
            }
        });
        messageTable.getSelectionModel().addListSelectionListener(this::tableSelectionChanged);
        scrollPanel.setViewportView(messageTable);
        if (lastSelectedRow >= 0) {
            messageTable.goAndSelectRow(lastSelectedRow);
        }
        mqttInstance.getEventListeners().forEach(InstanceEventListener::viewInitializeCompleted);
    }

    private void initEventListeners() {
        mqttInstance.addEventListeners(new InstanceEventAdapter() {
            @Override
            public void onViewModeChanged(MessageViewMode viewMode) {
                MessageViewPanel.this.toggleViewMode(viewMode);
            }

            @Override
            public void clearAllMessages() {
                messageTableModel.clear();
            }

            @Override
            public void onMessage(MqttMessage message) {
                MessageViewPanel.this.messageReceived(message);
            }

            @Override
            public void clearMessages(Subscription subscription) {
                MessageViewPanel.this.clearMessages(subscription);
            }

            @Override
            public void exportAllMessages() {
                MessageViewPanel.this.exportAllMessages();
            }

            @Override
            public void exportMessages(Subscription subscription) {
                MessageViewPanel.this.exportMessages(subscription);
            }

            @Override
            public void toggleAutoScroll(boolean autoScroll) {
                MessageViewPanel.this.toggleAutoScroll(autoScroll);
            }
        });
    }

    private void toggleViewMode(MessageViewMode viewMode) {
        if (!messageTableModel.getViewMode().equals(viewMode)) {
            messageTableModel.setViewMode(viewMode);
            initializeMessageTable();
        }
    }

    public MessageTable getMessageTable() {
        return messageTable;
    }

    public JComponent getRootPanel() {
        return rootPanel;
    }

    private void toggleAutoScroll(boolean autoScroll) {
        messageTable.setAutoScroll(autoScroll);
        if (messageTable.isAutoScroll()) {
            messageTable.goAndSelectRow(messageTable.getRowCount() - 1);
        }
    }

    private void exportAllMessages() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(LangUtil.getString("JsonFileFilter"), "json"));
        jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(LangUtil.getString("TextFileFilter"), "txt"));
        jFileChooser.setDialogTitle(LangUtil.getString("ExportMessages"));
        String directory = Configuration.instance().getString(ConfKeys.EXPORT_SAVE_DIALOG_PATH);
        if (directory != null) {
            jFileChooser.setCurrentDirectory(new File(directory));
        }
        int result = jFileChooser.showSaveDialog(MqttInsightApplication.frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            Configuration.instance().set(ConfKeys.EXPORT_SAVE_DIALOG_PATH, jFileChooser.getCurrentDirectory().getAbsolutePath());
            File file = jFileChooser.getSelectedFile();
            FileNameExtensionFilter currentFilter = (FileNameExtensionFilter) jFileChooser.getFileFilter();
            String ext = currentFilter.getExtensions()[0];
            String absolutePath = file.getAbsolutePath();
            if (!absolutePath.endsWith("." + ext)) {
                absolutePath += "." + ext;
                file = new File(absolutePath);
            }
            if (file.exists()) {
                int opt = Utils.Message.confirm(String.format(LangUtil.getString("FileExists"), file.getName()));
                if (JOptionPane.YES_OPTION != opt) {
                    return;
                }
            }
            String fileContent = messageTableModel.getMessages()
                .stream()
                .map(m -> JSONUtil.parse(m).toJSONString(0))
                .collect(Collectors.joining("\n"));
            FileUtil.writeString(fileContent, file, StandardCharsets.UTF_8);
        }
    }

    private void clearMessages(Subscription subscription) {
        messageTableModel.cleanMessages(subscription);
        if (messageTable.getSelectedRow() < 0) {
            mqttInstance.previewMessage(null);
        }
    }

    private void exportMessages(Subscription subscription) {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(LangUtil.getString("JsonFileFilter"), "json"));
        jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(LangUtil.getString("TextFileFilter"), "txt"));
        jFileChooser.setDialogTitle(LangUtil.getString("ExportMessages"));
        String directory = Configuration.instance().getString(ConfKeys.EXPORT_SAVE_DIALOG_PATH);
        if (directory != null) {
            jFileChooser.setCurrentDirectory(new File(directory));
        }
        int result = jFileChooser.showSaveDialog(MqttInsightApplication.frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            Configuration.instance().set(ConfKeys.EXPORT_SAVE_DIALOG_PATH, jFileChooser.getCurrentDirectory().getAbsolutePath());
            File file = jFileChooser.getSelectedFile();
            FileNameExtensionFilter currentFilter = (FileNameExtensionFilter) jFileChooser.getFileFilter();
            String ext = currentFilter.getExtensions()[0];
            String absolutePath = file.getAbsolutePath();
            if (!absolutePath.endsWith("." + ext)) {
                absolutePath += "." + ext;
                file = new File(absolutePath);
            }
            if (file.exists()) {
                int opt = Utils.Message.confirm(String.format(LangUtil.getString("FileExists"), file.getName()));
                if (JOptionPane.YES_OPTION != opt) {
                    return;
                }
            }
            String fileContent = messageTableModel.getMessages()
                .stream()
                .filter(m -> m instanceof ReceivedMqttMessage)
                .map(m -> (ReceivedMqttMessage) m)
                .filter(m -> m.getSubscription().equals(subscription))
                .map(m -> JSONUtil.parse(m).toJSONString(0))
                .collect(Collectors.joining("\n"));
            FileUtil.writeString(fileContent, file, StandardCharsets.UTF_8);
        }
    }

    /**
     * When received or published a message
     */
    private void messageReceived(MqttMessage message) {
        SwingUtilities.invokeLater(() -> {
            if (message instanceof ReceivedMqttMessage) {
                ReceivedMqttMessage subscriptionMessage = (ReceivedMqttMessage) message;
                if (subscriptionMessage.getSubscription().isMuted()) {
                    return;
                }
            }
            messageTableModel.add(message);
            if (messageTable.isAutoScroll()) {
                messageTable.goAndSelectRow(messageTable.getRowCount() - 1);
            }
        });
    }

    private void tableSelectionChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (!e.getValueIsAdjusting()) {
            int selectedRow = lsm.getMaxSelectionIndex();
            if (selectedRow >= 0 && selectedRow != lastSelectedRow) {
                lastSelectedRow = selectedRow;
                mqttInstance.previewMessage(messageTableModel.get(messageTable.convertRowIndexToModel(selectedRow)));
            }
            mqttInstance.getEventListeners().forEach(InstanceEventListener::tableSelectionChanged);
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout(0, 0));
        scrollPanel = new JScrollPane();
        scrollPanel.setDoubleBuffered(true);
        rootPanel.add(scrollPanel, BorderLayout.CENTER);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
