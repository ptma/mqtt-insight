package com.mqttinsight.ui.component;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.ui.event.InstanceEventListener;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
public class SubscriptionItem extends JPanel implements MouseListener {

    private final Color borderColor = UIManager.getColor("Component.borderColor");

    private final MqttInstance mqttInstance;
    @Getter
    private final Subscription subscription;
    private boolean subscribed = true;

    @Setter
    private boolean selected;

    private JXLabel topicLabel;
    private JToolBar toolBar;
    private JXLabel counterLabel;
    private JButton favoriteButton;
    private JButton muteButton;
    private PopupColorButton paletteButton;
    private PopupMenuButton moreButton;
    private JMenuItem unsubscribeMenu;
    private JMenuItem resubscribeMenu;
    private JMenu formatMenu;

    public SubscriptionItem(MqttInstance mqttInstance, Subscription subscription) {
        super();
        this.mqttInstance = mqttInstance;
        this.subscription = subscription;
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

        paletteButton = new PopupColorButton(Icons.PALETTE, false);
        paletteButton.setMoreText(LangUtil.getString("MoreColor"));
        paletteButton.setDialogTitle(LangUtil.getString("ChooseColor"));
        toolBar.add(paletteButton);

        // More Menu
        moreButton = new PopupMenuButton(Icons.MORE);

        unsubscribeMenu = new JMenuItem();
        LangUtil.buttonText(unsubscribeMenu, "Unsubscribe");
        unsubscribeMenu.addActionListener(e -> this.unsubscribe(false));
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

        JMenuItem clearMessageMenu = new JMenuItem(Icons.CLEAR);
        LangUtil.buttonText(clearMessageMenu, "ClearMessages");
        moreButton.addMunuItem(clearMessageMenu).addActionListener(this::clearMessages);

        JMenuItem exportMessageMenu = new JMenuItem(Icons.EXPORT);
        LangUtil.buttonText(exportMessageMenu, "ExportMessages");
        moreButton.addMunuItem(exportMessageMenu).addActionListener(this::exportMessages);

        moreButton.addSeparator();

        JMenuItem closeMenu = new JMenuItem(Icons.CANCEL);
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

        counterLabel.setOpaque(false);
        counterLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        favoriteButton.addActionListener(e -> {
            if (isFavorite()) {
                mqttInstance.getProperties().removeFavorite(subscription.getTopic());
                favoriteButton.setIcon(Icons.FAVORITE);
            } else {
                mqttInstance.getProperties().addFavorite(subscription.getTopic(), subscription.getQos(), subscription.getSelfPayloadFormat());
                favoriteButton.setIcon(Icons.FAVORITE_FILL);
            }
            mqttInstance.applyEvent(InstanceEventListener::favoriteChanged);
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

        paletteButton.addColorSelectionListener(color -> {
            subscription.setColor(color);
            updateComponents();
        });

        topicLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    moreButton.getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        toolBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    moreButton.getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
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

    public boolean isSubscribed() {
        return subscribed;
    }

    private boolean isFavorite() {
        return mqttInstance.getProperties().isFavorite(subscription.getTopic());
    }

    private void unsubscribe(boolean closable) {
        mqttInstance.applyEvent(l -> l.onUnsubscribe(subscription, closable));
    }

    private void close(ActionEvent e) {
        this.unsubscribe(true);
    }

    public void resubscribe() {
        SwingUtilities.invokeLater(() -> {
            if (mqttInstance.subscribe(subscription)) {
                setSubscribed(true);
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
        mqttInstance.applyEvent(eventListener -> {
            eventListener.clearMessages(subscription);
        });
        subscription.resetMessageCount();
        updateMessageCounter();
    }

    private void exportMessages(ActionEvent e) {
        mqttInstance.applyEvent(eventListener -> {
            eventListener.exportMessages(subscription);
        });
    }

    public void updateComponents() {
        RectanglePainter badgePainter = new RectanglePainter();
        badgePainter.setRounded(true);
        badgePainter.setRoundWidth(16);
        badgePainter.setRoundHeight(16);
        boolean isDarkTheme = UIManager.getBoolean("laf.dark");
        Color bgColor = subscription.getColor();
        Color fgColor = Utils.getReverseForegroundColor(bgColor);
        Color badgeColor;
        if (isDarkTheme) {
            badgeColor = Utils.brighter(bgColor, 0.7f);
        } else {
            badgeColor = Utils.darker(bgColor, 0.85f);
        }
        badgePainter.setFillPaint(badgeColor);
        badgePainter.setBorderPaint(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 128));
        counterLabel.setBackgroundPainter(badgePainter);
        counterLabel.setForeground(fgColor);

        topicLabel.setBackground(bgColor);
        topicLabel.setForeground(fgColor);

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
}
