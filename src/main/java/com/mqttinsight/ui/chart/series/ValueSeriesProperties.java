package com.mqttinsight.ui.chart.series;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ptma
 */
@Getter
@Setter
@EqualsAndHashCode
public class ValueSeriesProperties implements SeriesProperties {
    private String seriesName;
    private Match match;
    private MatchMode matchMode;
    private MatchExpression matchExpression;
    private ExtractingMode extractingMode;
    private String extractingExpression;

    private transient LimitedList<ValueSeriesXYData> xyDatas = new LimitedList<>();

    public void setXYDataLimit(Limit limit) {
        xyDatas.setLimit(limit);
    }

    public void resetDatas() {
        xyDatas.clear();
    }

    public List<Date> xDataList() {
        return xyDatas.stream().map(ValueSeriesXYData::getXData).collect(Collectors.toList());
    }

    public List<Number> yDataList() {
        return xyDatas.stream().map(ValueSeriesXYData::getYData).collect(Collectors.toList());
    }

    public void addXyData(Date xData, Number yData) {
        xyDatas.add(ValueSeriesXYData.of(xData, yData));
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    static class ValueSeriesXYData implements TimeBasedElement {
        // X轴数据
        private final Date xData;
        // Y轴数据
        private final Number yData;

        @Override
        public long getTimestamp() {
            return xData.getTime();
        }
    }
}
