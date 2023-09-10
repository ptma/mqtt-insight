package com.mqttinsight.ui.component;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.ui.form.panel.SubscriptionListPanel;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.painter.RectanglePainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author ptma
 */
@EqualsAndHashCode(callSuper = false)
public class SubscriptionListItem extends JPanel implements MouseListener {

    private final Color borderColor = UIManager.getColor("Component.borderColor");

    private final Subscription subscription;
    private final UnsubscribeListener unsubscribeListener;
    private final SubscriptionListPanel parent;
    private boolean subscribed = true;

    @Setter
    private boolean selected;

    private JXLabel topicLabel;
    private JToolBar toolBar;
    private JXLabel counterLabel;
    private JButton favoriteButton;
    private JButton muteButton;
    private PopupMenuButton moreButton;
    private JMenuItem unsubscribeMenu;
    private JMenuItem resubscribeMenu;
    private JMenu formatMenu;

    public SubscriptionListItem(SubscriptionListPanel parent, Subscription subscription, UnsubscribeListener unsubscribeListener) {
        super();
        this.parent = parent;
        this.subscription = subscription;
        this.unsubscribeListener = unsubscribeListener;
        initComponents();
        initActionsAndStyles();
        updateComponents();
        this.setOpaque(true);
    }

    private void initComponents() {
        setBorder(new SingleLineBorder(borderColor, false, false, true, false));
        setLayout(new MigLayout(
            "fillx,insets 5,nocache",
            "[grow]5[]",
            "[]0[]")
        );

        topicLabel = new JXLabel(subscription.getTopic());
        topicLabel.setToolTipText(subscription.getTopic());
        topicLabel.setOpaque(false);
        add(topicLabel, "span 2,growx,wmin 50px,wmax 100%-30px,wrap");

        //======== toolBar ========
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        favoriteButton = new JButton(Icons.FAVORITE);
        favoriteButton.setToolTipText(LangUtil.getString("Favorite"));
        favoriteButton.putClientProperty("FlatLaf.styleClass", "small");
        toolBar.add(favoriteButton);

        muteButton = new JButton(Icons.EYE);
        muteButton.setToolTipText(LangUtil.getString("Mute"));
        muteButton.putClientProperty("FlatLaf.styleClass", "small");
        toolBar.add(muteButton);

        // More Menu
        moreButton = new PopupMenuButton(Icons.MORE);

        unsubscribeMenu = new JMenuItem();
        LangUtil.buttonText(unsubscribeMenu, "Unsubscribe");
        unsubscribeMenu.addActionListener(e -> unsubscribeListener.unsubscribe(false));
        moreButton.addMunuItem(unsubscribeMenu);

        resubscribeMenu = new JMenuItem();
        LangUtil.buttonText(resubscribeMenu, "Resubscribe");
        resubscribeMenu.setEnabled(false);
        resubscribeMenu.addActionListener(e -> resubscribe());
        moreButton.addMunuItem(resubscribeMenu);

        moreButton.addSeparator();

        formatMenu = new JMenu();
        LangUtil.buttonText(formatMenu, "PayloadFormat");
        moreButton.addMunuItem(formatMenu);
        loadFormatMenus();

        moreButton.addSeparator();

        JMenuItem clearMessageMenu = new JMenuItem();
        LangUtil.buttonText(clearMessageMenu, "ClearMessages");
        moreButton.addMunuItem(clearMessageMenu).addActionListener(this::clearMessages);

        JMenuItem exportMessageMenu = new JMenuItem();
        LangUtil.buttonText(exportMessageMenu, "ExportMessages");
        moreButton.addMunuItem(exportMessageMenu).addActionListener(this::exportMessages);

        moreButton.addSeparator();

        JMenuItem closeMenu = new JMenuItem();
        LangUtil.buttonText(closeMenu, "Close");
        moreButton.addMunuItem(closeMenu).addActionListener(this::close);

        toolBar.add(moreButton);
        add(toolBar, "cell 0 1,growx");

        // Counter badge
        counterLabel = new JXLabel("0");
        add(counterLabel, "cell 1 1, right");
    }

