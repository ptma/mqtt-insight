package com.mqttinsight.ui.chart.series;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author ptma
 */
@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class FavoriteSeries {

    private String name;

    private List<MessageSeriesDefinition> series;

}
