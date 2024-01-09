package com.mqttinsight.ui.frame;

import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.ui.form.*;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;

import javax.swing.*;

/**
 * @author ptma
 */
public class MainMenu extends JMenuBar {

    public MainMenu() {
        initMenu();
    }

    public void initMenu() {

        JMenuItem fileMenu = Utils.UI.createMenu(LangUtil.getString("&File"));

        fileMenu.add(Utils.UI.createMenuItem(LangUtil.getString("&ConnectionManager"),
            e -> ConnectionManagerForm.open()
        ));

        fileMenu.add(Utils.UI.createMenuItem(LangUtil.getString("Co&decs"),
            e -> DynamicCodecForm.open()
        ));

        fileMenu.add(Utils.UI.createMenuItem(LangUtil.getString("&Options"),
            e -> OptionsForm.open()
        ));

        fileMenu.add(new JSeparator());
        // 退出程序
        fileMenu.add(Utils.UI.createMenuItem(LangUtil.getString("E&xit"),
            e -> {
                MqttInsightApplication.frame.close();
                System.exit(0);
            }
        ));
        this.add(fileMenu);

        JMenuItem viewMenu = Utils.UI.createMenu(LangUtil.getString("&View"));
        viewMenu.add(Utils.UI.createMenuItem(LangUtil.getString("Show&Log"),
            e -> {
                MainWindowForm.instance().openLogTab();
            }
        ));
        this.add(viewMenu);

        JMenuItem helpMenu = Utils.UI.createMenu(LangUtil.getString("&Help"));
        helpMenu.add(Utils.UI.createMenuItem(LangUtil.getString("&About"),
            e -> {
                AboutForm.open();
            }
        ));
        this.add(helpMenu);
    }

}
