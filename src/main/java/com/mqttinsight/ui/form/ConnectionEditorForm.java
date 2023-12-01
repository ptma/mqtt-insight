package com.mqttinsight.ui.form;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.config.ConnectionNode;
import com.mqttinsight.exception.VerificationException;
import com.mqttinsight.mqtt.*;
import com.mqttinsight.mqtt.options.Mqtt3Options;
import com.mqttinsight.mqtt.options.Mqtt5Options;
import com.mqttinsight.ui.component.SyntaxTextEditor;
import com.mqttinsight.ui.component.model.PayloadFormatComboBoxModel;
import com.mqttinsight.ui.component.model.PropertiesTableModel;
import com.mqttinsight.ui.component.renderer.TextableListRenderer;
import com.mqttinsight.ui.form.panel.*;
import com.mqttinsight.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.formdev.flatlaf.FlatClientProperties.*;

/**
 * @author ptma
 */
@SuppressWarnings("ALL")
@Slf4j
public class ConnectionEditorForm extends JDialog {
    private JPanel contentPanel;
    private JButton buttonOk;
    private JButton buttonCancel;
    private JTabbedPane mainTabPanel;
    private JPanel generalPanel;
    private JPanel otherPanel;
    private JTextField serverField;
    private JComboBox<Transport> transportComboBox;
    private JTextField clientIdField;
    private JButton generateClientIdButton;
    private JCheckBox cleanSessionCheckBox;
    private JCheckBox autoReconnectCheckBox;
    private JPanel bottomPanel;
    private JPanel bottomButtonPanel;
    private JSpinner portField;
    private JSpinner connectTimeoutField;
    private JSpinner keepAliveIntervalField;
    private JSpinner reconnectIntervalField;
    private JComboBox<Version> versionComboBox;
    private JLabel nameLabel;
    private JTextField nameField;
    private JLabel transportLabel;
    private JLabel versionLabel;
    private JLabel serverLabel;
    private JLabel portLabel;
    private JLabel clientIdLabel;
    private JLabel connectionTimeoutLabel;
    private JLabel keepAliveIntervalLabel;
    private JLabel reconnectIntervalLabel;
    private JPanel willPanel;
    private JCheckBox willEnableCheckBox;
    private JTextField topicField;
    private JComboBox<Integer> qosComboBox;
    private JCheckBox retainedCheckBox;
    private JComboBox<String> willPayloadFormatComboBox;
    private JPanel sslPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPanel editorPanel;

    private JLabel topicLabel;
    private JLabel qosLabel;
    private JLabel payloadLabel;
    private JLabel willPayloadFormatLabel;
    private JPanel bodyPanel;
    private JCheckBox randomClientIdCheckBox;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JCheckBox sslEnableCheckBox;
    private JComboBox<SecureProtocol> sslProtocolComboBox;
    private JComboBox<SecureMode> sslModeComboBox;
    private JLabel sslProtocolLabel;
    private JPanel sslFieldsPanel;
    private JPanel mqtt5Panel;
    private JTextField receiveMaximumField;
    private JTextField sessionExpiryIntervalField;
    private JCheckBox cleanStartCheckBox;
    private JTextField maximumPacketSizeField;
    private JTextField topicAliasMaximumField;
    private JCheckBox requestResponseInfoCheckBox;
    private JCheckBox requestProblemInfoCheckBox;
    private JLabel sessionExpiryIntervalLabel;
    private JLabel receiveMaximumLabel;
    private JLabel maximumPacketSizeLabel;
    private JLabel topicAliasMaximumLabel;
    private JLabel userPropertiesLabel;
    private JTable userPropertiesTable;
    private JButton removeUserPropertyButton;
    private JButton addUserPropertyButton;
    private JLabel sslModeLabel;
    private JSpinner maxMessagesStoredField;
    private JComboBox<String> defaultPayloadFormatComboBox;
    private JLabel maxMessagesStoredLabel;
    private JLabel defaultPayloadFormatLabel;
    private JCheckBox clearUnsubMessageCheckBox;
    private JButton buttonTest;
    private SyntaxTextEditor payloadField;

    private ConnectionNode editingNode;
    private final Consumer<ConnectionNode> consumer;
    private static final Map<SecureMode, SecurePanel> SECURE_MODE_PANEL = new HashMap<>();
    private final PropertiesTableModel propertiesTableModel = new PropertiesTableModel();

    static {
        SECURE_MODE_PANEL.put(SecureMode.BASIC, new SecureEmptyPanel());
        SECURE_MODE_PANEL.put(SecureMode.SERVER_ONLY, new ServerOnlyPanel());
        SECURE_MODE_PANEL.put(SecureMode.SERVER_KEYSTORE, new ServerKeystorePanel());
        SECURE_MODE_PANEL.put(SecureMode.SERVER_AND_CLIENT, new ServerAndClientPanel());
        SECURE_MODE_PANEL.put(SecureMode.SERVER_AND_CLIENT_KEYSTORES, new ServerAndClientKeystorePanel());
        SECURE_MODE_PANEL.put(SecureMode.PROPERTIES, new SecurePropertiesPanel());
    }

    public static void open(ConnectionNode editingNode, Consumer<ConnectionNode> consumer) {
        JDialog dialog = new ConnectionEditorForm(MqttInsightApplication.frame, editingNode, consumer);
        dialog.setMinimumSize(new Dimension(630, 450));
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(MqttInsightApplication.frame);
        dialog.setVisible(true);
    }

    private ConnectionEditorForm(Frame owner, ConnectionNode editingNode, Consumer<ConnectionNode> consumer) {
        super(owner);
        this.consumer = consumer;
        this.editingNode = editingNode;
        $$$setupUI$$$();

        this.setModal(true);
        this.setContentPane(contentPanel);
        this.setResizable(false);
        getRootPane().setDefaultButton(buttonOk);

        initComponents();
        initComponentsAction();
        initFormFieldsData();
        applyLanguage();
    }

