package com.mqttinsight.ui.form.panel;

import cn.hutool.core.util.StrUtil;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.component.TopicSegment;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author ptma
 */
public class TopicTreePanel extends JScrollPane {

    private final MqttInstance mqttInstance;

    private final JPanel segmentsContainer;

    private TopicSegment selectedSegment;

    public TopicTreePanel(final MqttInstance mqttInstance) {
        super();
        this.mqttInstance = mqttInstance;
        getVerticalScrollBar().setUnitIncrement(30);
        segmentsContainer = new JPanel();
        segmentsContainer.setLayout(new VerticalLayout(0));
        segmentsContainer.setBackground(UIManager.getColor("Table.background"));
        setViewportView(segmentsContainer);

        initEventListeners();

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
    }

    public void changeSelectedSegment(TopicSegment segment) {
        if (selectedSegment != null && selectedSegment != segment) {
            selectedSegment.setSelected(false);
        }
        selectedSegment = segment;
    }

    public void notifyTopicSegmentsVisibleChange() {
        Set<String> invisibleTopics = getRootSegments().stream()
            .map(TopicSegment::getInvisibleTopics)
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

    private void updateSegments(String topic) {
        extractSegmentAndHandle(topic, (segment, remainTopic) -> {
            TopicSegment rootSegment = getRootSegments().stream()
                .filter(item -> item.getName().equals(segment))
                .findFirst()
                .orElse(null);
            if (rootSegment == null) {
                rootSegment = new TopicSegment(mqttInstance, this, this, segment, true);
                addRootSegment(rootSegment);
                segmentsContainer.revalidate();
                segmentsContainer.repaint();
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
            Optional<TopicSegment> rootSegment = getRootSegments().stream()
                .filter(item -> item.getName().equals(segment))
                .findFirst();
            rootSegment.ifPresent((topicSegment) -> {
                topicSegment.decrementMessages(topic);
            });
        });
    }

    /**
     * When double clicking a message in the message table, locate the topic segment node
     *
     * @param topic segment
     */
    private void locateSegments(String topic) {
        extractSegmentAndHandle(topic, (segment, remainTopic) -> {
            int height = 0;
            for (TopicSegment rootSegment : getRootSegments()) {
                if (rootSegment.getName().equals(segment)) {
                    if (!rootSegment.isExpanded()) {
                        rootSegment.toggleExpanded();
                    }
                    height += rootSegment.calcSegmentScrollHeight(StrUtil.split(remainTopic, '/'));
                    getViewport().setViewPosition(new Point(0, height));
                    updateUI();
                    break;
                } else {
                    height += rootSegment.getComponentSize().height;
                }
            }
        });

    }

    public List<TopicSegment> getRootSegments() {
        return Arrays.stream(segmentsContainer.getComponents())
            .map(c -> (TopicSegment) c)
            .toList();
    }

    private void addRootSegment(TopicSegment segment) {
        List<TopicSegment> segments = getRootSegments();
        for (int i = 0; i < segments.size(); i++) {
            TopicSegment existingSegment = segments.get(i);
            if (segment.getName().compareTo(existingSegment.getName()) <= 0) {
                segmentsContainer.add(segment, i);
                return;
            }
        }
        segmentsContainer.add(segment);
    }

    public void removeRootSegment(TopicSegment segment) {
        segmentsContainer.remove(segment);
        segmentsContainer.revalidate();
        segmentsContainer.repaint();
    }
}
