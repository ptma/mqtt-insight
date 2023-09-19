package com.mqttinsight.ui.form;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.exception.VerificationException;
import com.mqttinsight.mqtt.FavoriteSubscription;
import com.mqttinsight.mqtt.MqttQos;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.ui.component.ColorPicker;
import com.mqttinsight.ui.component.model.PayloadFormatComboBoxModel;
import com.mqttinsight.ui.component.renderer.TextableListRenderer;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.*;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author ptma
 */
public class NewSubscriptionForm extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<Object> topicComboBox;
    private JComboBox<MqttQos> qosComboBox;
    private JComboBox<String> formatComboBox;
    private JPanel bottomPanel;
    private JPanel buttonPanel;
    private JPanel mainPanel;
    private JLabel topicLabel;
    private JLabel qoSLabel;
    private JLabel formatLabel;
    private JLabel colorLabel;
    private ColorPicker colorPicker;

    private final MqttInstance mqttInstance;
    private final Consumer<Subscription> okConsumer;

    public static void open(final MqttInstance mqttInstance, Consumer<Subscription> okConsumer) {
        JDialog dialog = new NewSubscriptionForm(MqttInsightApplication.frame, mqttInstance, okConsumer);
        dialog.setMinimumSize(new Dimension(550, 120));
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(MqttInsightApplication.frame);
        dialog.setVisible(true);
    }

    private NewSubscriptionForm(final Frame owner, final MqttInstance mqttInstance, Consumer<Subscription> okConsumer) {
        super(owner);
        this.mqttInstance = mqttInstance;
        this.okConsumer = okConsumer;
        setTitle(LangUtil.getString("NewSubscription"));
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        initComponents();
    }

    private void initComponents() {
        List<FavoriteSubscription> favoriteSubscriptions = mqttInstance.getProperties().getFavoriteSubscriptions();
        if (favoriteSubscriptions != null) {
            favoriteSubscriptions.sort(Comparator.comparing(FavoriteSubscription::getTopic));
            favoriteSubscriptions.forEach(favorite -> topicComboBox.addItem(favorite));
        }
        topicComboBox.setSelectedItem("");
        topicComboBox.setRenderer(new TextableListRenderer());
        topicComboBox.addActionListener(e -> {
            if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
                Object sel = topicComboBox.getSelectedItem();
                if (sel instanceof FavoriteSubscription) {
                    FavoriteSubscription subscription = (FavoriteSubscription) sel;
                    qosComboBox.setSelectedItem(MqttQos.of(subscription.getQos()));
                    formatComboBox.setSelectedItem(subscription.getPayloadFormat());
                }
            }
        });
        AutoCompleteDecorator.decorate(topicComboBox);

        qosComboBox.setModel(new EnumComboBoxModel(MqttQos.class));
        qosComboBox.setSelectedItem(MqttQos.QOS_0);
        qosComboBox.setRenderer(new TextableListRenderer());
        formatComboBox.setModel(new PayloadFormatComboBoxModel(true));
        formatComboBox.setSelectedItem(CodecSupport.DEFAULT);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        topicLabel.setText(LangUtil.getString("Topic"));
        qoSLabel.setText(LangUtil.getString("QoS"));
        formatLabel.setText(LangUtil.getString("PayloadFormat"));
        formatLabel.setToolTipText(LangUtil.getString("PayloadFormatTip"));
        formatLabel.setIcon(Icons.TIPS);
        formatLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        colorLabel.setText(LangUtil.getString("Color"));
        colorPicker.setMoreText(LangUtil.getString("MoreColor"));
        colorPicker.setDialogTitle(LangUtil.getString("ChooseColor"));
        LangUtil.buttonText(buttonOK, "&Ok");
        LangUtil.buttonText(buttonCancel, "&Cancel");

    }

    private void verifyFields() throws VerificationException {
        Validator.notEmpty(topicComboBox, () -> LangUtil.format("FieldRequiredValidation", LangUtil.getString("Topic")));
        TopicUtil.validate(topicComboBox.getSelectedItem().toString());
    }

    private void onOK() {
        try {
            verifyFields();
        } catch (VerificationException e) {
            Utils.Toast.warn(e.getMessage());
            return;
        }
        String topic = topicComboBox.getSelectedItem().toString();
        MqttQos qos = (MqttQos) qosComboBox.getSelectedItem();
        String payloadFormat = (String) formatComboBox.getSelectedItem();
        Subscription subscription = new Subscription(mqttInstance, topic, qos.getValue(), payloadFormat, colorPicker.getColor());
        okConsumer.accept(subscription);
        dispose();
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
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setMinimumSize(new Dimension(416, 130));
        contentPane.setPreferredSize(new Dimension(416, 130));
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
        mainPanel = new JPanel();
        mainPanel.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:8dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:8dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:5dlu:noGrow,fill:max(d;60px):grow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        contentPane.add(mainPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        topicLabel = new JLabel();
        topicLabel.setText("Topic");
        CellConstraints cc = new CellConstraints();
        mainPanel.add(topicLabel, cc.xy(1, 1));
        topicComboBox = new JComboBox();
        topicComboBox.setEditable(true);
        mainPanel.add(topicComboBox, cc.xyw(3, 1, 11));
        qoSLabel = new JLabel();
        qoSLabel.setText("QoS");
        mainPanel.add(qoSLabel, cc.xy(1, 3));
        qosComboBox = new JComboBox();
        mainPanel.add(qosComboBox, cc.xy(3, 3));
        formatLabel = new JLabel();
        formatLabel.setText("Paylaod Format");
        mainPanel.add(formatLabel, cc.xy(5, 3));
        formatComboBox = new JComboBox();
        mainPanel.add(formatComboBox, cc.xy(7, 3));
        colorLabel = new JLabel();
        colorLabel.setText("Color");
        mainPanel.add(colorLabel, cc.xy(9, 3));
        mainPanel.add(colorPicker, cc.xy(11, 3));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private void createUIComponents() {
        colorPicker = new ColorPicker(Utils.generateRandomColor());
    }
}
