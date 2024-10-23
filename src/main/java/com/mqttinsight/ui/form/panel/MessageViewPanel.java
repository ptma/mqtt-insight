package com.mqttinsight.ui.form.panel;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ptma
 */
@Slf4j
public class MessageViewPanel extends JScrollPane {

    private final MqttInstance mqttInstance;
    protected MessageTableModel messageTableModel;

    @Getter
    private MessageTable messageTable;
    private int lastSelectedRow = -1;
    private boolean autoScroll;

    public MessageViewPanel(final MqttInstance mqttInstance, MessageViewMode viewMode) {
        super();
        this.mqttInstance = mqttInstance;
        setDoubleBuffered(true);
        initComponents(viewMode);
        initializeMessageTable();
        initEventListeners();
    }

    private void initComponents(MessageViewMode viewMode) {
        Integer maxMessageStored = mqttInstance.getProperties().getMaxMessageStored();
        messageTableModel = new MessageTableModel(maxMessageStored == null ? Const.MESSAGES_STORED_MAX_SIZE : maxMessageStored);
        messageTableModel.setViewMode(viewMode);
        Border scrollPanelBorder = new SingleLineBorder(UIManager.getColor("Component.borderColor"), true, true, true, true);
        setBorder(scrollPanelBorder);
        setMinimumSize(new Dimension(400, 250));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void initializeMessageTable() {
        RowFilter rowFilter = null;
        if (messageTable != null) {
            rowFilter = messageTable.getRowFilter();
            messageTable.resetKeyboardActions();
            remove(messageTable);
            messageTable = null;
            System.gc();
        }
        messageTable = new MessageTable(mqttInstance, messageTableModel);
        messageTable.setAutoScroll(autoScroll);
        if (rowFilter != null) {
            messageTable.setRowFilter(rowFilter);
        }
        messageTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    mqttInstance.applyEvent(InstanceEventListener::requestFocusPreview);
                }
            }
        });
        messageTable.getSelectionModel().addListSelectionListener(this::tableSelectionChanged);
        setViewportView(messageTable);
        if (lastSelectedRow >= 0) {
            messageTable.goAndSelectRow(lastSelectedRow);
        }
        mqttInstance.applyEvent(InstanceEventListener::viewInitializeCompleted);
    }

    private void initEventListeners() {
        mqttInstance.addEventListener(new InstanceEventAdapter() {
            @Override
            public void onViewModeChanged(MessageViewMode viewMode) {
                MessageViewPanel.this.toggleViewMode(viewMode);
            }

            @Override
            public void clearAllMessages() {
                MessageViewPanel.this.doClearAllMessages();
            }

            @Override
            public void onMessage(MqttMessage message) {
                MessageViewPanel.this.messageReceived(message, null);
            }

            @Override
            public void onMessage(MqttMessage message, MqttMessage parent) {
                MessageViewPanel.this.messageReceived(message, parent);
            }

            @Override
            public void payloadFormatChanged() {
                MessageViewPanel.this.repaintTable();
            }

            @Override
            public void subscriptionColorChanged() {
                MessageViewPanel.this.repaintTable();
            }

            @Override
            public void clearMessages(Subscription subscription, Runnable done) {
                MessageViewPanel.this.doClearMessages(subscription, done);
            }

            @Override
            public void clearMessages(String topicPrefix, Runnable done) {
                MessageViewPanel.this.doClearMessages(topicPrefix, done);
            }

            @Override
            public void exportAllMessages() {
                MessageViewPanel.this.doExportMessages(null);
            }

            @Override
            public void exportMessages(Subscription subscription) {
                MessageViewPanel.this.doExportMessages(subscription);
            }

            @Override
            public void toggleAutoScroll(boolean autoScroll) {
                MessageViewPanel.this.toggleAutoScroll(autoScroll);
            }

            @Override
            public void applyFilterTopics(Set<String> topics) {
                MessageViewPanel.this.doApplyFilterTopics(topics);
            }
        });
    }

    private void toggleViewMode(MessageViewMode viewMode) {
        if (!messageTableModel.getViewMode().equals(viewMode)) {
            messageTableModel.setViewMode(viewMode);
            initializeMessageTable();
        }
    }

    private void toggleAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
        messageTable.setAutoScroll(autoScroll);
        if (messageTable.isAutoScroll()) {
            messageTable.goAndSelectRow(messageTable.getRowCount() - 1);
        }
    }

    private void doApplyFilterTopics(Set<String> topics) {
        messageTable.doApplyFilterTopics(topics);
    }

    private String toCsvLineText(MqttMessage message) {
        StringBuilder sb = new StringBuilder();
        sb.append(message.timeWithFormat("yyyy/MM/dd HH:mm:ss")).append(",");
        sb.append(message.getMessageType().name()).append(",");
        sb.append(message.getTopic()).append(",");
        String payload = message.getPayload();
        if (StrUtil.contains(payload, ",")) {
            sb.append("\"").append(payload.replaceAll("\"", "\"\"")).append("\",");
        } else {
            sb.append(payload).append(",");
        }
        sb.append(message.getQos()).append(",");
        sb.append(message.isRetained()).append(",");
        sb.append(message.isDuplicate());
        return sb.toString();
    }

    private void repaintTable() {
        SwingUtilities.invokeLater(() -> {
            messageTable.revalidate();
            messageTable.repaint();
        });
    }

    private void doClearAllMessages() {
        SwingUtilities.invokeLater(() -> {
            messageTableModel.clear();
            lastSelectedRow = -1;
            messageTable.goAndSelectRow(-1);
        });
    }

    public void doClearMessages(Subscription subscription, Runnable done) {
        SwingUtilities.invokeLater(() -> {
            messageTableModel.cleanMessages(subscription, (msg) -> {
                mqttInstance.applyEvent(l -> l.onMessageRemoved(msg));
            });
            if (done != null) {
                done.run();
            }
        });
    }

    public void doClearMessages(String topicPrefix, Runnable done) {
        SwingUtilities.invokeLater(() -> {
            messageTableModel.cleanMessages(topicPrefix, (msg) -> {
                mqttInstance.applyEvent(l -> l.onMessageRemoved(msg));
            });
            if (done != null) {
                done.run();
            }
        });
    }

    /**
     * @param subscription if null, export all messages
     */
    private void doExportMessages(Subscription subscription) {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(LangUtil.getString("JsonFileFilter"), "json"));
        jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(LangUtil.getString("CsvFileFilter"), "csv"));
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

            List<MqttMessage> exportingMessages;

            if (subscription == null) {
                // export from view
                if (messageTable.getRowFilter() == null) {
                    exportingMessages = messageTableModel.getMessages();
                } else {
                    // Filter has been used, only export filtered messages
                    exportingMessages = new ArrayList<>();
                    for (int i = 0; i < messageTable.getRowCount(); i++) {
                        int modelRow = messageTable.convertRowIndexToModel(i);
                        exportingMessages.add(messageTableModel.getMessages().get(modelRow));
                    }
                }
            } else {
                // export from subscription
                exportingMessages = messageTableModel.getMessages()
                    .stream()
                    .filter(m -> m instanceof ReceivedMqttMessage)
                    .map(m -> (ReceivedMqttMessage) m)
                    .filter(m -> m.getSubscription() != null && m.getSubscription().equals(subscription))
                    .collect(Collectors.toList());
            }
            StringBuilder lines = new StringBuilder();
            switch (ext) {
                case "csv":
                    lines.append("Time,MessageType,Topic,Payload,QoS,Retained,Duplicate\n");
                    lines.append(
                        exportingMessages
                            .stream()
                            .map(this::toCsvLineText)
                            .collect(Collectors.joining("\n"))
                    );
                    break;
                case "json":
                    lines.append("[\n");
                    lines.append(
                        exportingMessages
                            .stream()
                            .map(m -> {
                                try {
                                    return Utils.JSON.toString(m);
                                } catch (JsonProcessingException e) {
                                    log.error(e.getMessage(), e);
                                    Utils.Toast.error(e.getMessage());
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(Collectors.joining(",\n"))
                    );
                    lines.append("\n]");
                    break;
                default:
                    lines.append(
                        exportingMessages
                            .stream()
                            .map(m -> {
                                try {
                                    return Utils.JSON.toString(m);
                                } catch (JsonProcessingException e) {
                                    log.error(e.getMessage(), e);
                                    Utils.Toast.error(e.getMessage());
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(Collectors.joining("\n"))
                    );
            }
            FileUtil.writeString(lines.toString(), file, StandardCharsets.UTF_8);
        }
    }

    /**
     * When received or published a message
     */
    private void messageReceived(MqttMessage message, MqttMessage parent) {
        SwingUtilities.invokeLater(() -> {
            if (parent != null) {
                int parentIndex = messageTableModel.lastIndexOf(parent);
                if (parentIndex >= 0) {
                    messageTableModel.add(parentIndex + 1, message);
                } else {
                    messageTableModel.add(message);
                }
            } else {
                messageTableModel.add(message);
            }
            if (messageTable.isAutoScroll()) {
                messageTable.goAndSelectRow(messageTable.getRowCount() - 1);
            }
        });
    }

    private void tableSelectionChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (!e.getValueIsAdjusting()) {
            int selectedRow = lsm.getMaxSelectionIndex();
            if (selectedRow >= 0) {
                if (selectedRow != lastSelectedRow) {
                    lastSelectedRow = selectedRow;
                    final MqttMessage message = messageTableModel.get(messageTable.convertRowIndexToModel(selectedRow));
                    mqttInstance.applyEvent(l -> l.tableSelectionChanged(message));
                }
            } else {
                mqttInstance.applyEvent(l -> l.tableSelectionChanged(null));
            }
        }
    }

}
