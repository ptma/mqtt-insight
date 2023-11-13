package com.mqttinsight.ui.component;

import javax.swing.*;

/**
 * @author ptma
 */
public class PopupMenuButton extends AbstractPopupButton {

    public PopupMenuButton(String text) {
        this(text, null, true);
    }

    public PopupMenuButton(String text, boolean showArrow) {
        this(text, null, showArrow);
    }

    public PopupMenuButton(Icon icon) {
        this(null, icon, true);
    }

    public PopupMenuButton(Icon icon, boolean showArrow) {
        this(null, icon, showArrow);
    }

    public PopupMenuButton() {
        this(null, null, true);
    }

    public PopupMenuButton(String text, Icon icon, boolean showArrow) {
        super(text, icon, showArrow);
    }

    public JMenuItem addMenuItem(String menuText) {
        return addMenuItem(new JMenuItem(menuText));
    }

    public JMenuItem addMenuItem(JMenuItem menuItem) {
        return getPopupMenu().add(menuItem);
    }

    public void addSeparator() {
        getPopupMenu().addSeparator();
    }

}
