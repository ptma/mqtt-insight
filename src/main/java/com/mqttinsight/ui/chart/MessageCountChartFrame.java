package com.mqttinsight.ui.chart;

import cn.hutool.core.img.ColorUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.chart.series.*;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.table.TableColumnExt;
import org.knowm.xchart.*;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ptma
 */
@Slf4j
public class MessageCountChartFrame extends BaseChartFrame<CountSeriesProperties> {
    private JToggleButton pieChartButton;
    private JToggleButton barChartButton;

    private Map<String, AtomicInteger> seriesCache = new ConcurrentHashMap<>();

    private Chart chart;
    private ChartMode chartMode;
    private XChartPanel chartPanel;

    public static void open(MqttInstance mqttInstance) {
        JFrame dialog = new MessageCountChartFrame(mqttInstance);
        dialog.setMinimumSize(new Dimension(800, 600));
        dialog.setResizable(true);
        dialog.pack();
        dialog.setLocationRelativeTo(MqttInsightApplication.frame);
        dialog.setVisible(true);
    }

    private MessageCountChartFrame(MqttInstance mqttInstance) {
        super(mqttInstance);
        createUIComponents();
        initComponents();
        initChart(ChartMode.PIE);
        setTitle(String.format(LangUtil.getString("MessageCountStatisticsChartTitle"), mqttInstance.getProperties().getName()));
    }

    @Override
    protected void bottomPanelResized(int width, int height) {
        // do nothing
    }

    @Override
    protected void addSeriesAction(ActionEvent e) {
        CountSeriesEditor.open(MessageCountChartFrame.this, null, newSeries -> {
            seriesTableModel.addRow(newSeries);
        });
    }

    @Override
    protected void doubleClickOnTableRow(int rowIndex) {
        CountSeriesProperties oldSeries = seriesTableModel.getRow(seriesTable.convertRowIndexToModel(rowIndex));
        String oldSeriesName = oldSeries.getSeriesName();
        CountSeriesEditor.open(MessageCountChartFrame.this, oldSeries, newSeries -> {
            // Series Name changed
            if (!oldSeriesName.equals(newSeries.getSeriesName())) {
                seriesNameChanged(oldSeriesName, newSeries.getSeriesName());
            }
            seriesTableModel.fireTableDataChanged();
        });
    }

