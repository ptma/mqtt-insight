package com.mqttinsight.ui.component;

import cn.hutool.core.img.ColorUtil;
import com.formdev.flatlaf.FlatLightLaf;
import com.mqttinsight.MqttInsightApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.graphics.ColorUtilities;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

/**
 * @author ptma
 */
public class ColorPicker extends JComboBox<ColorPicker.ColorValue> {

    private final ColorValue value;
    private JPanel popupPanel;
    private JButton moreButton;
    private String dialogTitle = "Choose Color";

    private int gridRows = 11;
    private int gridCols = 11;

    public ColorPicker(Color color) {
        super();
        this.value = new ColorValue(color);
        this.addItem(value);
        this.setSelectedItem(this.value);
        this.setEditable(false);

        this.setRenderer(new ColorCellRenderer());
        this.addPopupMenuListener(new PickerPopupListener());

        initPopupPanel();
    }

    private void initPopupPanel() {
        int cellSize = 15;
        int cellSpace = 1;
        int borderInset = 10;
        MigLayout migLayout = new MigLayout(
            String.format("insets %d", borderInset),
            String.format("[]%d[]", cellSpace),
            String.format("[]%d[]", cellSpace)
        );
        popupPanel = new JPanel(migLayout);

        ActionListener colorCellActionListener = e -> {
            if (e.getSource() instanceof JButton) {
                applyColor(((JButton) e.getSource()).getBackground());
            }
            this.hidePopup();
        };

        JButton cellButton;
        float saturation = 1.0f;
        float lightness = 0.1f;

        int colorCols = gridCols - 1;
        for (int row = 0; row < gridRows; row++) {
            float grayLightness = row * 1.0f / (gridRows - 1);
            Color grayColor = ColorUtilities.HSLtoRGB(0, 0, grayLightness);
            cellButton = new JButton();
            cellButton.setToolTipText(ColorUtil.toHex(grayColor));
            cellButton.setBackground(grayColor);
            cellButton.addActionListener(colorCellActionListener);
            popupPanel.add(cellButton, String.format("w %d!,h %d!", cellSize, cellSize));

            float rowLightness = lightness + row * (1 - lightness) / gridRows;
            for (int col = 0; col < colorCols; col++) {
                float hue = col * 1.0f / colorCols;
                Color color = ColorUtilities.HSLtoRGB(hue, saturation, rowLightness);
                cellButton = new JButton();
                cellButton.setToolTipText(ColorUtil.toHex(color));
                cellButton.setBackground(color);
                cellButton.addActionListener(colorCellActionListener);
                if (col == colorCols - 1) {
                    popupPanel.add(cellButton, String.format("w %d!,h %d!,wrap", cellSize, cellSize));
                } else {
                    popupPanel.add(cellButton, String.format("w %d!,h %d!", cellSize, cellSize));
                }
            }
        }
        moreButton = new JButton("More ...");
        moreButton.addActionListener(e -> {
            this.hidePopup();
            JColorChooser colorChooser = new JColorChooser();
            colorChooser.setColor(value.getColor());
            JDialog dialog = JColorChooser.createDialog(MqttInsightApplication.frame,
                dialogTitle,
                true,
                colorChooser,
                e1 -> {
                    applyColor(colorChooser.getColor());
                },
                null
            );
            dialog.setVisible(true);
        });
        popupPanel.add(moreButton, "h 25!,gaptop 5,spanx,growx");

        int width = cellSpace * (gridCols - 1) + cellSize * gridCols + borderInset * 2;
        int height = cellSpace * (gridRows - 1) + cellSize * gridRows + borderInset * 2 + 5 + 25;
        popupPanel.setPreferredSize(new Dimension(width, height));
    }

    private void applyColor(Color color) {
        value.setColor(color);
        selectedItemChanged();
    }

    public Color getColor() {
        return value.getColor();
    }

    public void setMoreText(String text) {
        moreButton.setText(text);
    }

    public void setDialogTitle(String title) {
        dialogTitle = title;
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
            if (cPopup.getComponentCount() != 1 || cPopup.getComponent(0) != popupPanel) {
                cPopup.removeAll();
                cPopup.add(popupPanel);
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
