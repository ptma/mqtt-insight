package com.mqttinsight.ui.component;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * An implementation of JTextArea used for rendering. It overrides methods for performance reasons.
 *
 * @author ptma
 */
@Slf4j
public class RendererTextArea extends JTextArea {

    public RendererTextArea() {
        super();
        setEditable(false);
    }

    public void setMaxDisplayRows(int displayRows) {
        if (displayRows > 0) {
            int maxHeight = displayRows * getRowHeight() + getInsets().top + getInsets().bottom;
            setMaximumSize(new Dimension(0, maxHeight));
        }
    }

    /**
     * {@inheritDoc} <p>
     * <p>
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
     */
    @Override
    public void revalidate() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(Rectangle r) {
    }

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
        if ("document".equals(propertyName)
            || "font".equals(propertyName)
            || "FlatLaf.styleClass".equals(propertyName)
        ) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }

    @Override
    protected void paintBorder(Graphics g) {

    }

}
