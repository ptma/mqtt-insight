package com.mqttinsight.ui.component;

import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.renderer.IconAware;
import org.jdesktop.swingx.renderer.PainterAware;

import javax.swing.*;
import java.awt.*;

/**
 * Copy from <code>JRendererLabel</code>
 * @author ptma
 */
public class RendererLabel extends JLabel implements PainterAware, IconAware {

    protected Painter painter;

    /**
     *
     */
    public RendererLabel() {
        super();
        setOpaque(true);
    }

    /**
     * {@inheritDoc}
     */
    public void setPainter(Painter painter) {
        Painter old = getPainter();
        this.painter = painter;
        firePropertyChange("painter", old, getPainter());
    }

    /**
     * {@inheritDoc}
     */
    public Painter getPainter() {
        return painter;
    }
    /**
     * {@inheritDoc} <p>
     *
     * Overridden to inject Painter's painting. <p>
     * TODO: cleanup logic - see JRendererCheckBox.
     *
     */
    @Override
    protected void paintComponent(Graphics g) {
        // JW: hack around for #1178-swingx (core issue)
        // grab painting if Nimbus detected
        if ((painter != null) || isNimbus()) {
            // we have a custom (background) painter
            // try to inject if possible
            // there's no guarantee - some LFs have their own background
            // handling  elsewhere
            if (isOpaque()) {
                // replace the paintComponent completely
                paintComponentWithPainter((Graphics2D) g);
            } else {
                // transparent apply the background painter before calling super
                paintPainter(g);
                super.paintComponent(g);
            }
        } else {
            // nothing to worry about - delegate to super
            super.paintComponent(g);
        }
    }

    /**
     * Hack around Nimbus not respecting background colors if UIResource.
     * So by-pass ...
     *
     * @return
     */
    private boolean isNimbus() {
        return UIManager.getLookAndFeel().getName().contains("Nimbus");
    }


    /**
     *
     * Hack around AbstractPainter.paint bug which disposes the Graphics.
     * So here we give it a scratch to paint on. <p>
     * TODO - remove again, the issue is fixed?
     *
     * @param g the graphics to paint on
     */
    private void paintPainter(Graphics g) {
        if (painter == null) return;
        // fail fast: we assume that g must not be null
        // which throws an NPE here instead deeper down the bowels
        // this differs from corresponding core implementation!
        Graphics2D scratch = (Graphics2D) g.create();
        try {
            painter.paint(scratch, this, getWidth(), getHeight());
        }
        finally {
            scratch.dispose();
        }
    }

    protected void paintComponentWithPainter(Graphics2D g) {
        // 1. be sure to fill the background
        // 2. paint the painter
        // by-pass ui.update and hook into ui.paint directly
        if (ui != null) {
            // fail fast: we assume that g must not be null
            // which throws an NPE here instead deeper down the bowels
            // this differs from corresponding core implementation!
            Graphics2D scratchGraphics = (Graphics2D) g.create();
            try {
                scratchGraphics.setColor(getBackground());
                scratchGraphics.fillRect(0, 0, getWidth(), getHeight());
                paintPainter(g);
                ui.paint(scratchGraphics, this);
            }
            finally {
                scratchGraphics.dispose();
            }
        }
    }


    /**
     * {@inheritDoc} <p>
     *
     * Overridden to not automatically de/register itself from/to the ToolTipManager.
     * As rendering component it is not considered to be active in any way, so the
     * manager must not listen.
     */
    @Override
    public void setToolTipText(String text) {
        putClientProperty(TOOL_TIP_TEXT_KEY, text);
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     *
     * @since 1.5
     */
    @Override
    public void invalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void validate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void revalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(Rectangle r) { }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     *
     * @since 1.5
     */
    @Override
    public void repaint() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if ("text".equals(propertyName) || "FlatLaf.style".equals(propertyName) || "FlatLaf.styleClass".equals(propertyName)) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }
}
