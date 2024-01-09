package com.mqttinsight.ui.frame;

import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;
import com.mqttinsight.codec.CodecSupportLoader;
import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.ui.form.ConnectionManagerForm;
import com.mqttinsight.ui.form.MainWindowForm;
import com.mqttinsight.util.Const;
import com.mqttinsight.util.Icons;
import org.jdesktop.swingx.JXFrame;
import raven.toast.Notifications;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author ptma
 */
public class MainFrame extends JXFrame {

    private static final int MIN_WIDTH = 950;
    private static final int MIN_HEIGHT = 600;

    private int windowState = 0;
    private boolean loaded = false;

    public MainFrame() {
        super(Const.APP_NAME, true);
        this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setIconImages(Icons.WINDOW_ICON);
        setJMenuBar(new MainMenu());
        initGlobalComponentStyles();

        CodecSupportLoader.loadCodecs();

        initMainWindowForm();
        initListeners();

        FlatDesktop.setQuitHandler(response -> {
            MainWindowForm.instance().close();
            response.performQuit();
        });
    }

    private void initMainWindowForm() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(MainWindowForm.instance().getContentPanel(), BorderLayout.CENTER);
    }

    private void initGlobalComponentStyles() {
        UIManager.put("TitlePane.unifiedBackground", false);
        UIManager.put("MenuItem.selectionType", true);
        UIManager.put("Component.borderWidth", 1);
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("ScrollBar.showButtons", true);
        UIManager.put("Component.arrowType", "chevron");
        UIManager.put("TextComponent.arc", 5);
        UIManager.put("SplitPaneDivider.style", "grip");
        UIManager.put("SplitPane.centerOneTouchButtons", true);

        UIManager.put("PasswordField.showRevealButton", true);

        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.cellMargins", new Insets(2, 5, 2, 5));

        UIManager.put("TabbedPane.closeArc", 999);
        UIManager.put("TabbedPane.closeCrossFilledSize", 5.5f);
        UIManager.put("TabbedPane.closeIcon", new FlatTabbedPaneCloseIcon());
        UIManager.put("TabbedPane.tabsOpaque", false);

        // Swing-Toast-Notifications style
        Notifications.getInstance().setJFrame(this);
        UIManager.put("Toast.maximumWidth", 400);
        UIManager.put("Toast.effectWidth", 1000);
    }

    private void loadFrameSize() {
        Integer windowWidth = Configuration.instance().getInt(ConfKeys.WINDOW_WIDTH);
        Integer windowHeight = Configuration.instance().getInt(ConfKeys.WINDOW_HEIGHT);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (windowWidth != null && windowHeight != null) {
            // Cannot exceeds screen size
            int width = Math.min(windowWidth, screenSize.width);
            int height = Math.min(windowHeight, screenSize.height);
            // Cannot be smaller than the minimum size
            width = Math.max(width, MIN_WIDTH);
            height = Math.max(height, MIN_HEIGHT);
            Dimension size = new Dimension(width, height);
            this.setPreferredSize(size);
            this.setSize(size);

            Integer windowTop = Configuration.instance().getInt(ConfKeys.WINDOW_TOP);
            Integer windowLeft = Configuration.instance().getInt(ConfKeys.WINDOW_LEFT);
            if (windowTop != null && windowLeft != null) {
                int x = Math.max(windowLeft, 0);
                int y = Math.max(windowTop, 0);
                x = Math.min(x, screenSize.width - width);
                y = Math.min(y, screenSize.height - height);
                this.setLocation(new Point(x, y));
            } else {
                this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
            }
        } else {
            // Default size
            Dimension size;
            if (screenSize.getWidth() > 1280) {
                this.setLocation((screenSize.width - 1280) / 2, (screenSize.height - 800) / 2);
                size = new Dimension(1280, 800);
            } else if (screenSize.getWidth() > 1024) {
                this.setLocation((screenSize.width - 1200) / 2, (screenSize.height - 768) / 2);
                size = new Dimension(1200, 768);
            } else {
                this.setLocation((screenSize.width - 960) / 2, (screenSize.height - 640) / 2);
                size = new Dimension(960, 640);
            }
            this.setPreferredSize(size);
            this.setSize(size);
        }

        windowState = Configuration.instance().getInt(ConfKeys.WINDOW_STATE, 0);
        if (windowState == JFrame.MAXIMIZED_BOTH) {
            this.setExtendedState(windowState);
        }
        loaded = true;
    }

    public void close() {
        CodecSupportLoader.dispose();
        Configuration.instance().save();
        MainWindowForm.instance().close();
        Configuration.instance().clearTempPath();
        this.dispose();
    }

    private void initListeners() {
        this.addWindowStateListener(e -> {
            windowState = e.getNewState();
            Configuration.instance().set(ConfKeys.WINDOW_STATE, e.getNewState());
            Configuration.instance().changed();
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                loadFrameSize();
                ConnectionManagerForm.open();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                MainFrame.this.close();
                super.windowClosing(e);
            }

        });
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (loaded && windowState != JFrame.MAXIMIZED_BOTH) {
                    Dimension size = e.getComponent().getSize();
                    Configuration.instance().set(ConfKeys.WINDOW_WIDTH, size.width);
                    Configuration.instance().set(ConfKeys.WINDOW_HEIGHT, size.height);
                    Configuration.instance().changed();
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if (loaded && windowState != JFrame.MAXIMIZED_BOTH) {
                    Point location = e.getComponent().getLocation();
                    // 窗口最大化时, 坐标均会变成负数
                    if (location.x >= 0 && location.y >= 0) {
                        Configuration.instance().set(ConfKeys.WINDOW_LEFT, location.x);
                        Configuration.instance().set(ConfKeys.WINDOW_TOP, location.y);
                        Configuration.instance().changed();
                    }
                }
            }
        });
    }
}
