package com.mqttinsight.ui.event;

import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.ui.component.model.MessageViewMode;

import java.io.File;

/**
 * @author ptma
 */
public abstract class InstanceEventAdapter implements InstanceEventListener {

    @Override
    public void onViewModeChanged(MessageViewMode viewMode) {
    }

    @Override
    public void viewInitializeCompleted() {
    }

    @Override
    public void onSubscribe(Subscription subscription) {
    }

    @Override
    public void onUnsubscribe(Subscription subscription, boolean closable) {
    }

    @Override
    public void onMessage(MqttMessage message) {
    }

    @Override
    public void onMessage(MqttMessage message, MqttMessage parent) {
    }

    @Override
    public void clearAllMessages() {
    }

    @Override
    public void clearMessages(Subscription subscription) {
    }

    @Override
    public void exportAllMessages() {
    }

    @Override
    public void exportMessages(Subscription subscription) {
    }

    @Override
    public void toggleAutoScroll(boolean autoScroll) {
    }

    @Override
    public void tableSelectionChanged(MqttMessage message) {
    }

    @Override
    public void requestFocusPreview() {
    }

    @Override
    public void favoriteChanged() {

    }

    @Override
    public void fireLoadScript() {
    }

    @Override
    public void scriptLoaded(File scriptFile) {
    }

    @Override
    public void fireScriptRemove(File scriptFile) {
    }

    @Override
    public void fireScriptReload(File scriptFile) {
    }
}