    private void initComponents() {
        if (editingNode == null) {
            this.setTitle(LangUtil.getString("NewConnection"));
        } else {
            this.setTitle(LangUtil.getString("EditConnection"));
        }

        mainTabPanel.putClientProperty(TABBED_PANE_SHOW_TAB_SEPARATORS, false);
        mainTabPanel.putClientProperty(TABBED_PANE_TAB_TYPE, TABBED_PANE_TAB_TYPE_UNDERLINED);
        // General
        generateClientIdButton = new JButton(Icons.REFRESH);
        JToolBar clientIdFieldToolbar = new JToolBar();
        clientIdFieldToolbar.add(generateClientIdButton);
        clientIdField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, clientIdFieldToolbar);
        portField.setModel(new SpinnerNumberModel(1883, 0, 65535, 1));
        portField.setEditor(new JSpinner.NumberEditor(portField, "####"));
        passwordField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        connectTimeoutField.setModel(new SpinnerNumberModel(30, 0, Integer.MAX_VALUE, 1));
        connectTimeoutField.setEditor(new JSpinner.NumberEditor(connectTimeoutField, "####"));
        keepAliveIntervalField.setModel(new SpinnerNumberModel(60, 0, Integer.MAX_VALUE, 1));
        keepAliveIntervalField.setEditor(new JSpinner.NumberEditor(keepAliveIntervalField, "####"));
        reconnectIntervalField.setModel(new SpinnerNumberModel(30, 30, Integer.MAX_VALUE, 1));
        reconnectIntervalField.setEditor(new JSpinner.NumberEditor(reconnectIntervalField, "####"));
        transportComboBox.setModel(new EnumComboBoxModel(Transport.class));
        transportComboBox.setSelectedIndex(0);
        transportComboBox.setRenderer(new TextableListRenderer());
        versionComboBox.setModel(new EnumComboBoxModel(Version.class));
        versionComboBox.setSelectedItem(Version.MQTT_3_1_1);
        versionComboBox.setRenderer(new TextableListRenderer());

