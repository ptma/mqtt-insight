package com.mqttinsight.ui.component;

import com.formdev.flatlaf.FlatLightLaf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * @author ptma
 */
public class ColorPicker extends JComboBox<ColorPicker.ColorValue> {

    private final ColorValue value;
    private final ColorGridPanel colorPanel;

    public ColorPicker(Color color) {
        super();
        this.value = new ColorValue(color);
        this.addItem(value);
        this.setSelectedItem(this.value);
        this.setEditable(false);

        this.setRenderer(new ColorCellRenderer());
        this.addPopupMenuListener(new PickerPopupListener());

        colorPanel = new ColorGridPanel();
        colorPanel.setColorValue(color);
        colorPanel.addColorSelectionListener(c -> {
            value.setColor(c);
            selectedItemChanged();
            this.hidePopup();
        });
    }

    public Color getColor() {
        return value.getColor();
    }

    public void setMoreText(String text) {
        colorPanel.setMoreText(text);
    }

    public void setDialogTitle(String title) {
        colorPanel.setDialogTitle(title);
    }

    @Override
    public Object getSelectedItem() {
        return value;
    }

    @Override
    protected void selectedItemChanged() {
        if (value != null) {
            fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, value, ItemEvent.SELECTED));
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    protected static class ColorValue {
        private Color color;
    }

    protected static class ColorCellRenderer implements ListCellRenderer<ColorValue> {
        protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList list, ColorValue value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            renderer.setBackground(value.color);
            renderer.setIcon(new ColorIcon(value.color, 30, 11));
            renderer.setText(" ");
            renderer.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
            renderer.setVerticalAlignment(SwingConstants.CENTER);
            return renderer;
        }
    }

    protected static class ColorIcon implements Icon {

        private final Color color;
        private final int width;
        private final int height;

        public ColorIcon(Color color, int width, int height) {
            this.color = color;
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (null == color) {
                g.setColor(Color.black);
                g.drawLine(x, y + height, x + width, y);
            } else {
                g.setColor(color);
                g.fillRect(x, y, width, height);
            }
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }

    }

    protected class PickerPopupListener implements PopupMenuListener {

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            Accessible accPopup = getUI().getAccessibleChild(ColorPicker.this, 0);
            Container cPopup = (Container) accPopup;
            if (cPopup.getComponentCount() != 1 || cPopup.getComponent(0) != colorPanel) {
                cPopup.removeAll();
                cPopup.add(colorPanel);
            }
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            // Nothing to do
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            Accessible accPopup = getUI().getAccessibleChild(ColorPicker.this, 0);
            BasicComboPopup cPopup = (BasicComboPopup) accPopup;
            cPopup.hide();
        }
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame();
        frame.setSize(600, 200);
        frame.setLocation(100, 100);
        Container container = frame.getContentPane();
        container.setLayout(null);

        FlatLightLaf.setup();

        ColorPicker colorPicker = new ColorPicker(Color.WHITE);
        container.add(colorPicker);
        colorPicker.setLocation(10, 10);
        colorPicker.setSize(colorPicker.getPreferredSize());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
