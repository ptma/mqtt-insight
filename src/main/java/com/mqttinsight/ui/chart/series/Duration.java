package com.mqttinsight.ui.chart.series;

import lombok.*;

/**
 * @author ptma
 */
@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
public class Duration {

    private int duration;

    private TimeUnit unit;

    public long toMillis() {
        return this.unit.toMillis(duration);
    }

    @Override
    public String toString() {
        return String.format("%d %s", duration, unit.getText());
    }
}