    @Override
    protected void removeSeriesAction(ActionEvent e) {
        int selectedRow = seriesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRowIndex = seriesTable.convertRowIndexToModel(selectedRow);
            CountSeriesProperties series = seriesTableModel.getRow(modelRowIndex);
            seriesTableModel.removeRow(modelRowIndex);
            seriesCache.remove(series.getSeriesName());
            chart.removeSeries(series.getSeriesName());
        }
    }

    @Override
    protected void resetChartAction(ActionEvent e) {
        seriesCache.clear();
        chart.getSeriesMap().clear();
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    @Override
    protected AbstractSeriesTableModel<CountSeriesProperties> createSeriesTableModel() {
        return new CountSeriesTableModel();
    }

    @Override
    protected void beforeSeriesLoad(CountSeriesProperties series) {

    }

    @Override
    protected void onMessage(MqttMessage message) {
        for (CountSeriesProperties series : seriesTableModel.getSeries()) {
            if (series.isDynamic()) {
                dynamicSeries(series, message);
            } else {
                if (messageMatchesSeries(series, message)) {
                    saveOrUpdateSeriesData(series.getSeriesName());
                }
            }
        }
    }

    @Override
    protected List<FavoriteSeries<CountSeriesProperties>> getFavoriteSeries() {
        return mqttInstance.getProperties().getFavoriteCountSeries();
    }

    @Override
    protected void saveSeriesToFavorite(List<FavoriteSeries<CountSeriesProperties>> favoriteSeries) {
        mqttInstance.getProperties().setFavoriteCountSeries(favoriteSeries);
    }

    private void initComponents() {
        initTableColumns();

        pieChartButton.addActionListener(this::chartChanged);
        barChartButton.addActionListener(this::chartChanged);
    }

    private void initTableColumns() {
        // Name column
        TableColumnExt column = seriesTable.getColumnExt(0);
        column.setPreferredWidth(100);
        column.setWidth(100);
        // Dynamic column
        column = seriesTable.getColumnExt(1);
        column.setPreferredWidth(60);
        column.setWidth(60);
        column.setMinWidth(60);
        column.setMaxWidth(60);
        column.putClientProperty("Alignment", JLabel.CENTER);
        // Match column
        column = seriesTable.getColumnExt(2);
        column.setPreferredWidth(80);
        column.setWidth(80);
        column.setMinWidth(80);
        column.setMaxWidth(80);
        column.putClientProperty("Alignment", JLabel.CENTER);
        // Type column
        column = seriesTable.getColumnExt(3);
        column.setPreferredWidth(130);
        column.setWidth(130);
        column.setMinWidth(100);
        column.setMaxWidth(150);
        column.putClientProperty("Alignment", JLabel.CENTER);
        // Expression column
        column = seriesTable.getColumnExt(4);
        column.setPreferredWidth(300);
        column.setWidth(300);
    }

    private void initChart(ChartMode chartMode) {
        this.chartMode = chartMode;
        if (ChartMode.PIE.equals(chartMode)) {
            PieChart pieChart = new PieChartBuilder()
                .width(bottomPanel.getPreferredSize().width)
                .height(bottomPanel.getPreferredSize().height)
                .build();
            pieChart.getStyler().setLabelType(PieStyler.LabelType.NameAndPercentage);
            pieChart.getStyler().setLabelsDistance(1.15);
            pieChart.getStyler().setSliceBorderWidth(2);
            pieChart.getStyler().setPlotContentSize(0.8);
            if (UIManager.getBoolean("laf.dark")) {
                pieChart.getStyler().setLabelsFontColor(UIManager.getColor("Label.foreground"));
                pieChart.getStyler().setLabelsFontColorAutomaticEnabled(false);
            }
            chart = pieChart;
        } else {
            CategoryChart barChart = new CategoryChartBuilder()
                .width(bottomPanel.getPreferredSize().width)
                .height(bottomPanel.getPreferredSize().height)
                .build();
            barChart.getStyler().setShowWithinAreaPoint(true);
            barChart.getStyler().setLabelsVisible(true);
            barChart.getStyler().setLabelsPosition(0.9);
            barChart.getStyler().setXAxisTicksVisible(false);
            if (UIManager.getBoolean("laf.dark")) {
                barChart.getStyler().setLabelsFontColor(UIManager.getColor("Label.foreground"));
                barChart.getStyler().setLabelsFontColorAutomaticEnabled(false);
                barChart.getStyler().setPlotGridLinesColor(UIManager.getColor("Component.borderColor"));
                barChart.getStyler().setYAxisTickLabelsColor(UIManager.getColor("Label.foreground"));
                barChart.getStyler().setXAxisTickLabelsColor(UIManager.getColor("Label.foreground"));
            }
            chart = barChart;
        }

        chart.getStyler().setChartPadding(10);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setToolTipsEnabled(true);
        chart.getStyler().setToolTipType(Styler.ToolTipType.xAndYLabels);
        chart.getStyler().setBaseFont(UIManager.getFont("Label.font"));
        if (UIManager.getBoolean("laf.dark")) {
            chart.getStyler().setChartFontColor(UIManager.getColor("Label.foreground"));
            chart.getStyler().setChartBackgroundColor(UIManager.getColor("Panel.background"));

            chart.getStyler().setPlotBackgroundColor(ColorUtil.hexToColor("#282c34"));
            chart.getStyler().setPlotBorderColor(UIManager.getColor("Component.borderColor"));
            chart.getStyler().setPlotContentSize(0.8);

            chart.getStyler().setLegendBackgroundColor(ColorUtil.hexToColor("#282c34"));
            chart.getStyler().setLegendBorderColor(UIManager.getColor("Component.borderColor"));

            chart.getStyler().setAnnotationTextPanelBorderColor(UIManager.getColor("Component.borderColor"));
            chart.getStyler().setAnnotationTextPanelBackgroundColor(ColorUtil.hexToColor("#282c34"));
            chart.getStyler().setAnnotationTextFontColor(UIManager.getColor("Label.foreground"));

            chart.getStyler().setToolTipBackgroundColor(UIManager.getColor("ToolTip.background"));
            chart.getStyler().setToolTipFont(UIManager.getFont("ToolTip.font"));
            chart.getStyler().setToolTipBorderColor(UIManager.getColor("Component.borderColor"));
        }
        chartPanel = new XChartPanel(chart);
        chartPanel.setSaveAsString(LangUtil.getString("SaveAs"));
        chartPanel.setPrintString(LangUtil.getString("Print"));
        chartPanel.setResetString(LangUtil.getString("ResetZoom"));
        chartPanel.setExportAsString(LangUtil.getString("ExportAs"));


        if (!seriesCache.isEmpty()) {
            seriesCache.forEach((seriesName, seriesValue) -> {
                if (ChartMode.PIE.equals(chartMode)) {
                    ((PieChart) chart).addSeries(seriesName, seriesValue.intValue());
                } else {
                    ((CategoryChart) chart).addSeries(seriesName, List.of(seriesName), List.of(seriesValue.intValue()));
                }
            });
        }
        bottomPanel.removeAll();
        bottomPanel.add(chartPanel, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void chartChanged(ActionEvent e) {
        ChartMode chartMode = pieChartButton.isSelected() ? ChartMode.PIE : ChartMode.BAR;
        initChart(chartMode);
    }

    private void dynamicSeries(CountSeriesProperties series, MqttMessage message) {
        if (!series.isDynamic()) {
            return;
        }
        switch (series.getMatch()) {
            case TOPIC -> {
                // only Regular expression
                String seriesName = Utils.findRegexMatchGroup(series.getMatchExpression().getExpression(), message.getTopic());
                saveOrUpdateSeriesData(seriesName);
            }
            case PAYLOAD -> {
                switch (series.getMatchMode()) {
                    case REGEXP -> {
                        String seriesName = Utils.findRegexMatchGroup(series.getMatchExpression().getExpression(), message.getTopic());
                        saveOrUpdateSeriesData(seriesName);
                    }
                    case JSON_PATH -> {
                        String payloadStr = message.payloadAsString(false);
                        MatchExpression expression = series.getMatchExpression();
                        String seriesName = Utils.getSingleValueByJsonPath(expression.getExpression(), payloadStr);
                        saveOrUpdateSeriesData(seriesName);
                    }
                    case XPATH -> {
                        String payloadStr = message.payloadAsString(false);
                        MatchExpression expression = series.getMatchExpression();
                        String seriesName = Utils.getByXPath(expression.getExpression(), payloadStr);
                        saveOrUpdateSeriesData(seriesName);
                    }
                }
            }
        }
    }

    private void saveOrUpdateSeriesData(String seriesName) {
        if (StrUtil.isNotBlank(seriesName)) {
            SwingUtilities.invokeLater(() -> {
                AtomicInteger seriesValue = seriesCache.computeIfAbsent(seriesName, (key) -> new AtomicInteger(0));
                int value = seriesValue.incrementAndGet();
                if (!isPaused()) {
                    if (chart.getSeriesMap().containsKey(seriesName)) {
                        if (ChartMode.PIE.equals(chartMode)) {
                            PieSeries exists = ((PieChart) chart).getSeriesMap().get(seriesName);
                            exists.replaceData(value);
                        } else {
                            CategorySeries exist = ((CategoryChart) chart).getSeriesMap().get(seriesName);
                            exist.replaceData(List.of(value));
                        }
                    } else {
                        if (ChartMode.PIE.equals(chartMode)) {
                            ((PieChart) chart).addSeries(seriesName, value);
                        } else {
                            ((CategoryChart) chart).addSeries(seriesName, List.of(seriesName), List.of(value));
                        }
                    }
                    chartPanel.revalidate();
                    chartPanel.repaint();
                }
            });
        }
    }

    private void seriesNameChanged(String oldSeriesName, String newSeriesName) {
        SwingUtilities.invokeLater(() -> {
            if (chart.getSeriesMap().containsKey(oldSeriesName)) {
                if (ChartMode.PIE.equals(chartMode)) {
                    Map<String, PieSeries> seriesMap = ((PieChart) chart).getSeriesMap();
                    PieSeries chartSeries = seriesMap.get(oldSeriesName);
                    chartSeries.setLabel(newSeriesName);
                    seriesMap.remove(oldSeriesName);
                    seriesMap.put(newSeriesName, chartSeries);
                } else {
                    Map<String, CategorySeries> seriesMap = ((CategoryChart) chart).getSeriesMap();
                    CategorySeries chartSeries = seriesMap.get(oldSeriesName);
                    chartSeries.setLabel(newSeriesName);
                    seriesMap.remove(oldSeriesName);
                    seriesMap.put(newSeriesName, chartSeries);
                }
            }
            if (seriesCache.containsKey(oldSeriesName)) {
                AtomicInteger data = seriesCache.get(oldSeriesName);
                seriesCache.remove(oldSeriesName);
                seriesCache.put(newSeriesName, data);
            }
            chartPanel.revalidate();
            chartPanel.repaint();
        });
    }

    private void createUIComponents() {
        toolbar.addSeparator();

        pieChartButton = new JToggleButton(Icons.CHART_PIE);
        pieChartButton.setToolTipText(ChartMode.PIE.getText());
        barChartButton = new JToggleButton(Icons.CHART_BAR);
        barChartButton.setToolTipText(ChartMode.BAR.getText());
        ButtonGroup viewButtonGroup = new ButtonGroup();
        viewButtonGroup.add(pieChartButton);
        viewButtonGroup.add(barChartButton);
        pieChartButton.setSelected(true);
        toolbar.add(pieChartButton);
        toolbar.add(barChartButton);
    }
}
