package com.mqttinsight.ui.component;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.formdev.flatlaf.util.UIScale.scale;

/**
 * @author ptma
 */
public abstract class AbstractPopupButton extends JButton implements ActionListener {

    protected Color buttonArrowColor = UIManager.getColor("ComboBox.buttonArrowColor");
    protected Color buttonDisabledArrowColor = UIManager.getColor("ComboBox.buttonDisabledArrowColor");
    protected Color buttonHoverArrowColor = UIManager.getColor("ComboBox.buttonHoverArrowColor");
    protected Color buttonPressedArrowColor = UIManager.getColor("ComboBox.buttonPressedArrowColor");

    private static final int ARROW_SIZE = 9;
    /**
     * Add spaces at the end of the text to increase the button width
     */
    private static final String TAIL_BLANK = "     ";
    private static final String TAIL_BLANK_WITHOUT_TEXT = "    ";

    private boolean hover;
    private boolean pressed;

    private boolean roundBorderAutoXOffset = true;

    private JPopupMenu popup;

    public AbstractPopupButton(String text) {
        this(text, null);
    }

    public AbstractPopupButton(Action action) {
        super(action);
        super.setText(TAIL_BLANK);
        init();
    }

    public AbstractPopupButton(Icon icon) {
        this(null, icon);
    }

    public AbstractPopupButton() {
        this(null, null);
    }

    public AbstractPopupButton(String text, Icon icon) {
        super(text, icon);
        if (text != null) {
            super.setText(text + TAIL_BLANK);
        } else {
            super.setText(TAIL_BLANK_WITHOUT_TEXT);
        }
        init();
    }

    @Override
    public void setText(String text) {
        if (text != null) {
            super.setText(text + TAIL_BLANK);
        } else {
            super.setText(TAIL_BLANK_WITHOUT_TEXT);
        }
    }

    private void init() {
        this.setHorizontalAlignment(SwingConstants.LEFT);
        this.popup = createPopup();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pressed = true;
                    showPopup();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pressed = false;
                }
            }
        });
    }

    protected abstract JPopupMenu createPopup();

    protected JPopupMenu getPopup() {
        return this.popup;
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        super.fireActionPerformed(e);
    }

    public boolean isRoundBorderAutoXOffset() {
        return roundBorderAutoXOffset;
    }

    public void setRoundBorderAutoXOffset(boolean roundBorderAutoXOffset) {
        this.roundBorderAutoXOffset = roundBorderAutoXOffset;
    }

    public void hidePopup() {
        if (isEnabled()) {
            if (popup == null) {
                return;
            }
            popup.setVisible(false);
        }
    }

    private void showPopup() {
        if (isEnabled()) {
            if (popup == null) {
                return;
            }
            Point loc = adjustPopupLocationToFitScreen(0, this.getHeight());
            popup.show(this, loc.x, loc.y);
        }
    }

    Point adjustPopupLocationToFitScreen(int xPosition, int yPosition) {
        Point popupLocation = new Point(xPosition, yPosition);

        if (GraphicsEnvironment.isHeadless()) {
            return popupLocation;
        }

        GraphicsConfiguration gc = getCurrentGraphicsConfiguration(popupLocation);
        if (gc == null) {
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();
        }
        Rectangle scrBounds = gc.getBounds();

        Dimension popupSize = popup.getPreferredSize();
        long popupRightX = (long) popupLocation.x + (long) popupSize.width;
        long popupBottomY = (long) popupLocation.y + (long) popupSize.height;
        int scrWidth = scrBounds.width;
        int scrHeight = scrBounds.height;

        // Insets include the task bar. Take them into account.
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Insets scrInsets = toolkit.getScreenInsets(gc);
        scrBounds.x += scrInsets.left;
        scrBounds.y += scrInsets.top;
        scrWidth -= scrInsets.left + scrInsets.right;
        scrHeight -= scrInsets.top + scrInsets.bottom;

        int scrRightX = scrBounds.x + scrWidth;
        int scrBottomY = scrBounds.y + scrHeight;

        // Ensure that popup menu fits the screen
        if (popupRightX > (long) scrRightX) {
            popupLocation.x = scrRightX - popupSize.width;
        }

        if (popupBottomY > (long) scrBottomY) {
            popupLocation.y = scrBottomY - popupSize.height;
        }

        if (popupLocation.x < scrBounds.x) {
            popupLocation.x = scrBounds.x;
        }

        if (popupLocation.y < scrBounds.y) {
            popupLocation.y = scrBounds.y;
        }

        return popupLocation;
    }

    private GraphicsConfiguration getCurrentGraphicsConfiguration(Point popupLocation) {
        GraphicsConfiguration gc = null;
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        for (GraphicsDevice graphicsDevice : gd) {
            if (graphicsDevice.getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                GraphicsConfiguration dgc =
                    graphicsDevice.getDefaultConfiguration();
                if (dgc.getBounds().contains(popupLocation)) {
                    gc = dgc;
                    break;
                }
            }
        }
        if (gc == null) {
            gc = this.getGraphicsConfiguration();
        }
        return gc;
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
    public void paint(Graphics g) {
        super.paint(g);
        Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
        Color oldColor = g.getColor();
        g.setColor(FlatUIUtils.deriveColor(getArrowColor(), getArrowColor()));
        paintArrow((Graphics2D) g);
        g.setColor(oldColor);
        FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
    }

    protected void paintArrow(Graphics2D g) {
        int x = 0;
        if (FlatUIUtils.hasRoundBorder((JComponent) this)) {
            x -= scale(this.getComponentOrientation().isLeftToRight() ? 1 : -1);
        }
        int xOffset = Math.max(UIScale.unscale((getWidth() - getHeight()) / 2), 0);

        if (isRoundBorderAutoXOffset()) {
            Container parent = getParent();
            if (parent instanceof JComponent && FlatUIUtils.hasRoundBorder((JComponent) parent)) {
                x -= scale(parent.getComponentOrientation().isLeftToRight() ? 1 : -1);
            }
        }
        FlatUIUtils.paintArrow(g, x, 0, getWidth(), getHeight(), SwingConstants.SOUTH, true,
            ARROW_SIZE, 1, xOffset, 0);
    }
}
