package com.mqttinsight.ui.component;

import javax.swing.*;

/**
 * @author ptma
 */
public class PopupMenuButton extends AbstractPopupButton {

    public PopupMenuButton(String text) {
        this(text, null);
    }

    public PopupMenuButton(Action action) {
        super(action);
    }

    public PopupMenuButton(Icon icon) {
        this(null, icon);
    }

    public PopupMenuButton() {
        this(null, null);
    }

    public PopupMenuButton(String text, Icon icon) {
        super(text, icon);
    }

    @Override
    protected JPopupMenu createPopup() {
        return new JPopupMenu();
    }

    public JMenuItem addMunuItem(String menuText) {
        return addMunuItem(new JMenuItem(menuText));
    }

    public JMenuItem addMunuItem(JMenuItem menuItem) {
        return getPopup().add(menuItem);
    }

    public void addSeparator() {
        getPopup().addSeparator();
    }
}
