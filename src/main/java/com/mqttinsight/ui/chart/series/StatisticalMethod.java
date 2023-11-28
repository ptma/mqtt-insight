package com.mqttinsight.ui.chart.series;

import com.mqttinsight.ui.component.Textable;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;

/**
 * @author ptma
 */

@Getter
public enum StatisticalMethod implements Textable {

    COUNT(LangUtil.getString("MessageCount")),
    AVG(LangUtil.getString("AverageMessageSize")),
    SUM(LangUtil.getString("SumOfMessageSize")),
    MAX(LangUtil.getString("MaximumMessageSize")),
    MIN(LangUtil.getString("MinimumMessageSize"));

    private final String text;

    StatisticalMethod(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return name();
    }

    public static StatisticalMethod of(String name) {
        for (StatisticalMethod c : StatisticalMethod.values()) {
            if (c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return StatisticalMethod.AVG;
    }

}
