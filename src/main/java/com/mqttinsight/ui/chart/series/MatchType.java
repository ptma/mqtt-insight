package com.mqttinsight.ui.chart.series;

import com.mqttinsight.ui.component.Textable;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;

/**
 * @author ptma
 */

@Getter
public enum MatchType implements Textable {

    WILDCARD(LangUtil.getString("Wildcards"), false),
    REGEXP(LangUtil.getString("RegularExpression"), true),
    JSON_PATH(LangUtil.getString("JsonPath"), true);

    private final String text;
    private final boolean supportsDynamic;

    MatchType(String text, boolean supportsDynamic) {
        this.text = text;
        this.supportsDynamic = supportsDynamic;
    }

    @Override
    public String toString() {
        return name();
    }

    public static MatchType of(String name) {
        for (MatchType c : MatchType.values()) {
            if (c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return MatchType.WILDCARD;
    }

}
