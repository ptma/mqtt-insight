package com.mqttinsight.ui.chart;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mqttinsight.exception.VerificationException;
import com.mqttinsight.mqtt.SecureMode;
import com.mqttinsight.ui.chart.series.*;
import com.mqttinsight.ui.component.renderer.TextableListRenderer;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import com.mqttinsight.util.Validator;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class CountSeriesEditor extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField seriesNameField;
    private JComboBox<Match> matchCombo;
    private JComboBox<MatchType> matchTypeCombo;
    private JTextField matchExpressionField;
    private JPanel bottomPanel;
    private JPanel centerPanel;
    private JLabel matchLabel;
    private JLabel seriesNameLabel;
    private JLabel matchTypeLabel;
    private JLabel matchExpressionLabel;
    private JPanel buttonPanel;
    private JCheckBox dynamicCheckBox;
    private JComboBox<String> comparatorComboBox;
    private JTextField valueField;

    private MessageSeriesDefinition currentDefinition;
    private final Consumer<MessageSeriesDefinition> consumer;

    public static void open(Frame owner, MessageSeriesDefinition series, Consumer<MessageSeriesDefinition> consumer) {
        JDialog dialog = new CountSeriesEditor(owner, series, consumer);
        dialog.setMinimumSize(new Dimension(450, 260));
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    public CountSeriesEditor(Frame owner, MessageSeriesDefinition definition, Consumer<MessageSeriesDefinition> consumer) {
        super(owner);
        $$$setupUI$$$();
        this.consumer = consumer;
        this.currentDefinition = definition;

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        initComponents();

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        applyLanguage();
    }

    private void initComponents() {
        matchCombo.setModel(new EnumComboBoxModel<Match>(Match.class));
        matchCombo.setSelectedItem(SecureMode.BASIC);
        matchCombo.setRenderer(new TextableListRenderer());

        matchTypeCombo.setRenderer(new TextableListRenderer());

        matchCombo.addActionListener(e -> {
            if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
                resetMatchTypeCombo();
            }
        });
        dynamicCheckBox.addActionListener(e -> {
            expressionFieldsVisible();
            resetMatchTypeCombo();
        });
        matchTypeCombo.addActionListener(e -> {
            if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
                expressionFieldsVisible();
            }
        });

        if (currentDefinition != null) {
            dynamicCheckBox.setSelected(currentDefinition.isDynamic());
            matchCombo.setSelectedItem(currentDefinition.getMatch());
            seriesNameField.setText(currentDefinition.getSeriesName());

            MatchExpression matchExpression = currentDefinition.getMatchExpression();
            if (MatchType.JSON_PATH.equals(currentDefinition.getMatchType())) {
                matchExpressionField.setText(matchExpression.getExpression());
                comparatorComboBox.setSelectedItem(matchExpression.getComparator());
                valueField.setText(matchExpression.getValue());
            } else {
                matchExpressionField.setText(currentDefinition.getMatchExpression().getExpression());
            }
        }
        expressionFieldsVisible();
        resetMatchTypeCombo();
    }

    private void applyLanguage() {
        setTitle(LangUtil.getString("SeriesEditor"));
        LangUtil.buttonText(dynamicCheckBox, "DynamicSeries");
        seriesNameLabel.setText(LangUtil.getString("SeriesName"));
        matchLabel.setText(LangUtil.getString("Match"));
        matchTypeLabel.setText(LangUtil.getString("Type"));
        matchExpressionLabel.setText(LangUtil.getString("Expression"));
        LangUtil.buttonText(buttonOK, "&Ok");
        LangUtil.buttonText(buttonCancel, "&Cancel");
    }

    private void expressionFieldsVisible() {
        boolean isDynamic = dynamicCheckBox.isSelected();
        boolean isJsonpath = MatchType.JSON_PATH.equals(matchTypeCombo.getSelectedItem());
        boolean visible = !isDynamic && isJsonpath;
        comparatorComboBox.setVisible(visible);
        valueField.setVisible(visible);
    }

    private void resetMatchTypeCombo() {
        boolean isDynamic = dynamicCheckBox.isSelected();
        Match match = (Match) matchCombo.getSelectedItem();
        if (match != null) {
            matchTypeCombo.removeAllItems();
            for (MatchType matchType : match.getMatchTypes()) {
                if (isDynamic) {
                    if (matchType.isSupportsDynamic()) {
                        matchTypeCombo.addItem(matchType);
                    }
                } else {
                    matchTypeCombo.addItem(matchType);
                }
            }
            if (currentDefinition != null) {
                for (int i = 0; i < matchTypeCombo.getItemCount(); i++) {
                    if (matchTypeCombo.getItemAt(i).equals(currentDefinition.getMatchType())) {
                        matchTypeCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void verifyFields() throws VerificationException {
        Validator.notEmpty(seriesNameField, () -> LangUtil.format("FieldRequiredValidation", seriesNameLabel.getText()));
        Validator.notEmpty(matchExpressionField, () -> LangUtil.format("FieldRequiredValidation", matchExpressionLabel.getText()));
        boolean isDynamic = dynamicCheckBox.isSelected();
        boolean isJsonpath = MatchType.JSON_PATH.equals(matchTypeCombo.getSelectedItem());
        if (!isDynamic && isJsonpath) {
            Validator.notEmpty(comparatorComboBox, () -> LangUtil.format("FieldRequiredValidation", "Comparer"));
        }
    }

    private void onOK() {
        try {
            verifyFields();
        } catch (VerificationException e) {
            Utils.Toast.warn(e.getMessage());
            return;
        }

        if (currentDefinition == null) {
            currentDefinition = new MessageSeriesDefinition();
        }
        currentDefinition.setDynamic(dynamicCheckBox.isSelected());
        currentDefinition.setSeriesName(seriesNameField.getText());
        currentDefinition.setMatch((Match) matchCombo.getSelectedItem());
        currentDefinition.setMatchType((MatchType) matchTypeCombo.getSelectedItem());
        if (MatchType.JSON_PATH.equals(matchTypeCombo.getSelectedItem())) {
            currentDefinition.setMatchExpression(
                MatchExpression.jsonPath(
                    matchExpressionField.getText(),
                    (String) comparatorComboBox.getSelectedItem(),
                    valueField.getText()
                )
            );
        } else {
            currentDefinition.setMatchExpression(MatchExpression.normal(matchExpressionField.getText()));
        }

        if (consumer != null) {
            consumer.accept(currentDefinition);
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        CountSeriesEditor.open(null, null, (c) -> {
            System.exit(0);
        });
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(bottomPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        bottomPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        bottomPanel.add(buttonPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        buttonPanel.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        buttonPanel.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayoutManager(7, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(centerPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        seriesNameLabel = new JLabel();
        seriesNameLabel.setText("Series Name");
        centerPanel.add(seriesNameLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        centerPanel.add(spacer2, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        seriesNameField = new JTextField();
        centerPanel.add(seriesNameField, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        matchLabel = new JLabel();
        matchLabel.setText("Match");
        centerPanel.add(matchLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        matchCombo = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        matchCombo.setModel(defaultComboBoxModel1);
        centerPanel.add(matchCombo, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        matchTypeLabel = new JLabel();
        matchTypeLabel.setText("Type");
        centerPanel.add(matchTypeLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        matchTypeCombo = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        matchTypeCombo.setModel(defaultComboBoxModel2);
        centerPanel.add(matchTypeCombo, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        matchExpressionLabel = new JLabel();
        matchExpressionLabel.setText("Expression");
        centerPanel.add(matchExpressionLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        matchExpressionField = new JTextField();
        centerPanel.add(matchExpressionField, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        dynamicCheckBox = new JCheckBox();
        dynamicCheckBox.setText("Dynamic Series");
        centerPanel.add(dynamicCheckBox, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comparatorComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("=");
        defaultComboBoxModel3.addElement("!=");
        defaultComboBoxModel3.addElement("contains");
        defaultComboBoxModel3.addElement("not contains");
        defaultComboBoxModel3.addElement(">");
        defaultComboBoxModel3.addElement(">=");
        defaultComboBoxModel3.addElement("<");
        defaultComboBoxModel3.addElement("<=");
        comparatorComboBox.setModel(defaultComboBoxModel3);
        centerPanel.add(comparatorComboBox, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        valueField = new JTextField();
        centerPanel.add(valueField, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
