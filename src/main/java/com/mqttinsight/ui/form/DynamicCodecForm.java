package com.mqttinsight.ui.form;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.codec.DynamicCodec;
import com.mqttinsight.codec.DynamicCodecSupport;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.ui.component.model.CodecTableModel;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;

@Slf4j
public class DynamicCodecForm extends JDialog {
    private JPanel contentPane;
    private JButton closeButton;
    private JPanel bottomPanel;
    private JPanel buttonPanel;
    private JPanel tablePanel;
    private JScrollPane scrollPanel;
    private JButton addButton;
    private JButton editButton;
    private JButton removeButton;
    private JXTable codecsTable;
    private CodecTableModel codecTableModel;

    public static void open() {
        JDialog dialog = new DynamicCodecForm(MqttInsightApplication.frame);
        dialog.setMinimumSize(new Dimension(650, 400));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(MqttInsightApplication.frame);
        dialog.setVisible(true);
    }

    public DynamicCodecForm(Frame owner) {
        super(owner);
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(closeButton);

        closeButton.addActionListener(this::close);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close(e);
            }
        });

        setTitle(LangUtil.getString("Codecs"));

        initComponents();
        loadCodecsFromConfiguration();
    }

    private void initComponents() {
        LangUtil.buttonText(closeButton, "&Close");
        LangUtil.buttonText(addButton, "&New");
        LangUtil.buttonText(editButton, "&Edit");
        LangUtil.buttonText(removeButton, "&Remove");

        codecTableModel = new CodecTableModel();
        codecsTable.setModel(codecTableModel);
        codecsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        codecsTable.setRowHeight(25);
        codecsTable.getColumnExt(0).setPreferredWidth(50);
        codecsTable.getColumnExt(1).setPreferredWidth(80);
        codecsTable.getColumnExt(2).setPreferredWidth(300);

        ListSelectionModel cellSelectionModel = codecsTable.getSelectionModel();
        cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cellSelectionModel.addListSelectionListener(e -> {
            int selectedRow = codecsTable.getSelectedRow();
            editButton.setEnabled(selectedRow >= 0);
            removeButton.setEnabled(selectedRow >= 0);
        });

        // Buttons
        addButton.setIcon(Icons.ADD);
        addButton.addActionListener(this::addAction);
        codecsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    editRow(codecsTable.rowAtPoint(e.getPoint()));
                }
            }
        });
        editButton.setIcon(Icons.EDIT);
        editButton.addActionListener(this::editAction);
        removeButton.setIcon(Icons.REMOVE);
        removeButton.setEnabled(false);
        removeButton.addActionListener(this::removeAction);
    }

    private void loadCodecsFromConfiguration() {
        codecTableModel.clear();
        Configuration.instance().getDynamicCodecs().forEach(dynamicCodec -> {
            codecTableModel.addRow(dynamicCodec);
        });
    }

    private void addAction(ActionEvent e) {
        CodecEditorForm.open(null, newItem -> {
            DynamicCodecSupport codecSupport = CodecSupports.instance().getDynamicByName(newItem.getType());
            if (codecSupport == null) {
                Utils.Toast.error(LangUtil.format("NoCodec", newItem.getType()));
                return false;
            } else {
                try {
                    if (CodecSupports.instance().nameExists(newItem.getName())) {
                        Utils.Toast.warn(LangUtil.getString("CodecExists"));
                        return false;
                    }
                    DynamicCodecSupport dynamicInstance = codecSupport.newDynamicInstance(newItem.getName(), newItem.getSchemaFile(), Collections.emptyList());
                    CodecSupports.instance().register(dynamicInstance);
                    Configuration.instance().getDynamicCodecs().add(newItem);
                    Configuration.instance().changed();
                    codecTableModel.addRow(newItem);
                    codecTableModel.fireTableDataChanged();
                    MainWindowForm.instance().fireCodecsChanged();
                    return true;
                } catch (Exception ex) {
                    Utils.Toast.error(ex.getMessage());
                    log.error(ex.getMessage(), ex);
                    return false;
                }
            }
        });
    }

    private void editAction(ActionEvent e) {
        editRow(codecsTable.getSelectedRow());
    }

    private void editRow(int rowIndex) {
        if (rowIndex >= 0) {
            DynamicCodec oldItem = codecTableModel.getRow(codecsTable.convertRowIndexToModel(rowIndex));
            CodecEditorForm.open(oldItem, newItem -> {
                DynamicCodecSupport codecSupport = CodecSupports.instance().getDynamicByName(newItem.getType());
                if (codecSupport == null) {
                    Utils.Toast.error(LangUtil.format("NoCodec", newItem.getType()));
                    return false;
                } else {
                    try {
                        if (CodecSupports.instance().nameExists(newItem.getName())) {
                            Utils.Toast.warn(LangUtil.getString("CodecExists"));
                            return false;
                        }
                        DynamicCodecSupport dynamicInstance = codecSupport.newDynamicInstance(newItem.getName(), newItem.getSchemaFile(), Collections.emptyList());
                        int oldIndex = Configuration.instance().getDynamicCodecs().indexOf(oldItem);
                        CodecSupports.instance().remove(oldItem.getName());
                        CodecSupports.instance().register(dynamicInstance);
                        Configuration.instance().getDynamicCodecs().remove(oldIndex);
                        Configuration.instance().getDynamicCodecs().add(oldIndex, newItem);
                        Configuration.instance().changed();
                        codecTableModel.fireTableDataChanged();
                        MainWindowForm.instance().fireCodecsChanged();
                        return true;
                    } catch (Exception ex) {
                        Utils.Toast.error(ex.getMessage());
                        log.error(ex.getMessage(), ex);
                        return false;
                    }
                }
            });
        }
    }

    private void removeAction(ActionEvent e) {
        int selectedRow = codecsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRowIndex = codecsTable.convertRowIndexToModel(selectedRow);
            DynamicCodec selItem = codecTableModel.getRow(modelRowIndex);
            codecTableModel.removeRow(modelRowIndex);
            CodecSupports.instance().remove(selItem.getName());
            Configuration.instance().getDynamicCodecs().remove(selItem);
            MainWindowForm.instance().fireCodecsChanged();
        }
    }

    private void close(AWTEvent evt) {
        dispose();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(bottomPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        bottomPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        bottomPanel.add(buttonPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        closeButton = new JButton();
        closeButton.setText("Close");
        buttonPanel.add(closeButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tablePanel = new JPanel();
        tablePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 5, 0), -1, -1));
        contentPane.add(tablePanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        scrollPanel = new JScrollPane();
        tablePanel.add(scrollPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        codecsTable = new JXTable();
        codecsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        scrollPanel.setViewportView(codecsTable);
        final JToolBar toolBar1 = new JToolBar();
        contentPane.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addButton = new JButton();
        addButton.setText("Add");
        toolBar1.add(addButton);
        editButton = new JButton();
        editButton.setText("Edit");
        toolBar1.add(editButton);
        removeButton = new JButton();
        removeButton.setText("Remove");
        toolBar1.add(removeButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
