package com.mqttinsight.ui.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import com.mqttinsight.mqtt.MqttProperties;
import com.mqttinsight.mqtt.Version;
import com.mqttinsight.ui.component.ShortcutManager;
import com.mqttinsight.ui.form.panel.Mqtt3InstanceTabPanel;
import com.mqttinsight.ui.form.panel.Mqtt5InstanceTabPanel;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.ui.form.panel.MqttInstanceTabPanel;
import com.mqttinsight.ui.log.LogTab;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

/**
 * MainWindowForm
 *
 * @author ptma
 */
public class MainWindowForm {

    private static final Logger log = LoggerFactory.getLogger(MainWindowForm.class);

    private JPanel contentPanel;
    private FlatTabbedPane tabPanel;
    private LogTab logTab = new LogTab();

    private static class MainWindowFormHolder {
        final static MainWindowForm INSTANCE = new MainWindowForm();
    }

    public static MainWindowForm instance() {
        return MainWindowFormHolder.INSTANCE;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    private MainWindowForm() {
        $$$setupUI$$$();
        initComponents();
        contentPanel.setDoubleBuffered(true);
        ShortcutManager.instance().registerShortcut(KeyStroke.getKeyStroke("ctrl O"), ConnectionManagerForm::open);
        ShortcutManager.instance().registerShortcut(KeyStroke.getKeyStroke("ctrl shift S"), this::newSubscription);
    }

    public void openLogTab() {
        for (int tabIndex = 0; tabIndex < tabPanel.getTabCount(); tabIndex++) {
            Component component = tabPanel.getComponentAt(tabIndex);
            if (component instanceof LogTab) {
                tabPanel.setSelectedIndex(tabIndex);
                return;
            }
        }
        tabPanel.insertTab(LangUtil.getString("Log"), Icons.LOG_VERBOSE, logTab, null, 0);
        tabPanel.setSelectedIndex(0);
    }

    public void close() {
        for (int tabIndex = 0; tabIndex < tabPanel.getTabCount(); tabIndex++) {
            Component component = tabPanel.getComponentAt(tabIndex);
            if (component instanceof MqttInstance) {
                ((MqttInstance) component).close();
            }
        }
    }

    private boolean isMqttInstanceAtTab(int index) {
        return tabPanel.getComponentAt(index) instanceof MqttInstance;
    }

    private MqttInstance getMqttInstanceAtTab(int index) {
        return (MqttInstance) tabPanel.getComponentAt(index);
    }

    public void addTabActionPerformed(final MqttProperties mqttProperties, Runnable afterConnected) {
        SwingWorker<MqttInstanceTabPanel, Integer> addTabWorker = new SwingWorker<>() {
            @Override
            protected void done() {
                try {
                    MqttInstanceTabPanel mqttInstance = get();
                    if (mqttInstance != null) {
                        tabPanel.addTab(mqttProperties.getName(), mqttInstance);
                        int tabIndex = tabPanel.getTabCount() - 1;
                        tabPanel.setIconAt(tabIndex, mqttInstance.getConnectionStatus().getSmallIcon());
                        tabPanel.setSelectedIndex(tabIndex);
                        mqttInstance.connect();
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    if (ex.getCause() != null) {
                        Utils.Message.error(LangUtil.getString("OpenConnectionError"), ex.getCause());
                    } else {
                        Utils.Message.error(LangUtil.getString("OpenConnectionError"), ex);
                    }
                }
            }

            @Override
            protected MqttInstanceTabPanel doInBackground() throws Exception {
                for (int tabIndex = 0; tabIndex < tabPanel.getTabCount(); tabIndex++) {
                    if (isMqttInstanceAtTab(tabIndex)
                        && getMqttInstanceAtTab(tabIndex).getProperties().getId().equals(mqttProperties.getId())
                        && !mqttProperties.isRandomClientId()
                    ) {
                        Utils.Toast.info(LangUtil.getString("ConnectionExists"));
                        tabPanel.setSelectedIndex(tabIndex);
                        return null;
                    }
                }
                afterConnected.run();

                MqttInstanceTabPanel mqttInstance;
                if (mqttProperties.getVersion().equals(Version.MQTT_5)) {
                    mqttInstance = Mqtt5InstanceTabPanel.newInstance(mqttProperties);
                } else {
                    mqttInstance = Mqtt3InstanceTabPanel.newInstance(mqttProperties);
                }
                return mqttInstance;
            }
        };
        addTabWorker.execute();
    }

    public void onConnectionChanged(MqttInstance mqttInstance) {
        SwingUtilities.invokeLater(() -> {
            for (int tabIndex = 0; tabIndex < tabPanel.getTabCount(); tabIndex++) {
                if (isMqttInstanceAtTab(tabIndex) && getMqttInstanceAtTab(tabIndex).equals(mqttInstance)) {
                    tabPanel.setIconAt(tabIndex, mqttInstance.getConnectionStatus().getSmallIcon());
                    return;
                }
            }
        });
    }

    public void fireCodecsChanged() {
        for (int tabIndex = 0; tabIndex < tabPanel.getTabCount(); tabIndex++) {
            if (isMqttInstanceAtTab(tabIndex)) {
                MqttInstanceTabPanel mqttInstance = (MqttInstanceTabPanel) tabPanel.getComponentAt(tabIndex);
                mqttInstance.fireCodecsChanged();
            }
        }
    }

    private void newSubscription() {
        int tabIndex = tabPanel.getSelectedIndex();
        if (tabIndex >= 0 && isMqttInstanceAtTab(tabIndex)) {
            MqttInstanceTabPanel mqttInstance = (MqttInstanceTabPanel) tabPanel.getComponentAt(tabIndex);
            mqttInstance.openSubscriptionForm();
        }
    }

    private void initComponents() {
        tabPanel = new FlatTabbedPane();
        tabPanel.setTabPlacement(1);
        tabPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        contentPanel.add(tabPanel, BorderLayout.CENTER);

        JToolBar trailing = new JToolBar();
        trailing.setFloatable(false);
        trailing.setBorder(null);
        JButton connManagerButton = new JButton(Icons.ADD);
        connManagerButton.setToolTipText(LangUtil.getString("OpenConnection") + " (Ctrl + O)");
        trailing.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        connManagerButton.addActionListener(e -> ConnectionManagerForm.open());
        trailing.add(connManagerButton);
        trailing.add(Box.createHorizontalGlue());

        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, trailing);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.LEFT);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, false);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SCROLL_BUTTONS_PLACEMENT, FlatClientProperties.TABBED_PANE_PLACEMENT_BOTH);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, SwingConstants.LEADING);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSABLE, true);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, LangUtil.getString("Close"));
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK, (BiConsumer<JTabbedPane, Integer>) (tabbedPane, tabIndex) -> {
            if (isMqttInstanceAtTab(tabIndex)) {
                if (getMqttInstanceAtTab(tabIndex).close()) {
                    tabPanel.removeTabAt(tabIndex);
                    System.gc();
                }
            } else {
                tabPanel.removeTabAt(tabIndex);
            }
        });
        contentPanel.add(tabPanel, BorderLayout.CENTER, 0);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(0, 0));
        contentPanel.setEnabled(true);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
