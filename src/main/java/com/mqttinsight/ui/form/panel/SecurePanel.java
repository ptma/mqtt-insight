package com.mqttinsight.ui.form.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.exception.VerificationException;
import com.mqttinsight.mqtt.SecureSetting;
import com.mqttinsight.ui.component.FileExtensionsFilter;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;

import javax.swing.*;
import java.io.File;

/**
 * @author ptma
 */
public abstract class SecurePanel {


    public SecurePanel() {
    }

    public abstract JPanel getRootPanel();

    public abstract void applyLanguage();

    public abstract void changeFieldsEnable(boolean enabled);

    public abstract void applySetting(SecureSetting setting);

    public abstract void resetFields();

    public abstract void verifyFields() throws VerificationException;

    public abstract SecureSetting getSetting() throws VerificationException;

    protected void initSecureFileBrowserButton(JTextField textField) {
        JButton browserFileButton = new JButton(Icons.FOLDER_OPEN);
        JToolBar fileFieldToolbar = new JToolBar();
        fileFieldToolbar.add(browserFileButton);
        fileFieldToolbar.setBorder(null);
        textField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, fileFieldToolbar);
        browserFileButton.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jFileChooser.setAcceptAllFileFilterUsed(false);
            jFileChooser.addChoosableFileFilter(new FileExtensionsFilter(LangUtil.getString("CaAndKeysFileFilter"), "jks", "jceks", "p12", "pfx", "bks", "pem", "key"));
            jFileChooser.addChoosableFileFilter(new FileExtensionsFilter(LangUtil.getString("AllFileFilter"), "*"));
            jFileChooser.setDialogTitle(LangUtil.getString("ChooseFile"));
            String directory = Configuration.instance().getString(ConfKeys.CERT_OPEN_DIALOG_PATH);
            if (directory != null) {
                jFileChooser.setCurrentDirectory(new File(directory));
            }
            int option = jFileChooser.showOpenDialog(MqttInsightApplication.frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                Configuration.instance().set(ConfKeys.CERT_OPEN_DIALOG_PATH, jFileChooser.getCurrentDirectory().getAbsolutePath());
                File file = jFileChooser.getSelectedFile();
                textField.setText(file.getAbsolutePath());
            }
        });
    }

    public void refresh() {
        getRootPanel().invalidate();
        getRootPanel().repaint();
    }
}
