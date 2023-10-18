package com.mqttinsight.ui.component;

import com.mqttinsight.ui.event.ColorSelectionListener;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ptma
 */
public class PopupColorButton extends AbstractPopupButton {

    private ColorGridPanel colorPanel;
    private final List<ColorSelectionListener> selectionListeners = new ArrayList<>();

    public PopupColorButton(Icon icon, boolean showArrow) {
        super(icon, showArrow);
        createPopupContent();
    }

    public void addColorSelectionListener(ColorSelectionListener listener) {
        selectionListeners.add(listener);
    }

    protected void createPopupContent() {
        colorPanel = new ColorGridPanel();
        colorPanel.addColorSelectionListener(c -> {
            selectionListeners.forEach(listener -> listener.onColorSelected(c));
            this.hidePopup();
        });
        getPopupMenu().add(colorPanel);
    }

    public void setMoreText(String text) {
        colorPanel.setMoreText(text);
    }

    public void setDialogTitle(String title) {
        colorPanel.setDialogTitle(title);
    }
}
