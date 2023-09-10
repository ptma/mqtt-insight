package com.mqttinsight.ui.form.panel;

import cn.hutool.core.thread.ThreadUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.*;
import com.mqttinsight.scripting.ScriptLoader;
import com.mqttinsight.ui.component.*;
import com.mqttinsight.ui.component.model.MessageViewMode;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.ui.event.InstanceEventListener;
import com.mqttinsight.ui.form.MainWindowForm;
import com.mqttinsight.ui.form.NewSubscriptionForm;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class MqttInstanceTabPanel extends JPanel implements MqttInstance {

    private static final int PREVIEW_PANEL_MIN_HEIGHT = 105;
    private static final int PREVIEW_PANEL_MIN_WIDTH = 370;
    @Getter
    protected final MqttProperties properties;

    protected ConnectionStatus connectionStatus = ConnectionStatus.CONNECTING;
    protected int reasonCode;
    protected String lastCauseMessage;

    private boolean layoutInitialized;
    private String payloadFormat;

    private JPanel rootPanel;
    private JPanel topPanel;
    private JSplitPane subscriptionSplitPanel;
    private JSplitPane messageSplitPanel;
    private JTabbedPane detailTabbedPanel;

    private JLabel statusLabel;
    private JButton connectButton;
    private SplitButton subscribeButton;
    private MessageToolbar messageToolbar;

    protected SubscriptionListPanel subscriptionListPanel;
    protected MessageViewPanel messageViewPanel;
    protected MessagePublishPanel messagePublishPanel;
    protected MessagePreviewPanel messagePreviewPanel;

    private final List<InstanceEventListener> eventListeners;

    private ScriptLoader scriptLoader;

    public MqttInstanceTabPanel(MqttProperties properties) {
        super();
        this.properties = properties;
        eventListeners = new ArrayList<>();
        setLayout(new BorderLayout());
        $$$setupUI$$$();
        add(rootPanel, BorderLayout.CENTER);
        initComponents();
        initEventListeners();
        initMqttClient();
    }

    private void initComponents() {
        MessageViewMode viewMode = MessageViewMode.of(Configuration.instance().getString(ConfKeys.MESSAGE_VIEW, MessageViewMode.TABLE.toString()));
        subscriptionListPanel = new SubscriptionListPanel(this);
        messageViewPanel = new MessageViewPanel(this, viewMode);
        messagePublishPanel = new MessagePublishPanel(this);
        messagePreviewPanel = new MessagePreviewPanel(this);
        subscriptionSplitPanel.setLeftComponent(subscriptionListPanel.getRootPanel());
        messageSplitPanel.setTopComponent(messageViewPanel.getRootPanel());

        Border tabbedPanelBorder = new SingleLineBorder(UIManager.getColor("Component.borderColor"), true, true, true, true);
        detailTabbedPanel.setBorder(tabbedPanelBorder);
        detailTabbedPanel.setTabPlacement(JTabbedPane.LEFT);
        detailTabbedPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP);

        detailTabbedPanel.addTab(LangUtil.getString("Preview"), Icons.PREVIEW, messagePreviewPanel, LangUtil.getString("Preview"));
        detailTabbedPanel.addTab(LangUtil.getString("Publish"), Icons.SEND, messagePublishPanel, LangUtil.getString("Publish"));

        Integer subscriptionDivider = Configuration.instance().getInt(ConfKeys.SUBSCRIPTION_HORIZONTAL_DIVIDER, 255);
        subscriptionSplitPanel.setDividerLocation(subscriptionDivider);
        subscriptionSplitPanel.setOneTouchExpandable(true);
        subscriptionSplitPanel.putClientProperty("JSplitPane.expandableSide", "right");
        subscriptionSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            Configuration.instance().set(ConfKeys.SUBSCRIPTION_HORIZONTAL_DIVIDER, evt.getNewValue());
            Configuration.instance().changed();
        });
        messageSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            if (messageSplitPanel.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                Configuration.instance().set(ConfKeys.MESSAGE_VERTICAL_DIVIDER, evt.getNewValue());
            } else {
                Configuration.instance().set(ConfKeys.MESSAGE_HORIZONTAL_DIVIDER, evt.getNewValue());
            }
            Configuration.instance().changed();
        });
        messageSplitPanel.setOneTouchExpandable(true);
        messageSplitPanel.putClientProperty("JSplitPane.expandableSide", "left");

        // Should be initialized after MessageTable initialization
        initTopBar();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                if (!layoutInitialized) {
                    applyLayout(viewMode);
                    layoutInitialized = true;
                }
            }
        });
    }

    private void initTopBar() {
        topPanel.setLayout(new MigLayout(
            "insets 3, gap 0",
            "[left][left][grow][right]"
        ));

        JToolBar leftToolbar = new JToolBar();
        connectButton = new JButton(Icons.EXECUTE);
        LangUtil.buttonText(connectButton, "Connect");
        connectButton.setEnabled(false);
        connectButton.addActionListener(e -> connect());
        leftToolbar.add(connectButton);
        subscribeButton = new SplitButton();
        subscribeButton.setIcon(Icons.SUBSCRIBE);
        LangUtil.buttonText(subscribeButton, "NewSubscription");
        subscribeButton.setToolTipText(LangUtil.getString("NewSubscription") + " (Ctrl + Shift + S)");
        subscribeButton.setEnabled(false);
        subscribeButton.addActionListener(e -> {
            openSubscriptionForm();
        });
        JPopupMenu popup = new JPopupMenu();
        // TODO: 收藏的订阅列表菜单加载并监听变更
        subscribeButton.setPopupMenu(popup);
        leftToolbar.add(subscribeButton);

        leftToolbar.addSeparator();

        topPanel.add(leftToolbar, "cell 0 0");

        statusLabel = new JLabel();
        topPanel.add(statusLabel, "cell 1 0, gapx 10px 0");

        messageToolbar = new MessageToolbar(this);
        topPanel.add(messageToolbar, "cell 3 0");
    }

    private void initEventListeners() {
        addEventListeners(new InstanceEventAdapter() {
            @Override
            public void onViewModeChanged(MessageViewMode viewMode) {
                applyLayout(viewMode);
            }

            @Override
            public void requestFocusPreview() {
                detailTabbedPanel.setSelectedIndex(0);
            }

            @Override
            public void fireLoadScript() {
                doLoadScript();
            }

            @Override
            public void fireScriptReload(File scriptFile) {
                doScriptReload(scriptFile, true);
            }

            @Override
            public void fireScriptRemove(File scriptFile) {
                doScriptRemove(scriptFile);
            }
        });
    }

    @Override
    public void close() {
        if (isConnected()) {
            onConnectionChanged(ConnectionStatus.DISCONNECTING);
            disconnect();
        }
    }

    @Override
    public MessageTable getMessageTable() {
        return messageViewPanel.getMessageTable();
    }

    @Override
    public void addEventListeners(InstanceEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    @Override
    public List<InstanceEventListener> getEventListeners() {
        return eventListeners;
    }

    protected int getReasonCode() {
        return reasonCode;
    }

    protected void onConnectionChanged(ConnectionStatus status) {
        connectionStatus = status;
        if (status.equals(ConnectionStatus.FAILED)) {
            statusLabel.setIcon(Icons.ERROR);
            String reasonKey = String.format("MqttReasonCode_%d", reasonCode);
            if (LangUtil.contains(reasonKey)) {
                statusLabel.setToolTipText(LangUtil.getString(reasonKey));
            } else {
                statusLabel.setToolTipText(String.format("Code: %d, %s", reasonCode, lastCauseMessage));
            }
        } else {
            this.reasonCode = 0;
            statusLabel.setIcon(status.getIcon());
            statusLabel.setToolTipText(null);
        }
        statusLabel.setText(LangUtil.getString(status.getText()));
        connectButton.setEnabled(status.equals(ConnectionStatus.DISCONNECTED) || status.equals(ConnectionStatus.FAILED));
        subscribeButton.setEnabled(status.equals(ConnectionStatus.CONNECTED));
        MainWindowForm.getInstance().onConnectionChanged(this);
        subscriptionListPanel.onConnectionChanged(status);
    }

    protected void onConnectionChanged(final ConnectionStatus status, int reasonCode, String causeMessage) {
        this.reasonCode = reasonCode;
        this.lastCauseMessage = causeMessage;
        onConnectionChanged(status);
    }

    protected void onConnectionChanged(final ConnectionStatus status, String causeMessage) {
        onConnectionChanged(status, -1, causeMessage);
    }

    protected void onConnectionFailed(int reasonCode, String causeMessage) {
        this.reasonCode = reasonCode;
        this.lastCauseMessage = causeMessage;
        onConnectionChanged(ConnectionStatus.FAILED);
    }

    public void openSubscriptionForm() {
        NewSubscriptionForm.open(this, (subscription) -> {
            subscriptionListPanel.doSubscribe(subscription);
        });
    }

    @Override
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    @Override
    public void messageReceived(MqttMessage message) {
        getEventListeners().forEach(l -> l.onMessage(message));
        if (scriptLoader != null && message instanceof ReceivedMqttMessage) {
            ReceivedMqttMessage receivedMessage = (ReceivedMqttMessage) message;
            if (receivedMessage.getSubscription().isMuted()) {
                return;
            }
            SwingUtilities.invokeLater(() -> {
                scriptLoader.decode(receivedMessage, decodedMessage -> {
                    if (decodedMessage != null) {
                        messageReceived(decodedMessage);
                    }
                });
            });
        }
    }

    private void applyLayout(MessageViewMode viewMode) {
        if (viewMode == MessageViewMode.TABLE) {
            messageSplitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
            Integer divider = Configuration.instance().getInt(ConfKeys.MESSAGE_VERTICAL_DIVIDER, 500);
            int maxHeight = messageSplitPanel.getPreferredSize().height - PREVIEW_PANEL_MIN_HEIGHT;
            messageSplitPanel.setDividerLocation(Math.min(divider, maxHeight));
            detailTabbedPanel.setTabPlacement(JTabbedPane.LEFT);
            detailTabbedPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP);
        } else {
            messageSplitPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            Integer divider = Configuration.instance().getInt(ConfKeys.MESSAGE_HORIZONTAL_DIVIDER, 850);
            int maxWidth = messageSplitPanel.getPreferredSize().width - PREVIEW_PANEL_MIN_WIDTH;
            messageSplitPanel.setDividerLocation(Math.min(divider, maxWidth));
            detailTabbedPanel.setTabPlacement(JTabbedPane.TOP);
            detailTabbedPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.LEADING);
        }
        messagePublishPanel.toggleViewMode(viewMode);
        messagePreviewPanel.toggleViewMode(viewMode);
    }

    @Override
    public void previewMessage(MqttMessage message) {
        messagePreviewPanel.previewMessage(message);
    }

    public abstract boolean doPublishMessage(PublishedMqttMessage message);

    @Override
    public void publishMessage(PublishedMqttMessage message) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (doPublishMessage(message)) {
                    getEventListeners().forEach(l -> l.onMessage(message));
                }
            } catch (RuntimeException e) {
                Utils.Toast.warn(e.getMessage());
                log.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public String getPayloadFormat() {
        return payloadFormat != null ? payloadFormat : properties.getPayloadFormat();
    }

    @Override
    public void setPayloadFormat(String payloadFormat) {
        this.payloadFormat = payloadFormat;
    }

    private void doLoadScript() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.addChoosableFileFilter(new FileExtensionsFilter(LangUtil.getString("JavaScriptFileFilter"), "js"));
        jFileChooser.setDialogTitle(LangUtil.getString("ChooseFile"));
        String directory = Configuration.instance().getString(ConfKeys.SCRITP_OPEN_DIALOG_PATH);
        if (directory != null) {
            jFileChooser.setCurrentDirectory(new File(directory));
        }
        int option = jFileChooser.showOpenDialog(MqttInsightApplication.frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            Configuration.instance().set(ConfKeys.SCRITP_OPEN_DIALOG_PATH, jFileChooser.getCurrentDirectory().getAbsolutePath());
            File selectedFile = jFileChooser.getSelectedFile();
            try {
                doScriptReload(selectedFile, false);
            } catch (Exception e) {
                String error = e.getMessage();
                log.error(error, e);
                Utils.Message.error(error, e);
            }
        }
    }

    private void doScriptReload(File scriptFile, boolean isReload) {
        ThreadUtil.execAsync(() -> {
            if (scriptLoader == null) {
                scriptLoader = new ScriptLoader(this);
            }
            scriptLoader.loadScript(scriptFile, (result) -> {
                if (result.isSuccess()) {
                    if (!isReload) {
                        getEventListeners().forEach(l -> l.scriptLoaded(scriptFile));
                    }
                    String message = LangUtil.format("ScriptSuccess", scriptFile.getAbsolutePath());
                    log.debug(message);
                    Utils.Toast.success(message);
                } else if (result.getException() != null) {
                    String error = LangUtil.format("ScriptError", scriptFile.getAbsolutePath());
                    error += "\n" + result.getMessage();
                    log.error(error, result.getException());
                    Utils.Message.error(error);
                } else {
                    String error = LangUtil.format("ScriptError", scriptFile.getAbsolutePath());
                    error += "\n" + result.getMessage();
                    log.error(error);
                    Utils.Message.error(error);
                }
            });
        });
    }

    private void doScriptRemove(File scriptFile) {
        if (scriptLoader != null) {
            scriptLoader.removeScript(scriptFile.getAbsolutePath());
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
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(0, 0));
        rootPanel.add(topPanel, BorderLayout.NORTH);
        subscriptionSplitPanel = new JSplitPane();
        subscriptionSplitPanel.setContinuousLayout(false);
        subscriptionSplitPanel.setDividerLocation(219);
        subscriptionSplitPanel.setResizeWeight(0.0);
        rootPanel.add(subscriptionSplitPanel, BorderLayout.CENTER);
        messageSplitPanel = new JSplitPane();
        messageSplitPanel.setDividerLocation(385);
        messageSplitPanel.setOrientation(0);
        messageSplitPanel.setResizeWeight(1.0);
        subscriptionSplitPanel.setRightComponent(messageSplitPanel);
        detailTabbedPanel = new JTabbedPane();
        detailTabbedPanel.setDoubleBuffered(true);
        messageSplitPanel.setRightComponent(detailTabbedPanel);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
