package com.mqttinsight.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TopicUtilTest {

    @Test
    void match() {
        Assertions.assertTrue(TopicUtil.match("$share/test-group/topic1", "topic1"));
        Assertions.assertTrue(TopicUtil.match("$share/test-group/topic1/#", "topic1/1"));
        Assertions.assertTrue(TopicUtil.match("$share/test-group/topic1/#", "topic1/1/2/3"));
        Assertions.assertTrue(TopicUtil.match("$share/test-group/topic1/+/1", "topic1/2/1"));
        Assertions.assertTrue(TopicUtil.match("$share/test-group/topic1/+/+/1", "topic1/2/3/1"));

        Assertions.assertTrue(TopicUtil.match("$share/test-group//topic1", "/topic1"));
        Assertions.assertTrue(TopicUtil.match("$share/test-group//topic1/#", "/topic1/1"));
        Assertions.assertTrue(TopicUtil.match("$share/test-group//topic1/#", "/topic1/1/2/3"));
        Assertions.assertTrue(TopicUtil.match("$share/test-group//topic1/+/1", "/topic1/2/1"));
        Assertions.assertTrue(TopicUtil.match("$share/test-group//topic1/+/+/1", "/topic1/2/3/1"));

        Assertions.assertTrue(TopicUtil.match("topic1/#", "topic1/1"));
        Assertions.assertTrue(TopicUtil.match("topic1/+/1", "topic1/2/1"));
        Assertions.assertTrue(TopicUtil.match("topic1/+/+/1", "topic1/2/3/1"));

        Assertions.assertTrue(TopicUtil.match("/topic1/#", "/topic1/1"));
        Assertions.assertTrue(TopicUtil.match("/topic1/+/1", "/topic1/2/1"));
        Assertions.assertTrue(TopicUtil.match("/topic1/+/+/1", "/topic1/2/3/1"));

        Assertions.assertFalse(TopicUtil.match("/topic1/#", "/topic2/2/3/1"));
        Assertions.assertFalse(TopicUtil.match("/topic1/+/2", "/topic2/2"));
        Assertions.assertFalse(TopicUtil.match("/topic1/+/2", "/topic2/2/1"));
    }
}
