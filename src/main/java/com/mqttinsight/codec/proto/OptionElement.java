package com.mqttinsight.codec.proto;

import cn.hutool.core.util.NumberUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class OptionElement implements Element {

    private String name;
    private Kind kind;
    private Object value;
    private boolean isParenthesized;

    public static OptionElement of(String name, Kind kind, Object value) {
        return of(name, kind, value, false);
    }

    public static OptionElement of(String name, Kind kind, Object value, boolean isParenthesized) {
        Element.checkNotNull(name, "name");
        Element.checkNotNull(value, "value");

        return new OptionElement(name, kind, value, isParenthesized);
    }

    public static OptionElement findByName(List<OptionElement> options, String name) {
        Element.checkNotNull(options, "options");
        Element.checkNotNull(name, "name");
        OptionElement found = null;
        for (OptionElement option : options) {
            if (option.getName().equals(name)) {
                if (found != null) {
                    throw new IllegalStateException("Multiple options match name: " + name);
                }
                found = option;
            }
        }
        return found;
    }

    public Object convertValue() {
        Object value = getValue();
        return switch (getKind()) {
            case STRING -> String.valueOf(value);
            case BOOLEAN -> Boolean.valueOf(String.valueOf(value));
            case NUMBER -> NumberUtil.parseNumber(String.valueOf(value));
            default -> value;
        };
    }

    private void formatOptionMap(StringBuilder builder, Map<String, ?> valueMap) {
        List<? extends Map.Entry<String, ?>> entries = new ArrayList<>(valueMap.entrySet());
        for (int i = 0, count = entries.size(); i < count; i++) {
            Map.Entry<String, ?> entry = entries.get(i);
            String endl = (i < count - 1) ? "," : "";
            appendIndented(builder,
                entry.getKey() + ": " + formatOptionMapValue(entry.getValue()) + endl);
        }
    }

    private String formatOptionMapValue(Object value) {
        Element.checkNotNull(value, "value == null");
        if (value instanceof String) {
            return "\"" + value + '"';
        }
        if (value instanceof Map) {
            StringBuilder builder = new StringBuilder().append("{\n");
            //noinspection unchecked
            Map<String, ?> map = (Map<String, ?>) value;
            formatOptionMap(builder, map);
            return builder.append('}').toString();
        }
        if (value instanceof List) {
            StringBuilder builder = new StringBuilder().append("[\n");
            List<?> list = (List<?>) value;
            for (int i = 0, count = list.size(); i < count; i++) {
                String endl = (i < count - 1) ? "," : "";
                appendIndented(builder, formatOptionMapValue(list.get(i)) + endl);
            }
            return builder.append("]").toString();
        }
        return value.toString();
    }

    public enum Kind {
        STRING, BOOLEAN, NUMBER, ENUM, MAP, LIST, OPTION
    }
}