        // MQTT5
        KeyAdapter numberKeyAdpter = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                int keyChar = e.getKeyChar();
                if (keyChar != KeyEvent.VK_DELETE
                    && keyChar != KeyEvent.VK_BACK_SPACE
                    && !(keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9)
                    && !(keyChar >= KeyEvent.VK_NUMPAD0 && keyChar <= KeyEvent.VK_NUMPAD9)
                ) {
                    e.consume();
                }
            }
        };
        sessionExpiryIntervalField.addKeyListener(numberKeyAdpter);
        receiveMaximumField.addKeyListener(numberKeyAdpter);
        maximumPacketSizeField.addKeyListener(numberKeyAdpter);
        topicAliasMaximumField.addKeyListener(numberKeyAdpter);
        initUserPropertiesTable();

        // TLS/SSL
        sslModeComboBox.setModel(new EnumComboBoxModel(SecureMode.class));
        sslModeComboBox.setSelectedItem(SecureMode.BASIC);
        sslModeComboBox.setRenderer(new TextableListRenderer());
        sslProtocolComboBox.setModel(new EnumComboBoxModel(SecureProtocol.class));
        sslProtocolComboBox.setSelectedItem(SecureProtocol.TLS_1_2);
        sslProtocolComboBox.setRenderer(new TextableListRenderer());
        sslFieldsPanel.setOpaque(false);

        // Last will
        payloadField = new SyntaxTextEditor();
        payloadField.setEnabled(false);
        editorPanel.add(payloadField, BorderLayout.CENTER);
        willPayloadFormatComboBox.setModel(new PayloadFormatComboBoxModel(false));
        willPayloadFormatComboBox.setSelectedItem(CodecSupport.PLAIN);

        // Other
        maxMessagesStoredField.setModel(new SpinnerNumberModel(Const.MESSAGES_STORED_MAX_SIZE, 1000, Integer.MAX_VALUE, 100));
        maxMessagesStoredField.setEditor(new JSpinner.NumberEditor(maxMessagesStoredField, "####"));
        defaultPayloadFormatComboBox.setModel(new PayloadFormatComboBoxModel(false));
        defaultPayloadFormatComboBox.setSelectedItem(CodecSupport.PLAIN);
    }

    private void initUserPropertiesTable() {
        userPropertiesTable.setModel(propertiesTableModel);
        userPropertiesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        ListSelectionModel cellSelectionModel = userPropertiesTable.getSelectionModel();
        cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cellSelectionModel.addListSelectionListener(e -> {
            int selectedRow = userPropertiesTable.getSelectedRow();
            removeUserPropertyButton.setEnabled(selectedRow >= 0);
        });
        addUserPropertyButton.addActionListener(e -> {
            propertiesTableModel.addRow(Property.of(LangUtil.getString("NewProperty"), LangUtil.getString("NewValue")));
            int row = propertiesTableModel.getRowCount() - 1;
            userPropertiesTable.changeSelection(row, 0, false, false);
            if (userPropertiesTable.editCellAt(row, 0)) {
                userPropertiesTable.getEditorComponent().requestFocusInWindow();
            }
        });
        removeUserPropertyButton.addActionListener(e -> {
            int selectedRow = userPropertiesTable.getSelectedRow();
            if (selectedRow >= 0) {
                propertiesTableModel.removeRow(selectedRow);
            }
        });
    }

    private void initComponentsAction() {
        buttonTest.addActionListener(e -> onTest());
        buttonOk.addActionListener(e -> onOk());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPanel.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        generateClientIdButton.addActionListener(e -> {
            clientIdField.setText("MqttInsight_" + RandomUtil.randomString(8));
        });

        versionComboBox.addActionListener(this::versionChanged);
        cleanStartCheckBox.addChangeListener(e -> {
            sessionExpiryIntervalField.setEnabled(cleanStartCheckBox.isSelected());
        });
        // SSL Mode
        sslEnableCheckBox.addChangeListener(this::sslStatusChanged);
        sslModeComboBox.addActionListener(this::sslModeChanged);

        willEnableCheckBox.addChangeListener(this::willCheckboxChanged);
    }

    private void sslModeChanged(ActionEvent e) {
        if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
            executeWithCurrentSecureMode(securePanel -> {
                if (securePanel != null) {
                    securePanel.resetFields();
                    securePanel.applyLanguage();
                    if (editingNode != null) {
                        securePanel.applySetting(editingNode.getProperties().getSecure());
                    }
                    securePanel.changeFieldsEnable(sslEnableCheckBox.isSelected());
                    sslFieldsPanel.removeAll();
                    sslFieldsPanel.add(securePanel.getRootPanel(), BorderLayout.CENTER);
                    securePanel.refresh();
                    sslFieldsPanel.invalidate();
                    sslFieldsPanel.repaint();
                }
                return true;
            });
        }
    }

    private void sslStatusChanged(ChangeEvent e) {
        sslModeComboBox.setEnabled(sslEnableCheckBox.isSelected());
        sslProtocolComboBox.setEnabled(sslEnableCheckBox.isSelected());
        executeWithCurrentSecureMode(securePanel -> {
            if (securePanel != null) {
                securePanel.resetFields();
                securePanel.applyLanguage();
                if (editingNode != null) {
                    securePanel.applySetting(editingNode.getProperties().getSecure());
                }
                securePanel.changeFieldsEnable(sslEnableCheckBox.isSelected());
                securePanel.refresh();
            }
            return true;
        });
    }

    private void versionChanged(ActionEvent e) {
        if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
            boolean isMqtt5 = Version.MQTT_5.equals(versionComboBox.getSelectedItem());
            cleanSessionCheckBox.setVisible(!isMqtt5);
            if (isMqtt5) {
                mainTabPanel.insertTab(LangUtil.getString("MQTT5Options"), null, mqtt5Panel, null, 1);
            } else if (mainTabPanel.getComponentAt(1) == mqtt5Panel) {
                mainTabPanel.removeTabAt(1);
            }
        }
    }

    private void willCheckboxChanged(ChangeEvent e) {
        topicField.setEnabled(willEnableCheckBox.isSelected());
        qosComboBox.setEnabled(willEnableCheckBox.isSelected());
        retainedCheckBox.setEnabled(willEnableCheckBox.isSelected());
        payloadField.setEnabled(willEnableCheckBox.isSelected());
        willPayloadFormatComboBox.setEnabled(willEnableCheckBox.isSelected());
    }

    @SneakyThrows
    private void initFormFieldsData() {
        if (editingNode == null) {
            clientIdField.setText("MqttInsight_" + RandomUtil.randomString(8));
            versionComboBox.setSelectedItem(Version.MQTT_3_1_1);
            cleanSessionCheckBox.setSelected(true);
            autoReconnectCheckBox.setSelected(true);
            clearUnsubMessageCheckBox.setSelected(true);
            return;
        }
        final MqttProperties properties = editingNode.getProperties();
        // General
        nameField.setText(properties.getName());
        transportComboBox.setSelectedItem(properties.getTransport());
        versionComboBox.setSelectedItem(properties.getVersion());
        serverField.setText(properties.getHost());
        portField.setValue(properties.getPort());
        portField.commitEdit();
        clientIdField.setText(properties.getClientId());
        usernameField.setText(properties.getUsername());
        passwordField.setText(properties.getPassword());
        randomClientIdCheckBox.setSelected(properties.isRandomClientId());
        cleanSessionCheckBox.setSelected(properties.isCleanSession());
        connectTimeoutField.setValue(Math.max(0, properties.getConnectionTimeout()));
        connectTimeoutField.commitEdit();
        keepAliveIntervalField.setValue(Math.max(0, properties.getKeepAliveInterval()));
        keepAliveIntervalField.commitEdit();
        ReconnectionSettings reConn = properties.getReconnection();
        autoReconnectCheckBox.setSelected(reConn.isEnable());
        reconnectIntervalField.setValue(Math.max(0, reConn.getReconnectInterval()));
        reconnectIntervalField.commitEdit();
        // MQTT5
        if (Version.MQTT_5.equals(properties.getVersion())) {
            cleanStartCheckBox.setSelected(properties.isCleanStart());
            sessionExpiryIntervalField.setText(StrUtil.toStringOrNull(properties.getSessionExpiryInterval()));
            sessionExpiryIntervalField.setEnabled(properties.isCleanStart());
            receiveMaximumField.setText(StrUtil.toStringOrNull(properties.getReceiveMaximum()));
            maximumPacketSizeField.setText(StrUtil.toStringOrNull(properties.getMaximumPacketSize()));
            topicAliasMaximumField.setText(StrUtil.toStringOrNull(properties.getTopicAliasMaximum()));
            requestResponseInfoCheckBox.setSelected(properties.isRequestResponseInfo());
            requestProblemInfoCheckBox.setSelected(properties.isRequestProblemInfo());
            propertiesTableModel.setProperties(properties.getUserProperties());
        }

        // SSL
        SecureSetting secure = properties.getSecure();
        sslEnableCheckBox.setSelected(secure.isEnable());
        sslModeComboBox.setEnabled(sslEnableCheckBox.isSelected());
        sslModeComboBox.setSelectedItem(secure.getMode());
        sslProtocolComboBox.setEnabled(sslEnableCheckBox.isSelected());
        sslProtocolComboBox.setSelectedItem(secure.getProtocol());

        // Last will
        WillMessage will = properties.getLastWill();
        willEnableCheckBox.setSelected(will.isEnable());
        topicField.setText(will.getTopic());
        qosComboBox.setSelectedIndex(will.getQos());
        retainedCheckBox.setSelected(will.isRetained());
        payloadField.textArea().setText(will.getPayload());
        if (will.getPayloadFormat() != null) {
            willPayloadFormatComboBox.setSelectedItem(will.getPayloadFormat());
        }

        // Other
        if (properties.getMaxMessageStored() != null) {
            maxMessagesStoredField.setValue(properties.getMaxMessageStored());
        }
        defaultPayloadFormatComboBox.setSelectedItem(properties.getPayloadFormat());
        clearUnsubMessageCheckBox.setSelected(properties.isClearUnsubMessage());
    }

    public void applyLanguage() {
        generateClientIdButton.setToolTipText(LangUtil.getString("Generate"));
        cleanSessionCheckBox.setText(LangUtil.getString("CleanSession"));
        cleanSessionCheckBox.setToolTipText(LangUtil.getString("CleanSessionToolTip"));
        autoReconnectCheckBox.setText(LangUtil.getString("AutoReconnect"));
        nameLabel.setText(LangUtil.getString("Name"));
        transportLabel.setText(LangUtil.getString("Transport"));
        versionLabel.setText(LangUtil.getString("Version"));
        serverLabel.setText(LangUtil.getString("Server"));
        portLabel.setText(LangUtil.getString("Port"));
        clientIdLabel.setText(LangUtil.getString("ClientId"));
        connectionTimeoutLabel.setText(LangUtil.getString("ConnectionTimeout"));
        connectTimeoutField.setToolTipText(LangUtil.getString("ConnectionTimeoutToolTip"));
        keepAliveIntervalLabel.setText(LangUtil.getString("KeepAliveInterval"));
        keepAliveIntervalField.setToolTipText(LangUtil.getString("KeepAliveIntervalToolTip"));
        reconnectIntervalLabel.setText(LangUtil.getString("ReconnectInterval"));
        reconnectIntervalField.setToolTipText(LangUtil.getString("ReconnectIntervalToolTip"));
        willEnableCheckBox.setText(LangUtil.getString("EnableLastWill"));
        retainedCheckBox.setText(LangUtil.getString("Retained"));

        topicLabel.setText(LangUtil.getString("Topic"));
        qosLabel.setText(LangUtil.getString("QoS"));
        payloadLabel.setText(LangUtil.getString("Payload"));
        willPayloadFormatLabel.setText(LangUtil.getString("PayloadFormat"));
        randomClientIdCheckBox.setText(LangUtil.getString("RandomClientId"));
        usernameLabel.setText(LangUtil.getString("Username"));
        passwordLabel.setText(LangUtil.getString("Password"));
        sslEnableCheckBox.setText(LangUtil.getString("EnableSSL"));
        sslProtocolLabel.setText(LangUtil.getString("SslProtocol"));
        sslModeLabel.setText(LangUtil.getString("SslMode"));
        cleanStartCheckBox.setText(LangUtil.getString("CleanStart"));
        cleanStartCheckBox.setToolTipText(LangUtil.getString("CleanStartToolTip"));
        requestResponseInfoCheckBox.setText(LangUtil.getString("RequestResponseInfo"));
        requestResponseInfoCheckBox.setToolTipText(LangUtil.getString("RequestResponseInfoToolTip"));
        requestProblemInfoCheckBox.setText(LangUtil.getString("RequestProblemInfo"));
        requestProblemInfoCheckBox.setToolTipText(LangUtil.getString("RequestProblemInfoToolTip"));
        sessionExpiryIntervalLabel.setText(LangUtil.getString("SessionExpiryInterval"));
        sessionExpiryIntervalField.setToolTipText(LangUtil.getString("SessionExpiryIntervalToolTip"));
        receiveMaximumLabel.setText(LangUtil.getString("ReceiveMaximum"));
        receiveMaximumField.setToolTipText(LangUtil.getString("ReceiveMaximumToolTip"));
        maximumPacketSizeLabel.setText(LangUtil.getString("MaximumPacketSize"));
        maximumPacketSizeField.setToolTipText(LangUtil.getString("MaximumPacketSizeToolTip"));
        topicAliasMaximumLabel.setText(LangUtil.getString("TopicAliasMaximum"));
        topicAliasMaximumField.setToolTipText(LangUtil.getString("TopicAliasMaximumToolTip"));
        userPropertiesLabel.setText(LangUtil.getString("UserProperties"));
        LangUtil.buttonText(removeUserPropertyButton, "RemoveProperty");
        LangUtil.buttonText(addUserPropertyButton, "AddProperty");

        maxMessagesStoredLabel.setText(LangUtil.getString("MaxMessagesStored"));
        defaultPayloadFormatLabel.setText(LangUtil.getString("DefaultPayloadFormat"));
        clearUnsubMessageCheckBox.setText(LangUtil.getString("ClearUnsubMessage"));

        for (int tabIndex = 0; tabIndex < mainTabPanel.getTabCount(); tabIndex++) {
            Component tabComponent = mainTabPanel.getComponentAt(tabIndex);
            if (tabComponent.equals(generalPanel)) {
                mainTabPanel.setTitleAt(tabIndex, LangUtil.getString("General"));
            } else if (tabComponent.equals(mqtt5Panel)) {
                mainTabPanel.setTitleAt(tabIndex, LangUtil.getString("MQTT5Options"));
            } else if (tabComponent.equals(sslPanel)) {
                mainTabPanel.setTitleAt(tabIndex, LangUtil.getString("TLS/SSL"));
            } else if (tabComponent.equals(willPanel)) {
                mainTabPanel.setTitleAt(tabIndex, LangUtil.getString("LastWill"));
            } else if (tabComponent.equals(otherPanel)) {
                mainTabPanel.setTitleAt(tabIndex, LangUtil.getString("Other"));
            }
        }

        LangUtil.buttonText(buttonTest, "TestConnection");
        LangUtil.buttonText(buttonOk, "&Ok");
        LangUtil.buttonText(buttonCancel, "&Cancel");
    }

    /**
     * 获取当前安全设置模式并处理
     *
     * @param function
     * @return
     */
    private Boolean executeWithCurrentSecureMode(Function<SecurePanel, Boolean> function) {
        SecureMode mode = (SecureMode) sslModeComboBox.getSelectedItem();
        if (mode != null) {
            SecurePanel currentPanel;
            currentPanel = SECURE_MODE_PANEL.get(mode);
            return function.apply(currentPanel);
        } else {
            return false;
        }
    }

    private void verifyFields() throws VerificationException {
        Validator.notEmpty(nameField, () -> LangUtil.format("FieldRequiredValidation", nameLabel.getText()));
        Validator.notEmpty(serverField, () -> LangUtil.format("FieldRequiredValidation", serverLabel.getText()));
        Validator.notEmpty(clientIdField, () -> LangUtil.format("FieldRequiredValidation", clientIdLabel.getText()));
        Validator.maxLength(clientIdField, 23, () -> LangUtil.format("FieldMaxLengthValidation", clientIdLabel.getText(), 23));
        if (Version.MQTT_5.equals(versionComboBox.getSelectedItem())) {
            Validator.range(receiveMaximumField, 1, 65535, () -> LangUtil.format("FieldRangeValidation", receiveMaximumLabel.getText(), 1, 65535));
            Validator.range(maximumPacketSizeField, 1, 2684354656l, () -> LangUtil.format("FieldRangeValidation", maximumPacketSizeLabel.getText(), 1, 2684354656l));
            Validator.range(topicAliasMaximumField, 0, 65535, () -> LangUtil.format("FieldRangeValidation", topicAliasMaximumLabel.getText(), 0, 65535));
        }
        if (willEnableCheckBox.isSelected()) {
            Validator.notEmpty(topicField, () -> LangUtil.format("FieldRequiredValidation", topicLabel.getText()));
            Validator.notEmpty(payloadField.textArea(), () -> LangUtil.format("FieldRequiredValidation", payloadLabel.getText()));
        }
        if (sslEnableCheckBox.isSelected()) {
            SecureMode mode = (SecureMode) sslModeComboBox.getSelectedItem();
            if (mode != null) {
                SecurePanel currentPanel = SECURE_MODE_PANEL.get(mode);
                currentPanel.verifyFields();
            }
        }
    }

    private void verifyProperties(Consumer<MqttProperties> consumer) {
        try {
            verifyFields();
        } catch (VerificationException e) {
            Utils.Toast.warn(e.getMessage());
            return;
        }
        MqttProperties properties;
        if (editingNode == null) {
            properties = new MqttProperties();
        } else {
            properties = editingNode.getProperties();
        }
        properties.setName(nameField.getText());
        properties.setTransport((Transport) transportComboBox.getSelectedItem());
        properties.setVersion((Version) versionComboBox.getSelectedItem());
        properties.setHost(serverField.getText());
        properties.setPort((Integer) portField.getValue());
        properties.setClientId(clientIdField.getText());
        properties.setUsername(usernameField.getText());
        properties.setPassword(String.valueOf(passwordField.getPassword()));
        properties.setRandomClientId(randomClientIdCheckBox.isSelected());

        properties.setCleanSession(cleanSessionCheckBox.isSelected());
        properties.setConnectionTimeout((Integer) connectTimeoutField.getValue());
        properties.setKeepAliveInterval((Integer) keepAliveIntervalField.getValue());
        ReconnectionSettings reConn = properties.getReconnection();
        reConn.setEnable(autoReconnectCheckBox.isSelected());
        reConn.setReconnectInterval((Integer) reconnectIntervalField.getValue());
        // MQTT5
        if (Version.MQTT_5.equals(properties.getVersion())) {
            properties.setCleanStart(cleanStartCheckBox.isSelected());
            if (NumberUtil.isLong(sessionExpiryIntervalField.getText())) {
                properties.setSessionExpiryInterval(Long.valueOf(sessionExpiryIntervalField.getText()));
            }
            if (NumberUtil.isInteger(receiveMaximumField.getText())) {
                properties.setReceiveMaximum(Integer.valueOf(receiveMaximumField.getText()));
            }
            if (NumberUtil.isLong(maximumPacketSizeField.getText())) {
                properties.setMaximumPacketSize(Long.valueOf(maximumPacketSizeField.getText()));
            }
            if (NumberUtil.isInteger(topicAliasMaximumField.getText())) {
                properties.setTopicAliasMaximum(Integer.valueOf(topicAliasMaximumField.getText()));
            }
            properties.setRequestResponseInfo(requestResponseInfoCheckBox.isSelected());
            properties.setRequestProblemInfo(requestProblemInfoCheckBox.isSelected());
            properties.setUserProperties(propertiesTableModel.getProperties());
        }
        // SSL
        boolean valid = executeWithCurrentSecureMode(securePanel -> {
            try {
                SecureSetting secure = securePanel.getSetting();
                secure.setEnable(sslEnableCheckBox.isSelected());
                secure.setMode((SecureMode) sslModeComboBox.getSelectedItem());
                secure.setProtocol((SecureProtocol) sslProtocolComboBox.getSelectedItem());
                properties.setSecure(secure);
                return true;
            } catch (VerificationException e) {
                Utils.Toast.warn(e.getMessage());
                return false;
            }
        });
        if (!valid) {
            return;
        }

        // Last will
        WillMessage will = new WillMessage();
        will.setEnable(willEnableCheckBox.isSelected());
        will.setTopic(topicField.getText());
        will.setQos(qosComboBox.getSelectedIndex());
        will.setRetained(retainedCheckBox.isSelected());
        will.setPayload(payloadField.textArea().getText());
        will.setPayloadFormat((String) willPayloadFormatComboBox.getSelectedItem());
        properties.setLastWill(will);

        // Other
        properties.setMaxMessageStored((Integer) maxMessagesStoredField.getValue());
        properties.setPayloadFormat((String) defaultPayloadFormatComboBox.getSelectedItem());
        properties.setClearUnsubMessage(clearUnsubMessageCheckBox.isSelected());
        consumer.accept(properties);
    }

    private void onTest() {
        SwingUtilities.invokeLater(() -> {
            verifyProperties(properties -> {
                buttonTest.setEnabled(false);
                try {
                    if (properties.getVersion().equals(Version.MQTT_5)) {
                        testMqttV5(properties);
                    } else {
                        testMqttV3(properties);
                    }
                } catch (Exception e) {
                    Utils.Toast.warn(String.format(LangUtil.getString("TestConnectionError"), e.getMessage()));
                }
                buttonTest.setEnabled(true);
            });
        });
    }

    private void testMqttV5(MqttProperties properties) throws org.eclipse.paho.mqttv5.common.MqttException {
        org.eclipse.paho.mqttv5.client.MqttClient mqttClient = new org.eclipse.paho.mqttv5.client.MqttClient(
            properties.completeServerURI(),
            properties.getClientId()
        );
        mqttClient.setTimeToWait(5000L);
        MqttConnectionOptions options = Mqtt5Options.fromProperties(properties);
        options.setConnectionTimeout(10);
        org.eclipse.paho.mqttv5.client.IMqttToken token = mqttClient.connectWithResult(options);
        token.waitForCompletion();
        if (token.getException() != null) {
            String causeMessage = token.getException().getMessage();
            if (token.getException().getCause() != null) {
                causeMessage = token.getException().getCause().getMessage();
            }
            Utils.Toast.warn(String.format(LangUtil.getString("TestConnectionFailed"), token.getException().getReasonCode(), causeMessage));
        } else {
            Utils.Toast.success(LangUtil.getString("TestConnectionSuccessful"));
        }
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
            mqttClient.close();
        }
    }

    private void testMqttV3(MqttProperties properties) throws MqttException {
        MqttClient mqttClient = new MqttClient(
            properties.completeServerURI(),
            properties.getClientId()
        );
        mqttClient.setTimeToWait(5000L);
        MqttConnectOptions options = Mqtt3Options.fromProperties(properties);
        options.setConnectionTimeout(10);
        IMqttToken token = mqttClient.connectWithResult(options);
        token.waitForCompletion();
        if (token.getException() != null) {
            String causeMessage = token.getException().getMessage();
            if (token.getException().getCause() != null) {
                causeMessage = token.getException().getCause().getMessage();
            }
            Utils.Toast.warn(String.format(LangUtil.getString("TestConnectionFailed"), token.getException().getReasonCode(), causeMessage));
        } else {
            Utils.Toast.success(LangUtil.getString("TestConnectionSuccessful"));
        }
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
            mqttClient.close(true);
        }
    }

    private void onOk() {
        verifyProperties(properties -> {
            if (editingNode == null) {
                editingNode = new ConnectionNode(properties);
            } else {
                properties.setId(editingNode.getProperties().getId());
                editingNode.setProperties(properties);
            }
            if (consumer != null) {
                consumer.accept(editingNode);
            }
            dispose();
        });
    }

    private void onCancel() {
        // add your code here if necessary
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
        bottomPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(bottomPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        bottomPanel.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        bottomButtonPanel = new JPanel();
        bottomButtonPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        bottomPanel.add(bottomButtonPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOk = new JButton();
        buttonOk.setEnabled(true);
        buttonOk.setText("OK");
        bottomButtonPanel.add(buttonOk, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        bottomButtonPanel.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonTest = new JButton();
        buttonTest.setText("Test Connection");
        bottomPanel.add(buttonTest, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bodyPanel = new JPanel();
        bodyPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(bodyPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        mainTabPanel = new JTabbedPane();
        mainTabPanel.setTabLayoutPolicy(0);
        bodyPanel.add(mainTabPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        generalPanel = new JPanel();
        generalPanel.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:80px:noGrow,left:4dlu:noGrow,left:4dlu:noGrow,fill:60px:noGrow,left:4dlu:noGrow,left:40px:grow,left:4dlu:noGrow,fill:d:noGrow,fill:max(d;4px):noGrow,fill:110px:noGrow,left:4dlu:noGrow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        mainTabPanel.addTab("General", generalPanel);
        generalPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        transportLabel = new JLabel();
        transportLabel.setText("Transport");
        CellConstraints cc = new CellConstraints();
        generalPanel.add(transportLabel, cc.xy(1, 3));
        serverLabel = new JLabel();
        serverLabel.setText("Server");
        generalPanel.add(serverLabel, cc.xy(1, 5));
        serverField = new JTextField();
        generalPanel.add(serverField, cc.xyw(3, 5, 8, CellConstraints.FILL, CellConstraints.DEFAULT));
        transportComboBox = new JComboBox();
        generalPanel.add(transportComboBox, cc.xyw(3, 3, 8));
        clientIdLabel = new JLabel();
        clientIdLabel.setText("ClientId");
        generalPanel.add(clientIdLabel, cc.xy(1, 7));
        clientIdField = new JTextField();
        generalPanel.add(clientIdField, cc.xyw(3, 7, 8, CellConstraints.FILL, CellConstraints.DEFAULT));
        portLabel = new JLabel();
        portLabel.setText("Port");
        generalPanel.add(portLabel, cc.xy(12, 5));
        portField = new JSpinner();
        generalPanel.add(portField, cc.xy(14, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        versionLabel = new JLabel();
        versionLabel.setText("Version");
        generalPanel.add(versionLabel, cc.xy(12, 3));
        versionComboBox = new JComboBox();
        generalPanel.add(versionComboBox, cc.xy(14, 3));
        nameLabel = new JLabel();
        nameLabel.setText("Name");
        generalPanel.add(nameLabel, cc.xy(1, 1));
        nameField = new JTextField();
        generalPanel.add(nameField, cc.xyw(3, 1, 12, CellConstraints.FILL, CellConstraints.DEFAULT));
        randomClientIdCheckBox = new JCheckBox();
        randomClientIdCheckBox.setText("Random ClientId");
        generalPanel.add(randomClientIdCheckBox, cc.xyw(12, 7, 3));
        usernameLabel = new JLabel();
        usernameLabel.setText("Username");
        generalPanel.add(usernameLabel, cc.xy(1, 9));
        usernameField = new JTextField();
        generalPanel.add(usernameField, cc.xyw(3, 9, 8, CellConstraints.FILL, CellConstraints.DEFAULT));
        passwordLabel = new JLabel();
        passwordLabel.setText("Password");
        generalPanel.add(passwordLabel, cc.xy(1, 11));
        passwordField = new JPasswordField();
        generalPanel.add(passwordField, cc.xyw(3, 11, 8, CellConstraints.FILL, CellConstraints.DEFAULT));
        connectionTimeoutLabel = new JLabel();
        connectionTimeoutLabel.setText("Connect Timeout");
        generalPanel.add(connectionTimeoutLabel, cc.xy(1, 13));
        connectTimeoutField = new JSpinner();
        generalPanel.add(connectTimeoutField, cc.xyw(3, 13, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        keepAliveIntervalLabel = new JLabel();
        keepAliveIntervalLabel.setText("Keep Alive Interval");
        generalPanel.add(keepAliveIntervalLabel, cc.xyw(10, 13, 3));
        keepAliveIntervalField = new JSpinner();
        generalPanel.add(keepAliveIntervalField, cc.xy(14, 13, CellConstraints.FILL, CellConstraints.DEFAULT));
        reconnectIntervalLabel = new JLabel();
        reconnectIntervalLabel.setText("Reconnect Interval");
        generalPanel.add(reconnectIntervalLabel, cc.xyw(10, 15, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        reconnectIntervalField = new JSpinner();
        generalPanel.add(reconnectIntervalField, cc.xy(14, 15, CellConstraints.FILL, CellConstraints.DEFAULT));
        autoReconnectCheckBox = new JCheckBox();
        autoReconnectCheckBox.setSelected(true);
        autoReconnectCheckBox.setText("Auto Reconnect");
        generalPanel.add(autoReconnectCheckBox, cc.xyw(3, 15, 3));
        cleanSessionCheckBox = new JCheckBox();
        cleanSessionCheckBox.setSelected(true);
        cleanSessionCheckBox.setText("Clean Session");
        generalPanel.add(cleanSessionCheckBox, cc.xyw(3, 17, 3));
        mqtt5Panel = new JPanel();
        mqtt5Panel.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:160px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        mainTabPanel.addTab("MQTT5 Options", mqtt5Panel);
        mqtt5Panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        sessionExpiryIntervalLabel = new JLabel();
        sessionExpiryIntervalLabel.setText("Session Expiry Interval");
        mqtt5Panel.add(sessionExpiryIntervalLabel, cc.xy(1, 3));
        maximumPacketSizeLabel = new JLabel();
        maximumPacketSizeLabel.setText("Maximum Packet Size");
        mqtt5Panel.add(maximumPacketSizeLabel, cc.xy(1, 5));
        receiveMaximumLabel = new JLabel();
        receiveMaximumLabel.setText("Receive Maximum");
        mqtt5Panel.add(receiveMaximumLabel, cc.xy(5, 3));
        cleanStartCheckBox = new JCheckBox();
        cleanStartCheckBox.setSelected(true);
        cleanStartCheckBox.setText("Clean Start");
        mqtt5Panel.add(cleanStartCheckBox, cc.xy(3, 1));
        requestResponseInfoCheckBox = new JCheckBox();
        requestResponseInfoCheckBox.setText("Request Response Info");
        mqtt5Panel.add(requestResponseInfoCheckBox, cc.xy(3, 7));
        requestProblemInfoCheckBox = new JCheckBox();
        requestProblemInfoCheckBox.setText("Request Problem Info");
        mqtt5Panel.add(requestProblemInfoCheckBox, cc.xy(7, 7));
        topicAliasMaximumLabel = new JLabel();
        topicAliasMaximumLabel.setText("Topic alias Maximum");
        mqtt5Panel.add(topicAliasMaximumLabel, cc.xy(5, 5));
        maximumPacketSizeField = new JTextField();
        maximumPacketSizeField.setHorizontalAlignment(4);
        mqtt5Panel.add(maximumPacketSizeField, cc.xy(3, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        sessionExpiryIntervalField = new JTextField();
        sessionExpiryIntervalField.setHorizontalAlignment(4);
        mqtt5Panel.add(sessionExpiryIntervalField, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        receiveMaximumField = new JTextField();
        receiveMaximumField.setHorizontalAlignment(4);
        mqtt5Panel.add(receiveMaximumField, cc.xy(7, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        topicAliasMaximumField = new JTextField();
        topicAliasMaximumField.setHorizontalAlignment(4);
        mqtt5Panel.add(topicAliasMaximumField, cc.xy(7, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        userPropertiesLabel = new JLabel();
        userPropertiesLabel.setText("User Properties");
        userPropertiesLabel.setVerticalAlignment(1);
        mqtt5Panel.add(userPropertiesLabel, new CellConstraints(1, 9, 1, 1, CellConstraints.DEFAULT, CellConstraints.FILL, new Insets(5, 0, 0, 0)));
        final JScrollPane scrollPane1 = new JScrollPane();
        mqtt5Panel.add(scrollPane1, cc.xyw(3, 9, 5, CellConstraints.FILL, CellConstraints.FILL));
        userPropertiesTable = new JTable();
        scrollPane1.setViewportView(userPropertiesTable);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        mqtt5Panel.add(panel1, cc.xyw(3, 11, 5));
        removeUserPropertyButton = new JButton();
        removeUserPropertyButton.setText("Remove property");
        panel1.add(removeUserPropertyButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        addUserPropertyButton = new JButton();
        addUserPropertyButton.setText("Add property");
        panel1.add(addUserPropertyButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sslPanel = new JPanel();
        sslPanel.setLayout(new FormLayout("fill:170px:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:100px:noGrow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow"));
        mainTabPanel.addTab("TLS/SSL", sslPanel);
        sslPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        sslModeLabel = new JLabel();
        sslModeLabel.setText("TLS/SSL Mode");
        sslPanel.add(sslModeLabel, cc.xy(1, 3));
        sslProtocolComboBox = new JComboBox();
        sslProtocolComboBox.setEnabled(false);
        sslPanel.add(sslProtocolComboBox, cc.xy(7, 3));
        sslProtocolLabel = new JLabel();
        sslProtocolLabel.setText("Protocol");
        sslPanel.add(sslProtocolLabel, cc.xy(5, 3));
        sslModeComboBox = new JComboBox();
        sslModeComboBox.setEnabled(false);
        sslPanel.add(sslModeComboBox, cc.xy(3, 3));
        sslEnableCheckBox = new JCheckBox();
        sslEnableCheckBox.setText("Enable TLS/SSL");
        sslPanel.add(sslEnableCheckBox, cc.xy(3, 1));
        sslFieldsPanel = new JPanel();
        sslFieldsPanel.setLayout(new BorderLayout(0, 0));
        sslPanel.add(sslFieldsPanel, cc.xyw(1, 5, 7, CellConstraints.DEFAULT, CellConstraints.FILL));
        willPanel = new JPanel();
        willPanel.setLayout(new GridLayoutManager(6, 5, new Insets(0, 0, 0, 0), -1, -1));
        mainTabPanel.addTab("Last Will", willPanel);
        willPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final Spacer spacer3 = new Spacer();
        willPanel.add(spacer3, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        willPanel.add(spacer4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        willEnableCheckBox = new JCheckBox();
        willEnableCheckBox.setText("Enable Last Will Message");
        willPanel.add(willEnableCheckBox, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        topicLabel = new JLabel();
        topicLabel.setText("Topic");
        willPanel.add(topicLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        topicField = new JTextField();
        topicField.setEnabled(false);
        willPanel.add(topicField, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        qosLabel = new JLabel();
        qosLabel.setText("QoS");
        willPanel.add(qosLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        qosComboBox = new JComboBox();
        qosComboBox.setEnabled(false);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("0");
        defaultComboBoxModel1.addElement("1");
        defaultComboBoxModel1.addElement("2");
        qosComboBox.setModel(defaultComboBoxModel1);
        willPanel.add(qosComboBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        retainedCheckBox = new JCheckBox();
        retainedCheckBox.setEnabled(false);
        retainedCheckBox.setText("Retained");
        willPanel.add(retainedCheckBox, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editorPanel = new JPanel();
        editorPanel.setLayout(new BorderLayout(0, 0));
        willPanel.add(editorPanel, new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 140), new Dimension(24, 140), null, 0, false));
        willPayloadFormatLabel = new JLabel();
        willPayloadFormatLabel.setText("PayloadFormat");
        willPanel.add(willPayloadFormatLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        willPayloadFormatComboBox = new JComboBox();
        willPayloadFormatComboBox.setEnabled(false);
        willPanel.add(willPayloadFormatComboBox, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        payloadLabel = new JLabel();
        payloadLabel.setText("Payload");
        payloadLabel.setVerticalAlignment(1);
        payloadLabel.setVerticalTextPosition(0);
        willPanel.add(payloadLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 0), null, null, 0, false));
        otherPanel = new JPanel();
        otherPanel.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:130px:noGrow,left:4dlu:noGrow,fill:max(d;4px):grow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        mainTabPanel.addTab("Other", otherPanel);
        otherPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        maxMessagesStoredLabel = new JLabel();
        maxMessagesStoredLabel.setText("Max Messages Stored");
        otherPanel.add(maxMessagesStoredLabel, cc.xy(1, 1));
        defaultPayloadFormatLabel = new JLabel();
        defaultPayloadFormatLabel.setText("Message Default Format");
        otherPanel.add(defaultPayloadFormatLabel, cc.xy(1, 3));
        maxMessagesStoredField = new JSpinner();
        otherPanel.add(maxMessagesStoredField, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        defaultPayloadFormatComboBox = new JComboBox();
        otherPanel.add(defaultPayloadFormatComboBox, cc.xy(3, 3));
        clearUnsubMessageCheckBox = new JCheckBox();
        clearUnsubMessageCheckBox.setText("Clear messages after unsubscribing");
        otherPanel.add(clearUnsubMessageCheckBox, cc.xyw(1, 5, 5));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
