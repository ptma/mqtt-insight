package com.mqttinsight.ui.component;

import org.jdesktop.swingx.renderer.JRendererPanel;

import java.awt.*;

/**
 * @author ptma
 */
public class RoundRendererPanel extends JRendererPanel {

    private int arc = 0;

    private int triangleSize = 6;
    private int yOffset = 10;
    private int xRectOffset = 5;

    private String alignment = "left";

    public RoundRendererPanel(int arc) {
        super();
        this.arc = arc;
        setOpaque(true);
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHints(qualityHints);
        g2.setPaint(getBackground());
        if ("right".equals(alignment)) {
            g2.fillRoundRect(xRectOffset, 0, width - triangleSize - xRectOffset, height, arc, arc);
        } else {
            g2.fillRoundRect(triangleSize, 0, width - triangleSize - xRectOffset, height, arc, arc);
        }
        g2.setColor(getBackground());
        int[] xPoints, yPoints;
        if ("right".equals(alignment)) {
            xPoints = new int[]{width - triangleSize - 1, width, width - triangleSize - 1};
            yPoints = new int[]{yOffset + triangleSize, yOffset + triangleSize * 2, yOffset + triangleSize * 3};
        } else {
            xPoints = new int[]{triangleSize + 1, 0, triangleSize + 1};
            yPoints = new int[]{yOffset + triangleSize, yOffset + triangleSize * 2, yOffset + triangleSize * 3};
        }
        g2.fillPolygon(xPoints, yPoints, 3);
        g2.dispose();
    }
}
