package com.mqttinsight.ui.component.tree;

import lombok.Getter;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SegmentTreeNode extends DefaultMutableTreeNode {

    private final TopicTree tree;
    private final SegmentTreeNode parent;
    @Getter
    private final String name;
    @Getter
    private final String fullTopic;

    @Getter
    private boolean segmentVisible = true;

    private final AtomicInteger messageCount = new AtomicInteger(0);

    public SegmentTreeNode(TopicTree tree, SegmentTreeNode parent, String name) {
        this.tree = tree;
        this.parent = parent;
        this.name = name;
        if (this.parent != null && !this.parent.getName().equals("#")) {
            this.fullTopic = this.parent.getName().equals("/") ?
                this.parent.getFullTopic() + name
                :
                this.parent.getFullTopic() + "/" + name;
        } else {
            this.fullTopic = name;
        }
        if (this.parent != null && !this.parent.isSegmentVisible()) {
            this.segmentVisible = false;
        }
    }

    public Set<String> getInvisibleTopics() {
        if (!isSegmentCompositeVisible()) {
            return new HashSet<>(Collections.singletonList(getFullTopic()));
        } else {
            return getChildren().stream()
                .map(SegmentTreeNode::getInvisibleTopics)
                .reduce((setA, setB) -> {
                    setA.addAll(setB);
                    return setA;
                })
                .orElseGet(HashSet::new);
        }
    }

    public void toggleSegmentVisible(boolean segmentVisible, boolean effectOnChildren, boolean updateParentCompositeVisibleStatus) {
        if (effectOnChildren) {
            getChildren().forEach(child -> child.toggleSegmentVisible(segmentVisible, true, false));
        }

        this.segmentVisible = segmentVisible;
        tree.getTreeModel().nodeChanged(this);
        
        if (updateParentCompositeVisibleStatus) {
            updateSegmentCompositeVisibleStatus(true, true);
        }
    }

    public boolean isSegmentCompositeVisible() {
        return segmentVisible || getChildren().stream().anyMatch(SegmentTreeNode::isSegmentCompositeVisible);
    }

    public void updateSegmentCompositeVisibleStatus(boolean updateParent, boolean notify) {
        if (updateParent && parent != null) {
            parent.updateSegmentCompositeVisibleStatus(true, notify);
        }
        if (notify && parent == null) {
            tree.notifyTopicSegmentsVisibleChange();
        }
    }

    public void incrementMessages() {
        messageCount.incrementAndGet();
        tree.getTreeModel().nodeChanged(this);
    }

    public void incrementMessages(List<String> topicSegments) {
        if (topicSegments.isEmpty()) {
            return;
        }
        String segment = topicSegments.get(0);

        AtomicBoolean childAppended = new AtomicBoolean(false);
        SegmentTreeNode child = getChild(segment).orElseGet(() -> {
            SegmentTreeNode newChild = new SegmentTreeNode(tree, this, segment);
            childAppended.set(true);
            tree.getTreeModel().insertNodeInto(newChild, this, this.getChildCount());
            return newChild;
        });
        if (topicSegments.size() > 1) {
            child.incrementMessages(topicSegments.subList(1, topicSegments.size()));
        } else {
            child.incrementMessages();
        }
        if (childAppended.get() && !isSegmentVisible()) {
            tree.notifyTopicSegmentsVisibleChange();
        }
    }

    public void decrementMessages(String topic) {
        if (fullTopic.equals(topic)) {
            if (messageCount.get() > 0) {
                messageCount.decrementAndGet();
                tree.getTreeModel().nodeChanged(this);
            }
            if (getTopicCount() == 0) {
                removeSelf();
            }
        } else if (topic.startsWith(fullTopic)) {
            getChildren().forEach(segment -> segment.decrementMessages(topic));
        }
    }

    public void removeSegmentMessages() {
        tree.removeSegmentMessages(this);
    }

    public void removeSelf() {
        if (parent != null) {
            parent.removeChild(this);
            tree.getTreeModel().nodeChanged(parent);
        }
    }

    public void removeChild(SegmentTreeNode child) {
        tree.getTreeModel().removeNodeFromParent(child);
        if (getTopicCount() == 0) {
            removeSelf();
        }
    }

    public int getSelfMessageCount() {
        return messageCount.get();
    }

    public int getTotalMessageCount() {
        return getSelfMessageCount() + getChildren().stream().map(SegmentTreeNode::getTotalMessageCount)
            .reduce(0, Integer::sum);
    }

    public int getTopicCount() {
        int count = 0;
        if (getSelfMessageCount() > 0) {
            count++;
        }
        count += getChildren().stream()
            .map(SegmentTreeNode::getTopicCount)
            .reduce(0, Integer::sum);
        return count;
    }

    public Optional<SegmentTreeNode> getChild(String segment) {
        return getChildren().stream()
            .filter(c -> c.getName().equals(segment))
            .findFirst();
    }

    public List<SegmentTreeNode> getChildren() {
        if (children == null) {
            return List.of();
        } else {
            return children.stream()
                .map(c -> (SegmentTreeNode) c)
                .toList();
        }
    }

    @Override
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    public String toString() {
        return name;
    }
}
