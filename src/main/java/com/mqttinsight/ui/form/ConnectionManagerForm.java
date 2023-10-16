package com.mqttinsight.ui.form;

import cn.hutool.core.util.StrUtil;
import com.formdev.flatlaf.extras.components.FlatPopupMenu;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.config.ConnectionNode;
import com.mqttinsight.ui.component.model.ConnectionTreeTableModel;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;

/**
 * @author ptma
 */
@Slf4j
public class ConnectionManagerForm extends JDialog {

    private JPanel contentPanel;
    private JPanel tablePanel;
    private JButton btnClose;
    private JButton btnConnect;
    private JPanel bottomPanel;
    private JPanel bottomButtonPanel;
    private JScrollPane scrollPanel;

    private JButton btnNewGroup;
    private JButton btnNewConnection;
    private JButton btnEdit;
    private JButton btnDelete;

    private FlatPopupMenu popupMenu;
    private JMenuItem menuConnect;
    private JMenuItem menuNewGroup;
    private JMenuItem menuNewConnection;
    private JMenuItem menuDuplicate;
    private JMenuItem menuEdit;
    private JMenuItem menuDelete;

    private ConnectionTreeTableModel treeTableModel;
    private JXTreeTable treeTable;

    private boolean connectionsModified = false;

    public static void open() {
        JDialog dialog = new ConnectionManagerForm(MqttInsightApplication.frame);
        dialog.setMinimumSize(new Dimension(650, 400));
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(MqttInsightApplication.frame);
        dialog.setVisible(true);
    }

    private ConnectionManagerForm(Frame owner) {
        super(owner);
        $$$setupUI$$$();
        this.setModal(true);
        this.setContentPane(contentPanel);
        this.setResizable(false);
        this.setTitle(LangUtil.getString("ConnectionManagement"));

        initComponents();
        initPopupMenu();
        initTreeTable();
    }

    private void closeWindow() {
        if (connectionsModified) {
            Configuration.instance().getConnections().clear();
            Configuration.instance().getConnections().addAll(treeTableModel.getConnectionNodes());
            Configuration.instance().save(true);
        }
        dispose();
    }

    /**
     * 新建分组
     */
    private void onNewGroupAction(boolean addToRoot) {
        ConnectionNode selectNode;
        TreePath selectedPath = treeTable.getTreeSelectionModel().getSelectionPath();
        if (selectedPath != null && !addToRoot) {
            selectNode = (ConnectionNode) selectedPath.getLastPathComponent();
            if (!selectNode.isGroup()) {
                return;
            }
        } else {
            selectNode = (ConnectionNode) treeTableModel.getRoot();
            selectedPath = new TreePath(selectNode);
        }

        final ConnectionNode parentNode = selectNode;
        final TreePath parentPath = selectedPath;
        GroupEditorForm.open(null, (addedNode) -> {
            treeTableModel.addChildNode(parentNode, addedNode);
            treeTable.expandPath(parentPath);
            TreePath childPath = parentPath.pathByAddingChild(addedNode);
            treeTable.expandPath(childPath);
            int row = treeTable.getRowForPath(childPath);
            treeTable.setRowSelectionInterval(row, row);
            connectionsModified = true;
        });
    }

    private void onNewConnectionAction() {
        TreePath selectedPath = treeTable.getTreeSelectionModel().getSelectionPath();
        if (selectedPath == null) {
            return;
        }
        ConnectionNode selectNode = (ConnectionNode) selectedPath.getLastPathComponent();
        if (!selectNode.isGroup()) {
            return;
        }

        final ConnectionNode parentNode = selectNode;
        ConnectionEditorForm.open(null, (addedNode) -> {
            treeTableModel.addChildNode(parentNode, addedNode);
            treeTable.expandPath(selectedPath);
            TreePath childPath = selectedPath.pathByAddingChild(addedNode);
            treeTable.expandPath(childPath);
            int row = treeTable.getRowForPath(childPath);
            treeTable.setRowSelectionInterval(row, row);
            connectionsModified = true;
        });
    }

    private void onEditAction() {
        TreePath selectedPath = treeTable.getTreeSelectionModel().getSelectionPath();
        if (selectedPath != null) {
            final ConnectionNode selectedNode = (ConnectionNode) selectedPath.getLastPathComponent();
            if (selectedNode.isGroup()) {
                GroupEditorForm.open(selectedNode, (editedNode) -> {
                    connectionsModified = true;
                    treeTableModel.fireChildChanged(editedNode);
                });
            } else {
                ConnectionEditorForm.open(selectedNode, (editedNode) -> {
                    connectionsModified = true;
                    treeTableModel.fireChildChanged(editedNode);
                });
            }
        }
    }

