package com.mqttinsight.ui.chart.series;

import com.mqttinsight.util.LangUtil;

/**
 * @author ptma
 */
public class LoadSeriesTableModel extends AbstractSeriesTableModel<LoadSeriesProperties> {

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int row, int column) {
        LoadSeriesProperties item = getSeries().get(row);
        return switch (column) {
            case 0 -> item.getSeriesName();
            case 1 -> item.getMatch().getText();
            case 2 -> item.getMatchMode().getText();
            case 3 -> item.getMatchExpression().toString();
            case 4 -> item.getStatisticalMethod().getText();
            case 5 -> item.getWindow().toString();
            default -> "";
        };
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> LangUtil.getString("SeriesName");
            case 1 -> LangUtil.getString("Match");
            case 2 -> LangUtil.getString("MatchMode");
            case 3 -> LangUtil.getString("MatchExpression");
            case 4 -> LangUtil.getString("StatisticalMethod");
            case 5 -> LangUtil.getString("StatisticalWindow");
            default -> "-";
        };
    }

}
