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
public class LoadSeriesProperties implements SeriesProperties {
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
    private StatisticalMethod statisticalMethod;
    @Getter
    private Duration window;

    @JsonIgnore
    private final transient LimitedList<LoadSeriesMessage> messages = new LimitedList<>();
    @JsonIgnore
    private final transient LimitedList<LoadSeriesXYData> xyDatas = new LimitedList<>();

    public void setXYDataLimit(Limit limit) {
        xyDatas.setLimit(limit);
    }

    public void resetDatas() {
        messages.clear();
        xyDatas.clear();
    }

    public void setWindow(Duration window) {
        this.window = window;
        if (window != null && window.getDuration() > 0 && window.getUnit() != null) {
            messages.setLimit(Limit.of(0, window.toMillis(), window.toString()));
        } else {
            messages.setLimit(Limit.of(0, 0, "No Limit"));
        }
    }

    public void addMessageData(long timestamp, int messageSize) {
        messages.add(LoadSeriesMessage.of(timestamp, messageSize));
    }

    public void calculateStatisticalValue(Date date) {
        Number value;
        switch (getStatisticalMethod()) {
            case COUNT -> {
                value = messages.size();
            }
            case SUM -> {
                value = messages.stream()
                    .collect(Collectors.summarizingInt(LoadSeriesProperties.LoadSeriesMessage::getMessageSize))
                    .getSum();
            }
            case AVG -> {
                value = messages.stream()
                    .collect(Collectors.averagingInt(LoadSeriesProperties.LoadSeriesMessage::getMessageSize))
                    .intValue();
            }
            case MAX -> {
                value = messages.stream()
                    .map(LoadSeriesProperties.LoadSeriesMessage::getMessageSize)
                    .max(Integer::compareTo)
                    .orElse(0);
            }
            case MIN -> {
                value = messages.stream()
                    .map(LoadSeriesProperties.LoadSeriesMessage::getMessageSize)
                    .min(Integer::compareTo)
                    .orElse(0);
            }
            default -> {
                return;
            }
        }
        xyDatas.add(LoadSeriesXYData.of(date, value));
    }

    public List<Date> xDataList() {
        return xyDatas.stream().map(LoadSeriesXYData::getXData).collect(Collectors.toList());
    }

    public List<Number> yDataList() {
        return xyDatas.stream().map(LoadSeriesXYData::getYData).collect(Collectors.toList());
    }

    public
    @Getter
    @AllArgsConstructor(staticName = "of")
    static class LoadSeriesMessage implements TimeBasedElement {
        private final long timestamp;
        private final int messageSize;
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    static class LoadSeriesXYData implements TimeBasedElement {
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
