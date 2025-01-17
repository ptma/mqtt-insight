package com.mqttinsight.ui.component.tree;

import cn.hutool.core.util.StrUtil;
import com.formdev.flatlaf.extras.components.FlatPopupMenu;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import org.jdesktop.swingx.JXTree;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;

public class TopicTree extends JXTree {

    private final MqttInstance mqttInstance;
    private final SegmentNode rootNode;
    private final DefaultTreeModel treeModel;
    private FlatPopupMenu popupMenu;
    private JMenuItem menuClearMessages;
    private JMenuItem menuHideMessages;
    private JMenuItem menuHideOtherMessages;

    public TopicTree(MqttInstance mqttInstance) {
        super();
        this.mqttInstance = mqttInstance;
        TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.setSelectionModel(selectionModel);
        treeModel = new DefaultTreeModel(null, false);
        rootNode = new SegmentNode(this, null, "#");
        treeModel.setRoot(rootNode);
        this.setModel(treeModel);
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
        this.setUI(new TopicTreeUI());

        this.setOpenIcon(null);
        this.setClosedIcon(null);
        this.setLeafIcon(TopicTreeCellRenderer.ICON_LEAF);
        this.setExpandedIcon(TopicTreeCellRenderer.ICON_EXPANDED);
        this.setCollapsedIcon(TopicTreeCellRenderer.ICON_COLLAPSED);

        TopicTreeCellRenderer renderer = new TopicTreeCellRenderer();
        this.setCellRenderer(renderer);

        popupMenu = new FlatPopupMenu();
        menuHideMessages = Utils.UI.createCheckBoxMenuItem(LangUtil.getString("HideRelatedMessages"), (e) -> toggleSegmentVisible());
        popupMenu.add(menuHideMessages);
        menuHideOtherMessages = Utils.UI.createMenuItem(LangUtil.getString("HideAllOtherMessages"), (e) -> toggleOtherSegmentVisible());
        popupMenu.add(menuHideOtherMessages);
        popupMenu.addSeparator();
        menuClearMessages = Utils.UI.createMenuItem(LangUtil.getString("ClearMessages"), (e) -> removeSegmentMessages());
        menuClearMessages.setIcon(Icons.CLEAR);
        popupMenu.add(menuClearMessages);

        initEventListeners();
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    private void initEventListeners() {
        mqttInstance.addEventListener(new InstanceEventAdapter() {
            @Override
            public void onMessage(MqttMessage message, MqttMessage parent) {
                SwingUtilities.invokeLater(() -> {
                    updateSegments(message.getTopic());
                });
            }

            @Override
            public void onMessageRemoved(MqttMessage message) {
                SwingUtilities.invokeLater(() -> {
                    removeTopicSegments(message.getTopic());
                });
            }

            @Override
            public void requestFocusPreview() {
                SwingUtilities.invokeLater(() -> {
                    mqttInstance.getMessageTable().getSelectedMessage()
                        .ifPresent(message -> {
                            locateSegments(message.getTopic());
                        });
                });
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    final int rowIndex = TopicTree.this.getClosestRowForLocation(e.getX(), e.getY());
                    if (rowIndex < 0) {
                        return;
                    }
                    TreePath clickedPath = TopicTree.this.getPathForRow(rowIndex);
                    if (clickedPath == null) {
                        return;
                    }
                    Rectangle bounds = TopicTree.this.getPathBounds(clickedPath);
                    if (bounds == null || e.getY() > (bounds.y + bounds.height)) {
                        return;
                    }

                    TopicTree.this.setSelectionRow(rowIndex);
                    SegmentNode node = (SegmentNode) clickedPath.getLastPathComponent();
                    menuHideMessages.setSelected(!node.isSegmentCompositeVisible());
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        TreePath treePath = getPathForLocation(e.getX(), e.getY());
        if (treePath != null) {
            if (treePath.getLastPathComponent() instanceof SegmentNode) {
                SegmentNode node = (SegmentNode) treePath.getLastPathComponent();
                return node.getFullTopic();
            }
        }
        return null;
    }

    private void removeSegmentMessages() {
        TreePath selectedPath = getSelectionPath();
        if (selectedPath != null) {
            final SegmentNode selectNode = (SegmentNode) selectedPath.getLastPathComponent();
            if (selectNode != null) {
                removeSegmentMessages(selectNode);
            }
        }
    }

    private void toggleSegmentVisible() {
        TreePath selectedPath = getSelectionPath();
        if (selectedPath != null) {
            final SegmentNode selectNode = (SegmentNode) selectedPath.getLastPathComponent();
            if (selectNode != null) {
                boolean segmentVisible = !selectNode.isSegmentVisible();
                selectNode.toggleSegmentVisible(segmentVisible, true, true);
            }
        }
    }

    private void toggleOtherSegmentVisible() {
        rootNode.getChildren().forEach(node -> {
            node.toggleSegmentVisible(false, true, false);
        });
        TreePath selectedPath = getSelectionPath();
        if (selectedPath != null) {
            final SegmentNode selectNode = (SegmentNode) selectedPath.getLastPathComponent();
            if (selectNode != null) {
                selectNode.toggleSegmentVisible(true, true, true);
            }
        }
    }

    public void removeSegmentMessages(SegmentNode node) {
        SwingUtilities.invokeLater(() -> {
            mqttInstance.applyEvent(l -> l.clearMessages(node.getFullTopic()));
        });
    }

    public void notifyTopicSegmentsVisibleChange() {
        Set<String> invisibleTopics = getRootSegments().stream()
            .map(SegmentNode::getInvisibleTopics)
            .reduce((setA, setB) -> {
                setA.addAll(setB);
                return setA;
            })
            .orElseGet(HashSet::new);
        mqttInstance.applyEvent(l -> l.applyFilterTopics(invisibleTopics));
    }

    private void extractSegmentAndHandle(String topic, BiConsumer<String, String> handler) {
        String segment, remainTopic;
        if (topic.startsWith("/")) {
            segment = "/";
            remainTopic = topic.substring(1);
        } else {
            int slashIndex = topic.indexOf('/');
            if (slashIndex > 0) {
                segment = topic.substring(0, slashIndex);
                remainTopic = topic.substring(slashIndex + 1);
            } else {
                segment = topic;
                remainTopic = null;
            }
        }

        if (handler != null) {
            handler.accept(segment, remainTopic);
        }
    }

    public List<SegmentNode> getRootSegments() {
        return Collections.list(rootNode.children())
            .stream()
            .map(item -> (SegmentNode) item)
            .toList();
    }

    private void updateSegments(String topic) {
        extractSegmentAndHandle(topic, (segment, remainTopic) -> {
            SegmentNode rootSegment = Collections.list(rootNode.children()).stream()
                .map(item -> (SegmentNode) item)
                .filter(item -> item.getName().equals(segment))
                .findFirst()
                .orElse(null);


            if (rootSegment == null) {
                rootSegment = new SegmentNode(this, rootNode, segment);
                addRootSegment(rootSegment);
            }
            if (remainTopic != null) {
                rootSegment.incrementMessages(StrUtil.split(remainTopic, '/'));
            } else {
                rootSegment.incrementMessages();
            }
        });
    }

    private void removeTopicSegments(String topic) {
        extractSegmentAndHandle(topic, (segment, remainTopic) -> {
            Optional<SegmentNode> rootSegment = getRootSegments().stream()
                .filter(item -> item.getName().equals(segment))
                .findFirst();
            rootSegment.ifPresent((topicSegment) -> {
                topicSegment.decrementMessages(topic);
            });
        });
    }

    private void addRootSegment(SegmentNode segmentNode) {
        List<SegmentNode> segments = getRootSegments();
        for (int i = 0; i < segments.size(); i++) {
            SegmentNode existingSegment = segments.get(i);
            if (segmentNode.getName().compareTo(existingSegment.getName()) <= 0) {
                treeModel.insertNodeInto(segmentNode, rootNode, i);
                return;
            }
        }
        treeModel.insertNodeInto(segmentNode, rootNode, segments.size());
    }

    private void locateSegments(String topic) {
        extractSegmentAndHandle(topic, (segment, remainTopic) -> {

        });
    }

}
