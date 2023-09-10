package com.mqttinsight.ui.component;

import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SplitButton extends JButton {

    private static Color buttonArrowColor = UIManager.getColor("ComboBox.buttonArrowColor");
    private static Color buttonDisabledArrowColor = UIManager.getColor("ComboBox.buttonDisabledArrowColor");
    private static Color buttonHoverArrowColor = UIManager.getColor("ComboBox.buttonHoverArrowColor");
    private static Color buttonPressedArrowColor = UIManager.getColor("ComboBox.buttonPressedArrowColor");
    private static int separatorSpacing = 4;
    private static int splitWidth = 24;
    private static int arrowSize = 9;

    private boolean hover;
    private boolean pressed;
    private boolean onSplit;
    private Rectangle splitRectangle;
    private boolean alwaysDropDown;
    private MouseHandler mouseHandler;

    private JPopupMenu popupMenu;

    public SplitButton() {
        super();
        addMouseMotionListener(getMouseHandler());
        addMouseListener(getMouseHandler());
        setAlwaysDropDown(true);
        InputMap im = getInputMap(WHEN_FOCUSED);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "PopupMenu.close");
        am.put("PopupMenu.close", new ClosePopupAction());
    }

    public SplitButton(String text) {
        this();
        setText(text);
    }

    public SplitButton(String text, Icon icon) {
        this();
        setText(text);
        setIcon(icon);
    }

    public SplitButton(String text, JPopupMenu popup) {
        this();
        setText(text);
        setPopupMenu(popup);
    }

    public SplitButton(String text, Icon icon, JPopupMenu popup) {
        this();
        setText(text);
        setIcon(icon);
        setPopupMenu(popup);
    }

    @Override
    public void addActionListener(ActionListener l) {
        if (l != null) {
            setAlwaysDropDown(false);
        }
        super.addActionListener(l);
    }

    @Override
    public void setAction(Action a) {
        super.setAction(a);
        if (a != null) {
            setAlwaysDropDown(false);
        }
    }

    public void setPopupMenu(JPopupMenu popup) {
        popupMenu = popup;
        this.setComponentPopupMenu(popup);
    }

    public JPopupMenu getPopupMenu() {
        if (popupMenu == null) {
            popupMenu = new JPopupMenu();
        }
        return popupMenu;
    }

    protected MouseHandler getMouseHandler() {
        if (mouseHandler == null) {
            mouseHandler = new MouseHandler();
        }
        return mouseHandler;
    }

    protected int getOptionsCount() {
        return getPopupMenu().getComponentCount();
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

    protected void closePopupMenu() {
        getPopupMenu().setVisible(false);
    }

    protected void showPopupMenu() {
        if (getOptionsCount() > 0) {
            JPopupMenu menu = getPopupMenu();
            menu.setVisible(true); //Necessary to calculate pop-up menu width the first time it's displayed.
            menu.show(this, (getWidth() - menu.getWidth()), getHeight());
        }
    }

    public boolean isAlwaysDropDown() {
        return alwaysDropDown;
    }

    public void setAlwaysDropDown(boolean value) {
        if (alwaysDropDown != value) {
            this.alwaysDropDown = value;
            firePropertyChange("alwaysDropDown", !alwaysDropDown, alwaysDropDown);
        }
    }

    protected Color getArrowColor() {
        return isEnabled()
            ? (pressed
            ? buttonPressedArrowColor
            : (hover
            ? buttonHoverArrowColor
            : buttonArrowColor
        )
        )
            : buttonDisabledArrowColor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color oldColor = g.getColor();
        splitRectangle = new Rectangle(getWidth() - splitWidth, 0, splitWidth, getHeight());
        g.translate(splitRectangle.x, splitRectangle.y);
        int mh = getHeight() / 2;
        int mw = splitWidth / 2;

        Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
        g.setColor(FlatUIUtils.deriveColor(getArrowColor(), getArrowColor()));
        FlatUIUtils.paintArrow((Graphics2D) g, mw - 1 - arrowSize / 2, mh - arrowSize / 2, arrowSize, arrowSize, SwingConstants.SOUTH, true, arrowSize, 1, 0, 0);
        FlatUIUtils.resetRenderingHints(g, oldRenderingHints);

        if (!alwaysDropDown) {
            if (getModel().isRollover() || isFocusable()) {
                g.setColor(UIManager.getLookAndFeelDefaults().getColor("Button.background"));
                g.drawLine(1, separatorSpacing + 2, 1, getHeight() - separatorSpacing - 2);
                g.setColor(UIManager.getLookAndFeelDefaults().getColor("Button.shadow"));
                g.drawLine(2, separatorSpacing + 2, 2, getHeight() - separatorSpacing - 2);
            }
        }
        g.setColor(oldColor);
        g.translate(-splitRectangle.x, -splitRectangle.y);
    }

    @Override
    protected void fireActionPerformed(ActionEvent event) {
        if (onSplit || isAlwaysDropDown()) {
            showPopupMenu();
        } else {
            super.fireActionPerformed(event);
        }
    }

    protected class MouseHandler extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent e) {
            hover = true;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            onSplit = false;
            hover = false;
            repaint(splitRectangle);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                pressed = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                pressed = false;
            }
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

    protected class ClosePopupAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            closePopupMenu();
        }
    }
}