    private void onDeleteAction() {
        TreePath selectedPath = treeTable.getTreeSelectionModel().getSelectionPath();
        if (selectedPath != null) {
            final ConnectionNode selectedNode = (ConnectionNode) selectedPath.getLastPathComponent();
            if (selectedNode != null) {
                int opt = Utils.Message.confirm(String.format(LangUtil.getString("ConnectionDeleteConfirm"), selectedNode.getName()));
                if (JOptionPane.YES_OPTION == opt) {
                    connectionsModified = true;
                    treeTableModel.removeById(selectedNode.getId());
                }
            }
        }
    }

    private void onConnectAction() {
        TreePath selectedPath = treeTable.getTreeSelectionModel().getSelectionPath();
        if (selectedPath != null) {
            final ConnectionNode selectedNode = (ConnectionNode) selectedPath.getLastPathComponent();
            if (selectedNode != null && !selectedNode.isGroup()) {
                MainWindowForm.getInstance().addTabActionPerformed(selectedNode.getProperties(), this::closeWindow);
            }
        }
    }

    private void onDuplicateAction() {
        TreePath selectedPath = treeTable.getTreeSelectionModel().getSelectionPath();
        if (selectedPath != null) {
            final ConnectionNode selectedNode = (ConnectionNode) selectedPath.getLastPathComponent();
            if (selectedNode != null && !selectedNode.isGroup()) {
                try {
                    ConnectionNode duplicateNode = new ConnectionNode(selectedNode.getProperties().clone());
                    String duplicateName = duplicateNode.getName() + " (Copy)";
                    int duplicateIndex = 0;
                    while (treeTableModel.hasName(duplicateName)) {
                        duplicateName = String.format("%s (Copy %d)", duplicateNode.getName(), ++duplicateIndex);
                    }
                    duplicateNode.setName(duplicateName);
                    treeTableModel.addChildNode(selectedNode.parentNode(), duplicateNode);
                } catch (CloneNotSupportedException e) {
                    Utils.Toast.warn(e.getMessage());
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private void initComponents() {
        btnNewGroup.setIcon(Icons.FOLDER);
        btnNewGroup.addActionListener(e -> onNewGroupAction(false));
        btnNewGroup.setToolTipText(LangUtil.getString("NewGroup"));

        btnNewConnection.setIcon(Icons.CONNECTION);
        btnNewConnection.addActionListener(e -> onNewConnectionAction());
        btnNewConnection.setToolTipText(LangUtil.getString("NewConnection"));

        btnEdit.setIcon(Icons.EDIT);
        btnEdit.addActionListener(e -> onEditAction());
        btnEdit.setToolTipText(LangUtil.getString("Edit"));
        btnDelete.setIcon(Icons.REMOVE);
        btnDelete.addActionListener(e -> onDeleteAction());
        btnDelete.setToolTipText(LangUtil.getString("Delete"));

        btnClose.addActionListener(e -> closeWindow());
        LangUtil.buttonText(btnClose, "&Close");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWindow();
            }
        });
        btnConnect.setIcon(Icons.EXECUTE);
        LangUtil.buttonText(btnConnect, "&OpenConnection");
        btnConnect.addActionListener(e -> onConnectAction());

        getRootPane().setDefaultButton(btnConnect);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeWindow();
            }
        });
    }

    private void changeButtonsEnableStatus(ConnectionNode node) {
        btnNewGroup.setEnabled(node == null || node.isGroup());
        btnNewConnection.setEnabled(node != null && node.isGroup());
        btnEdit.setEnabled(node != null);
        btnDelete.setEnabled(node != null && node.getChildCount() == 0);
        btnConnect.setEnabled(node != null && !node.isGroup());
    }

    private void initPopupMenu() {
        popupMenu = new FlatPopupMenu();
        menuConnect = Utils.UI.createMenuItem(LangUtil.getString("&OpenConnection"), (e) -> onConnectAction());
        menuConnect.setIcon(Icons.EXECUTE);
        popupMenu.add(menuConnect);
        popupMenu.addSeparator();
        JMenuItem menuNewRootGroup = Utils.UI.createMenuItem(LangUtil.getString("New&RootGroup"), (e) -> onNewGroupAction(true));
        menuNewRootGroup.setIcon(Icons.FOLDER);
        popupMenu.add(menuNewRootGroup);
        menuNewGroup = Utils.UI.createMenuItem(LangUtil.getString("New&Group"), (e) -> onNewGroupAction(false));
        menuNewGroup.setIcon(Icons.FOLDER);
        popupMenu.add(menuNewGroup);
        menuNewConnection = Utils.UI.createMenuItem(LangUtil.getString("New&Connection"), (e) -> onNewConnectionAction());
        menuNewConnection.setIcon(Icons.CONNECTION);
        popupMenu.add(menuNewConnection);
        menuDuplicate = Utils.UI.createMenuItem(LangUtil.getString("Du&plicate"), (e) -> onDuplicateAction());
        popupMenu.add(menuDuplicate);
        popupMenu.addSeparator();
        menuEdit = Utils.UI.createMenuItem(LangUtil.getString("&Edit"), (e) -> onEditAction());
        menuEdit.setIcon(Icons.EDIT);
        popupMenu.add(menuEdit);
        menuDelete = Utils.UI.createMenuItem(LangUtil.getString("&Delete"), (e) -> onDeleteAction());
        menuDelete.setIcon(Icons.REMOVE);
        popupMenu.add(menuDelete);
    }

    private void changeMenuItemsEnableStatus(ConnectionNode node) {
        menuConnect.setEnabled(node != null && !node.isGroup());
        menuNewGroup.setEnabled(node != null && node.isGroup());
        menuNewConnection.setEnabled(node != null && node.isGroup());
        menuDuplicate.setEnabled(node != null && !node.isGroup());
        menuEdit.setEnabled(node != null);
        menuDelete.setEnabled(node != null && node.getChildCount() == 0);
    }

    private void initTreeTable() {
        treeTableModel = ConnectionTreeTableModel.newInstance();
        treeTable = new JXTreeTable(treeTableModel);
        treeTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        treeTable.getColumnModel().getColumn(1).setMaxWidth(400);
        treeTable.getColumnModel().getColumn(1).setMinWidth(200);
        treeTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        treeTable.getColumnModel().getColumn(2).setMaxWidth(100);
        treeTable.getColumnModel().getColumn(2).setMinWidth(50);
        treeTable.setOpenIcon(Icons.FOLDER_OPEN);
        treeTable.setClosedIcon(Icons.FOLDER);
        treeTable.setLeafIcon(Icons.CONNECTION);
        treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treeTable.setRowSelectionAllowed(true);
        treeTable.setColumnSelectionAllowed(false);
        treeTable.setRowHeight(25);
        treeTable.setShowHorizontalLines(true);
        scrollPanel.setViewportView(treeTable);

        treeTable.addTreeSelectionListener((e) -> {
            ConnectionNode node = (ConnectionNode) e.getPath().getLastPathComponent();
            if (node != null && node.isGroup()) {
                for (TreePath path : e.getPaths()) {
                    if (e.isAddedPath(path) && !treeTable.isExpanded(path)) {
                        treeTable.expandPath(path);
                    }
                }
            }
            changeButtonsEnableStatus(node);
            changeMenuItemsEnableStatus(node);
        });
        treeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                final int rowIndex = treeTable.rowAtPoint(e.getPoint());
                if (rowIndex < 0) {
                    return;
                }
                tableDoubleClickOrEnterKey();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    final int rowIndex = treeTable.rowAtPoint(e.getPoint());
                    if (rowIndex >= 0) {
                        treeTable.setRowSelectionInterval(rowIndex, rowIndex);
                    } else {
                        changeMenuItemsEnableStatus(null);
                    }
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        treeTable.registerKeyboardAction(e -> tableDoubleClickOrEnterKey(), KeyStroke.getKeyStroke("ENTER"), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        treeTableModel.addChildren(Configuration.instance().getConnections());
        treeTable.setDragEnabled(true);
        treeTable.setDropMode(DropMode.ON_OR_INSERT_ROWS);
        treeTable.setTransferHandler(new RowTransferHandler());
        treeTable.expandAll();
    }

    private void tableDoubleClickOrEnterKey() {
        TreePath selectedPath = treeTable.getTreeSelectionModel().getSelectionPath();
        if (selectedPath != null) {
            int rowIndex = treeTable.getSelectedRow();
            final ConnectionNode selectedNode = (ConnectionNode) selectedPath.getLastPathComponent();
            if (selectedNode != null) {
                if (selectedNode.isGroup()) {
                    if (treeTable.isExpanded(rowIndex)) {
                        treeTable.collapseRow(rowIndex);
                    } else {
                        treeTable.expandRow(rowIndex);
                    }
                } else {
                    onConnectAction();
                }
            }
        }
    }

    private class RowTransferHandler extends TransferHandler {
        @Override
        protected Transferable createTransferable(JComponent c) {
            JXTreeTable table = (JXTreeTable) c;
            TreePath path = table.getPathForRow(table.getSelectedRow());
            Object component = path.getLastPathComponent();
            if (component instanceof ConnectionNode) {
                ConnectionNode node = (ConnectionNode) component;
                return new StringSelection(node.getId());
            }
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            JTable.DropLocation dropLocation = (JTable.DropLocation) support.getDropLocation();
            TreePath path = treeTable.getPathForRow(dropLocation.getRow());
            Object component = path.getLastPathComponent();
            String sourceId = null;
            try {
                sourceId = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                // ignore
            }
            if (StrUtil.isNotEmpty(sourceId) && component instanceof ConnectionNode) {
                ConnectionNode destNode = (ConnectionNode) component;
                ConnectionNode sourceNode = treeTableModel.recursiveFindChildById(sourceId);
                if (destNode.equals(sourceNode)) {
                    return false;
                }
                if (dropLocation.isInsertRow()) {
                    return sourceNode.canMoveToFrontOf(destNode);
                } else {
                    return sourceNode.canMoveOverTo(destNode);
                }
            }
            return false;
        }

        @Override
        public boolean importData(TransferSupport support) {
            JTable.DropLocation dropLocation = (JTable.DropLocation) support.getDropLocation();
            TreePath path = treeTable.getPathForRow(dropLocation.getRow());
            Object component = path.getLastPathComponent();
            String sourceId = null;
            try {
                sourceId = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                // ignore
            }
            if (StrUtil.isNotEmpty(sourceId) && component instanceof ConnectionNode) {
                ConnectionNode destNode = (ConnectionNode) component;
                ConnectionNode sourceNode = treeTableModel.recursiveFindChildById(sourceId);
                // Drag to adjust order
                if (dropLocation.isInsertRow() && sourceNode.canMoveToFrontOf(destNode)) {
                    int sourceIndex = sourceNode.indexOfParent();
                    int destIndex = destNode.indexOfParent();
                    treeTableModel.removeChild(sourceNode.parentNode(), sourceIndex);
                    if (sourceIndex > destIndex) {
                        // Moving from back to front
                        treeTableModel.insertChild(destNode.parentNode(), destIndex, sourceNode);
                    } else {
                        // Moving from front to back
                        treeTableModel.insertChild(destNode.parentNode(), destIndex - 1, sourceNode);
                    }
                    connectionsModified = true;
                    return true;
                } else if (sourceNode.canMoveOverTo(destNode)) {
                    // Drag to group
                    int sourceIndex = sourceNode.indexOfParent();
                    treeTableModel.removeChild(sourceNode.parentNode(), sourceIndex);
                    treeTableModel.addChildNode(destNode, sourceNode);
                    connectionsModified = true;
                    return true;
                }
            }
            return false;
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
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.setMinimumSize(new Dimension(400, 300));
        contentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tablePanel = new JPanel();
        tablePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 5, 0), -1, -1));
        contentPanel.add(tablePanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(300, 332), null, 0, false));
        scrollPanel = new JScrollPane();
        tablePanel.add(scrollPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(bottomPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        bottomPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        bottomButtonPanel = new JPanel();
        bottomButtonPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        bottomPanel.add(bottomButtonPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnClose = new JButton();
        btnClose.setText("Close");
        btnClose.setMnemonic('C');
        btnClose.setDisplayedMnemonicIndex(0);
        bottomButtonPanel.add(btnClose, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        bottomButtonPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnConnect = new JButton();
        btnConnect.setEnabled(false);
        btnConnect.setText("Open Connection");
        bottomButtonPanel.add(btnConnect, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setBorderPainted(true);
        toolBar1.setFloatable(false);
        toolBar1.setRollover(true);
        toolBar1.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        contentPanel.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        btnNewGroup = new JButton();
        btnNewGroup.setEnabled(true);
        btnNewGroup.setHideActionText(false);
        btnNewGroup.setText("");
        btnNewGroup.setToolTipText("New directory");
        toolBar1.add(btnNewGroup);
        btnNewConnection = new JButton();
        btnNewConnection.setEnabled(false);
        btnNewConnection.setText("");
        btnNewConnection.setToolTipText("New Connection");
        toolBar1.add(btnNewConnection);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator1);
        btnEdit = new JButton();
        btnEdit.setEnabled(false);
        btnEdit.setText("");
        btnEdit.setToolTipText("Edit");
        toolBar1.add(btnEdit);
        btnDelete = new JButton();
        btnDelete.setEnabled(false);
        btnDelete.setText("");
        btnDelete.setToolTipText("Delete");
        toolBar1.add(btnDelete);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
