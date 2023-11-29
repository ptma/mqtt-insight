package com.mqttinsight.ui.component;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author ptma
 */
public class SplitIconMenuItem extends JMenuItem {

    private static int separatorSpacing = 4;
    private static int splitWidth = 48;
    private static int splitIconSize = 16;

    private boolean onSplit;
    private Rectangle splitRectangle;
    private MouseHandler mouseHandler;
    private Icon splitIcon;
    protected EventListenerList spiltListenerList = new EventListenerList();

    public SplitIconMenuItem(String text, Icon icon, Icon splitIcon) {
        super(text, icon);
        setSplitIcon(splitIcon);
        addMouseMotionListener(getMouseHandler());
        addMouseListener(getMouseHandler());
    }

    public void setSplitIcon(Icon splitIcon) {
        this.splitIcon = splitIcon;
    }

    public void addSplitActionListener(ActionListener l) {
        spiltListenerList.add(ActionListener.class, l);
    }

    protected MouseHandler getMouseHandler() {
        if (mouseHandler == null) {
            mouseHandler = new MouseHandler();
        }
        return mouseHandler;
    }

    @Override
    public Insets getInsets() {
        Insets insets = (Insets) super.getInsets().clone();
        insets.right += splitWidth;
        return insets;
    }

    @Override
    public Insets getInsets(Insets insets) {
        Insets insets1 = getInsets();
        insets.left = insets1.left;
        insets.right = insets1.right;
        insets.bottom = insets1.bottom;
        insets.top = insets1.top;
        return insets1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color oldColor = g.getColor();
        splitRectangle = new Rectangle(getWidth() - splitWidth, 0, splitWidth, getHeight());
        g.translate(splitRectangle.x, splitRectangle.y);
        int mh = getHeight() / 2;
        int mw = splitWidth / 2;

        if (splitIcon != null) {
            splitIcon.paintIcon(this, g, mw - 1 - splitIconSize / 2, mh - splitIconSize / 2);
        }
        g.setColor(UIManager.getLookAndFeelDefaults().getColor("Button.shadow"));
        g.drawLine(2, separatorSpacing + 2, 2, getHeight() - separatorSpacing - 2);
        g.setColor(oldColor);
        g.translate(-splitRectangle.x, -splitRectangle.y);
    }

    @Override
    protected void fireActionPerformed(ActionEvent event) {
        if (onSplit) {
            Object[] listeners = spiltListenerList.getListenerList();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == ActionListener.class) {
                    ((ActionListener) listeners[i + 1]).actionPerformed(event);
                }
            }
        } else {
            super.fireActionPerformed(event);
        }
    }

    protected class MouseHandler extends MouseAdapter {

        @Override
        public void mouseExited(MouseEvent e) {
            onSplit = false;
            repaint(splitRectangle);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (splitRectangle.contains(e.getPoint())) {
                onSplit = true;
            } else {
                onSplit = false;
            }
            repaint(splitRectangle);
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        // left mouse button only
        if (!SwingUtilities.isRightMouseButton(e) && !SwingUtilities.isMiddleMouseButton(e)) {
            super.processMouseEvent(e);
        }
    }
}
