package com.mqttinsight.ui.chart.series;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ptma
 */
@Getter
@Setter
public class MessageSeriesDefinition {
    private boolean dynamic;
    private String seriesName;
    private Match match;
    private MatchType matchType;
    private MatchExpression matchExpression;
}
