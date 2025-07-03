package com.mqttinsight.ui.form;

import cn.hutool.core.util.StrUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.exception.VerificationException;
import com.mqttinsight.mqtt.FavoriteSubscription;
import com.mqttinsight.ui.component.model.PayloadFormatComboBoxModel;
import com.mqttinsight.ui.component.model.SubscriptionTableModel;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.TopicUtil;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Slf4j
public class SubscriptionManagerForm extends JDialog {
    private static final DefaultCellEditor QOS_EDITOR = new DefaultCellEditor(new JComboBox<>(new Integer[]{0, 1, 2}));
    private static final DefaultCellEditor FORMAT_EDITOR = new DefaultCellEditor(new JComboBox<>(new PayloadFormatComboBoxModel(true, false)));

    private final MqttInstance mqttInstance;
    private JPanel contentPane;
    private JButton closeButton;
    private JPanel bottomPanel;
    private JPanel buttonPanel;
    private JPanel tablePanel;
    private JScrollPane scrollPanel;
    private JButton addButton;
    private JButton removeButton;
    private JXTable subscriptionsTable;
    private SubscriptionTableModel subscriptionTableModel;

    public static void open(MqttInstance mqttInstance) {
        JDialog dialog = new SubscriptionManagerForm(MqttInsightApplication.frame, mqttInstance);
        dialog.setMinimumSize(new Dimension(650, 400));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(MqttInsightApplication.frame);
        dialog.setVisible(true);
    }

    public SubscriptionManagerForm(Frame owner, MqttInstance mqttInstance) {
        super(owner);
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(closeButton);

        this.mqttInstance = mqttInstance;
        closeButton.addActionListener(this::close);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close(e);
            }
        });

        setTitle(LangUtil.getString("SubscriptionsManagement"));

        initComponents();
    }

    private void initComponents() {
        LangUtil.buttonText(closeButton, "&Close");
        LangUtil.buttonText(addButton, "&New");
        LangUtil.buttonText(removeButton, "&Remove");

        subscriptionTableModel = new SubscriptionTableModel(mqttInstance.getProperties().getFavoriteSubscriptions());
        subscriptionsTable.setModel(subscriptionTableModel);
        subscriptionsTable.setSortable(false);
        subscriptionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        subscriptionsTable.setRowHeight(25);

        subscriptionsTable.getColumnExt(0).setPreferredWidth(400);
        subscriptionsTable.getColumnExt(0).setCellEditor(new DefaultCellEditor(new JTextField()));
        subscriptionsTable.getColumnExt(1).setPreferredWidth(50);
        subscriptionsTable.getColumnExt(1).setCellEditor(QOS_EDITOR);
        subscriptionsTable.getColumnExt(2).setPreferredWidth(80);
        subscriptionsTable.getColumnExt(2).setCellEditor(FORMAT_EDITOR);

        ListSelectionModel cellSelectionModel = subscriptionsTable.getSelectionModel();
        cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cellSelectionModel.addListSelectionListener(e -> {
            int selectedRow = subscriptionsTable.getSelectedRow();
            removeButton.setEnabled(selectedRow >= 0);
        });

        // Buttons
        addButton.setIcon(Icons.ADD);
        addButton.addActionListener(this::addAction);
        removeButton.setIcon(Icons.REMOVE);
        removeButton.setEnabled(false);
        removeButton.addActionListener(this::removeAction);
    }

    private void addAction(ActionEvent e) {
        int modelRow = subscriptionTableModel.appendRow();
        int tableRow = subscriptionsTable.convertRowIndexToView(modelRow);
        subscriptionsTable.scrollCellToVisible(tableRow, 0);
        subscriptionsTable.editCellAt(tableRow, 0);
    }

    private void removeAction(ActionEvent e) {
        int selectedRow = subscriptionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRowIndex = subscriptionsTable.convertRowIndexToModel(selectedRow);
            FavoriteSubscription selItem = subscriptionTableModel.getRow(modelRowIndex);
            subscriptionTableModel.removeRow(modelRowIndex);
            mqttInstance.getProperties().getFavoriteSubscriptions().remove(selItem);
        }
    }

    private void close(AWTEvent evt) {
        for (int i = 0; i < subscriptionTableModel.getSubscriptions().size(); i++) {
            FavoriteSubscription subscription = subscriptionTableModel.getRow(i);
            int tableRow = subscriptionsTable.convertRowIndexToView(i);
            if (StrUtil.isBlank(subscription.getTopic())) {
                Utils.Toast.error(LangUtil.getString("SubscriptionTopicBlank"));
                subscriptionsTable.scrollCellToVisible(tableRow, 0);
                subscriptionsTable.editCellAt(tableRow, 0);
                return;
            }
            try {
                TopicUtil.validate(subscription.getTopic());
            } catch (VerificationException e) {
                Utils.Toast.error(e.getMessage());
                subscriptionsTable.scrollCellToVisible(tableRow, 0);
                subscriptionsTable.editCellAt(tableRow, 0);
                return;
            }
        }
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
        subscriptionsTable = new JXTable();
        subscriptionsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        scrollPanel.setViewportView(subscriptionsTable);
        final JToolBar toolBar1 = new JToolBar();
        contentPane.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        addButton = new JButton();
        addButton.setText("Add");
        toolBar1.add(addButton);
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
