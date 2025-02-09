package com.mqttinsight.ui.component;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.ui.form.panel.TopicTreePanel;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author ptma
 */
public class TopicSegment extends JPanel {

    private static final Color HOVER_BG_COLOR = UIManager.getColor("Button.default.hoverBackground");
    private static final Color NORMAL_BG_COLOR = UIManager.getColor("Tree.background");
    private static final Color SELECTED_BG_COLOR = UIManager.getColor("Table.selectionInactiveBackground");

    private static final Color TEXT_VISIBLE_COLOR = UIManager.getColor("Label.foreground");
    private static final Color TEXT_INVISIBLE_COLOR = UIManager.getColor("Label.disabledForeground");
    private static final Icon ICON_COLLAPSED = UIManager.getIcon("Tree.collapsedIcon");
    private static final Icon ICON_EXPANDED = UIManager.getIcon("Tree.expandedIcon");
    private static final Icon ICON_EMPTY = new FlatSVGIcon("svg/icons/dot.svg", ICON_EXPANDED.getIconWidth(), ICON_EXPANDED.getIconHeight());

    private final MqttInstance mqttInstance;
    private final TopicTreePanel topicTreePanel;
    private final JComponent parent;

    private final SegmentNodePanel nodePanel;
    private final JPanel childrenPanel;

    @Getter
    private final String name;
    @Getter
    private final String fullTopic;
    @Getter
    private boolean expanded;
    @Getter
    private boolean segmentVisible = true;

    private boolean selected = false;
    private int level;

    private final AtomicInteger messageCount = new AtomicInteger(0);

    public TopicSegment(MqttInstance mqttInstance, TopicTreePanel topicTreePanel, JComponent parent, String name, int level, boolean expanded) {
        super();
        setOpaque(false);
        setLayout(new VerticalLayout(0));

        this.mqttInstance = mqttInstance;
        this.topicTreePanel = topicTreePanel;
        this.parent = parent;
        this.name = name;
        this.expanded = expanded;
        this.level = level;
        this.fullTopic = (parent instanceof TopicSegment parentSegment) ?
            (
                // Root segment, topic start with /
                parentSegment.getName().equals("/") ?
                    parentSegment.getFullTopic() + name
                    :
                    parentSegment.getFullTopic() + "/" + name
            )
            :
            name;

        nodePanel = new SegmentNodePanel(this);
        add(nodePanel);

        childrenPanel = new JPanel();
        childrenPanel.setLayout(new VerticalLayout(0));
        childrenPanel.setOpaque(false);
        childrenPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        if (!expanded) {
            childrenPanel.setPreferredSize(new Dimension(0, 0));
        }
        add(childrenPanel);

        if (parent instanceof TopicSegment parentSegment) {
            if (!parentSegment.isSegmentVisible()) {
                toggleSegmentVisible(false, false, false);
            }
        }
    }

    public Set<String> getInvisibleTopics() {
        if (!isSegmentCompositeVisible()) {
            return new HashSet<>(Collections.singletonList(getFullTopic()));
        } else {
            return getChildren().stream()
                .map(TopicSegment::getInvisibleTopics)
                .reduce((setA, setB) -> {
                    setA.addAll(setB);
                    return setA;
                })
                .orElseGet(HashSet::new);
        }
    }

    public int getChildrenCount() {
        return childrenPanel.getComponentCount();
    }

