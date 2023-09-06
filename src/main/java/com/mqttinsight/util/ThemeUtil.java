package com.mqttinsight.util;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.config.Themes;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * @author ptma
 */
@Slf4j
public class ThemeUtil {

    public static final String THEMES_PACKAGE = "/com/formdev/flatlaf/intellijthemes/themes/";

    public static void setupTheme(String[] args) {
        try {
            if (args.length > 0) {
                UIManager.setLookAndFeel(args[0]);
            } else {
                String themeName = Configuration.instance().getString(ConfKeys.THEME, Themes.LIGHT.name());
                Themes theme = Themes.of(themeName);
                if (theme.getLafClassName() != null) {
                    UIManager.setLookAndFeel(theme.getLafClassName());
                } else {
                    IntelliJTheme.setup(ThemeUtil.class.getResourceAsStream(THEMES_PACKAGE + theme.getResourceName()));
                }
                UIManager.put("laf.dark", theme.isDark());
                FlatLaf.updateUI();
            }
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
            FlatLightLaf.setup();
        }
    }

}
