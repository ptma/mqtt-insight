package com.mqttinsight.ui.chart;

import com.mqttinsight.ui.component.Textable;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;

/**
 * @author ptma
 */

@Getter
public enum ChartMode implements Textable {

    PIE(LangUtil.getString("PieChart")), BAR(LangUtil.getString("BarChart"));

    private final String text;

    ChartMode(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return name();
    }

    public static ChartMode of(String name) {
        for (ChartMode c : ChartMode.values()) {
            if (c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return ChartMode.PIE;
    }

}
