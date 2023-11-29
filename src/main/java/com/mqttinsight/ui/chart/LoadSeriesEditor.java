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

public class LoadSeriesEditor extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField seriesNameField;
    private JComboBox<Match> matchCombo;
    private JComboBox<MatchMode> matchModeCombo;
    private JTextField matchExpressionField;
    private JPanel bottomPanel;
    private JPanel centerPanel;
    private JLabel matchLabel;
    private JLabel seriesNameLabel;
    private JLabel matchModeLabel;
    private JLabel matchExpressionLabel;
    private JPanel buttonPanel;
    private JComboBox<ValueComparator> comparatorComboBox;
    private JTextField valueField;
    private JComboBox<TimeUnit> statisticalWindowCombo;
    private JSpinner statisticalWindowField;
    private JLabel statisticalWindowLabel;
    private JLabel statisticalMethodLabel;
    private JComboBox<StatisticalMethod> statisticalMethodCombo;

    private LoadSeriesProperties currentProperties;
    private final Consumer<LoadSeriesProperties> consumer;

    public static void open(Frame owner, LoadSeriesProperties properties, Consumer<LoadSeriesProperties> consumer) {
        JDialog dialog = new LoadSeriesEditor(owner, properties, consumer);
        dialog.setMinimumSize(new Dimension(450, 280));
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    public LoadSeriesEditor(Frame owner, LoadSeriesProperties properties, Consumer<LoadSeriesProperties> consumer) {
        super(owner);
        $$$setupUI$$$();
        this.consumer = consumer;
        this.currentProperties = properties;

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
        matchCombo.setModel(new EnumComboBoxModel(Match.class));
        matchCombo.setSelectedItem(SecureMode.BASIC);
        matchCombo.setRenderer(new TextableListRenderer());

        matchModeCombo.setRenderer(new TextableListRenderer());

        matchCombo.addActionListener(e -> {
            if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
                resetMatchModeCombo();
            }
        });

        comparatorComboBox.setModel(new EnumComboBoxModel(ValueComparator.class));
        comparatorComboBox.setSelectedItem(ValueComparator.EQUALS);
        comparatorComboBox.setRenderer(new TextableListRenderer());


        statisticalMethodCombo.setModel(new EnumComboBoxModel(StatisticalMethod.class));
        statisticalMethodCombo.setSelectedItem(StatisticalMethod.AVG);
        statisticalMethodCombo.setRenderer(new TextableListRenderer());

        statisticalWindowField.setModel(new SpinnerNumberModel(1, 1, 65535, 1));
        statisticalWindowField.setEditor(new JSpinner.NumberEditor(statisticalWindowField, "####"));

        statisticalWindowCombo.setModel(new EnumComboBoxModel(TimeUnit.class));
        statisticalWindowCombo.setSelectedItem(TimeUnit.MINUTES);
        statisticalWindowCombo.setRenderer(new TextableListRenderer());

        matchModeCombo.addActionListener(e -> {
            if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
                expressionFieldsVisible();
            }
        });

        if (currentProperties != null) {
            seriesNameField.setText(currentProperties.getSeriesName());
            matchCombo.setSelectedItem(currentProperties.getMatch());
            matchModeCombo.setSelectedItem(currentProperties.getMatchMode());

            MatchExpression matchExpression = currentProperties.getMatchExpression();
            if (MatchMode.JSON_PATH.equals(currentProperties.getMatchMode()) || MatchMode.XPATH.equals(matchModeCombo.getSelectedItem())) {
                matchExpressionField.setText(matchExpression.getExpression());
                comparatorComboBox.setSelectedItem(matchExpression.getComparator());
                valueField.setText(matchExpression.getValue());
            } else {
                matchExpressionField.setText(currentProperties.getMatchExpression().getExpression());
            }
            statisticalMethodCombo.setSelectedItem(currentProperties.getStatisticalMethod());
            statisticalWindowField.setValue(currentProperties.getWindow().getDuration());
            statisticalWindowCombo.setSelectedItem(currentProperties.getWindow().getUnit());
        }
        expressionFieldsVisible();
        resetMatchModeCombo();
    }

    private void applyLanguage() {
        setTitle(LangUtil.getString("SeriesEditor"));
        seriesNameLabel.setText(LangUtil.getString("SeriesName"));
        matchLabel.setText(LangUtil.getString("Match"));
        matchModeLabel.setText(LangUtil.getString("MatchMode"));
        matchExpressionLabel.setText(LangUtil.getString("MatchExpression"));
        statisticalMethodLabel.setText(LangUtil.getString("StatisticalMethod"));
        statisticalWindowLabel.setText(LangUtil.getString("StatisticalWindow"));
        LangUtil.buttonText(buttonOK, "&Ok");
        LangUtil.buttonText(buttonCancel, "&Cancel");
    }

    private void expressionFieldsVisible() {
        boolean isJsonOrXPath = MatchMode.JSON_PATH.equals(matchModeCombo.getSelectedItem())
            || MatchMode.XPATH.equals(matchModeCombo.getSelectedItem());
        comparatorComboBox.setVisible(isJsonOrXPath);
        valueField.setVisible(isJsonOrXPath);
    }

    private void resetMatchModeCombo() {
        Match match = (Match) matchCombo.getSelectedItem();
        if (match != null) {
            matchModeCombo.removeAllItems();
            for (MatchMode matchMode : match.getMatchModes()) {
                matchModeCombo.addItem(matchMode);
            }
            if (currentProperties != null) {
                for (int i = 0; i < matchModeCombo.getItemCount(); i++) {
                    if (matchModeCombo.getItemAt(i).equals(currentProperties.getMatchMode())) {
                        matchModeCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void verifyFields() throws VerificationException {
        Validator.notEmpty(seriesNameField, () -> LangUtil.format("FieldRequiredValidation", seriesNameLabel.getText()));
        Validator.notEmpty(matchExpressionField, () -> LangUtil.format("FieldRequiredValidation", matchExpressionLabel.getText()));
        boolean isJsonOrXPath = MatchMode.JSON_PATH.equals(matchModeCombo.getSelectedItem())
            || MatchMode.XPATH.equals(matchModeCombo.getSelectedItem());
        if (isJsonOrXPath) {
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

        if (currentProperties == null) {
            currentProperties = new LoadSeriesProperties();
        }
        currentProperties.setSeriesName(seriesNameField.getText());
        currentProperties.setMatch((Match) matchCombo.getSelectedItem());
        currentProperties.setMatchMode((MatchMode) matchModeCombo.getSelectedItem());
        if (MatchMode.JSON_PATH.equals(matchModeCombo.getSelectedItem()) || MatchMode.XPATH.equals(matchModeCombo.getSelectedItem())) {
            currentProperties.setMatchExpression(
                MatchExpression.jsonPath(
                    matchExpressionField.getText(),
                    (ValueComparator) comparatorComboBox.getSelectedItem(),
                    valueField.getText()
                )
            );
        } else {
            currentProperties.setMatchExpression(MatchExpression.normal(matchExpressionField.getText()));
        }
        currentProperties.setStatisticalMethod((StatisticalMethod) statisticalMethodCombo.getSelectedItem());
        currentProperties.setWindow(Duration.of((int) statisticalWindowField.getValue(), (TimeUnit) statisticalWindowCombo.getSelectedItem()));

        if (consumer != null) {
            consumer.accept(currentProperties);
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        LoadSeriesEditor.open(null, null, (c) -> {
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
        centerPanel.setLayout(new GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(centerPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        seriesNameLabel = new JLabel();
        seriesNameLabel.setText("Series Name");
        centerPanel.add(seriesNameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        centerPanel.add(spacer2, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        seriesNameField = new JTextField();
        centerPanel.add(seriesNameField, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        matchLabel = new JLabel();
        matchLabel.setText("Match");
        centerPanel.add(matchLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        matchCombo = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        matchCombo.setModel(defaultComboBoxModel1);
        centerPanel.add(matchCombo, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        matchExpressionLabel = new JLabel();
        matchExpressionLabel.setText("Expression");
        centerPanel.add(matchExpressionLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        matchExpressionField = new JTextField();
        centerPanel.add(matchExpressionField, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        comparatorComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("=");
        defaultComboBoxModel2.addElement("!=");
        defaultComboBoxModel2.addElement("contains");
        defaultComboBoxModel2.addElement("not contains");
        defaultComboBoxModel2.addElement(">");
        defaultComboBoxModel2.addElement(">=");
        defaultComboBoxModel2.addElement("<");
        defaultComboBoxModel2.addElement("<=");
        comparatorComboBox.setModel(defaultComboBoxModel2);
        centerPanel.add(comparatorComboBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        valueField = new JTextField();
        centerPanel.add(valueField, new GridConstraints(3, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        statisticalWindowLabel = new JLabel();
        statisticalWindowLabel.setText("Sliding Window");
        centerPanel.add(statisticalWindowLabel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statisticalWindowCombo = new JComboBox();
        centerPanel.add(statisticalWindowCombo, new GridConstraints(5, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statisticalWindowField = new JSpinner();
        centerPanel.add(statisticalWindowField, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        matchModeLabel = new JLabel();
        matchModeLabel.setText("Match Mode");
        centerPanel.add(matchModeLabel, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        matchModeCombo = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        matchModeCombo.setModel(defaultComboBoxModel3);
        centerPanel.add(matchModeCombo, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statisticalMethodCombo = new JComboBox();
        centerPanel.add(statisticalMethodCombo, new GridConstraints(4, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statisticalMethodLabel = new JLabel();
        statisticalMethodLabel.setText("StatisticalMethod");
        centerPanel.add(statisticalMethodLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