    public List<TopicSegment> getChildren() {
        return Arrays.stream(childrenPanel.getComponents())
            .map(c -> (TopicSegment) c)
            .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    public Optional<TopicSegment> getChild(String segment) {
        return Arrays.stream(childrenPanel.getComponents()).map(c -> (TopicSegment) c)
            .filter(c -> c.getName().equals(segment))
            .findFirst();
    }

    public void removeSelf() {
        if (parent instanceof TopicSegment parentSegment) {
            parentSegment.removeChildSegment(this);
        } else if (parent instanceof TopicTreePanel topicTree) {
            topicTree.removeRootSegment(this);
        }
    }

    private void removeChildSegment(TopicSegment child) {
        childrenPanel.remove(child);
        updateNode(true);
        if (isExpanded()) {
            updateSize();
        }
        if (getTopicCount() == 0) {
            removeSelf();
        }
    }

    public void incrementMessages() {
        messageCount.incrementAndGet();
        updateNode(false);
    }

    public void incrementMessages(List<String> topicSegments) {
        if (topicSegments.isEmpty()) {
            return;
        }
        String segment = topicSegments.get(0);

        AtomicBoolean childAppended = new AtomicBoolean(false);
        TopicSegment child = getChild(segment).orElseGet(() -> {
            TopicSegment newChild = new TopicSegment(mqttInstance, topicTreePanel, this, segment, level + 1, false);
            addChildSegment(newChild);
            updateSegmentCompositeVisibleStatus(true, false);
            childAppended.set(true);
            return newChild;
        });
        if (topicSegments.size() > 1) {
            child.incrementMessages(topicSegments.subList(1, topicSegments.size()));
        } else {
            child.incrementMessages();
        }
        if (childAppended.get() && isExpanded()) {
            updateSize();
        }
        if (childAppended.get() && !isSegmentVisible()) {
            topicTreePanel.notifyTopicSegmentsVisibleChange();
        }
    }

    private void addChildSegment(TopicSegment segment) {
        List<TopicSegment> segments = getChildren();
        for (int i = 0; i < segments.size(); i++) {
            TopicSegment existingSegment = segments.get(i);
            if (segment.getName().compareTo(existingSegment.getName()) <= 0) {
                childrenPanel.add(segment, i);
                return;
            }
        }
        childrenPanel.add(segment);
    }

    public void decrementMessages(String topic) {
        if (fullTopic.equals(topic)) {
            if (messageCount.get() > 0) {
                messageCount.decrementAndGet();
            }
            if (getTopicCount() == 0) {
                removeSelf();
            } else {
                updateNode(false);
            }
        } else if (topic.startsWith(fullTopic)) {
            getChildren().forEach(segment -> segment.decrementMessages(topic));
        }
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
        updateNode(false);
        updateSize();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        nodePanel.selected(selected);
        if (selected) {
            topicTreePanel.changeSelectedSegment(this);
        }
    }

    public int getSelfMessageCount() {
        return messageCount.get();
    }

    public int getTotalMessageCount() {
        return getSelfMessageCount() + getChildren().stream().map(TopicSegment::getTotalMessageCount)
            .reduce(0, Integer::sum);
    }

    public int getTopicCount() {
        int count = 0;
        if (getSelfMessageCount() > 0) {
            count++;
        }
        count += getChildren().stream()
            .map(TopicSegment::getTopicCount)
            .reduce(0, Integer::sum);
        return count;
    }

    public Dimension getComponentSize() {
        int width = nodePanel.getPreferredSize().width;
        int height = nodePanel.getPreferredSize().height;
        if (this.expanded) {
            resizeChildrenPanel();
            height += childrenPanel.getPreferredSize().height;
        }
        return new Dimension(width, height);
    }

    public int calcSegmentScrollHeight(List<String> topicSegments) {
        int height = 0;
        for (TopicSegment segment : getChildren()) {
            if (segment.getName().equals(topicSegments.get(0))) {
                height += segment.nodePanel.getHeight();
                if (topicSegments.size() > 1) {
                    if (!segment.isExpanded()) {
                        segment.toggleExpanded();
                    }
                    height += segment.calcSegmentScrollHeight(topicSegments.subList(1, topicSegments.size()));
                } else {
                    segment.setSelected(true);
                }
                return height;
            } else {
                height += segment.getHeight();
            }
        }
        return height;
    }

    private void resizeChildrenPanel() {
        int width = nodePanel.getPreferredSize().width;
        int height = 0;
        if (this.expanded) {
            for (TopicSegment child : getChildren()) {
                Dimension itemSize = child.getComponentSize();
                height += itemSize.height;
            }
        }
        childrenPanel.setPreferredSize(new Dimension(width, height));
        childrenPanel.revalidate();
        //childrenPanel.repaint();
    }

    /**
     * During the clearing process, the number of messages in this segment will be updated synchronously.
     * When the number is 0, this segment will be removed
     * <p>
     * {@link com.mqttinsight.ui.form.panel.MessageViewPanel#doClearMessages(String)} (String)}
     */
    public void removeSegmentMessages() {
        SwingUtilities.invokeLater(() -> {
            mqttInstance.applyEvent(l -> l.clearMessages(getFullTopic()));
        });
    }

    public boolean isSegmentCompositeVisible() {
        return segmentVisible || getChildren().stream().anyMatch(TopicSegment::isSegmentCompositeVisible);
    }

    public void updateSegmentCompositeVisibleStatus(boolean updateParent, boolean notify) {
        nodePanel.changeSegmentVisibleStatus(isSegmentCompositeVisible());
        if (updateParent && parent instanceof TopicSegment parentSegment) {
            parentSegment.updateSegmentCompositeVisibleStatus(true, notify);
        }
        if (notify && parent instanceof TopicTreePanel topicTree) {
            topicTree.notifyTopicSegmentsVisibleChange();
        }
    }

    /**
     * @param segmentVisible   Whether the segment is visible
     * @param effectOnChildren Does visibility change need to affect its children
     */
    public void toggleSegmentVisible(boolean segmentVisible, boolean effectOnChildren, boolean updateParentCompositeVisibleStatus) {
        if (effectOnChildren) {
            getChildren().forEach(child -> child.toggleSegmentVisible(segmentVisible, true, false));
        }

        this.segmentVisible = segmentVisible;
        nodePanel.changeSegmentVisibleStatus(this.segmentVisible);

        if (updateParentCompositeVisibleStatus) {
            updateSegmentCompositeVisibleStatus(true, true);
        }
    }

    public void updateNode(boolean repaint) {
        nodePanel.setIcon(getChildrenCount() > 0 ? (this.expanded ? ICON_EXPANDED : ICON_COLLAPSED) : ICON_EMPTY);

        String topicsDescription, messagesDescription;
        int topicsCount = getTopicCount();
        if (topicsCount > 1) {
            topicsDescription = LangUtil.getString("SegmentTopicsDescription");
        } else {
            topicsDescription = LangUtil.getString("SegmentTopicDescription");
        }
        int messagesCount = getTotalMessageCount();
        if (messagesCount > 1) {
            messagesDescription = LangUtil.getString("SegmentMessagesDescription");
        } else {
            messagesDescription = LangUtil.getString("SegmentMessageDescription");
        }
        String descriptionTemplate = String.format("(%s, %s)", topicsDescription, messagesDescription);
        nodePanel.description(String.format(descriptionTemplate, topicsCount, messagesCount));
        this.revalidate();
        if (repaint) {
            this.repaint();
        }

        if (parent instanceof TopicSegment) {
            ((TopicSegment) parent).updateNode(repaint);
        }
    }

    public void updateSize() {
        this.setPreferredSize(getComponentSize());
        this.revalidate();
        //this.repaint();
        if (parent instanceof TopicSegment) {
            ((TopicSegment) parent).updateSize();
        }
    }

    public static class SegmentNodePanel extends JPanel implements MouseListener {

        private final TopicSegment segment;
        private final JXLabel iconLabel;
        private final JXLabel nameLabel;
        private final JXLabel descriptionLabel;
        private final JToolBar toolBar;
        private final JButton visibleButton;
        private final JButton clearButton;

        private boolean segmentVisible = true;

        public SegmentNodePanel(TopicSegment segment) {
            super();
            this.segment = segment;
            setLayout(new MigLayout(
                String.format("insets 0 %d 0 0", segment.level * 10 + 5),
                "[]5[]5[grow]5[]",
                "[]"
            ));
            setBackground(UIManager.getColor("Tree.background"));
            iconLabel = new JXLabel(ICON_COLLAPSED);
            iconLabel.setOpaque(false);
            add(iconLabel, "cell 0 0,hmin 26px");
            nameLabel = new JXLabel(segment.getName());
            nameLabel.setOpaque(false);
            nameLabel.putClientProperty("FlatLaf.styleClass", "h4");
            nameLabel.setToolTipText(segment.getFullTopic());
            add(nameLabel, "cell 1 0");
            descriptionLabel = new JXLabel();
            descriptionLabel.setOpaque(false);
            descriptionLabel.setForeground(TEXT_INVISIBLE_COLOR);
            add(descriptionLabel, "cell 2 0,wmin 10px,growx");

            toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.setOpaque(false);
            toolBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            toolBar.setVisible(false);

            clearButton = new JButton(Icons.CLEAR);
            clearButton.setToolTipText(LangUtil.getString("ClearMessages"));
            clearButton.addActionListener((e) -> segment.removeSegmentMessages());
            toolBar.add(clearButton);

            visibleButton = new JButton(Icons.EYE);
            visibleButton.setToolTipText(LangUtil.getString("ShowOrHideMessages"));
            visibleButton.addActionListener((e) -> segment.toggleSegmentVisible(!segmentVisible, true, true));
            toolBar.add(visibleButton);

            add(toolBar, "cell 3 0, hidemode 2");

            addMouseListener(this);
            initActions();
        }

        public void initActions() {
            iconLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    segment.toggleExpanded();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    SegmentNodePanel.this.mouseEntered(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    SegmentNodePanel.this.mouseExited(e);
                }
            });
            clearButton.addMouseListener(this);
            visibleButton.addMouseListener(this);
            nameLabel.addMouseListener(this);
            descriptionLabel.addMouseListener(this);
        }

