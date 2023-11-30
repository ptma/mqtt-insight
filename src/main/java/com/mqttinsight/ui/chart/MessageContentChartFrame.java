package com.mqttinsight.ui.chart;

import cn.hutool.core.img.ColorUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.chart.series.*;
import com.mqttinsight.ui.component.PopupMenuButton;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.table.TableColumnExt;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author ptma
 */
@Slf4j
public class MessageContentChartFrame extends BaseChartFrame<ValueSeriesProperties> {

    private PopupMenuButton seriesLimitButton;

    private ExecutorService executorService;
    private XYChart chart;
    private XChartPanel chartPanel;

    private final static List<Limit> seriesLimitList = Arrays.asList(
        Limit.of(100, 0, "100 " + LangUtil.getString("DataPoints")),
        Limit.of(200, 0, "200 " + LangUtil.getString("DataPoints")),
        Limit.of(500, 0, "500 " + LangUtil.getString("DataPoints")),
        Limit.of(1000, 0, "1K " + LangUtil.getString("DataPoints")),
        Limit.of(0, 1000 * 60, "1 " + LangUtil.getString("Minutes")),
        Limit.of(0, 1000 * 60 * 5, "5 " + LangUtil.getString("Minutes")),
        Limit.of(0, 1000 * 60 * 10, "10 " + LangUtil.getString("Minutes")),
        Limit.of(0, 1000 * 60 * 30, "30 " + LangUtil.getString("Minutes")),
        Limit.of(0, 1000 * 60 * 60, "1 " + LangUtil.getString("Hours"))
    );
    private Limit defaultSeriesLimit = seriesLimitList.get(0);


    public static void open(MqttInstance mqttInstance) {
        JFrame dialog = new MessageContentChartFrame(mqttInstance);
        dialog.setMinimumSize(new Dimension(800, 600));
        dialog.setResizable(true);
        dialog.pack();
        dialog.setLocationRelativeTo(MqttInsightApplication.frame);
        dialog.setVisible(true);
    }

    private MessageContentChartFrame(MqttInstance mqttInstance) {
        super(mqttInstance);
        createUIComponents();
        initComponents();
        initChart();
        initMessageEvent();
        setTitle(String.format(LangUtil.getString("MessageContentStatisticsChartTitle"), mqttInstance.getProperties().getName()));
    }

    @Override
    protected void addSeriesAction(ActionEvent e) {
        ValueSeriesEditor.open(MessageContentChartFrame.this, null, newSeries -> {
            newSeries.setXYDataLimit(defaultSeriesLimit);
            seriesTableModel.addRow(newSeries);
        });
    }

    @Override
    protected void doubleClickOnTableRow(int rowIndex) {
        ValueSeriesProperties oldSeries = seriesTableModel.getRow(seriesTable.convertRowIndexToModel(rowIndex));
        String oldSeriesName = oldSeries.getSeriesName();
        ValueSeriesEditor.open(MessageContentChartFrame.this, oldSeries, newSeries -> {
            // Series Name changed
            if (!oldSeriesName.equals(newSeries.getSeriesName())) {
                seriesNameChanged(oldSeriesName, newSeries.getSeriesName());
            }
            newSeries.setXYDataLimit(defaultSeriesLimit);
            seriesTableModel.fireTableDataChanged();
        });
    }

