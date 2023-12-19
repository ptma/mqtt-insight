package com.mqttinsight.ui.chart.series;

import com.mqttinsight.ui.component.Textable;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;

/**
 * @author ptma
 */
@Getter
public enum ExtractingMode implements Textable {

    PAYLOAD(LangUtil.getString("PayloadContent"), false),
    REGEXP(LangUtil.getString("RegularExpression"), true),
    JSON_PATH(LangUtil.getString("JsonPath"), true),
    XPATH("XPath", true);

    private final String text;
    private final boolean supportsExpression;

    ExtractingMode(String text, boolean supportsExpression) {
        this.text = text;
        this.supportsExpression = supportsExpression;
    }

    @Override
    public String toString() {
        return name();
    }

    public static ExtractingMode of(String name) {
        for (ExtractingMode c : ExtractingMode.values()) {
            if (c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return ExtractingMode.PAYLOAD;
    }
}
