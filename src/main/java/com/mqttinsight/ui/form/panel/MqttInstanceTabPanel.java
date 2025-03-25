package com.mqttinsight.ui.form.panel;

import cn.hutool.core.thread.ThreadUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.*;
import com.mqttinsight.scripting.ScriptLoader;
import com.mqttinsight.ui.chart.BaseChartFrame;
import com.mqttinsight.ui.chart.series.SeriesProperties;
import com.mqttinsight.ui.component.*;
import com.mqttinsight.ui.component.model.MessageViewMode;
import com.mqttinsight.ui.component.tree.TopicTree;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.ui.event.InstanceEventListener;
import com.mqttinsight.ui.form.MainWindowForm;
import com.mqttinsight.ui.form.NewSubscriptionForm;
import com.mqttinsight.ui.form.SubscriptionManagerForm;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.TopicUtil;
import com.mqttinsight.util.Utils;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author ptma
 */
public abstract class MqttInstanceTabPanel extends JPanel implements MqttInstance {

    protected static final Logger log = LoggerFactory.getLogger(MqttInstanceTabPanel.class);

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
    private JSplitPane mainSplitPanel;
    private JSplitPane leftSplitPanel;
    private JSplitPane rightSplitPanel;
    private JTabbedPane detailTabbedPanel;

    private JLabel statusLabel;
    private JButton connectButton;
    private SplitButton subscribeButton;
    private JPopupMenu favoriteMenu;
    private MessageToolbar messageToolbar;

    protected MessageViewMode viewMode;
    protected MessageViewPanel messageViewPanel;
    protected MessagePublishPanel messagePublishPanel;
    protected MessagePreviewPanel messagePreviewPanel;

    protected SubscriptionListPanel subscriptionListPanel;
    protected TopicTree topicTree;
    private JScrollPane topicTreeScrollPanel;

    private final List<InstanceEventListener> eventListeners;

    private final List<BaseChartFrame<? extends SeriesProperties>> chartFrames;

    private ScriptLoader scriptLoader;

    protected AtomicInteger reconnectAttempts = new AtomicInteger(0);

    public MqttInstanceTabPanel(MqttProperties properties) {
        super();
        this.properties = properties;
        eventListeners = new CopyOnWriteArrayList<>();
        chartFrames = new ArrayList<>();
        setLayout(new BorderLayout());
        $$$setupUI$$$();
        add(rootPanel, BorderLayout.CENTER);
        initComponents();
        initEventListeners();
    }

