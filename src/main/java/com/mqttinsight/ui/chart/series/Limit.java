package com.mqttinsight.ui.chart.series;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author ptma
 */
@Getter
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class Limit {

    private final int sizeLimit;
    private final long timeLimit;
    private final String name;

}
