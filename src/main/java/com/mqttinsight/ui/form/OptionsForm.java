package com.mqttinsight.ui.form;

import cn.hutool.core.date.DatePattern;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.config.Languages;
import com.mqttinsight.config.Themes;
import com.mqttinsight.ui.component.model.MessageViewMode;
import com.mqttinsight.ui.component.renderer.TextableListRenderer;
import com.mqttinsight.util.Const;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import org.jdesktop.swingx.JXTitledSeparator;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ptma
 */
public class OptionsForm extends JDialog {

    private boolean optionsChanged;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel bottomPanel;
    private JPanel buttonPanel;
    private JPanel mainPanel;
    private JSpinner fontSizeField;
    private JComboBox<String> fontComboBox;
    private JComboBox<String> themeComboBox;
    private JComboBox<String> languageComboBox;
    private JLabel fontLabel;
    private JLabel fontSizeLabel;
    private JXTitledSeparator messageEditorSeparator;
    private JXTitledSeparator uiSeparator;
    private JLabel themeLabel;
    private JLabel languageLabel;
    private JComboBox<String> messageViewComboBox;
    private JXTitledSeparator messageViewSeparator;
    private JLabel defaultViewLabel;
    private JLabel maxMessageRowsLabel;
    private JSpinner maxMessageRowsField;
    private JComboBox<String> timeFormatComboBox;
    private JLabel timeFormatLabel;

    public static void open() {
        JDialog dialog = new OptionsForm(MqttInsightApplication.frame);
        dialog.setMinimumSize(new Dimension(450, 380));
        dialog.setLocationRelativeTo(MqttInsightApplication.frame);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setVisible(true);
    }

    private OptionsForm(Frame owner) {
        super(owner);
        $$$setupUI$$$();
        setContentPane(contentPane);
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

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        applyLanguage();
    }

    private void applyLanguage() {
        this.setTitle(LangUtil.getString("Options"));
        uiSeparator.setTitle(LangUtil.getString("UserInterface"));
        languageLabel.setText(LangUtil.getString("Language"));
        themeLabel.setText(LangUtil.getString("Theme"));

        messageEditorSeparator.setTitle(LangUtil.getString("MessageEditor"));
        fontLabel.setText(LangUtil.getString("Font"));
        fontSizeLabel.setText(LangUtil.getString("FontSize"));

        messageViewSeparator.setTitle(LangUtil.getString("MessageView"));
        defaultViewLabel.setText(LangUtil.getString("DefaultView"));

        maxMessageRowsLabel.setText(LangUtil.getString("MaxMessageRows"));
        maxMessageRowsLabel.setToolTipText(LangUtil.getString("MaxMessageRowsTip"));

        timeFormatLabel.setText(LangUtil.getString("TimeFormat"));

        LangUtil.buttonText(buttonOK, "&Ok");
        LangUtil.buttonText(buttonCancel, "&Cancel");
    }

    private void initComponents() {
        themeComboBox.setModel(new EnumComboBoxModel(Themes.class));
        themeComboBox.setRenderer(new TextableListRenderer());
        themeComboBox.addActionListener(e -> {
            if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
                optionsChanged = true;
            }
        });
        themeComboBox.setSelectedItem(Themes.of(Configuration.instance().getString(ConfKeys.THEME, Themes.LIGHT.name())));

