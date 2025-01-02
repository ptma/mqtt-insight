package com.mqttinsight.ui.form;

import cn.hutool.core.util.StrUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.codec.DynamicCodec;
import com.mqttinsight.codec.DynamicCodecSupport;
import com.mqttinsight.codec.proto.MappingField;
import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.exception.VerificationException;
import com.mqttinsight.ui.component.FileExtensionsFilter;
import com.mqttinsight.ui.component.model.CodecMappingTableModel;
import com.mqttinsight.ui.component.model.DynaminCodecComboModel;
import com.mqttinsight.ui.component.renderer.EmptyPlaceholderTableCellRenderer;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import com.mqttinsight.util.Validator;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author ptma
 */
public class CodecEditorForm extends JDialog {
    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel bottomPanel;
    private JPanel bottomButtonPanel;
    private JPanel formPanel;
    private JTextField nameField;
    private JLabel nameLabel;
    private JComboBox<String> typeComboBox;
    private JTextField schemaFileField;
    private JLabel schemaFileLabel;
    private JLabel typeLabel;
    private JPanel mappingPanel;
    private JButton removeButton;
    private JButton addButton;
    private JPanel mappingBottomPanel;
    private JScrollPane tableScrollPanel;
    private JTable mappingTable;
    private JLabel mappingLabel;

    private DynamicCodec editingItem;
    private final Function<DynamicCodec, Boolean> function;
    private DynamicCodecSupport codecSupport;
    private CodecMappingTableModel mappingTableModel;

    public static void open(DynamicCodec editingItem, Function<DynamicCodec, Boolean> function) {
        CodecEditorForm dialog = new CodecEditorForm(MqttInsightApplication.frame, editingItem, function);
        dialog.setMinimumSize(new Dimension(700, 400));
        dialog.pack();
        dialog.setResizable(false);
        dialog.resetTable();
        dialog.setLocationRelativeTo(MqttInsightApplication.frame);
        dialog.setVisible(true);

    }

    private CodecEditorForm(Frame owner, DynamicCodec editingItem, Function<DynamicCodec, Boolean> function) {
        super(owner);
        this.function = function;
        this.editingItem = editingItem;

        $$$setupUI$$$();
        setContentPane(contentPanel);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        initComponents();

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPanel.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        applyLanguage();
    }

    private void initComponents() {
        mappingTableModel = new CodecMappingTableModel();
        mappingTable.setModel(mappingTableModel);

        typeComboBox.setModel(new DynaminCodecComboModel());
        if (editingItem != null) {
            this.setTitle(LangUtil.getString("EditCodec"));
            nameField.setText(editingItem.getName());
            nameField.setEnabled(false);
            typeComboBox.setSelectedItem(editingItem.getType());
            typeComboBox.setEnabled(false);
            schemaFileField.setText(editingItem.getSchemaFile());
        } else {
            this.setTitle(LangUtil.getString("NewCodec"));
            typeComboBox.addItemListener(e -> {
                codecSupport = CodecSupports.instance().getDynamicByName((String) e.getItem());
                resetTable();
            });
        }
        codecSupport = CodecSupports.instance().getDynamicByName((String) typeComboBox.getSelectedItem());
        initFileBrowserButton(schemaFileField);

        mappingTable.setRowHeight(25);
        mappingTable.setShowGrid(true);
        mappingTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        mappingTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        mappingTable.setCellEditor(new DefaultCellEditor(new JTextField()));
        mappingTable.setDefaultRenderer(String.class, new EmptyPlaceholderTableCellRenderer());

        initTableActions();
    }

