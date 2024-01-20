package com.mqttinsight.ui.component;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.ui.event.InstanceEventAdapter;
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

    private InstanceEventAdapter instanceEventAdapter;

    private JXLabel topicLabel;
    private JToolBar toolBar;
    private JXLabel counterLabel;
    private JButton favoriteButton;
    private JButton visibleButton;
    private PopupColorButton paletteButton;
    private PopupMenuButton moreButton;
    private JMenuItem unsubscribeMenu;
    private JMenuItem resubscribeMenu;
    private JMenuItem exportMessageMenu;
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

        visibleButton = new JButton(Icons.EYE);
        visibleButton.setToolTipText(LangUtil.getString("ShowOrHideMessages"));
        visibleButton.putClientProperty("FlatLaf.styleClass", "small");
        toolBar.add(visibleButton);

        paletteButton = new PopupColorButton(Icons.PALETTE, false);
        paletteButton.setMoreText(LangUtil.getString("MoreColor"));
        paletteButton.setDialogTitle(LangUtil.getString("ChooseColor"));
        toolBar.add(paletteButton);

        // More Menu
        moreButton = new PopupMenuButton(Icons.MORE);

        unsubscribeMenu = new NormalMenuItem();
        LangUtil.buttonText(unsubscribeMenu, "Unsubscribe");
        unsubscribeMenu.addActionListener(e -> this.unsubscribe(false));
        moreButton.addMenuItem(unsubscribeMenu);

        resubscribeMenu = new NormalMenuItem();
        LangUtil.buttonText(resubscribeMenu, "Resubscribe");
        resubscribeMenu.setEnabled(false);
        resubscribeMenu.addActionListener(e -> resubscribe());
        moreButton.addMenuItem(resubscribeMenu);

        moreButton.addSeparator();

        formatMenu = new JMenu();
        LangUtil.buttonText(formatMenu, "PayloadFormat");
        moreButton.addMenuItem(formatMenu);
        loadFormatMenus();

        moreButton.addSeparator();

        JMenuItem clearMessageMenu = new NormalMenuItem(Icons.CLEAR);
        LangUtil.buttonText(clearMessageMenu, "ClearMessages");
        moreButton.addMenuItem(clearMessageMenu).addActionListener(this::clearMessages);

        exportMessageMenu = new NormalMenuItem(Icons.EXPORT);
        LangUtil.buttonText(exportMessageMenu, "ExportMessages");
        exportMessageMenu.setEnabled(false);
        moreButton.addMenuItem(exportMessageMenu).addActionListener(this::exportMessages);

        moreButton.addSeparator();

        JMenuItem closeMenu = new NormalMenuItem(Icons.CANCEL);
        LangUtil.buttonText(closeMenu, "Close");
        moreButton.addMenuItem(closeMenu).addActionListener(this::close);

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

        visibleButton.addActionListener(e -> {
            if (subscription.isVisible()) {
                subscription.setVisible(false);
                visibleButton.setIcon(Icons.EYE_CLOSE);
            } else {
                subscription.setVisible(true);
                visibleButton.setIcon(Icons.EYE);
            }
            mqttInstance.getMessageTable().getTableModel().fireTableDataChanged();
        });

        paletteButton.addColorSelectionListener(color -> {
            subscription.setColor(color);
            mqttInstance.applyEvent(InstanceEventListener::subscriptionColorChanged);
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

        instanceEventAdapter = new InstanceEventAdapter() {
            @Override
            public void onCodecsChanged() {
                loadFormatMenus();
            }
        };
        mqttInstance.addEventListener(instanceEventAdapter);
    }

    private void loadFormatMenus() {
        formatMenu.removeAll();
        ButtonGroup formatGroup = new ButtonGroup();

        JCheckBoxMenuItem formatMenuItem = new NormalCheckBoxMenuItem(CodecSupport.DEFAULT);
        formatMenuItem.addActionListener(this::payloadFormatChangeAction);
        if (CodecSupport.DEFAULT.equals(subscription.getSelfPayloadFormat())) {
            formatMenuItem.setSelected(true);
        }
        formatMenu.add(formatMenuItem);
        formatGroup.add(formatMenuItem);

        for (CodecSupport codecSupport : CodecSupports.instance().getCodecs()) {
            formatMenuItem = new NormalCheckBoxMenuItem(codecSupport.getName());
            formatMenuItem.addActionListener(this::payloadFormatChangeAction);
            if (codecSupport.getName().equals(subscription.getSelfPayloadFormat())) {
                formatMenuItem.setSelected(true);
            }
            formatMenu.add(formatMenuItem);
            formatGroup.add(formatMenuItem);
        }
    }

    private void payloadFormatChangeAction(ActionEvent e) {
        String format = e.getActionCommand();
        subscription.setPayloadFormat(format);
        mqttInstance.applyEvent(InstanceEventListener::payloadFormatChanged);
        if (isFavorite()) {
            mqttInstance.getProperties().updateFavorite(subscription.getTopic(), subscription.getQos(), subscription.getSelfPayloadFormat());
            mqttInstance.applyEvent(InstanceEventListener::favoriteChanged);
        }
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
        mqttInstance.removeEventListener(instanceEventAdapter);
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
        if (counterLabel != null) {
            counterLabel.setText(String.valueOf(subscription.getMessageCount()));
            exportMessageMenu.setEnabled(subscription.getMessageCount().get() > 0);
        }
    }

    public void decrementMessageCount() {
        subscription.decrementMessageCount();
        updateMessageCounter();
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
        visibleButton.setIcon(subscription.isVisible() ? Icons.EYE : Icons.EYE_CLOSE);
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
