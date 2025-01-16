package com.mqttinsight.ui.frame;

import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;
import com.mqttinsight.codec.CodecSupportLoader;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.scripting.ScriptEnginePool;
import com.mqttinsight.ui.component.StatePersistenceFrame;
import com.mqttinsight.ui.form.ConnectionManagerForm;
import com.mqttinsight.ui.form.MainWindowForm;
import com.mqttinsight.ui.log.LogTab;
import com.mqttinsight.util.Const;
import com.mqttinsight.util.Icons;
import raven.toast.Notifications;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

/**
 * @author ptma
 */
public class MainFrame extends StatePersistenceFrame {

    private static final Dimension MIN_DIMENSION = new Dimension(950, 600);

    public MainFrame() {
        super(Const.APP_NAME, true);
        setMinimumSize(MIN_DIMENSION);
        setIconImages(Icons.WINDOW_ICON);
        setJMenuBar(new MainMenu());
        initGlobalComponentStyles();

        LogTab.instance();

        CodecSupportLoader.loadCodecs();

        initMainWindowForm();

        FlatDesktop.setQuitHandler(response -> {
            MainFrame.this.close();
            response.performQuit();
        });
    }

    @Override
    protected String getConfigKeyPrefix() {
        return "";
    }

    @Override
    protected void onWindowOpened(WindowEvent e) {
        ConnectionManagerForm.open();
    }

    @Override
    protected void onWindowClosing(WindowEvent e) {
        MainFrame.this.close();
    }

    private void initMainWindowForm() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(MainWindowForm.instance().getContentPanel(), BorderLayout.CENTER);
    }

    private void initGlobalComponentStyles() {
        UIManager.put("TitlePane.unifiedBackground", false);
        UIManager.put("MenuItem.selectionType", true);
        UIManager.put("MenuItem.selectionArc", 5);
        UIManager.put("MenuItem.selectionInsets", new Insets(0, 2, 0, 2));
        UIManager.put("Component.borderWidth", 1);
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.arrowType", "chevron");
        UIManager.put("ScrollBar.showButtons", true);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("TextComponent.arc", 5);
        UIManager.put("SplitPaneDivider.style", "grip");
        UIManager.put("SplitPane.centerOneTouchButtons", true);
        UIManager.put("Tree.leftChildIndent", 4);
        UIManager.put("Tree.rightChildIndent", 8);

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

    public void close() {
        CodecSupportLoader.dispose();
        ScriptEnginePool.instance().close();
        Configuration.instance().save();
        MainWindowForm.instance().close();
        Configuration.instance().clearTempPath();
        this.dispose();
    }

}
