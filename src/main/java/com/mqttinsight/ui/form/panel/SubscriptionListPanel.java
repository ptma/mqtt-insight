package com.mqttinsight.ui.form.panel;

import com.intellij.uiDesigner.core.GridLayoutManager;
import com.mqttinsight.mqtt.*;
import com.mqttinsight.ui.component.SubscriptionListItem;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import lombok.Getter;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ptma
 */
public class SubscriptionListPanel {

    @Getter
    private final MqttInstance mqttInstance;
    private final List<SubscriptionListItem> subscriptionList;
    private SubscriptionListItem selectedItem;

    private JPanel rootPanel;
    private JScrollPane scrollPanel;
    private JPanel containerPanel;

    public SubscriptionListPanel(final MqttInstance mqttInstance) {
        this.mqttInstance = mqttInstance;
        this.subscriptionList = new ArrayList<>();
        $$$setupUI$$$();
        initComponents();
        initEventListeners();
    }

    private void initComponents() {
        scrollPanel.getVerticalScrollBar().setUnitIncrement(16);
        containerPanel.setLayout(new VerticalLayout(0));
        containerPanel.setBackground(UIManager.getColor("Table.background"));
    }

    private void initEventListeners() {
        mqttInstance.addEventListeners(new InstanceEventAdapter() {
            @Override
            public void onMessage(MqttMessage message) {
                onMessageReceived(message);
            }

            @Override
            public void clearAllMessages() {
                SwingUtilities.invokeLater(() -> {
                    for (SubscriptionListItem listItem : subscriptionList) {
                        listItem.resetMessageCount();
                    }
                });
            }
        });
    }

    private void onMessageReceived(MqttMessage message) {
        SwingUtilities.invokeLater(() -> {
            if (message instanceof ReceivedMqttMessage) {
                ReceivedMqttMessage receivedMqttMessage = (ReceivedMqttMessage) message;
                for (SubscriptionListItem listItem : subscriptionList) {
                    if (listItem.hasSubscription(receivedMqttMessage.getSubscription())) {
                        listItem.updateMessageCounter();
                    }
                }
            }
        });
    }

    public void onConnectionChanged(ConnectionStatus status) {
        SwingUtilities.invokeLater(() -> {
            if (status == ConnectionStatus.CONNECTING || status == ConnectionStatus.DISCONNECTING) {
                return;
            }
            boolean connected = status.equals(ConnectionStatus.CONNECTED);
            if (!connected) {
                // Connection lost, disable all subscriptions
                for (SubscriptionListItem listItem : subscriptionList) {
                    listItem.setSubscribed(false);
                }
            }
        });
    }

    public JComponent getRootPanel() {
        return rootPanel;
    }

    public MqttProperties getProperties() {
        return mqttInstance.getProperties();
    }

    public void doSubscribe(Subscription subscription) {
        SwingUtilities.invokeLater(() -> {
            for (SubscriptionListItem listItem : subscriptionList) {
                if (listItem.hasTopic(subscription.getTopic())) {
                    Utils.Toast.info(LangUtil.getString("TopicSubscribed"));
                    return;
                }
            }
            if (mqttInstance.subscribe(subscription)) {
                SubscriptionListItem itemPanel = new SubscriptionListItem(this, subscription, (closable) -> {
                    unsubscribe(subscription, closable);
                });
                containerPanel.add(itemPanel);
                subscriptionList.add(itemPanel);
                if (containerPanel.isShowing()) {
                    containerPanel.revalidate();
                    containerPanel.repaint();
                }
            }
        });
    }

    private void unsubscribe(Subscription subscription, boolean closable) {
        SwingUtilities.invokeLater(() -> {
            if (mqttInstance.unsubscribe(subscription)) {
                for (SubscriptionListItem listItem : subscriptionList) {
                    if (listItem.hasSubscription(subscription)) {
                        listItem.setSubscribed(false);
                        if (closable) {
                            containerPanel.remove(listItem);
                            subscriptionList.remove(listItem);
                            if (containerPanel.isShowing()) {
                                containerPanel.revalidate();
                                containerPanel.repaint();
                            }
                            if (selectedItem != null && selectedItem.equals(listItem)) {
                                selectedItem = null;
                            }
                            if (mqttInstance.getProperties().isClearUnsubMessage()) {
                                clearMessages(subscription);
                            }
                        }
                        return;
                    }
                }
            }
        });
    }

    public void remove(Subscription subscription) {
        SwingUtilities.invokeLater(() -> {
            for (SubscriptionListItem listItem : subscriptionList) {
                if (listItem.hasSubscription(subscription)) {
                    containerPanel.remove(listItem);
                    subscriptionList.remove(listItem);
                    if (mqttInstance.getProperties().isClearUnsubMessage()) {
                        clearMessages(subscription);
                    }
                    if (containerPanel.isShowing()) {
                        containerPanel.revalidate();
                        containerPanel.repaint();
                    }
                    if (selectedItem != null && selectedItem.equals(listItem)) {
                        selectedItem = null;
                    }
                    return;
                }
            }
        });
    }

    public void select(SubscriptionListItem itemPanel) {
        SwingUtilities.invokeLater(() -> {
            if (selectedItem != null) {
                selectedItem.setSelected(false);
                selectedItem.updateComponents();
            }
            if (itemPanel != null) {
                selectedItem = itemPanel;
                selectedItem.setSelected(true);
                selectedItem.updateComponents();
            }
        });
    }

    public void clearMessages(Subscription subscription) {
        mqttInstance.getEventListeners().forEach(eventListener -> {
            eventListener.clearMessages(subscription);
        });
    }

    public void exportMessages(Subscription subscription) {
        mqttInstance.getEventListeners().forEach(eventListener -> {
            eventListener.exportMessages(subscription);
        });
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
        scrollPanel.setHorizontalScrollBarPolicy(31);
        rootPanel.add(scrollPanel, BorderLayout.CENTER);
        containerPanel = new JPanel();
        containerPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        scrollPanel.setViewportView(containerPanel);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
