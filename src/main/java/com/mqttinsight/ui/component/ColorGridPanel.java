package com.mqttinsight.ui.component;

import cn.hutool.core.img.ColorUtil;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.ui.event.ColorSelectionListener;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.graphics.ColorUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ColorGridPanel extends JPanel {

    private static final int cellSize = 15;
    private static final int cellSpace = 1;
    private static final int borderInset = 10;
    private static final int gridRows = 11;
    private static final int gridCols = 11;
    private static final MigLayout migLayout = new MigLayout(
        String.format("insets %d", borderInset),
        String.format("[]%d[]", cellSpace),
        String.format("[]%d[]", cellSpace)
    );
    private final List<ColorSelectionListener> selectionListeners;

    private String dialogTitle = "Choose Color";
    private JButton moreButton;
    private Color colorValue;

    public ColorGridPanel() {
        super(migLayout);
        selectionListeners = new ArrayList<>();
        init();
    }

    private void init() {
        JButton cellButton;
        float saturation = 1.0f;
        float lightness = 0.1f;

        ActionListener colorCellActionListener = e -> {
            if (e.getSource() instanceof JButton) {
                applyColor(((JButton) e.getSource()).getBackground());
            }
        };

        int colorCols = gridCols - 1;
        for (int row = 0; row < gridRows; row++) {
            float grayLightness = row * 1.0f / (gridRows - 1);
            Color grayColor = ColorUtilities.HSLtoRGB(0, 0, grayLightness);
            cellButton = new JButton();
            cellButton.setToolTipText(ColorUtil.toHex(grayColor));
            cellButton.setBackground(grayColor);
            cellButton.addActionListener(colorCellActionListener);
            this.add(cellButton, String.format("w %d!,h %d!", cellSize, cellSize));

            float rowLightness = lightness + row * (1 - lightness) / gridRows;
            for (int col = 0; col < colorCols; col++) {
                float hue = col * 1.0f / colorCols;
                Color color = ColorUtilities.HSLtoRGB(hue, saturation, rowLightness);
                cellButton = new JButton();
                cellButton.setToolTipText(ColorUtil.toHex(color));
                cellButton.setBackground(color);
                cellButton.addActionListener(colorCellActionListener);
                if (col == colorCols - 1) {
                    this.add(cellButton, String.format("w %d!,h %d!,wrap", cellSize, cellSize));
                } else {
                    this.add(cellButton, String.format("w %d!,h %d!", cellSize, cellSize));
                }
            }
        }
        moreButton = new JButton("More ...");
        moreButton.addActionListener(e -> {
            JColorChooser colorChooser = new JColorChooser();
            if (this.colorValue != null) {
                colorChooser.setColor(this.colorValue);
            }
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
        this.add(moreButton, "h 25!,gaptop 5,spanx,growx");

        int width = cellSpace * (gridCols - 1) + cellSize * gridCols + borderInset * 2;
        int height = cellSpace * (gridRows - 1) + cellSize * gridRows + borderInset * 2 + 5 + 25;
        this.setPreferredSize(new Dimension(width, height));
    }

    public void addColorSelectionListener(ColorSelectionListener listener) {
        selectionListeners.add(listener);
    }

    private void applyColor(Color color) {
        this.colorValue = color;
        selectionListeners.forEach(listener -> listener.onColorSelected(color));
    }

    public Color getColorValue() {
        return this.colorValue;
    }

    public void setColorValue(Color color) {
        this.colorValue = color;
    }

    public void setMoreText(String text) {
        moreButton.setText(text);
    }

    public void setDialogTitle(String title) {
        dialogTitle = title;
    }

}
