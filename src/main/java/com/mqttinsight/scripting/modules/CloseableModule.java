package com.mqttinsight.scripting.modules;

public interface CloseableModule {

    void onClose(Runnable callback);

    void close();
}
