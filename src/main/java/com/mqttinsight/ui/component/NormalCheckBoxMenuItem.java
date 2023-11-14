package com.mqttinsight.ui.component;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * @author jinjq
 */
public class NormalCheckBoxMenuItem extends JCheckBoxMenuItem {

    public NormalCheckBoxMenuItem(){
        this(null, null, false);
    }

    public NormalCheckBoxMenuItem(Icon icon) {
        this(null, icon, false);
    }

    public NormalCheckBoxMenuItem(String text) {
        this(text, null, false);
    }

    public NormalCheckBoxMenuItem(String text, Icon icon, boolean selected) {
        super(text, icon, selected);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        // left mouse button only
        if ( !SwingUtilities.isRightMouseButton(e) && !SwingUtilities.isMiddleMouseButton(e)) {
            super.processMouseEvent(e);
        }
    }
}
