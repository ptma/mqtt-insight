package com.mqttinsight.ui.chart.series;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttinsight.ui.component.Textable;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;

/**
 * @author ptma
 */

@Getter
public enum ValueComparator implements Textable {

    EQUALS(LangUtil.getString("Equals")),
    NOT_EQUALS(LangUtil.getString("NotEquals")),
    CONTAINS(LangUtil.getString("Contains")),
    NOT_CONTAINS(LangUtil.getString("NotContains")),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<=")
    ;

    private final String text;

    ValueComparator(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return name();
    }

    public static boolean match(ValueComparator comparator, String expected, String readValue) {
        if (expected == null) {
            expected = "";
        }
        switch (comparator) {
            case EQUALS -> {
                return expected.equals(readValue);
            }
            case NOT_EQUALS -> {
                return !expected.equals(readValue);
            }
            case CONTAINS -> {
                return StrUtil.contains(readValue, expected);
            }
            case NOT_CONTAINS -> {
                return !StrUtil.contains(readValue, expected);
            }
            case GT -> {
                if (!NumberUtil.isNumber(expected) || !NumberUtil.isNumber(readValue)) {
                    return false;
                }
                return NumberUtil.parseNumber(readValue).doubleValue() > NumberUtil.parseNumber(expected).doubleValue();
            }
            case GTE -> {
                if (!NumberUtil.isNumber(expected) || !NumberUtil.isNumber(readValue)) {
                    return false;
                }
                return NumberUtil.parseNumber(readValue).doubleValue() >= NumberUtil.parseNumber(expected).doubleValue();
            }
            case LT -> {
                if (!NumberUtil.isNumber(expected) || !NumberUtil.isNumber(readValue)) {
                    return false;
                }
                return NumberUtil.parseNumber(readValue).doubleValue() < NumberUtil.parseNumber(expected).doubleValue();
            }
            case LTE -> {
                if (!NumberUtil.isNumber(expected) || !NumberUtil.isNumber(readValue)) {
                    return false;
                }
                return NumberUtil.parseNumber(readValue).doubleValue() <= NumberUtil.parseNumber(expected).doubleValue();
            }
            default -> {
                return false;
            }
        }
    }
}
