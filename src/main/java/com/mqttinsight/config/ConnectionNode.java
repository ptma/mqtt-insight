package com.mqttinsight.config;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mqttinsight.mqtt.MqttProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


/**
 * @author ptma
 */
public class ConnectionNode {

    @Setter
    private String id;

    @Getter
    @Setter
    private boolean group;

    private String name;

    @Getter
    @Setter
    private MqttProperties properties;

    @Getter
    private List<ConnectionNode> children = new LinkedList<>();

    private ConnectionNode parent;

    private final Comparator<ConnectionNode> comparator = (o1, o2) -> Boolean.compare(o2.isGroup(), o1.isGroup());

    public ConnectionNode() {

    }

    public ConnectionNode(String name) {
        this.id = IdUtil.fastUUID();
        this.group = true;
        this.name = name;
    }

    public ConnectionNode(MqttProperties properties) {
        this.properties = properties;
        this.group = false;
        this.name = properties.getName();
    }

    public String getId() {
        return this.group ? this.id : this.properties.getId();
    }

    public String getName() {
        return this.group ? this.name : this.properties.getName();
    }

    public void setName(String name) {
            this.name = name;
    }

    @JsonIgnore
    public String getHost() {
        return this.group ? "" : this.properties.getHost();
    }

    @JsonIgnore
    public String getPort() {
        return this.group ? "" : String.valueOf(this.properties.getPort());
    }

    public void setParent(ConnectionNode parent) {
        this.parent = parent;
    }

    public ConnectionNode parentNode() {
        return this.parent;
    }

    public int addChild(ConnectionNode child) {
        if (this.children == null) {
            this.children = new LinkedList<>();
        }
        if (child == null) {
            return -1;
        }
        child.setParent(this);
        this.children.add(child);
        this.children.sort(comparator);
        return child.indexOfParent();
    }

    public int insertChild(int index, ConnectionNode child) {
        if (this.children == null) {
            this.children = new LinkedList<>();
        }
        if (child == null) {
            return -1;
        }
        child.setParent(this);
        this.children.add(index, child);
        this.children.sort(comparator);
        return child.indexOfParent();
    }

    public void addChildren(List<ConnectionNode> children) {
        if (this.children == null) {
            this.children = new LinkedList<>();
        }
        if (children == null) {
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            ConnectionNode child = children.get(i);
            child.setParent(this);
            this.children.add(child);
        }
        this.children.sort(comparator);
    }

    public void setChildren(List<ConnectionNode> children) {
        addChildren(children);
    }

    @JsonIgnore
    public int getChildCount() {
        if (this.children == null) {
            return 0;
        }
        return this.children.size();
    }

    public Object getChild(int index) {
        if (this.children == null) {
            return null;
        }
        return this.children.get(index);
    }

    public int getIndexOfChild(ConnectionNode child) {
        if (this.children == null) {
            return -1;
        }
        for (int i = 0; i < this.children.size(); i++) {
            if (this.children.get(i) == child) {
                return i;
            }
        }
        return -1;
    }

    public ConnectionNode removeChild(int index) {
        if (this.children != null && !this.children.isEmpty()) {
            return this.children.remove(index);
        }
        return null;
    }

    public boolean removeChild(ConnectionNode child) {
        if (this.children != null && !this.children.isEmpty()) {
            return this.children.remove(child);
        }
        return false;
    }

    public ConnectionNode recursiveFindChildById(String id) {
        if (getChildCount() > 0) {
            for (int i = 0; i < children.size(); i++) {
                ConnectionNode foundNode = children.get(i);
                if (foundNode.getId().equals(id)) {
                    return foundNode;
                }
                foundNode = foundNode.recursiveFindChildById(id);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }

    public boolean isChildOf(ConnectionNode node) {
        return !node.getChildren().isEmpty() && node.getChildren().contains(this);
    }

    public boolean isAncestorOf(ConnectionNode node) {
        if (node == null || node.parent == null) {
            return false;
        }
        if (node.parent.equals(this)) {
            return true;
        }
        return isAncestorOf(node.parent);
    }

    public boolean isCommonParent(ConnectionNode node) {
        return parent != null && node.parent != null && parent.equals(node.parent);
    }

    public int indexOfParent() {
        if (parent == null) {
            return -1;
        }
        return parent.getChildren().indexOf(this);
    }

    public boolean canMoveToFrontOf(ConnectionNode targetNode) {
        if (!this.isGroup() && targetNode.isGroup()) {
            return false;
        }
        if (this.isGroup() && !targetNode.isGroup()) {
            return false;
        }
        if (isCommonParent(targetNode)) {
            return this.indexOfParent() != targetNode.indexOfParent() - 1;
        } else {
            return false;
        }
    }

    public boolean canMoveOverTo(ConnectionNode targetNode) {
        if (!targetNode.isGroup() || isChildOf(targetNode) || isAncestorOf(targetNode)) {
            return false;
        }
        return true;
    }

    public boolean hasNameInChildren(String name) {
        if (getChildCount() > 0) {
            for (int i = 0; i < children.size(); i++) {
                ConnectionNode childNode = children.get(i);
                if (childNode.getName().equals(name)) {
                    return true;
                }
                boolean found = childNode.hasNameInChildren(name);
                if (found) {
                    return true;
                }
            }
        }
        return false;
    }
}
