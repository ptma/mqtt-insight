package com.mqttinsight.ui.form.panel;

import com.intellij.uiDesigner.core.GridLayoutManager;
import com.mqttinsight.mqtt.ConnectionStatus;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.ReceivedMqttMessage;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.scripting.DecodedMqttMessage;
import com.mqttinsight.ui.component.SubscriptionItem;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import lombok.Getter;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ptma
 */
public class SubscriptionListPanel extends JScrollPane {

    @Getter
    private final MqttInstance mqttInstance;
    @Getter
    private final List<SubscriptionItem> subscriptions;

    private JPanel containerPanel;

    public SubscriptionListPanel(final MqttInstance mqttInstance) {
        super();
        this.mqttInstance = mqttInstance;
        this.subscriptions = new ArrayList<>();
        initComponents();
        initEventListeners();
    }

    private void initComponents() {
        setHorizontalScrollBarPolicy(31);
        containerPanel = new JPanel();
        containerPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        setViewportView(containerPanel);

        containerPanel.setLayout(new VerticalLayout(0));
        containerPanel.setBackground(UIManager.getColor("Table.background"));
    }

    private void initEventListeners() {
        mqttInstance.addEventListener(new InstanceEventAdapter() {

            @Override
            public void onSubscribe(Subscription subscription) {
                updateOnSubscribe(subscription);
            }

            @Override
            public void onUnsubscribe(Subscription subscription, boolean closable) {
                updateOnUnsubscribe(subscription, closable);
            }

            @Override
            public void onMessage(MqttMessage message) {
                onMessageReceived(message);
            }

            @Override
            public void clearAllMessages() {
                SwingUtilities.invokeLater(() -> {
                    for (SubscriptionItem listItem : subscriptions) {
                        listItem.resetMessageCount();
                    }
                });
            }

            @Override
            public void onMessageRemoved(MqttMessage message) {
                if (message instanceof ReceivedMqttMessage && !(message instanceof DecodedMqttMessage)) {
                    for (SubscriptionItem item : subscriptions) {
                        if (item.hasSubscription(((ReceivedMqttMessage) message).getSubscription())) {
                            item.decrementMessageCount();
                            break;
                        }
                    }
                }
            }
        });
    }

    private void onMessageReceived(MqttMessage message) {
        SwingUtilities.invokeLater(() -> {
            if (message instanceof ReceivedMqttMessage) {
                for (SubscriptionItem item : subscriptions) {
                    if (item.hasSubscription(((ReceivedMqttMessage) message).getSubscription())) {
                        item.updateMessageCounter();
                        return;
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
                for (SubscriptionItem listItem : subscriptions) {
                    listItem.setSubscribed(false);
                }
            } else {
                for (SubscriptionItem listItem : subscriptions) {
                    if (!listItem.isSubscribed()) {
                        listItem.resubscribe();
                    }
                }
            }
        });
    }

    private void updateOnSubscribe(Subscription subscription) {
        SwingUtilities.invokeLater(() -> {
            // 订阅条目是否已存在
            for (SubscriptionItem item : subscriptions) {
                if (item.hasTopic(subscription.getTopic())) {
                    item.setSubscribed(true);
                    containerPanel.revalidate();
                    containerPanel.repaint();
                    return;
                }
            }
            SubscriptionItem newItem = new SubscriptionItem(mqttInstance, subscription);
            containerPanel.add(newItem);
            subscriptions.add(newItem);
            containerPanel.revalidate();
            containerPanel.repaint();
        });
    }

    private void updateOnUnsubscribe(Subscription subscription, boolean closable) {
        SwingUtilities.invokeLater(() -> {
            subscriptions.stream()
                .filter(item -> item.hasSubscription(subscription))
                .findFirst()
                .ifPresent(item -> {
                    if (item.isSubscribed()) {
                        mqttInstance.unsubscribe(subscription, (success) -> {
                            if (success) {
                                if (closable) {
                                    removeItem(item);
                                } else {
                                    item.setSubscribed(false);
                                    containerPanel.revalidate();
                                    containerPanel.repaint();
                                }
                            }
                        });
                    } else if (closable) {
                        removeItem(item);
                    }
                });
        });
    }

    private void removeItem(SubscriptionItem item) {
        subscriptions.remove(item);
        containerPanel.remove(item);
        if (mqttInstance.getProperties().isClearUnsubMessage()) {
            mqttInstance.applyEvent(eventListener -> {
                eventListener.clearMessages(item.getSubscription());
            });
        }
        containerPanel.revalidate();
        containerPanel.repaint();
    }

}