        public void setIcon(Icon icon) {
            iconLabel.setIcon(icon);
        }

        public void description(String description) {
            descriptionLabel.setText(description);
        }

        public void changeSegmentVisibleStatus(boolean segmentVisible) {
            if (this.segmentVisible == segmentVisible) {
                return;
            }
            this.segmentVisible = segmentVisible;
            visibleButton.setIcon(segmentVisible ? Icons.EYE : Icons.EYE_CLOSE);
            nameLabel.setForeground(segmentVisible ? TEXT_VISIBLE_COLOR : TEXT_INVISIBLE_COLOR);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                SwingUtilities.invokeLater(segment::toggleExpanded);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                SwingUtilities.invokeLater(() -> {
                    segment.setSelected(true);
                });
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {
            SwingUtilities.invokeLater(() -> {
                if (!segment.selected) {
                    this.setBackground(HOVER_BG_COLOR);
                }
                toolBar.setVisible(true);
            });
        }

        @Override
        public void mouseExited(MouseEvent e) {
            SwingUtilities.invokeLater(() -> {
                if (!segment.selected) {
                    this.setBackground(NORMAL_BG_COLOR);
                }
                toolBar.setVisible(false);
            });
        }

        public void selected(boolean selected) {
            if (selected) {
                this.setBackground(SELECTED_BG_COLOR);
            } else {
                this.setBackground(NORMAL_BG_COLOR);
            }
            this.revalidate();
            //this.repaint();
        }
    }

}
