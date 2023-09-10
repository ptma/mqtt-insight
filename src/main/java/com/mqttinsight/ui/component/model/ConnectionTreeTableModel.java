package com.mqttinsight.ui.component.model;

import com.mqttinsight.config.ConnectionNode;
import com.mqttinsight.util.LangUtil;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ptma
 */
public class ConnectionTreeTableModel extends AbstractTreeTableModel {

    protected ConnectionNode root;

    public static ConnectionTreeTableModel newInstance() {
        return new ConnectionTreeTableModel(new ConnectionNode(""));
    }

    public ConnectionTreeTableModel(ConnectionNode root) {
        super(root);
        this.root = root;
    }

    public boolean isGroup(Object node) {
        ConnectionNode treeNode = (ConnectionNode) node;
        return treeNode.isGroup();
    }

    public boolean hasName(String name) {
        return root.hasNameInChildren(name);
    }

    public void addChildNode(ConnectionNode parent, ConnectionNode child) {
        int index;
        if (parent == null) {
            index = root.addChild(child);
        } else {
            index = parent.addChild(child);
        }
        modelSupport.fireChildAdded(new TreePath(getPathToRoot(parent)), index, child);
    }

    public void addChildren(List<ConnectionNode> nodes) {
        if (nodes != null && !nodes.isEmpty()) {
            nodes.forEach(node -> {
                addChildNode(root, node);
            });
        }
    }

    public void insertChild(ConnectionNode parent, int index, ConnectionNode child) {
        if (child == null) {
            return;
        }
        child.setParent(parent);
        int orderedIndex = parent.insertChild(index, child);
        modelSupport.fireChildAdded(new TreePath(getPathToRoot(parent)), orderedIndex, child);
    }

    public void removeChild(ConnectionNode parent, int index) {
        ConnectionNode child = parent.removeChild(index);
        modelSupport.fireChildRemoved(new TreePath(getPathToRoot(parent)), index, child);
    }

    public void removeFromParent(ConnectionNode node) {
        ConnectionNode parent = node.parentNode();
        if (parent == null) {
            return;
        }
        int index = node.indexOfParent();
        parent.removeChild(node);
        modelSupport.fireChildRemoved(new TreePath(getPathToRoot(parent)), index, node);
    }

    public List<ConnectionNode> getConnectionNodes() {
        return root.getChildren();
    }

    public void removeById(String id) {
        ConnectionNode node = recursiveFindChildById(id);
        if (node != null) {
            removeFromParent(node);
        }
    }

    public void fireChildChanged(ConnectionNode node) {
        ConnectionNode parent = node.parentNode();
        if (parent == null) {
            return;
        }
        int index = node.indexOfParent();
        modelSupport.fireChildChanged(new TreePath(getPathToRoot(parent)), index, node);
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return LangUtil.getString("Name");
            case 1:
                return LangUtil.getString("Server");
            case 2:
                return LangUtil.getString("Port");
            default:
                return "Unknown";
        }
    }

    @Override
    public Object getValueAt(Object node, int column) {
        ConnectionNode treeNode = (ConnectionNode) node;
        switch (column) {
            case 0:
                return treeNode.getName();
            case 1:
                return treeNode.getHost();
            case 2:
                return treeNode.getPort();
            default:
                return "";
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        ConnectionNode treeNode = (ConnectionNode) node;
        return !treeNode.isGroup();
    }

    @Override
    public Object getChild(Object parent, int index) {
        ConnectionNode treeNode = (ConnectionNode) parent;
        if (treeNode == null) {
            return root.getChild(index);
        } else {
            return treeNode.getChild(index);
        }
    }

    @Override
    public int getChildCount(Object parent) {
        ConnectionNode treeNode = (ConnectionNode) parent;
        if (treeNode == null) {
            return root.getChildCount();
        } else {
            return treeNode.getChildCount();
        }
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        ConnectionNode treeNode = (ConnectionNode) parent;
        if (treeNode == null) {
            return root.getIndexOfChild((ConnectionNode) child);
        } else {
            return treeNode.getIndexOfChild((ConnectionNode) child);
        }
    }

    public ConnectionNode recursiveFindChildById(String id) {
        return root.recursiveFindChildById(id);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    public ConnectionNode[] getPathToRoot(ConnectionNode aNode) {
        List<ConnectionNode> path = new ArrayList<>();
        ConnectionNode node = aNode;

        while (node != root) {
            path.add(0, node);
            node = node.parentNode();
        }
        if (node == root) {
            path.add(0, node);
        }
        return path.toArray(new ConnectionNode[0]);
    }
}