    private void initTableActions() {
        ListSelectionModel cellSelectionModel = mappingTable.getSelectionModel();
        cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cellSelectionModel.addListSelectionListener(e -> {
            int selectedRow = mappingTable.getSelectedRow();
            removeButton.setEnabled(selectedRow >= 0);
        });
        addButton.addActionListener(e -> {
            mappingTableModel.addEmptyRow();
            int row = mappingTableModel.getRowCount() - 1;
            mappingTable.changeSelection(row, 0, false, false);
            if (mappingTable.editCellAt(row, 0)) {
                mappingTable.getEditorComponent().requestFocusInWindow();
            }
        });
        removeButton.addActionListener(e -> {
            int selectedRow = mappingTable.getSelectedRow();
            if (selectedRow >= 0) {
                mappingTableModel.removeRow(selectedRow);
            }
        });
    }

    private void resetTable() {
        mappingTable.setEnabled(codecSupport.mappable());
        mappingLabel.setEnabled(codecSupport.mappable());
        addButton.setEnabled(codecSupport.mappable());
        removeButton.setEnabled(codecSupport.mappable() && mappingTable.getSelectedRow() >= 0);

        mappingTableModel.clear();

        if (codecSupport.mappable()) {
            mappingTableModel.setFields(codecSupport.getMappings());
        } else {
            mappingTableModel.setFields(null);
        }
        List<MappingField> fields = mappingTableModel.getFields();
        for (int i = 0; i < fields.size(); i++) {
            MappingField mappingField = fields.get(i);
            TableColumn column = mappingTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(mappingTable.getWidth() * mappingField.getPercentageWidth() / 100);
            column.setWidth(mappingTable.getWidth() * mappingField.getPercentageWidth() / 100);
        }

        if (editingItem != null) {
            mappingTableModel.addAll(editingItem.getMappings());
        }

        mappingTable.revalidate();
        mappingTable.repaint();
    }

