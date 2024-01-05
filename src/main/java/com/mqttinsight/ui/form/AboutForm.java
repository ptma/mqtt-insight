package com.mqttinsight.ui.form;

import cn.hutool.core.io.resource.ResourceUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.util.Const;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Hashtable;

/**
 * @author jinjq
 */
public class AboutForm extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel logoLabel;
    private JLabel nameLabel;
    private JLabel openSourceLabel;
    private JLabel githubLinkLabel;
    private JEditorPane openSourceEditor;
    private JLabel giteeLinkLabel;
    private JLabel versionLabel;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    private JScrollPane openSourceScrollPanel;

    public static void open() {
        AboutForm aboutForm = new AboutForm();
        aboutForm.setMinimumSize(new Dimension(400, 380));
        aboutForm.setResizable(false);
        aboutForm.setLocationRelativeTo(MqttInsightApplication.frame);
        aboutForm.setVisible(true);
    }

    private AboutForm() {
        setTitle(LangUtil.getString("About"));
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        topPanel.setBackground(UIManager.getColor("Panel.background").brighter());

        logoLabel.setIcon(Icons.LOGO);
        logoLabel.setText("");
        nameLabel.setText(Const.APP_NAME);
        nameLabel.putClientProperty("FlatLaf.styleClass", "h2");
        String version = Utils.getSingleValueByJsonPath("$.version", ResourceUtil.readUtf8Str("version.json"));
        versionLabel.setText("v" + version);
        nameLabel.putClientProperty("FlatLaf.styleClass", "h3");
        String githubLink = "https://github.com/ptma/mqtt-insight";
        githubLinkLabel.setText("<html><a href=\"#\">" + githubLink + "</a></html>");
        githubLinkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        githubLinkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(githubLink));
                } catch (Exception ex) {
                    // ignore
                }
            }
        });
        String giteeLink = "https://gitee.com/ptma/mqtt-insight";
        giteeLinkLabel.setText("<html><a href=\"#\">" + giteeLink + "</a></html>");
        giteeLinkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        giteeLinkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(giteeLink));
                } catch (Exception ex) {
                    // ignore
                }
            }
        });

        openSourceLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        openSourceLabel.setText(LangUtil.getString("AboutOpenSource"));

        openSourceEditor.setContentType("text/html");
        openSourceEditor.setEditable(false);
        openSourceEditor.setText(""
            + "<html>"
            + "<a href=\"https://www.formdev.com/flatlaf/\">Flatlaf</a><br>"
            + "<a href=\"https://en.wikipedia.org/wiki/SwingLabs\">swingx</a><br>"
            + "<a href=\"http://www.miglayout.com/\">MigLayout</a><br>"
            + "<a href=\"https://github.com/bobbylight/RSyntaxTextArea\">RSyntaxTextArea</a><br>"
            + "<a href=\"https://github.com/eclipse/paho.mqtt.java\">Eclipse Paho Java Client</a><br>"
            + "<a href=\"https://github.com/caoccao/Javet\">Javet</a><br>"
            + "<a href=\"https://github.com/dromara/hutool\">HuTool</a><br>"
            + "<a href=\"https://github.com/DJ-Raven/swing-toast-notifications\">Swing Toast Notifications</a><br>"
            + "<a href=\"https://github.com/json-path/JsonPath\">JsonPath</a><br>"
            + "<a href=\"https://github.com/timmolter/xchart\">XChart</a><br>"
            + "</html>"
        );

        openSourceEditor.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception e1) {
                    // ignore
                }
            }
        });
        openSourceEditor.setCaretPosition(0);

        LangUtil.buttonText(buttonOK, "&Ok");
        buttonOK.addActionListener(e -> onOK());
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    public static void main(String[] args) {
        AboutForm dialog = new AboutForm();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    static class ResourceImageCache extends Hashtable<String, Image> {
        public Image get(String key) {
            Image result = super.get(key);
            if (result == null) {
                result = Toolkit.getDefaultToolkit().createImage(AboutForm.class.getResource(key));
                put(key, result);
            }
            return result;
        }
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
        contentPane.setLayout(new BorderLayout(0, 0));
        topPanel = new JPanel();
        topPanel.setLayout(new FormLayout("fill:d:noGrow,left:10dlu:noGrow,fill:max(d;4px):grow", "center:d:noGrow,top:2dlu:noGrow,center:max(d;4px):noGrow,top:2dlu:noGrow,center:max(d;4px):noGrow,top:2dlu:noGrow,center:max(d;4px):noGrow"));
        topPanel.setBackground(new Color(-1));
        contentPane.add(topPanel, BorderLayout.NORTH);
        topPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        logoLabel = new JLabel();
        logoLabel.setHorizontalAlignment(0);
        logoLabel.setText("LOGO");
        CellConstraints cc = new CellConstraints();
        topPanel.add(logoLabel, cc.xywh(1, 1, 1, 7));
        nameLabel = new JLabel();
        nameLabel.setHorizontalAlignment(10);
        nameLabel.setHorizontalTextPosition(0);
        nameLabel.setText("Name");
        topPanel.add(nameLabel, cc.xy(3, 1));
        versionLabel = new JLabel();
        versionLabel.setText("Version");
        topPanel.add(versionLabel, cc.xy(3, 3));
        githubLinkLabel = new JLabel();
        githubLinkLabel.setHorizontalAlignment(10);
        githubLinkLabel.setText("Github");
        topPanel.add(githubLinkLabel, cc.xy(3, 5));
        giteeLinkLabel = new JLabel();
        giteeLinkLabel.setHorizontalAlignment(10);
        giteeLinkLabel.setText("Gitee");
        topPanel.add(giteeLinkLabel, cc.xy(3, 7));
        centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayoutManager(2, 1, new Insets(10, 20, 10, 20), -1, -1));
        contentPane.add(centerPanel, BorderLayout.CENTER);
        openSourceLabel = new JLabel();
        openSourceLabel.setText("AboutOpenSource");
        centerPanel.add(openSourceLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openSourceScrollPanel = new JScrollPane();
        openSourceScrollPanel.setVerticalScrollBarPolicy(22);
        centerPanel.add(openSourceScrollPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 3, false));
        openSourceScrollPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        openSourceEditor = new JEditorPane();
        openSourceScrollPanel.setViewportView(openSourceEditor);
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final Spacer spacer1 = new Spacer();
        bottomPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        bottomPanel.add(panel1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel1.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
