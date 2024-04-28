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
    LTE("<=");

    private final String text;

    ValueComparator(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return name();
    }

    /**
     * 根据给定的匹配表达式和值判断是否匹配。
     *
     * @param expression 匹配表达式，包含要比较的值和比较运算符。
     * @param value      需要与表达式比较的值。
     * @return 如果给定的值与表达式匹配，则返回true；否则返回false。
     */
    public static boolean match(MatchExpression expression, String value) {
        if (value == null) {
            return false;
        }
        String expected = expression.getValue();
        if (expected == null) {
            expected = "";
        }
        switch (expression.getComparator()) {
            case EQUALS -> {
                return expected.equals(value);
            }
            case NOT_EQUALS -> {
                return !expected.equals(value);
            }
            case CONTAINS -> {
                return StrUtil.contains(value, expected);
            }
            case NOT_CONTAINS -> {
                return !StrUtil.contains(value, expected);
            }
            case GT -> {
                if (!NumberUtil.isNumber(expected) || !NumberUtil.isNumber(value)) {
                    return false;
                }
                return NumberUtil.parseNumber(value).doubleValue() > NumberUtil.parseNumber(expected).doubleValue();
            }
            case GTE -> {
                if (!NumberUtil.isNumber(expected) || !NumberUtil.isNumber(value)) {
                    return false;
                }
                return NumberUtil.parseNumber(value).doubleValue() >= NumberUtil.parseNumber(expected).doubleValue();
            }
            case LT -> {
                if (!NumberUtil.isNumber(expected) || !NumberUtil.isNumber(value)) {
                    return false;
                }
                return NumberUtil.parseNumber(value).doubleValue() < NumberUtil.parseNumber(expected).doubleValue();
            }
            case LTE -> {
                if (!NumberUtil.isNumber(expected) || !NumberUtil.isNumber(value)) {
                    return false;
                }
                return NumberUtil.parseNumber(value).doubleValue() <= NumberUtil.parseNumber(expected).doubleValue();
            }
            default -> {
                return false;
            }
        }
    }
}
