package com.mqttinsight.ui.component;

import javax.swing.border.Border;
import java.awt.*;

/**
 * @author ptma
 */
public class SingleLineBorder implements Border {

    private final Color color;
    private final boolean topLine;
    private final boolean leftLine;
    private final boolean bottomLine;
    private final boolean rightLine;

    private Insets borderInsets = new Insets(1, 1, 1, 1);

    public SingleLineBorder(Color color, boolean topLine, boolean leftLine, boolean bottomLine, boolean rightLine) {
        this.color = color;
        this.topLine = topLine;
        this.leftLine = leftLine;
        this.bottomLine = bottomLine;
        this.rightLine = rightLine;
    }

    public void setBorderInsets(Insets borderInsets) {
        this.borderInsets = borderInsets;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return borderInsets;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        g.setColor(color);
        if (topLine) {
            g.drawLine(0, 0, width - 1, 0);
        }
        if (leftLine) {
            g.drawLine(0, 0, 0, height - 1);
        }

        if (rightLine) {
            g.drawLine(width - 1, 0, width - 1, height - 1);
        }
        if (bottomLine) {
            g.drawLine(0, height - 1, width - 1, height - 1);
        }
        g.setColor(oldColor);
    }

}
