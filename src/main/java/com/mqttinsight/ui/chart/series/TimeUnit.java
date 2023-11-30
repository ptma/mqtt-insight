package com.mqttinsight.ui.chart.series;

import com.mqttinsight.ui.component.Textable;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;


/**
 * @author ptma
 */

@Getter
public enum TimeUnit implements Textable {

    SECONDS(LangUtil.getString("Seconds"), java.util.concurrent.TimeUnit.SECONDS),
    MINUTES(LangUtil.getString("Minutes"), java.util.concurrent.TimeUnit.MINUTES),
    HOURS(LangUtil.getString("Hours"), java.util.concurrent.TimeUnit.HOURS);

    private final String text;
    private final java.util.concurrent.TimeUnit timeUnit;

    TimeUnit(String text, java.util.concurrent.TimeUnit timeUnit) {
        this.text = text;
        this.timeUnit = timeUnit;
    }

    @Override
    public String toString() {
        return name();
    }

    public long toMillis(long duration) {
        return timeUnit.toMillis(duration);
    }

    public static TimeUnit of(String name) {
        for (TimeUnit c : TimeUnit.values()) {
            if (c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return TimeUnit.SECONDS;
    }

}
