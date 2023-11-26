package com.mqttinsight.ui.chart;

import cn.hutool.core.img.ColorUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.jayway.jsonpath.JsonPath;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.chart.series.ChartMode;
import com.mqttinsight.ui.chart.series.FavoriteSeries;
import com.mqttinsight.ui.chart.series.MatchExpression;
import com.mqttinsight.ui.chart.series.MessageSeriesDefinition;
import com.mqttinsight.ui.component.SplitButton;
import com.mqttinsight.ui.component.SplitIconMenuItem;
import com.mqttinsight.ui.component.model.CountSeriesTableModel;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.TopicUtil;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.knowm.xchart.*;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ptma
 */
@Slf4j
public class MessageCountChartFrame extends JFrame {
    private MqttInstance mqttInstance;
    private JPanel contentPanel;
    private JButton addSeriesButton;
    private JSplitPane splitPanel;
    private JPanel topPanel;
    private JScrollPane tableScrollPanel;
    private JToolBar toolbar;
    private JButton removeSeriesButton;
    private JButton resetChartButton;
    private JXTable seriesTable;
    private JToggleButton pieChartButton;
    private JToggleButton barChartButton;
    private SplitButton favoriteSplitButton;
    private CountSeriesTableModel seriesTableModel;

    private ExecutorService executorService;
    private Map<String, AtomicInteger> seriesCache = new ConcurrentHashMap<>();

    private Chart chart;
    private ChartMode chartMode;
    private XChartPanel chartPanel;
    private InstanceEventAdapter eventAdapter;

    public static void open(MqttInstance mqttInstance) {
        JFrame dialog = new MessageCountChartFrame(mqttInstance);
        dialog.setMinimumSize(new Dimension(800, 600));
        dialog.setResizable(true);
        dialog.pack();
        dialog.setLocationRelativeTo(MqttInsightApplication.frame);
        dialog.setVisible(true);
    }

    private MessageCountChartFrame(MqttInstance mqttInstance) {
        super();
        this.mqttInstance = mqttInstance;
        $$$setupUI$$$();
        setIconImages(Icons.WINDOW_ICON);
        setContentPane(contentPanel);

        initComponents();
        initChart(ChartMode.PIE);
        initMessageEvent();
        loadFavoriteSeries();
        applyLanguage();
    }

    private void applyLanguage() {
        setTitle(String.format(LangUtil.getString("MessagesCountStatisticsChartTitle"), mqttInstance.getProperties().getName()));
        LangUtil.buttonText(addSeriesButton, "AddSeries");
        LangUtil.buttonText(removeSeriesButton, "RemoveSeries");
        LangUtil.buttonText(resetChartButton, "ResetChart");
        LangUtil.buttonText(favoriteSplitButton, "Favorite");
        favoriteSplitButton.setToolTipText(LangUtil.getString("SaveCollectionToFavorites"));
        pieChartButton.setToolTipText(ChartMode.PIE.getText());
        barChartButton.setToolTipText(ChartMode.BAR.getText());
        chartPanel.setSaveAsString(LangUtil.getString("SaveAs"));
        chartPanel.setPrintString(LangUtil.getString("Print"));
    }

