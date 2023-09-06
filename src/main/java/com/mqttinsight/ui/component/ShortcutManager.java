package com.mqttinsight.ui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ptma
 */
public class ShortcutManager implements AWTEventListener {

    private static class Holder {
        final static ShortcutManager INSTANCE = new ShortcutManager();
    }

    public static ShortcutManager instance() {
        return ShortcutManager.Holder.INSTANCE;
    }

    private final Map<KeyStroke, ShortcutEventListener> listeners;

    private ShortcutManager() {
        listeners = new HashMap<>();
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
    }

    /**
     * Invoked when an event is dispatched
     */
    @Override
    public void eventDispatched(AWTEvent event) {
        if (event.getClass() == KeyEvent.class) {
            KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent((KeyEvent) event);
            ShortcutEventListener eventListener = listeners.get(keyStroke);
            if (eventListener != null) {
                eventListener.handle();
            }
        }
    }

    public void registerShortcut(KeyStroke keyStroke, ShortcutEventListener eventListener) {
        listeners.put(keyStroke, eventListener);
    }

    public void unRegisterShortcut(KeyStroke keyStroke) {
        listeners.remove(keyStroke);
    }

    public interface ShortcutEventListener {
        void handle();
    }

}
