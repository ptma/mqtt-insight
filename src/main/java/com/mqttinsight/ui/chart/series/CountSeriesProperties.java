package com.mqttinsight.ui.chart.series;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ptma
 */
@Getter
@Setter
public class CountSeriesProperties implements SeriesProperties {
    private boolean dynamic;
    private String seriesName;
    private Match match;
    private MatchMode matchMode;
    private MatchExpression matchExpression;
}