    private void initComponents() {
        splitPanel.setDividerLocation(200);

        seriesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        seriesTable.setEditable(false);
        seriesTable.setSortable(false);
        seriesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        seriesTable.setColumnSelectionAllowed(false);
        seriesTable.setCellSelectionEnabled(false);
        seriesTable.setRowSelectionAllowed(true);
        initTableColumns();
        if (UIManager.getBoolean("laf.dark")) {
            seriesTable.setShowHorizontalLines(true);
        }
        seriesTable.getSelectionModel().addListSelectionListener(this::tableSelectionChanged);
        seriesTable.revalidate();
        seriesTable.repaint();

        ListSelectionModel cellSelectionModel = seriesTable.getSelectionModel();
        cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cellSelectionModel.addListSelectionListener(e -> {
            int selectedRow = seriesTable.getSelectedRow();
            removeSeriesButton.setEnabled(selectedRow >= 0);
        });

        // Add series
        addSeriesButton.setIcon(Icons.ADD);
        addSeriesButton.addActionListener(e -> {
            CountSeriesEditor.open(MessageCountChartFrame.this, null, newSeries -> {
                seriesTableModel.addRow(newSeries);
            });
        });
        // Double click to editing selected series
        seriesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    final int rowIndex = seriesTable.rowAtPoint(e.getPoint());
                    if (rowIndex >= 0) {
                        MessageSeriesDefinition selected = seriesTableModel.getRow(seriesTable.convertRowIndexToModel(rowIndex));
                        CountSeriesEditor.open(MessageCountChartFrame.this, selected, series -> {
                            seriesTableModel.fireTableDataChanged();
                        });
                    }
                }
            }
        });
        // Remove selected series
        removeSeriesButton.setIcon(Icons.REMOVE);
        removeSeriesButton.setEnabled(false);
        removeSeriesButton.addActionListener(e -> {
            int selectedRow = seriesTable.getSelectedRow();
            if (selectedRow >= 0) {
                seriesTableModel.removeRow(seriesTable.convertRowIndexToModel(selectedRow));
            }
        });

        // Reset chart
        resetChartButton.setIcon(Icons.RESET);
        resetChartButton.addActionListener(e -> {
            seriesCache.clear();
            chart.getSeriesMap().clear();
            chartPanel.revalidate();
            chartPanel.repaint();
        });

        pieChartButton.addActionListener(this::chartChanged);
        barChartButton.addActionListener(this::chartChanged);

        favoriteSplitButton.addActionListener(this::saveSeries);
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
        // Match column
        column = seriesTable.getColumnExt(2);
        column.setPreferredWidth(80);
        column.setWidth(80);
        column.setMinWidth(80);
        column.setMaxWidth(80);
        // Type column
        column = seriesTable.getColumnExt(3);
        column.setPreferredWidth(130);
        column.setWidth(130);
        column.setMinWidth(100);
        column.setMaxWidth(150);
        // Expression column
        column = seriesTable.getColumnExt(4);
        column.setPreferredWidth(300);
        column.setWidth(300);
    }

    private void tableSelectionChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (!e.getValueIsAdjusting()) {
            int selectedRow = lsm.getMaxSelectionIndex();
            removeSeriesButton.setEnabled(selectedRow >= 0);
        }
    }

    private void initChart(ChartMode chartMode) {
        this.chartMode = chartMode;
        if (ChartMode.PIE.equals(chartMode)) {
            PieChart pieChart = new PieChartBuilder()
                .width(this.getWidth() - 10)
                .height(this.getHeight() - 215)
                .build();
            pieChart.getStyler().setLabelType(PieStyler.LabelType.NameAndPercentage);
            pieChart.getStyler().setLabelsDistance(1.1);
            pieChart.getStyler().setSliceBorderWidth(2);
            if (UIManager.getBoolean("laf.dark")) {
                pieChart.getStyler().setLabelsFontColor(UIManager.getColor("Label.foreground"));
                pieChart.getStyler().setLabelsFontColorAutomaticEnabled(false);
            }
            chart = pieChart;
        } else {
            CategoryChart barChart = new CategoryChartBuilder()
                .width(this.getWidth() - 10)
                .height(this.getHeight() - 215)
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
        splitPanel.setBottomComponent(chartPanel);

        if (!seriesCache.isEmpty()) {
            seriesCache.forEach((seriesName, seriesValue) -> {
                if (ChartMode.PIE.equals(chartMode)) {
                    ((PieChart) chart).addSeries(seriesName, seriesValue.intValue());
                } else {
                    ((CategoryChart) chart).addSeries(seriesName, List.of(seriesName), List.of(seriesValue.intValue()));
                }
            });
        }
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void initMessageEvent() {
        executorService = ThreadUtil.newFixedExecutor(1, "Chart", false);
        eventAdapter = new InstanceEventAdapter() {
            @Override
            public void onMessage(MqttMessage message) {
                MessageCountChartFrame.this.onMessage(message);
            }
        };
        mqttInstance.addEventListener(eventAdapter);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mqttInstance.removeEventListener(eventAdapter);
                executorService.shutdown();
                super.windowClosing(e);
            }
        });
    }

    private void chartChanged(ActionEvent e) {
        ChartMode chartMode = pieChartButton.isSelected() ? ChartMode.PIE : ChartMode.BAR;
        initChart(chartMode);
    }

    private void onMessage(MqttMessage message) {
        executorService.execute(() -> {
            for (MessageSeriesDefinition series : seriesTableModel.getSeries()) {
                if (series.isDynamic()) {
                    dynamicSeries(series, message);
                } else {
                    staticSeries(series, message);
                }
            }
        });
    }

    private void saveSeries(ActionEvent e) {
        if (seriesTableModel.getSeries().isEmpty()) {
            return;
        }
        String name = JOptionPane.showInputDialog(this, LangUtil.getString("EnterCollectionName"));
        if (StrUtil.isEmpty(name)) {
            Utils.Message.info(LangUtil.getString("EnterCollectionNameHint"));
        } else {
            List<FavoriteSeries> favoriteSeries = mqttInstance.getProperties().getFavoriteSeries();
            if (favoriteSeries == null) {
                favoriteSeries = new ArrayList<>();
                mqttInstance.getProperties().setFavoriteSeries(favoriteSeries);
            }
            favoriteSeries.removeIf(t -> t.getName().equals(name));
            favoriteSeries.add(FavoriteSeries.of(name, seriesTableModel.getSeries()));
            Configuration.instance().changed();
            loadFavoriteSeries();
        }
    }

    private void loadFavoriteSeries() {
        JPopupMenu menu = favoriteSplitButton.getPopupMenu();
        menu.removeAll();
        List<FavoriteSeries> favoriteSeries = mqttInstance.getProperties().getFavoriteSeries();
        if (favoriteSeries != null && !favoriteSeries.isEmpty()) {
            favoriteSeries.forEach(item -> {
                SplitIconMenuItem menuItem = new SplitIconMenuItem(item.getName(), null, Icons.REMOVE);
                menu.add(menuItem);
                menuItem.addActionListener(e -> {
                    seriesTableModel.removeAll();
                    for (MessageSeriesDefinition definition : item.getSeries()) {
                        seriesTableModel.addRow(definition);
                    }
                    resetChartButton.doClick();
                });
                menuItem.addSplitActionListener(e -> {
                    int opt = Utils.Message.confirm(this, String.format(LangUtil.getString("RemoveFavoriteCollection"), item.getName()));
                    if (JOptionPane.YES_OPTION == opt) {
                        favoriteSeries.remove(item);
                        Configuration.instance().changed();
                    }
                });
            });
        }
    }

    private void dynamicSeries(MessageSeriesDefinition definition, MqttMessage message) {
        if (!definition.isDynamic()) {
            return;
        }
        switch (definition.getMatch()) {
            case TOPIC -> {
                // only Regular expression
                String seriesName = Utils.findRegexMatchGroup(definition.getMatchExpression().getExpression(), message.getTopic());
                saveOrUpdateSeriesData(seriesName);
            }
            case PAYLOAD -> {
                switch (definition.getMatchType()) {
                    case REGEXP -> {
                        String seriesName = Utils.findRegexMatchGroup(definition.getMatchExpression().getExpression(), message.getTopic());
                        saveOrUpdateSeriesData(seriesName);
                    }
                    case JSON_PATH -> {
                        String payloadStr = message.payloadAsString(false);
                        MatchExpression expression = definition.getMatchExpression();
                        String seriesName = JsonPath.read(payloadStr, expression.getExpression()).toString();
                        saveOrUpdateSeriesData(seriesName);
                    }
                }
            }
        }
    }

    private void staticSeries(MessageSeriesDefinition definition, MqttMessage message) {
        if (definition.isDynamic()) {
            return;
        }
        switch (definition.getMatch()) {
            case TOPIC -> {
                switch (definition.getMatchType()) {
                    case WILDCARD -> {
                        if (TopicUtil.match(definition.getMatchExpression().getExpression(), message.getTopic())) {
                            saveOrUpdateSeriesData(definition.getSeriesName());
                        }
                    }
                    case REGEXP -> {
                        if (ReUtil.isMatch(definition.getMatchExpression().getExpression(), message.getTopic())) {
                            saveOrUpdateSeriesData(definition.getSeriesName());
                        }
                    }
                }

            }
            case PAYLOAD -> {
                String payloadStr = message.payloadAsString(false);
                switch (definition.getMatchType()) {
                    case REGEXP -> {
                        if (ReUtil.isMatch(definition.getMatchExpression().getExpression(), payloadStr)) {
                            saveOrUpdateSeriesData(definition.getSeriesName());
                        }
                    }
                    case JSON_PATH -> {
                        MatchExpression expression = definition.getMatchExpression();
                        String jsonpath = expression.getExpression();
                        String comparator = expression.getComparator();
                        String expectedValue = expression.getValue();
                        String readValue = JsonPath.read(payloadStr, jsonpath).toString();
                        if (isMatchComparator(comparator, expectedValue, readValue)) {
                            saveOrUpdateSeriesData(definition.getSeriesName());
                        }
                    }
                }
            }
        }
    }

    private void saveOrUpdateSeriesData(String seriesName) {
        if (StrUtil.isNotBlank(seriesName)) {
            SwingUtilities.invokeLater(() -> {
                AtomicInteger seriesValue;
                if (seriesCache.containsKey(seriesName)) {
                    seriesValue = seriesCache.get(seriesName);
                } else {
                    seriesValue = new AtomicInteger(0);
                    seriesCache.put(seriesName, seriesValue);
                }
                int value = seriesValue.incrementAndGet();
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
            });
        }
    }

    private boolean isMatchComparator(String comparator, String expected, String readValue) {
        if (expected == null) {
            expected = "";
        }
        switch (comparator) {
            case "=" -> {
                return expected.equals(readValue);
            }
            case "!=" -> {
                return !expected.equals(readValue);
            }
            case "contains" -> {
                return StrUtil.contains(readValue, expected);
            }
            case "not contains" -> {
                return !StrUtil.contains(readValue, expected);
            }
            case ">" -> {
                if (!NumberUtil.isNumber(expected) || !NumberUtil.isNumber(readValue)) {
                    return false;
                }
                return NumberUtil.parseNumber(readValue).doubleValue() > NumberUtil.parseNumber(expected).doubleValue();
            }
            case ">=" -> {
                if (!NumberUtil.isNumber(expected) || !NumberUtil.isNumber(readValue)) {
                    return false;
                }
                return NumberUtil.parseNumber(readValue).doubleValue() >= NumberUtil.parseNumber(expected).doubleValue();
            }
            case "<" -> {
                if (!NumberUtil.isNumber(expected) || !NumberUtil.isNumber(readValue)) {
                    return false;
                }
                return NumberUtil.parseNumber(readValue).doubleValue() < NumberUtil.parseNumber(expected).doubleValue();
            }
            case "<=" -> {
                if (!NumberUtil.isNumber(expected) || !NumberUtil.isNumber(readValue)) {
                    return false;
                }
                return NumberUtil.parseNumber(readValue).doubleValue() <= NumberUtil.parseNumber(expected).doubleValue();
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(0, 0));
        contentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        splitPanel = new JSplitPane();
        splitPanel.setDividerLocation(150);
        splitPanel.setOrientation(0);
        splitPanel.setResizeWeight(0.1);
        contentPanel.add(splitPanel, BorderLayout.CENTER);
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(0, 0));
        splitPanel.setLeftComponent(topPanel);
        toolbar = new JToolBar();
        topPanel.add(toolbar, BorderLayout.NORTH);
        addSeriesButton = new JButton();
        addSeriesButton.setText("Add Series");
        toolbar.add(addSeriesButton);
        removeSeriesButton = new JButton();
        removeSeriesButton.setText("Remove Series");
        toolbar.add(removeSeriesButton);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        toolbar.add(toolBar$Separator1);
        resetChartButton = new JButton();
        resetChartButton.setText("Reset Chart");
        toolbar.add(resetChartButton);
        final JToolBar.Separator toolBar$Separator2 = new JToolBar.Separator();
        toolbar.add(toolBar$Separator2);
        toolbar.add(pieChartButton);
        toolbar.add(barChartButton);
        final JToolBar.Separator toolBar$Separator3 = new JToolBar.Separator();
        toolbar.add(toolBar$Separator3);
        toolbar.add(favoriteSplitButton);
        tableScrollPanel = new JScrollPane();
        topPanel.add(tableScrollPanel, BorderLayout.CENTER);
        seriesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tableScrollPanel.setViewportView(seriesTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

    private void createUIComponents() {
        seriesTableModel = new CountSeriesTableModel();
        seriesTable = new JXTable(seriesTableModel);

        pieChartButton = new JToggleButton(Icons.CHART_PIE);
        barChartButton = new JToggleButton(Icons.CHART_BAR);
        ButtonGroup viewButtonGroup = new ButtonGroup();
        viewButtonGroup.add(pieChartButton);
        viewButtonGroup.add(barChartButton);
        pieChartButton.setSelected(true);

        favoriteSplitButton = new SplitButton("Favorite");
        favoriteSplitButton.setIcon(Icons.FAVORITE);
    }
}
