package com.mqttinsight.ui.form.panel;

import cn.hutool.core.util.StrUtil;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.component.TopicSegment;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author ptma
 */
public class TopicTreePanel extends JScrollPane {

    private final MqttInstance mqttInstance;

    private final JPanel segmentsContainer;

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
            public void onMessage(MqttMessage message) {
                SwingUtilities.invokeLater(() -> {
                    updateSegments(message.getTopic());
                });
            }

            @Override
            public void clearAllMessages() {
                SwingUtilities.invokeLater(() -> {
                    clearSegments();
                });
            }

            @Override
            public void onMessageRemoved(MqttMessage message) {
                SwingUtilities.invokeLater(() -> {
                    removeTopicSegments(message.getTopic());
                });
            }
        });
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
                rootSegment = new TopicSegment(mqttInstance, this, segment, true);
                addRootSegment(rootSegment);
            }
            if (remainTopic != null) {
                rootSegment.incrementMessages(StrUtil.split(remainTopic, '/'));
            } else {
                rootSegment.incrementMessages();
            }
            segmentsContainer.revalidate();
            segmentsContainer.repaint();
        });

    }

    private void clearSegments() {
        segmentsContainer.removeAll();
        segmentsContainer.revalidate();
        segmentsContainer.repaint();
    }

    private void removeTopicSegments(String topic) {
        extractSegmentAndHandle(topic, (segment, remainTopic) -> {
            Optional<TopicSegment> rootSegment = getRootSegments().stream()
                .filter(item -> item.getName().equals(segment))
                .findFirst();
            rootSegment.ifPresent((topicSegment) -> {
                if (remainTopic != null) {
                    topicSegment.decrementMessages(StrUtil.split(remainTopic, '/'));
                } else {
                    topicSegment.decrementMessages();
                }
                if (topicSegment.getTopicCount() == 0) {
                    removeRootSegment(topicSegment);
                }
                segmentsContainer.revalidate();
                segmentsContainer.repaint();
            });
        });
    }

    public List<TopicSegment> getRootSegments() {
        return Arrays.stream(segmentsContainer.getComponents())
            .map(c -> (TopicSegment) c)
            .toList();
    }

    private void addRootSegment(TopicSegment segment) {
        segmentsContainer.add(segment);
    }

    public void removeRootSegment(TopicSegment segment) {
        segmentsContainer.remove(segment);
    }
}
