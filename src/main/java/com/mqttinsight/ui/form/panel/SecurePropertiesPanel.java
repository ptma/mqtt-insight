package com.mqttinsight.ui.form.panel;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.mqttinsight.exception.VerificationException;
import com.mqttinsight.mqtt.Property;
import com.mqttinsight.mqtt.SecureSetting;
import com.mqttinsight.ui.component.model.PropertiesTableModel;
import com.mqttinsight.util.LangUtil;
import org.jdesktop.swingx.JXComboBox;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory.*;

/**
 * @author ptma
 */
public class SecurePropertiesPanel extends SecurePanel {

    private JPanel rootPanel;
    private JButton removePropertyButton;
    private JPanel bottomPanel;
    private JPanel bottomButtonPanel;
    private JPanel tablePanel;
    private JTable propertiesTable;
    private JScrollPane tableScrollPanel;
    private JButton addPropertyButton;

    private PropertiesTableModel propertiesTableModel;

    private static final String[] PROPERTY_KEYS = new String[]{
        SSLPROTOCOL, JSSEPROVIDER, KEYSTORE, KEYSTOREPWD, KEYSTORETYPE, KEYSTOREPROVIDER,
        KEYSTOREMGR, TRUSTSTORE, TRUSTSTOREPWD, TRUSTSTORETYPE, TRUSTSTOREPROVIDER,
        TRUSTSTOREMGR, CIPHERSUITES, CLIENTAUTH
    };

    public SecurePropertiesPanel() {
        super();
        $$$setupUI$$$();
        initPropertiesTable();
        applyLanguage();
    }

    @Override
    public void applyLanguage() {
        LangUtil.buttonText(removePropertyButton, "RemoveProperty");
        LangUtil.buttonText(addPropertyButton, "AddProperty");
    }

    private void initPropertiesTable() {
        propertiesTableModel = new PropertiesTableModel();
        propertiesTable.setModel(propertiesTableModel);
        propertiesTable.setRowHeight(25);
        propertiesTable.setShowGrid(true);
        propertiesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        propertiesTable.repaint();

        JXComboBox propertiesCombobox = new JXComboBox(PROPERTY_KEYS);
        propertiesCombobox.setSelectedIndex(0);
        TableColumnModel columnModel = propertiesTable.getColumnModel();
        columnModel.getColumn(0).setCellEditor(new DefaultCellEditor(propertiesCombobox));

        ListSelectionModel cellSelectionModel = propertiesTable.getSelectionModel();
        cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cellSelectionModel.addListSelectionListener(e -> {
            int selectedRow = propertiesTable.getSelectedRow();
            removePropertyButton.setEnabled(selectedRow >= 0);
        });
        addPropertyButton.addActionListener(e -> {
            propertiesTableModel.addRow(Property.of(LangUtil.getString("NewProperty"), LangUtil.getString("NewValue")));
            int row = propertiesTableModel.getRowCount() - 1;
            propertiesTable.changeSelection(row, 0, false, false);
            if (propertiesTable.editCellAt(row, 0)) {
                propertiesTable.getEditorComponent().requestFocusInWindow();
            }
        });
        removePropertyButton.addActionListener(e -> {
            int selectedRow = propertiesTable.getSelectedRow();
            if (selectedRow >= 0) {
                propertiesTableModel.removeRow(selectedRow);
            }
        });
    }

    @Override
    public JPanel getRootPanel() {
        return rootPanel;
    }

    @Override
    public void changeFieldsEnable(boolean enabled) {
        propertiesTable.setEnabled(enabled);
        addPropertyButton.setEnabled(enabled);
        removePropertyButton.setEnabled(enabled);
        propertiesTable.repaint();
    }

    @Override
    public void applySetting(SecureSetting setting) {
        propertiesTableModel.clear();
        for (Property property : setting.getProperties()) {
            propertiesTableModel.addRow(property);
        }
    }

    @Override
    public void resetFields() {
        propertiesTableModel.clear();
    }

    @Override
    public void verifyFields() throws VerificationException {

    }

    @Override
    public SecureSetting getSetting() throws VerificationException {
        SecureSetting setting = new SecureSetting();
        List<Property> properties = new ArrayList<>();
        for (int i = 0; i < propertiesTableModel.getRowCount(); i++) {
            String key = (String) propertiesTableModel.getValueAt(i, 0);
            if (!keyValid(key)) {
                throw new VerificationException(String.format(LangUtil.getString("SslPropertyNotValid"), key));
            }
            String value = (String) propertiesTableModel.getValueAt(i, 1);
            properties.add(Property.of(key, value));
        }
        setting.setProperties(properties);
        return setting;
    }

    private boolean keyValid(String key) {
        int i = 0;
        while (i < PROPERTY_KEYS.length) {
            if (PROPERTY_KEYS[i].equals(key)) {
                break;
            }
            ++i;
        }
        return i < PROPERTY_KEYS.length;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(bottomPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        bottomButtonPanel = new JPanel();
        bottomButtonPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        bottomPanel.add(bottomButtonPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        removePropertyButton = new JButton();
        removePropertyButton.setEnabled(false);
        removePropertyButton.setText("Remove property");
        bottomButtonPanel.add(removePropertyButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        bottomButtonPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        addPropertyButton = new JButton();
        addPropertyButton.setText("Add property");
        bottomButtonPanel.add(addPropertyButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tablePanel = new JPanel();
        tablePanel.setLayout(new FormLayout("fill:d:grow", "center:d:grow"));
        rootPanel.add(tablePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        tableScrollPanel = new JScrollPane();
        CellConstraints cc = new CellConstraints();
        tablePanel.add(tableScrollPanel, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.FILL));
        propertiesTable = new JTable();
        tableScrollPanel.setViewportView(propertiesTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