    public void initActionsAndStyles() {
        topicLabel.addMouseListener(this);
        toolBar.addMouseListener(this);
        counterLabel.addMouseListener(this);

        topicLabel.putClientProperty("FlatLaf.styleClass", "h4");

        RectanglePainter badgePainter = new RectanglePainter();
        badgePainter.setRounded(true);
        badgePainter.setRoundWidth(16);
        badgePainter.setRoundHeight(16);
        boolean isDarkTheme = UIManager.getBoolean("laf.dark");
        Color bgColor = subscription.getColor();
        Color badgeColor;
        if (isDarkTheme) {
            badgeColor = Utils.brighter(bgColor, 0.7f);
        } else {
            badgeColor = Utils.darker(bgColor, 0.85f);
        }
        badgePainter.setFillPaint(badgeColor);
        badgePainter.setBorderPaint(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 128));
        counterLabel.setBackgroundPainter(badgePainter);
        counterLabel.setOpaque(false);
        counterLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        favoriteButton.addActionListener(e -> {
            if (isFavorite()) {
                parent.getProperties().removeFavorite(subscription.getTopic());
                favoriteButton.setIcon(Icons.FAVORITE);
            } else {
                parent.getProperties().addFavorite(subscription.getTopic(), subscription.getQos(), subscription.getSelfPayloadFormat());
                favoriteButton.setIcon(Icons.FAVORITE_FILL);
            }
            Configuration.instance().changed();
        });
        muteButton.addActionListener(e -> {
            if (subscription.isMuted()) {
                subscription.setMuted(false);
                muteButton.setIcon(Icons.EYE);
            } else {
                subscription.setMuted(true);
                muteButton.setIcon(Icons.EYE_CLOSE);
            }
        });

        topicLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    moreButton.getPopup().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        toolBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    moreButton.getPopup().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void loadFormatMenus() {
        ButtonGroup formatGroup = new ButtonGroup();

        JCheckBoxMenuItem formatMenuItem = new JCheckBoxMenuItem(CodecSupport.DEFAULT);
        formatMenuItem.addActionListener(this::payloadFormatChanged);
        if (CodecSupport.DEFAULT.equals(subscription.getSelfPayloadFormat())) {
            formatMenuItem.setSelected(true);
        }
        formatMenu.add(formatMenuItem);
        formatGroup.add(formatMenuItem);

        for (CodecSupport codecSupport : CodecSupports.instance().getCodes()) {
            formatMenuItem = new JCheckBoxMenuItem(codecSupport.getName());
            formatMenuItem.addActionListener(this::payloadFormatChanged);
            if (codecSupport.getName().equals(subscription.getSelfPayloadFormat())) {
                formatMenuItem.setSelected(true);
            }
            formatMenu.add(formatMenuItem);
            formatGroup.add(formatMenuItem);
        }
    }

    private void payloadFormatChanged(ActionEvent e) {
        String format = e.getActionCommand();
        subscription.setPayloadFormat(format);
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
        topicLabel.setEnabled(subscribed);
        resubscribeMenu.setEnabled(!subscribed);
        unsubscribeMenu.setEnabled(subscribed);
    }

    private boolean isFavorite() {
        return parent.getProperties().isFavorite(subscription.getTopic());
    }

    private void close(ActionEvent e) {
        if (subscribed) {
            unsubscribeListener.unsubscribe(true);
        } else {
            parent.remove(subscription);
        }
    }

    private void resubscribe() {
        SwingUtilities.invokeLater(() -> {
            if (parent.getMqttInstance().subscribe(subscription)) {
                setSubscribed(true);
                this.revalidate();
                this.repaint();
            }
        });
    }

    public void updateMessageCounter() {
        if (subscription == null) {
            if (counterLabel != null) {
                counterLabel.setText("");
            }
        } else {
            if (counterLabel != null) {
                counterLabel.setText(String.valueOf(subscription.getMessageCount()));
            }
        }
    }

    public boolean hasSubscription(Subscription subscription) {
        return this.subscription.equals(subscription);
    }

    public boolean hasTopic(String topic) {
        return this.subscription.getTopic().equals(topic);
    }

    public void resetMessageCount() {
        subscription.resetMessageCount();
        updateMessageCounter();
    }

    private void clearMessages(ActionEvent e) {
        parent.clearMessages(subscription);
        subscription.resetMessageCount();
        updateMessageCounter();
    }

    private void exportMessages(ActionEvent e) {
        parent.exportMessages(subscription);
    }

    public void updateComponents() {
        Color bgColor = subscription.getColor();
        Color fgColor = Utils.getReverseForegroundColor(bgColor);
        topicLabel.setBackground(bgColor);
        topicLabel.setForeground(fgColor);
        counterLabel.setForeground(fgColor);
        this.setBackground(bgColor);
        favoriteButton.setIcon(isFavorite() ? Icons.FAVORITE_FILL : Icons.FAVORITE);
        muteButton.setIcon(subscription.isMuted() ? Icons.EYE_CLOSE : Icons.EYE);
    }

    @Override
    public void updateUI() {
        super.updateUI();
    }

    @Override
    public void repaint() {
        if (topicLabel != null) {
            topicLabel.repaint();
        }
        if (counterLabel != null) {
            counterLabel.repaint();
        }
        if (toolBar != null) {
            toolBar.repaint();
        }
        super.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        parent.select(this);
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public interface UnsubscribeListener {

        public void unsubscribe(boolean closable);

    }
}
