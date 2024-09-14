package com.mqttinsight.ui.chart.series;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@EqualsAndHashCode
public class ValueSeriesProperties implements SeriesProperties {
    @Getter
    @Setter
    private String seriesName;
    @Getter
    @Setter
    private Match match;
    @Getter
    @Setter
    private MatchMode matchMode;
    @Getter
    @Setter
    private MatchExpression matchExpression;
    @Getter
    @Setter
    private ExtractingMode extractingMode;
    @Getter
    @Setter
    private String extractingExpression;

    @JsonIgnore
    private final transient LimitedList<ValueSeriesXYData> xyDatas = new LimitedList<>();

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
