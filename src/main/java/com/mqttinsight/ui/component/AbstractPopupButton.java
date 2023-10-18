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

    private static final int ARROW_SPACING = 16;
    private static final int ARROW_SIZE = 9;

    private boolean hover;
    private boolean pressed;

    private boolean roundBorderAutoXOffset = true;

    private JPopupMenu popupMenu;

    private boolean showArrow;

    public AbstractPopupButton(String text) {
        this(text, null, true);
    }

    public AbstractPopupButton(String text, boolean showArrow) {
        this(text, null, showArrow);
    }

    public AbstractPopupButton(Icon icon) {
        this(null, icon, true);
    }

    public AbstractPopupButton(Icon icon, boolean showArrow) {
        this(null, icon, showArrow);
    }

    public AbstractPopupButton() {
        this(null, null, true);
    }


    public AbstractPopupButton(String text, Icon icon, boolean showArrow) {
        super(text, icon);
        this.showArrow = showArrow;
        init();
    }

    @Override
    public void setText(String text) {
        super.setText(text);
    }

    private void init() {
        this.setHorizontalAlignment(SwingConstants.LEFT);
        this.popupMenu = new JPopupMenu();
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

    @Override
    public Insets getInsets() {
        Insets insets = (Insets) super.getInsets().clone();
        if (showArrow) {
            insets.right += ARROW_SPACING;
        }
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

    public JPopupMenu getPopupMenu() {
        return this.popupMenu;
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
            if (popupMenu == null) {
                return;
            }
            popupMenu.setVisible(false);
        }
    }

    private void showPopup() {
        if (isEnabled()) {
            if (popupMenu == null) {
                return;
            }
            Point loc = adjustPopupLocationToFitScreen(0, this.getHeight());
            popupMenu.show(this, loc.x, loc.y);
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

        Dimension popupSize = popupMenu.getPreferredSize();
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
        if (showArrow) {
            Object[] oldRenderingHints = FlatUIUtils.setRenderingHints(g);
            Color oldColor = g.getColor();
            g.setColor(FlatUIUtils.deriveColor(getArrowColor(), getArrowColor()));
            paintArrow((Graphics2D) g);
            g.setColor(oldColor);
            FlatUIUtils.resetRenderingHints(g, oldRenderingHints);
        }
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
