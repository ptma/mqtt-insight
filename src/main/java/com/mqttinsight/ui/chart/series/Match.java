package com.mqttinsight.ui.chart.series;

import com.mqttinsight.ui.component.Textable;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;

/**
 * @author ptma
 */

@Getter
public enum Match implements Textable {

    TOPIC(LangUtil.getString("Topic"), new MatchType[]{MatchType.WILDCARD, MatchType.REGEXP}),
    PAYLOAD(LangUtil.getString("Payload"), new MatchType[]{MatchType.REGEXP, MatchType.JSON_PATH});

    private final String text;
    private final MatchType[] matchTypes;

    Match(String text, MatchType[] matchTypes) {
        this.text = text;
        this.matchTypes = matchTypes;
    }

    @Override
    public String toString() {
        return name();
    }

    public static Match of(String name) {
        for (Match c : Match.values()) {
            if (c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return Match.TOPIC;
    }

}