    private void initComponents() {
        {
            // main
            Integer divider = Configuration.instance().getInt(ConfKeys.SUBSCRIPTION_HORIZONTAL_DIVIDER, 255);
            mainSplitPanel.setDividerLocation(divider);
            mainSplitPanel.setOneTouchExpandable(true);
            mainSplitPanel.putClientProperty("JSplitPane.expandableSide", "right");
            mainSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
                Configuration.instance().set(ConfKeys.SUBSCRIPTION_HORIZONTAL_DIVIDER, evt.getNewValue());
                Configuration.instance().changed();
            });
        }

        {
            // left
            subscriptionListPanel = new SubscriptionListPanel(this);
            topicTree = new TopicTree(this);
            topicTreeScrollPanel = new JScrollPane(topicTree);
            Border scrollPanelBorder = new SingleLineBorder(UIManager.getColor("Component.borderColor"), true, true, true, true);
            topicTreeScrollPanel.setBorder(scrollPanelBorder);

            leftSplitPanel.setLeftComponent(subscriptionListPanel);
            leftSplitPanel.setRightComponent(topicTreeScrollPanel);

            Integer divider = Configuration.instance().getInt(ConfKeys.SUBSCRIPTION_VERTICAL_DIVIDER, 430);
            leftSplitPanel.setDividerLocation(divider);
            leftSplitPanel.setOneTouchExpandable(true);
            leftSplitPanel.putClientProperty("JSplitPane.expandableSide", "left");
            leftSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
                Configuration.instance().set(ConfKeys.SUBSCRIPTION_VERTICAL_DIVIDER, evt.getNewValue());
                Configuration.instance().changed();
            });
        }

        // right
        MessageViewMode viewMode = MessageViewMode.of(Configuration.instance().getString(ConfKeys.MESSAGE_VIEW, MessageViewMode.TABLE.toString()));
        messageViewPanel = new MessageViewPanel(this, viewMode);
        messagePublishPanel = new MessagePublishPanel(this);
        messagePreviewPanel = new MessagePreviewPanel(this);

        rightSplitPanel.setTopComponent(messageViewPanel);
        rightSplitPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            if (rightSplitPanel.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                Configuration.instance().set(ConfKeys.MESSAGE_VERTICAL_DIVIDER, evt.getNewValue());
            } else {
                Configuration.instance().set(ConfKeys.MESSAGE_HORIZONTAL_DIVIDER, evt.getNewValue());
            }
            Configuration.instance().changed();
        });
        rightSplitPanel.setOneTouchExpandable(true);
        rightSplitPanel.putClientProperty("JSplitPane.expandableSide", "left");

        Border tabbedPanelBorder = new SingleLineBorder(UIManager.getColor("Component.borderColor"), true, true, true, true);
        detailTabbedPanel.setBorder(tabbedPanelBorder);
        detailTabbedPanel.setTabPlacement(JTabbedPane.LEFT);
        detailTabbedPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP);

        detailTabbedPanel.addTab(LangUtil.getString("Preview"), Icons.PREVIEW, messagePreviewPanel, LangUtil.getString("Preview"));
        detailTabbedPanel.addTab(LangUtil.getString("Publish"), Icons.SEND, messagePublishPanel, LangUtil.getString("Publish"));
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
        connectButton.addActionListener(e -> connectButtonAction());
        leftToolbar.add(connectButton);

        subscribeButton = new SplitButton();
        subscribeButton.setIcon(Icons.SUBSCRIBE);
        LangUtil.buttonText(subscribeButton, "NewSubscription");
        subscribeButton.setToolTipText(LangUtil.getString("NewSubscription") + " (Ctrl + Shift + S)");
        subscribeButton.setEnabled(false);
        subscribeButton.addActionListener(e -> {
            openSubscriptionForm();
        });
        favoriteMenu = new JPopupMenu();
        loadFavoriteMenus();
        subscribeButton.setPopupMenu(favoriteMenu);
        leftToolbar.add(subscribeButton);

        leftToolbar.addSeparator();

        topPanel.add(leftToolbar, "cell 0 0");

        statusLabel = new JLabel();
        topPanel.add(statusLabel, "cell 1 0, gapx 10px 0");

        messageToolbar = new MessageToolbar(this);
        topPanel.add(messageToolbar, "cell 3 0");
    }

    private void applyLayout(MessageViewMode viewMode) {
        if (this.viewMode != null && this.viewMode.equals(viewMode)) {
            return;
        }
        if (viewMode == MessageViewMode.TABLE) {
            rightSplitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
            Integer divider = Configuration.instance().getInt(ConfKeys.MESSAGE_VERTICAL_DIVIDER, 430);
            int maxHeight = rightSplitPanel.getPreferredSize().height - PREVIEW_PANEL_MIN_HEIGHT;
            rightSplitPanel.setDividerLocation(Math.min(divider, maxHeight));
            detailTabbedPanel.setTabPlacement(JTabbedPane.LEFT);
            detailTabbedPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP);
        } else {
            rightSplitPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            Integer divider = Configuration.instance().getInt(ConfKeys.MESSAGE_HORIZONTAL_DIVIDER, 600);
            int maxWidth = rightSplitPanel.getPreferredSize().width - PREVIEW_PANEL_MIN_WIDTH;
            rightSplitPanel.setDividerLocation(Math.min(divider, maxWidth));
            detailTabbedPanel.setTabPlacement(JTabbedPane.TOP);
            detailTabbedPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.LEADING);
        }
        this.viewMode = viewMode;
        messagePublishPanel.toggleViewMode(viewMode);
        messagePreviewPanel.toggleViewMode(viewMode);
    }

    private void initEventListeners() {
        addEventListener(new InstanceEventAdapter() {
            @Override
            public void onViewModeChanged(MessageViewMode viewMode) {
                applyLayout(viewMode);
            }

            @Override
            public void requestFocusPreview() {
                detailTabbedPanel.setSelectedIndex(0);
            }

            @Override
            public void favoriteChanged() {
                loadFavoriteMenus();
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
    public boolean close() {
        if (!chartFrames.isEmpty()) {
            int opt = Utils.Message.confirm(LangUtil.getString("CloseTabConfirm"));
            if (JOptionPane.NO_OPTION == opt) {
                return false;
            } else {
                // 复制 list , 避免 ConcurrentModificationException
                chartFrames.stream().toList().forEach(BaseChartFrame::dispose);
            }
        }
        if (scriptLoader != null) {
            scriptLoader.closeAll();
        }
        if (isConnected()) {
            onConnectionChanged(ConnectionStatus.DISCONNECTING);
            disconnect(false);
        }
        dispose();
        return true;
    }

    @Override
    public MessageTable getMessageTable() {
        return messageViewPanel.getMessageTable();
    }

    @Override
    public void addEventListener(InstanceEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    @Override
    public void removeEventListener(InstanceEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    @Override
    public void applyEvent(Consumer<InstanceEventListener> action) {
        try {
            for (InstanceEventListener listener : eventListeners) {
                if (listener != null) {
                    action.accept(listener);
                }
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    protected int getReasonCode() {
        return reasonCode;
    }

    protected void connectButtonAction() {
        if (connectionStatus.equals(ConnectionStatus.CONNECTED)) {
            disconnect(false);
        } else if (connectionStatus.equals(ConnectionStatus.DISCONNECTED) || connectionStatus.equals(ConnectionStatus.FAILED)) {
            connect();
        }
    }

    protected void autoReconnect() {
        int reconnects = reconnectAttempts.incrementAndGet();
        if (reconnects > 10) {
            log.warn("Exceeded maximum reconnection attempts. {}", properties.completeServerURI());
        } else {
            connect();
        }
    }

    protected void onConnectionChanged(ConnectionStatus status) {
        SwingUtilities.invokeLater(() -> {
            connectionStatus = status;
            if (status.equals(ConnectionStatus.FAILED)) {
                statusLabel.setIcon(Icons.ERROR);
                String reasonKey = String.format("MqttReasonCode_%d", reasonCode);
                if (LangUtil.contains(reasonKey)) {
                    statusLabel.setToolTipText(LangUtil.getString(reasonKey));
                } else {
                    statusLabel.setToolTipText(String.format("Code: %d, %s", reasonCode, lastCauseMessage));
                }
                this.disconnect(true);
            } else {
                this.reasonCode = 0;
                statusLabel.setIcon(status.getIcon());
                statusLabel.setToolTipText(null);
            }
            statusLabel.setText(LangUtil.getString(status.getText()));

            connectButton.setEnabled(!status.equals(ConnectionStatus.CONNECTING) && !status.equals(ConnectionStatus.DISCONNECTING));
            if (status.equals(ConnectionStatus.DISCONNECTED) || status.equals(ConnectionStatus.FAILED) || status.equals(ConnectionStatus.DISCONNECTING)) {
                LangUtil.buttonText(connectButton, "Connect");
                connectButton.setIcon(Icons.EXECUTE);
            } else if (status.equals(ConnectionStatus.CONNECTED)) {
                LangUtil.buttonText(connectButton, "Disconnect");
                connectButton.setIcon(Icons.SUSPEND);
                reconnectAttempts.set(0);
            }

            subscribeButton.setEnabled(status.equals(ConnectionStatus.CONNECTED));
            subscriptionListPanel.onConnectionChanged(status);
            MainWindowForm.instance().onConnectionChanged(this);
        });
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
        NewSubscriptionForm.open(this, this::subscribe);
    }

    @Override
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    @Override
    public void messageReceived(MqttMessage message) {
        applyEvent(l -> l.onMessage(message, null));
        if (scriptLoader != null && message instanceof ReceivedMqttMessage receivedMessage) {
            SwingUtilities.invokeLater(() -> {
                try {
                    scriptLoader.executeDecode(receivedMessage, decodedMessage -> {
                        if (decodedMessage != null) {
                            applyEvent(l -> l.onMessage(decodedMessage, message));
                        }
                    });
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
    }

    public abstract boolean doPublishMessage(final PublishedMqttMessage message);

    public abstract boolean doSubscribe(final Subscription subscription);

    public abstract void dispose();

    @Override
    public boolean subscribe(Subscription subscription) {
        for (SubscriptionItem existItem : subscriptionListPanel.getSubscriptions()) {
            if (existItem.hasTopic(subscription.getTopic())) {
                if (existItem.isSubscribed()) {
                    Utils.Toast.info(LangUtil.getString("TopicSubscribed"));
                    return false;
                } else {
                    // resubscribe
                    return this.doSubscribe(existItem.getSubscription());
                }
            }
        }
        return this.doSubscribe(subscription);
    }

    public Optional<Subscription> matchAnySubscription(String topic) {
        return subscriptionListPanel.getSubscriptions()
            .stream()
            .map(SubscriptionItem::getSubscription)
            .filter(subscription -> TopicUtil.match(subscription.getTopic(), topic))
            .findAny();
    }

    @Override
    public void publishMessage(PublishedMqttMessage message) {
        ThreadUtil.execute(() -> {
            if (doPublishMessage(message)) {
                applyEvent(l -> l.onMessage(message, null));
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

    private void loadFavoriteMenus() {
        favoriteMenu.removeAll();
        List<FavoriteSubscription> favoriteSubscriptions = getProperties().getFavoriteSubscriptions();
        if (favoriteSubscriptions != null && !favoriteSubscriptions.isEmpty()) {
            if (favoriteSubscriptions.size() > 1) {
                favoriteMenu.add(Utils.UI.createMenuItem(LangUtil.getString("SubscribeAll")))
                    .addActionListener(e -> {
                        favoriteSubscriptions.forEach(favorite -> {
                            Subscription subscription = new Subscription(this, favorite.getTopic(), favorite.getQos(), favorite.getPayloadFormat(), Utils.generateRandomColor());
                            this.subscribe(subscription);
                        });
                    });
            }
            favoriteMenu.add(Utils.UI.createMenuItem(LangUtil.getString("SubscriptionsManagement")))
                .addActionListener(e -> {
                    SubscriptionManagerForm.open(this);
                    loadFavoriteMenus();
                });

            favoriteMenu.addSeparator();

            favoriteSubscriptions.sort(Comparator.comparing(FavoriteSubscription::getTopic));
            favoriteSubscriptions.forEach(favorite -> {
                SplitIconMenuItem menuItem = new SplitIconMenuItem(favorite.getTopic(), null, Icons.REMOVE);
                favoriteMenu.add(menuItem)
                    .addActionListener(e -> {
                        Subscription subscription = new Subscription(this, favorite.getTopic(), favorite.getQos(), favorite.getPayloadFormat(), Utils.generateRandomColor());
                        this.subscribe(subscription);
                    });
                menuItem.addSplitActionListener(e -> {
                    int opt = Utils.Message.confirm(this, LangUtil.format("RemoveFavoriteSubscription", favorite.getTopic()));
                    if (JOptionPane.YES_OPTION == opt) {
                        favoriteSubscriptions.remove(favorite);
                        favoriteMenu.remove(menuItem);
                        Configuration.instance().changed();
                    }
                });
            });
        } else {
            favoriteMenu.add(Utils.UI.createMenuItem(LangUtil.getString("NoSubscriptions")));
        }
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
                        applyEvent(l -> l.scriptLoaded(scriptFile));
                    }
                    String message = LangUtil.format("ScriptLoaded", scriptFile.getAbsolutePath());
                    log.info(message);
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
            String message = LangUtil.format("ScriptRemoved", scriptFile.getAbsolutePath());
            log.info(message);
            Utils.Toast.success(message);
        }
    }

    public void fireCodecsChanged() {
        applyEvent(InstanceEventListener::onCodecsChanged);
    }

    @Override
    public void registerChartFrame(BaseChartFrame<? extends SeriesProperties> chartFrame) {
        chartFrames.add(chartFrame);
    }

    @Override
    public void unregisterChartFrame(BaseChartFrame<? extends SeriesProperties> chartFrame) {
        chartFrames.remove(chartFrame);
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
        mainSplitPanel = new JSplitPane();
        mainSplitPanel.setContinuousLayout(false);
        mainSplitPanel.setDividerLocation(166);
        mainSplitPanel.setResizeWeight(0.0);
        rootPanel.add(mainSplitPanel, BorderLayout.CENTER);
        rightSplitPanel = new JSplitPane();
        rightSplitPanel.setDividerLocation(385);
        rightSplitPanel.setOrientation(0);
        rightSplitPanel.setResizeWeight(1.0);
        mainSplitPanel.setRightComponent(rightSplitPanel);
        detailTabbedPanel = new JTabbedPane();
        detailTabbedPanel.setDoubleBuffered(true);
        rightSplitPanel.setRightComponent(detailTabbedPanel);
        leftSplitPanel = new JSplitPane();
        leftSplitPanel.setDividerLocation(385);
        leftSplitPanel.setOrientation(0);
        leftSplitPanel.setResizeWeight(1.0);
        mainSplitPanel.setLeftComponent(leftSplitPanel);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
