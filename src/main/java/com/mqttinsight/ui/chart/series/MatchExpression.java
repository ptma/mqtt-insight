package com.mqttinsight.ui.chart.series;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ptma
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchExpression {

    private String expression;

    private ValueComparator comparator;

    private String value;

    @Override
    public String toString() {
        return String.format("%s %s %s", expression, comparator != null ? comparator.getText() : "", value != null ? value : "");
    }

    public static MatchExpression normal(String expression) {
        MatchExpression matchExpression = new MatchExpression();
        matchExpression.setExpression(expression);
        return matchExpression;
    }

    public static MatchExpression jsonPath(String expression, ValueComparator comparator, String value) {
        MatchExpression matchExpression = new MatchExpression();
        matchExpression.setExpression(expression);
        matchExpression.setComparator(comparator);
        matchExpression.setValue(value);
        return matchExpression;
    }
}