        languageComboBox.setModel(new EnumComboBoxModel(Languages.class));
        languageComboBox.setRenderer(new TextableListRenderer());
        languageComboBox.addActionListener(e -> {
            if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
                optionsChanged = true;
            }
        });
        languageComboBox.setSelectedItem(Languages.of(Configuration.instance().getString(ConfKeys.LANGUAGE, Locale.getDefault().toLanguageTag())));

        Set<String> monospaceFonts = loadMonospaceFonts();
        for (String fontName : monospaceFonts) {
            fontComboBox.addItem(fontName);
        }
        fontComboBox.setSelectedItem(Configuration.instance().getString(ConfKeys.FONT_NAME, Const.EDITOR_FONT_NAME));
        fontComboBox.addActionListener(e -> {
            if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
                optionsChanged = true;
            }
        });

        int fontSize = Configuration.instance().getInt(ConfKeys.FONT_SIZE, Const.EDITOR_FONT_SIZE);
        fontSizeField.setModel(new SpinnerNumberModel(fontSize, 9, 24, 1));
        fontSizeField.setEditor(new JSpinner.NumberEditor(fontSizeField, "####"));
        fontSizeField.addChangeListener(e -> optionsChanged = true);

        messageViewComboBox.setModel(new EnumComboBoxModel(MessageViewMode.class));
        messageViewComboBox.setRenderer(new TextableListRenderer());
        messageViewComboBox.addActionListener(e -> {
            if ("comboBoxChanged".equalsIgnoreCase(e.getActionCommand())) {
                optionsChanged = true;
            }
        });
        messageViewComboBox.setSelectedItem(MessageViewMode.of(Configuration.instance().getString(ConfKeys.MESSAGE_VIEW, MessageViewMode.TABLE.toString())));

        maxMessageRowsLabel.setIcon(Icons.TIPS);
        maxMessageRowsLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        int maxMessageRows = Configuration.instance().getInt(ConfKeys.MAX_MESSAGE_ROWS, 0);
        maxMessageRowsField.setModel(new SpinnerNumberModel(maxMessageRows, 0, 15, 1));
        maxMessageRowsField.setEditor(new JSpinner.NumberEditor(maxMessageRowsField, "####"));
        maxMessageRowsField.addChangeListener(e -> optionsChanged = true);

        timeFormatComboBox.setSelectedItem(Configuration.instance().getString(ConfKeys.TIME_FORMAT, DatePattern.NORM_DATETIME_MS_PATTERN));
    }

    private Set<String> loadMonospaceFonts() {
        Set<String> monospacedFonts = new HashSet<>(Arrays.asList(
            "Consolas", "Courier New", "DejaVu Sans Mono", "Droid Sans Mono",
            "Inconsolata", "JetBrains Mono", "Lucida Console", "Monaco",
            "Roboto Mono", "Source Code Pro", "Ubuntu Mono"));
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        List<String> availableFontFamilys = Arrays.asList(graphicsEnvironment.getAvailableFontFamilyNames());
        return monospacedFonts.stream().filter(availableFontFamilys::contains).collect(Collectors.toSet());
    }

    private void onOK() {
        if (optionsChanged) {
            Configuration.instance().set(ConfKeys.LANGUAGE, languageComboBox.getSelectedItem());
            Configuration.instance().set(ConfKeys.THEME, themeComboBox.getSelectedItem());
            Configuration.instance().set(ConfKeys.FONT_NAME, fontComboBox.getSelectedItem());
            Configuration.instance().set(ConfKeys.FONT_SIZE, fontSizeField.getValue());
            Configuration.instance().set(ConfKeys.MESSAGE_VIEW, messageViewComboBox.getSelectedItem());
            Configuration.instance().set(ConfKeys.MAX_MESSAGE_ROWS, maxMessageRowsField.getValue());
            Configuration.instance().set(ConfKeys.TIME_FORMAT, timeFormatComboBox.getSelectedItem());
            Configuration.instance().save(true);
        }
        dispose();
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
        mainPanel = new JPanel();
        mainPanel.setLayout(new FormLayout("fill:10dlu:noGrow,left:4dlu:noGrow,fill:d:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:20dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:noGrow,left:4dlu:noGrow,fill:max(d;4px):grow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:10dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:10dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        contentPane.add(mainPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        themeLabel = new JLabel();
        themeLabel.setText("Theme");
        CellConstraints cc = new CellConstraints();
        mainPanel.add(themeLabel, cc.xy(3, 3));
        languageLabel = new JLabel();
        languageLabel.setText("Language");
        mainPanel.add(languageLabel, cc.xy(3, 5));
        fontLabel = new JLabel();
        fontLabel.setText("Font");
        mainPanel.add(fontLabel, cc.xy(3, 9));
        fontSizeLabel = new JLabel();
        fontSizeLabel.setText("Font Size");
        mainPanel.add(fontSizeLabel, cc.xy(7, 9));
        fontSizeField = new JSpinner();
        mainPanel.add(fontSizeField, cc.xy(9, 9, CellConstraints.FILL, CellConstraints.DEFAULT));
        fontComboBox = new JComboBox();
        mainPanel.add(fontComboBox, cc.xy(5, 9));
        messageEditorSeparator = new JXTitledSeparator();
        messageEditorSeparator.setTitle("Message Editor");
        mainPanel.add(messageEditorSeparator, cc.xyw(1, 7, 11));
        uiSeparator = new JXTitledSeparator();
        uiSeparator.setTitle("User Interface");
        mainPanel.add(uiSeparator, cc.xyw(1, 1, 11));
        themeComboBox = new JComboBox();
        mainPanel.add(themeComboBox, cc.xyw(5, 3, 5));
        languageComboBox = new JComboBox();
        mainPanel.add(languageComboBox, cc.xyw(5, 5, 5));
        messageViewSeparator = new JXTitledSeparator();
        messageViewSeparator.setTitle("Message View");
        mainPanel.add(messageViewSeparator, cc.xyw(1, 11, 11));
        defaultViewLabel = new JLabel();
        defaultViewLabel.setText("Default View");
        mainPanel.add(defaultViewLabel, cc.xy(3, 13));
        messageViewComboBox = new JComboBox();
        mainPanel.add(messageViewComboBox, cc.xy(5, 13));
        maxMessageRowsLabel = new JLabel();
        maxMessageRowsLabel.setText("Max Message Rows");
        mainPanel.add(maxMessageRowsLabel, cc.xy(7, 13));
        maxMessageRowsField = new JSpinner();
        mainPanel.add(maxMessageRowsField, cc.xy(9, 13, CellConstraints.FILL, CellConstraints.DEFAULT));
        timeFormatLabel = new JLabel();
        timeFormatLabel.setText("Time Format");
        mainPanel.add(timeFormatLabel, cc.xy(3, 15));
        timeFormatComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("yyyy-MM-dd HH:mm:ss.SSS");
        defaultComboBoxModel1.addElement("yyyy-MM-dd HH:mm:ss");
        defaultComboBoxModel1.addElement("yyyy/MM/dd HH:mm:ss.SSS");
        defaultComboBoxModel1.addElement("yyyy/MM/dd HH:mm:ss");
        defaultComboBoxModel1.addElement("HH:mm:ss.SSS");
        defaultComboBoxModel1.addElement("HH:mm:ss");
        timeFormatComboBox.setModel(defaultComboBoxModel1);
        mainPanel.add(timeFormatComboBox, cc.xyw(5, 15, 5));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
