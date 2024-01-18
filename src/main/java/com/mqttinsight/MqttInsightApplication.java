package com.mqttinsight;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import com.formdev.flatlaf.util.SystemInfo;
import com.mqttinsight.ui.component.TextFieldPopupEventListener;
import com.mqttinsight.ui.frame.MainFrame;
import com.mqttinsight.util.Const;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.ThemeUtil;
import com.mqttinsight.util.Utils;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.swing.*;
import java.awt.*;

/**
 * @author ptma
 */
public class MqttInsightApplication {

    public static MainFrame frame;

    public static void main(String[] args) {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            if (SystemInfo.isMacOS) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("apple.awt.application.name", Const.APP_NAME);
                System.setProperty("apple.awt.application.appearance", "system");
            }

            if (SystemInfo.isLinux) {
                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
            }

            if (!SystemInfo.isJava_9_orLater && System.getProperty("flatlaf.uiScale") == null) {
                System.setProperty("flatlaf.uiScale", "2x");
            }

            SwingUtilities.invokeLater(() -> {

                FlatLaf.registerCustomDefaultsSource("com.mqttinsight");
                ToolTipManager.sharedInstance().setInitialDelay(300);
                ToolTipManager.sharedInstance().setDismissDelay(20000);
                ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
                FlatJetBrainsMonoFont.install();
                // FlatInspector.install("ctrl shift alt X");
                // FlatUIDefaultsInspector.install("ctrl shift alt Y");
                ThemeUtil.setupTheme(args);
                LangUtil.setupLanguage();
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                toolkit.addAWTEventListener(new TextFieldPopupEventListener(), AWTEvent.MOUSE_EVENT_MASK);

                frame = new MainFrame();
                if (SystemInfo.isMacFullWindowContentSupported) {
                    frame.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
                }

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });
        } catch (Exception e) {
            Utils.Message.error(e.getMessage(), e);
        }
    }

}
