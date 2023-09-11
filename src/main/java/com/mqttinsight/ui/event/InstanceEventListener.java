package com.mqttinsight.ui.event;

import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.ui.component.model.MessageViewMode;

import java.io.File;

public interface InstanceEventListener {

    void onViewModeChanged(MessageViewMode viewMode);

    void viewInitializeCompleted();

    void onMessage(MqttMessage message);

    void clearAllMessages();

    void clearMessages(Subscription subscription);

    void exportAllMessages();

    void exportMessages(Subscription subscription);

    void toggleAutoScroll(boolean autoScroll);

    void tableSelectionChanged();

    void requestFocusPreview();
    
    void favoriteChanged();

    void fireLoadScript();

    void scriptLoaded(File scriptFile);

    void fireScriptRemove(File scriptFile);

    void fireScriptReload(File scriptFile);
}
