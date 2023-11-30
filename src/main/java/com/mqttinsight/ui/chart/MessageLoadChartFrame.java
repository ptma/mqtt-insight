package com.mqttinsight.ui.chart;

import cn.hutool.core.img.ColorUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.chart.series.*;
import com.mqttinsight.ui.component.PopupMenuButton;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author ptma
 */
@Slf4j
public class MessageLoadChartFrame extends BaseChartFrame<LoadSeriesProperties> {

    private PopupMenuButton seriesIntervalButton;
    private PopupMenuButton seriesLimitButton;

    private ExecutorService executorService;
    private ScheduledThreadPoolExecutor scheduledExecutor;
    private XYChart chart;
    private XChartPanel chartPanel;

    private int sizeYAxisGroup = -1;
    private int countYAxisGroup = -1;

    private final static List<Duration> seriesIntervalList = Arrays.asList(
        Duration.of(1, TimeUnit.SECONDS),
        Duration.of(5, TimeUnit.SECONDS),
        Duration.of(10, TimeUnit.SECONDS),
        Duration.of(30, TimeUnit.SECONDS),
        Duration.of(1, TimeUnit.MINUTES)
    );
    private Duration defaultSeriesInterval = seriesIntervalList.get(0);

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
        JFrame dialog = new MessageLoadChartFrame(mqttInstance);
        dialog.setMinimumSize(new Dimension(800, 600));
        dialog.setResizable(true);
        dialog.pack();
        dialog.setLocationRelativeTo(MqttInsightApplication.frame);
        dialog.setVisible(true);
    }

    private MessageLoadChartFrame(MqttInstance mqttInstance) {
        super(mqttInstance);
        createUIComponents();
        initComponents();
        initChart();
        initMessageEvent();
        setTitle(String.format(LangUtil.getString("MessageLoadStatisticsChartTitle"), mqttInstance.getProperties().getName()));
    }

    @Override
    protected void addSeriesAction(ActionEvent e) {
        LoadSeriesEditor.open(MessageLoadChartFrame.this, null, newSeries -> {
            newSeries.setXYDataLimit(defaultSeriesLimit);
            seriesTableModel.addRow(newSeries);
        });
    }

    @Override
    protected void doubleClickOnTableRow(int rowIndex) {
        LoadSeriesProperties oldSeries = seriesTableModel.getRow(seriesTable.convertRowIndexToModel(rowIndex));
        String oldSeriesName = oldSeries.getSeriesName();
        LoadSeriesEditor.open(MessageLoadChartFrame.this, oldSeries, newSeries -> {
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
            LoadSeriesProperties series = seriesTableModel.getRow(modelRowIndex);
            seriesTableModel.removeRow(modelRowIndex);
            chart.removeSeries(series.getSeriesName());
        }
    }

    @Override
    protected void resetChartAction(ActionEvent e) {
        seriesTableModel.getSeries().forEach(LoadSeriesProperties::resetDatas);
        chart.getSeriesMap().clear();
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    @Override
    protected void onMessage(MqttMessage message) {
        executorService.execute(() -> {
            for (LoadSeriesProperties series : seriesTableModel.getSeries()) {
                if (messageMatchesSeries(series, message)) {
                    series.addMessageData(message.getTimestamp(), message.payloadSize());
                }
            }
        });
    }

    @Override
    protected List<FavoriteSeries<LoadSeriesProperties>> getFavoriteSeries() {
        return mqttInstance.getProperties().getFavoriteLoadSeries();
    }

    @Override
    protected void saveSeriesToFavorite(List<FavoriteSeries<LoadSeriesProperties>> favoriteSeries) {
        mqttInstance.getProperties().setFavoriteLoadSeries(favoriteSeries);
    }

    @Override
    protected AbstractSeriesTableModel<LoadSeriesProperties> createSeriesTableModel() {
        return new LoadSeriesTableModel();
    }

    private void initComponents() {
        initTableColumns();
        // Series table rows changed
        seriesTableModel.addTableModelListener(l -> {
            resetYAxisGroup();
            initScheduledSeriesDataTasks(false);
        });

        // Chart series data refresh interval
        ButtonGroup intervalGroup = new ButtonGroup();
        seriesIntervalList.forEach(duration -> {
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(duration.toString());
            seriesIntervalButton.addMenuItem(menuItem);
            intervalGroup.add(menuItem);
            menuItem.addActionListener(e -> {
                seriesIntervalButton.setText(duration.toString());
                menuItem.setSelected(true);
                if (!defaultSeriesInterval.equals(duration)) {
                    defaultSeriesInterval = duration;
                    initScheduledSeriesDataTasks(true);
                }
            });
            if (defaultSeriesInterval.equals(duration)) {
                menuItem.setSelected(true);
            }
        });
        seriesIntervalButton.setText(defaultSeriesInterval.toString());

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
        executorService = ThreadUtil.newFixedExecutor(1, "Load Chart ", false);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                executorService.shutdown();
                if (scheduledExecutor != null) {
                    scheduledExecutor.shutdownNow();
                }
                super.windowClosing(e);
            }
        });
    }

    private void initScheduledSeriesDataTasks(boolean reSchedule) {
        if (seriesTableModel.getSeries().isEmpty()) {
            return;
        }
        if (!reSchedule && scheduledExecutor != null) {
            return;
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdownNow();
        }
        scheduledExecutor = ThreadUtil.createScheduledExecutor(1);
        scheduledExecutor.scheduleAtFixedRate(() -> {
                Date now = new Date();
                seriesTableModel.getSeries().forEach(series -> {
                    addOrUpdateChartSeries(series, now);
                });
            },
            defaultSeriesInterval.toMillis(),
            defaultSeriesInterval.toMillis(),
            java.util.concurrent.TimeUnit.MILLISECONDS
        );
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
        column.setPreferredWidth(130);
        column.setWidth(130);
        column.setMinWidth(100);
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
        column.setPreferredWidth(100);
        column.setWidth(100);
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

    private void resetYAxisGroup() {
        countYAxisGroup = -1;
        sizeYAxisGroup = -1;
        for (LoadSeriesProperties series : seriesTableModel.getSeries()) {
            if (StatisticalMethod.COUNT.equals(series.getStatisticalMethod())) {
                // If the count index is not allocated
                if (countYAxisGroup == -1) {
                    countYAxisGroup = sizeYAxisGroup == -1 ? 0 : 1;
                }
                Optional.ofNullable(chart.getSeriesMap().get(series.getSeriesName()))
                    .ifPresent(xySeries -> {
                        xySeries.setYAxisGroup(countYAxisGroup);
                        chart.setYAxisGroupTitle(countYAxisGroup, LangUtil.getString("MessageCount"));
                    });
            } else {
                // If the size index is not allocated
                if (sizeYAxisGroup == -1) {
                    sizeYAxisGroup = countYAxisGroup == -1 ? 0 : 1;
                }
                Optional.ofNullable(chart.getSeriesMap().get(series.getSeriesName()))
                    .ifPresent(xySeries -> {
                        xySeries.setYAxisGroup(sizeYAxisGroup);
                        chart.setYAxisGroupTitle(sizeYAxisGroup, LangUtil.getString("MessageSizeAxis"));
                    });
            }
        }
    }

    private void addOrUpdateChartSeries(LoadSeriesProperties series, Date date) {
        SwingUtilities.invokeLater(() -> {
            series.calculateStatisticalValue(date);
            if (!isPaused()) {
                if (chart.getSeriesMap().containsKey(series.getSeriesName())) {
                    chart.updateXYSeries(series.getSeriesName(), series.xDataList(), series.yDataList(), null);
                } else {
                    chart.addSeries(series.getSeriesName(), series.xDataList(), series.yDataList());
                    XYSeries xySeries = chart.getSeriesMap().get(series.getSeriesName());
                    xySeries.setSmooth(true);
                    if (StatisticalMethod.COUNT.equals(series.getStatisticalMethod())) {
                        if (countYAxisGroup != -1) {
                            xySeries.setYAxisGroup(countYAxisGroup);
                            chart.setYAxisGroupTitle(countYAxisGroup, LangUtil.getString("MessageCount"));
                        }
                    } else {
                        if (sizeYAxisGroup != -1) {
                            xySeries.setYAxisGroup(sizeYAxisGroup);
                            chart.setYAxisGroupTitle(sizeYAxisGroup, LangUtil.getString("MessageSizeAxis"));
                        }
                    }
                }
                chartPanel.revalidate();
                chartPanel.repaint();
            }
        });
    }

    private void seriesNameChanged(String oldSeriesName, String newSeriesName) {
        SwingUtilities.invokeLater(() -> {
            chart.removeSeries(oldSeriesName);
            chartPanel.revalidate();
            chartPanel.repaint();
        });
    }

    private void createUIComponents() {
        seriesIntervalButton = new PopupMenuButton("", Icons.CLOCK, true);
        seriesLimitButton = new PopupMenuButton("", Icons.CHART_LINE, true);

        final JToolBar.Separator toolBar$Separator3 = new JToolBar.Separator();
        toolbar.add(toolBar$Separator3);
        toolbar.add(seriesIntervalButton);
        toolbar.add(seriesLimitButton);
    }

}
