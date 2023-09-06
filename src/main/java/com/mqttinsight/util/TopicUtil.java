package com.mqttinsight.util;

import cn.hutool.core.text.AntPathMatcher;
import com.mqttinsight.exception.VerificationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MQTT 主题工具
 *
 * @author ptma
 */
public class TopicUtil {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 订阅主题是否合法
     *
     * @param topicFilter 定于的主题，支持通配符
     * @return 是否合法
     */
    public static void validate(String topicFilter) throws VerificationException {
        parseTopic(topicFilter);
    }

    /**
     * 消息主题是否与订阅主题匹配
     *
     * @param subscriptionTopic 订阅的主题，支持通配符
     * @param messageTopic      发布的消息主题
     * @return 是否匹配
     */
    public static boolean match(String subscriptionTopic, String messageTopic) {
        return PATH_MATCHER.match(convertTopicToPattern(subscriptionTopic), messageTopic);
    }

    private static String convertTopicToPattern(String subscriptionTopic) {
        return subscriptionTopic.replace("#", "**").replace("+", "*");
    }


    private static List<Token> parseTopic(String topic) throws VerificationException {
        List<Token> res = new ArrayList<>();
        String[] splitted = topic.split("/");

        if (splitted.length == 0) {
            res.add(Token.EMPTY);
        }

        if (topic.endsWith("/")) {
            String[] newSplitted = new String[splitted.length + 1];
            System.arraycopy(splitted, 0, newSplitted, 0, splitted.length);
            newSplitted[splitted.length] = "";
            splitted = newSplitted;
        }

        for (int i = 0; i < splitted.length; i++) {
            String s = splitted[i];
            if (s.isEmpty()) {
                res.add(Token.EMPTY);
            } else if (s.equals("#")) {
                if (i != splitted.length - 1) {
                    throw new VerificationException(LangUtil.getString("InvaildTopicOfMultiSymbol"));
                }
                res.add(Token.MULTI);
            } else if (s.contains("#")) {
                throw new VerificationException(String.format(LangUtil.getString("InvalidSubtopic"), s));
            } else if (s.equals("+")) {
                res.add(Token.SINGLE);
            } else if (s.contains("+")) {
                throw new VerificationException(String.format(LangUtil.getString("InvalidSubtopic"), s));
            } else {
                res.add(new Token(s));
            }
        }

        return res;
    }

    private static class Token implements Comparable<Token> {

        static final Token EMPTY = new Token("");
        static final Token MULTI = new Token("#");
        static final Token SINGLE = new Token("+");
        final String name;

        protected Token(String s) {
            name = s;
        }

        protected String name() {
            return name;
        }

        protected boolean match(Token t) {
            if (MULTI.equals(t) || SINGLE.equals(t)) {
                return false;
            }

            if (MULTI.equals(this) || SINGLE.equals(this)) {
                return true;
            }

            return equals(t);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Token other = (Token) obj;
            return Objects.equals(this.name, other.name);
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(Token other) {
            if (name == null) {
                if (other.name == null) {
                    return 0;
                }
                return 1;
            }
            if (other.name == null) {
                return -1;
            }
            return name.compareTo(other.name);
        }
    }
}
