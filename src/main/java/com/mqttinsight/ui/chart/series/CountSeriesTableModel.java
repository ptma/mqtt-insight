package com.mqttinsight.ui.chart.series;

import com.mqttinsight.util.LangUtil;

/**
 * @author ptma
 */
public class CountSeriesTableModel extends AbstractSeriesTableModel<CountSeriesProperties> {

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int row, int column) {
        return switch (column) {
            case 0 -> getSeries().get(row).getSeriesName();
            case 1 -> getSeries().get(row).isDynamic();
            case 2 -> getSeries().get(row).getMatch().getText();
            case 3 -> getSeries().get(row).getMatchMode().getText();
            case 4 -> getSeries().get(row).getMatchExpression().toString();
            default -> "";
        };
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> LangUtil.getString("SeriesName");
            case 1 -> LangUtil.getString("Dynamic");
            case 2 -> LangUtil.getString("Match");
            case 3 -> LangUtil.getString("Mode");
            case 4 -> LangUtil.getString("Expression");
            default -> "-";
        };
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 1) {
            return Boolean.class;
        }
        return Object.class;
    }

}
