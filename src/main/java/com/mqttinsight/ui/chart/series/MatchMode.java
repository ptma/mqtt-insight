package com.mqttinsight.ui.chart.series;

import com.mqttinsight.ui.component.Textable;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;

/**
 * @author ptma
 */

@Getter
public enum MatchMode implements Textable {

    WILDCARD(LangUtil.getString("Wildcards"), false),
    REGEXP(LangUtil.getString("RegularExpression"), true),
    JSON_PATH(LangUtil.getString("JsonPath"), true),
    XPATH("XPath", true);

    private final String text;
    private final boolean supportsDynamic;

    MatchMode(String text, boolean supportsDynamic) {
        this.text = text;
        this.supportsDynamic = supportsDynamic;
    }

    @Override
    public String toString() {
        return name();
    }

    public static MatchMode of(String name) {
        for (MatchMode c : MatchMode.values()) {
            if (c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return MatchMode.WILDCARD;
    }

}
