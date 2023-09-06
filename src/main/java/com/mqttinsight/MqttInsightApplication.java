package com.mqttinsight;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import com.formdev.flatlaf.util.SystemInfo;
import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.ui.frame.MainFrame;
import com.mqttinsight.util.Const;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.ThemeUtil;

import javax.swing.*;
import java.util.Locale;

/**
 * @author ptma
 */
public class MqttInsightApplication {

    public static MainFrame frame;

    public static void main(String[] args) {

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
            ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
            FlatJetBrainsMonoFont.install();
            // FlatInspector.install("ctrl shift alt X");
            // FlatUIDefaultsInspector.install("ctrl shift alt Y");
            ThemeUtil.setupTheme(args);
            String languageTag = Configuration.instance().getString(ConfKeys.LANGUAGE, Locale.getDefault().toLanguageTag());
            LangUtil.setLocale(Locale.forLanguageTag(languageTag));

            frame = new MainFrame();
            if (SystemInfo.isMacFullWindowContentSupported) {
                frame.getRootPane().putClientProperty("apple.awt.transparentTitleBar", true);
            }

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

    }


}
