package com.mqttinsight.ui.chart.series;

import com.mqttinsight.ui.component.Textable;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;

/**
 * @author ptma
 */

@Getter
public enum Match implements Textable {

    TOPIC(LangUtil.getString("Topic"), new MatchMode[]{MatchMode.WILDCARD, MatchMode.REGEXP}),
    PAYLOAD(LangUtil.getString("Payload"), new MatchMode[]{MatchMode.REGEXP, MatchMode.JSON_PATH, MatchMode.XPATH});

    private final String text;
    private final MatchMode[] matchModes;

    Match(String text, MatchMode[] matchModes) {
        this.text = text;
        this.matchModes = matchModes;
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