    @Override
    protected void removeSeriesAction(ActionEvent e) {
        int selectedRow = seriesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRowIndex = seriesTable.convertRowIndexToModel(selectedRow);
            ValueSeriesProperties series = seriesTableModel.getRow(modelRowIndex);
            seriesTableModel.removeRow(modelRowIndex);
            chart.removeSeries(series.getSeriesName());
        }
    }

    @Override
    protected void resetChartAction(ActionEvent e) {
        seriesTableModel.getSeries().forEach(ValueSeriesProperties::resetDatas);
        chart.getSeriesMap().clear();
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    @Override
    protected void onMessage(MqttMessage message) {
        executorService.execute(() -> {
            for (ValueSeriesProperties series : seriesTableModel.getSeries()) {
                if (messageMatchesSeries(series, message)) {
                    Number value = extractValue(series, message);
                    if (value != null) {
                        series.addXyData(new Date(message.getTimestamp()), value);
                        if (!isPaused()) {
                            if (chart.getSeriesMap().containsKey(series.getSeriesName())) {
                                chart.updateXYSeries(series.getSeriesName(), series.xDataList(), series.yDataList(), null);
                            } else {
                                chart.addSeries(series.getSeriesName(), series.xDataList(), series.yDataList());
                                XYSeries xySeries = chart.getSeriesMap().get(series.getSeriesName());
                                xySeries.setSmooth(true);
                            }
                            chartPanel.revalidate();
                            chartPanel.repaint();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected List<FavoriteSeries<ValueSeriesProperties>> getFavoriteSeries() {
        return mqttInstance.getProperties().getFavoriteValueSeries();
    }

    @Override
    protected void saveSeriesToFavorite(List<FavoriteSeries<ValueSeriesProperties>> favoriteSeries) {
        mqttInstance.getProperties().setFavoriteValueSeries(favoriteSeries);
    }

    @Override
    protected AbstractSeriesTableModel<ValueSeriesProperties> createSeriesTableModel() {
        return new ValueSeriesTableModel();
    }

    private void initComponents() {
        initTableColumns();

        //Chart series data length or time limit
        ButtonGroup limitGroup = new ButtonGroup();
        seriesLimitList.forEach(limit -> {
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(limit.getName());
            seriesLimitButton.addMenuItem(menuItem);
            limitGroup.add(menuItem);
            menuItem.addActionListener(e -> {
                seriesLimitButton.setText(limit.getName());
                menuItem.setSelected(true);
                defaultSeriesLimit = limit;
                seriesTableModel.getSeries().forEach(series -> {
                    series.setXYDataLimit(defaultSeriesLimit);
                });
            });
            if (defaultSeriesLimit.equals(limit)) {
                menuItem.setSelected(true);
            }
        });
        seriesLimitButton.setText(defaultSeriesLimit.getName());
    }

    private void initMessageEvent() {
        executorService = ThreadUtil.newFixedExecutor(1, "Value Chart ", false);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                executorService.shutdown();
                super.windowClosing(e);
            }
        });
    }

    private void initTableColumns() {
        // Name column
        TableColumnExt column = seriesTable.getColumnExt(0);
        column.setPreferredWidth(100);
        column.setWidth(100);
        // Match column
        column = seriesTable.getColumnExt(1);
        column.setPreferredWidth(80);
        column.setWidth(80);
        column.setMinWidth(80);
        column.setMaxWidth(80);
        column.putClientProperty("Alignment", JLabel.CENTER);
        // Type column
        column = seriesTable.getColumnExt(2);
        column.setPreferredWidth(100);
        column.setWidth(100);
        column.setMinWidth(80);
        column.setMaxWidth(150);
        column.putClientProperty("Alignment", JLabel.CENTER);
        // Expression column
        column = seriesTable.getColumnExt(3);
        column.setPreferredWidth(300);
        column.setWidth(300);
        // Method column
        column = seriesTable.getColumnExt(4);
        column.setPreferredWidth(100);
        column.setWidth(100);
        column.putClientProperty("Alignment", JLabel.CENTER);
        // Window column
        column = seriesTable.getColumnExt(5);
        column.setPreferredWidth(300);
        column.setWidth(300);
    }

    private void initChart() {
        chart = new XYChartBuilder()
            .width(bottomPanel.getPreferredSize().width)
            .height(bottomPanel.getPreferredSize().height)
            .build();
        chart.getStyler().setShowWithinAreaPoint(true);
        chart.getStyler().setYAxisTickLabelsColor(UIManager.getColor("Label.foreground"));
        chart.getStyler().setXAxisTickLabelsColor(UIManager.getColor("Label.foreground"));
        chart.getStyler().setPlotGridLinesColor(UIManager.getColor("Component.borderColor"));
        chart.getStyler().setZoomEnabled(true);
        chart.getStyler().setZoomResetByDoubleClick(true);
        chart.getStyler().setDatePattern("HH:mm:ss");
        chart.getStyler().setChartPadding(10);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
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

        bottomPanel.removeAll();
        bottomPanel.add(chartPanel, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private Number extractValue(ValueSeriesProperties series, MqttMessage message) {
        String payload = message.payloadAsString(false);
        if (StrUtil.isBlank(payload)) {
            return null;
        }
        payload = payload.trim();
        String value = switch (series.getExtractingMode()) {
            case PAYLOAD -> payload;
            case REGEXP -> Utils.findRegexMatchGroup(series.getExtractingExpression(), payload);
            case JSON_PATH -> Utils.getByJsonPath(series.getExtractingExpression(), payload);
            case XPATH -> Utils.getByXPath(series.getExtractingExpression(), payload);
        };
        return NumberUtil.isNumber(value) ? NumberUtil.parseNumber(value).doubleValue() : null;
    }

    private void seriesNameChanged(String oldSeriesName, String newSeriesName) {
        SwingUtilities.invokeLater(() -> {
            chart.removeSeries(oldSeriesName);
            chartPanel.revalidate();
            chartPanel.repaint();
        });
    }

    private void createUIComponents() {
        toolbar.add(new JToolBar.Separator());
        seriesLimitButton = new PopupMenuButton("", Icons.CHART_LINE, true);
        toolbar.add(seriesLimitButton);
    }

}