    protected void initFileBrowserButton(JTextField textField) {
        JButton browserFileButton = new JButton(Icons.FOLDER_OPEN);
        JToolBar fileFieldToolbar = new JToolBar();
        fileFieldToolbar.add(browserFileButton);
        fileFieldToolbar.setBorder(null);
        textField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, fileFieldToolbar);
        browserFileButton.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jFileChooser.setAcceptAllFileFilterUsed(false);
            String filterDescription = LangUtil.getString("SchemaFile");
            filterDescription += " (";
            filterDescription += Arrays.stream(codecSupport.getSchemaFileExtensions()).map(ext -> "*." + ext).collect(Collectors.joining(","));
            filterDescription += ")";
            jFileChooser.addChoosableFileFilter(new FileExtensionsFilter(filterDescription, codecSupport.getSchemaFileExtensions()));
            jFileChooser.setDialogTitle(LangUtil.getString("ChooseFile"));
            String directory = Configuration.instance().getString(ConfKeys.SCHEMA_OPEN_DIALOG_PATH);
            if (directory != null) {
                jFileChooser.setCurrentDirectory(new File(directory));
            }
            int option = jFileChooser.showOpenDialog(MqttInsightApplication.frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                Configuration.instance().set(ConfKeys.SCHEMA_OPEN_DIALOG_PATH, jFileChooser.getCurrentDirectory().getAbsolutePath());
                File file = jFileChooser.getSelectedFile();
                textField.setText(file.getAbsolutePath());
            }
        });
    }

    private void applyLanguage() {
        nameLabel.setText(LangUtil.getString("Name"));
        typeLabel.setText(LangUtil.getString("Type"));
        schemaFileLabel.setText(LangUtil.getString("SchemaFile"));
        mappingLabel.setText(LangUtil.getString("MessageMapping"));
        mappingLabel.setToolTipText(LangUtil.getString("MessageMappingToolTip"));
        mappingLabel.setIcon(Icons.TIPS);
        mappingLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        addButton.setText(LangUtil.getString("AddMapping"));
        removeButton.setText(LangUtil.getString("RemoveMapping"));
        LangUtil.buttonText(buttonOK, "&Ok");
        LangUtil.buttonText(buttonCancel, "&Cancel");
    }

    private void verifyFields() throws VerificationException {
        Validator.notEmpty(nameField, () -> LangUtil.format("FieldRequiredValidation", LangUtil.getString("Name")));
        Validator.notEmpty(typeComboBox, () -> LangUtil.format("FieldRequiredValidation", LangUtil.getString("Type")));
        Validator.notEmpty(schemaFileField, () -> LangUtil.format("FieldRequiredValidation", LangUtil.getString("SchemaFile")));
        if (codecSupport.mappable()) {
            List<Map<String, String>> existingRows = mappingTableModel.getRows();
            for (int i = 0; i < existingRows.size(); i++) {
                Map<String, String> existingRow = existingRows.get(i);
                for (MappingField field : codecSupport.getMappings()) {
                    if (StrUtil.isBlank(existingRow.get(field.getKey()))) {
                        throw new VerificationException(LangUtil.format("IncompleteMessageMapping", i + 1));
                    }
                }
            }
        }
    }

    private void onOK() {
        try {
            verifyFields();
        } catch (VerificationException e) {
            Utils.Toast.warn(e.getMessage());
            return;
        }

        String name = StrUtil.trim(nameField.getText());
        String type = (String) typeComboBox.getSelectedItem();
        String schemaFile = schemaFileField.getText();
        DynamicCodec dynamicCodec = new DynamicCodec(name, type, schemaFile);
        if (codecSupport.mappable()) {
            dynamicCodec.setMappings(mappingTableModel.getRows());
        }
        if (function.apply(dynamicCodec)) {
            dispose();
        }
    }

    private void onCancel() {
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
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(bottomPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        bottomPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        bottomButtonPanel = new JPanel();
        bottomButtonPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        bottomPanel.add(bottomButtonPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        bottomButtonPanel.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        bottomButtonPanel.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        formPanel = new JPanel();
        formPanel.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,top:100px:grow"));
        contentPanel.add(formPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        nameLabel = new JLabel();
        nameLabel.setText("Name");
        CellConstraints cc = new CellConstraints();
        formPanel.add(nameLabel, cc.xy(1, 1));
        nameField = new JTextField();
        formPanel.add(nameField, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        typeLabel = new JLabel();
        typeLabel.setText("Type");
        formPanel.add(typeLabel, cc.xy(1, 3));
        typeComboBox = new JComboBox();
        formPanel.add(typeComboBox, cc.xy(3, 3));
        schemaFileField = new JTextField();
        formPanel.add(schemaFileField, cc.xy(3, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        schemaFileLabel = new JLabel();
        schemaFileLabel.setText("Schema File");
        formPanel.add(schemaFileLabel, cc.xy(1, 5));
        mappingPanel = new JPanel();
        mappingPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        formPanel.add(mappingPanel, cc.xy(3, 7, CellConstraints.DEFAULT, CellConstraints.FILL));
        tableScrollPanel = new JScrollPane();
        mappingPanel.add(tableScrollPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        mappingTable = new JTable();
        mappingTable.setFillsViewportHeight(true);
        tableScrollPanel.setViewportView(mappingTable);
        mappingBottomPanel = new JPanel();
        mappingBottomPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mappingPanel.add(mappingBottomPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        removeButton = new JButton();
        removeButton.setEnabled(false);
        removeButton.setHorizontalTextPosition(11);
        removeButton.setText("Remove mapping");
        mappingBottomPanel.add(removeButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addButton = new JButton();
        addButton.setText("Add mapping");
        mappingBottomPanel.add(addButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        mappingBottomPanel.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        mappingLabel = new JLabel();
        mappingLabel.setText("Message Mapping");
        mappingLabel.setVerticalAlignment(1);
        mappingLabel.setVerticalTextPosition(0);
        formPanel.add(mappingLabel, new CellConstraints(1, 7, 1, 1, CellConstraints.DEFAULT, CellConstraints.TOP, new Insets(5, 0, 0, 0)));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
