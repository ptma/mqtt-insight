package com.mqttinsight.ui.component;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * @author ptma
 */
public class RoundRendererLabel extends RendererLabel {

    private int arc = 0;

    public RoundRendererLabel(int arc) {
        super();
        this.arc = arc;
        setOpaque(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        int x = 0;
        int y = 0;
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHints(qualityHints);
        g2.setPaint(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        RoundRectangle2D rect = new RoundRectangle2D.Double(x + 1, y + 1, width - 2, height - 2, arc, arc);
        g.setClip(rect);
        super.paintComponent(g);
    }
}
