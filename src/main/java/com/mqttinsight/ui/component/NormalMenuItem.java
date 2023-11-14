package com.mqttinsight.ui.component;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * @author jinjq
 */
public class NormalMenuItem extends JMenuItem {

    public NormalMenuItem(){
        this(null, null);
    }

    public NormalMenuItem(Icon icon) {
        this(null, icon);
    }

    public NormalMenuItem(String text) {
        this(text, null);
    }

    public NormalMenuItem(String text, Icon icon) {
        super(text, icon);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        // left mouse button only
        if ( !SwingUtilities.isRightMouseButton(e) && !SwingUtilities.isMiddleMouseButton(e)) {
            super.processMouseEvent(e);
        }
    }
}
