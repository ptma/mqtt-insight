package com.mqttinsight.ui.chart;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.chart.series.*;
import com.mqttinsight.ui.component.SplitButton;
import com.mqttinsight.ui.component.SplitIconMenuItem;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.TopicUtil;
import com.mqttinsight.util.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ptma
 */
@Slf4j
public abstract class BaseChartFrame<T extends SeriesProperties> extends JFrame {
    protected final MqttInstance mqttInstance;
    private JPanel contentPanel;
    protected JButton addSeriesButton;
    private JSplitPane splitPanel;
    private JPanel topPanel;
    private JScrollPane tableScrollPanel;
    protected JToolBar toolbar;
    protected JButton removeSeriesButton;
    protected JButton resetChartButton;
    protected JXTable seriesTable;
    protected SplitButton favoriteSplitButton;
    protected JPanel bottomPanel;
    private JButton pauseButton;
    protected AbstractSeriesTableModel<T> seriesTableModel;
    protected InstanceEventAdapter eventAdapter;
    @Getter
    private boolean paused = false;

    protected BaseChartFrame(MqttInstance mqttInstance) {
        super();
        this.mqttInstance = mqttInstance;
        $$$setupUI$$$();
        setIconImages(Icons.WINDOW_ICON);
        setContentPane(contentPanel);

        initComponents();
        initMessageEvent();
        loadFavoriteSeries();
        applyLanguage();
    }

    /**
     * 添加系列按钮点击时调用，子类实现具体的业务逻辑。
     */
    protected abstract void addSeriesAction(ActionEvent e);

    /**
     * 在系列表格中双击行时调用，子类实现具体的业务逻辑。
     */
    protected abstract void doubleClickOnTableRow(int rowIndex);

    /**
     * 删除系列按钮点击时调用，子类实现具体的业务逻辑。
     */
    protected abstract void removeSeriesAction(ActionEvent e);

    /**
     * 重置图表按钮点击时调用，子类实现具体的业务逻辑。
     */
    protected abstract void resetChartAction(ActionEvent e);

    /**
     * 获取子类实际使用的表格模型
     */
    protected abstract AbstractSeriesTableModel<T> createSeriesTableModel();

    /**
     * 接收到 MQTT 消息时调用，子类实现具体的业务逻辑。
     *
     * @param message MQTT消息
     */
    protected abstract void onMessage(MqttMessage message);

    /**
     * 获取收藏的系列集列表。
     */
    protected abstract List<FavoriteSeries<T>> getFavoriteSeries();

    protected abstract void saveSeriesToFavorite(List<FavoriteSeries<T>> favoriteSeries);

    private void applyLanguage() {
        setTitle(String.format(LangUtil.getString("MessageCountStatisticsChartTitle"), mqttInstance.getProperties().getName()));
        LangUtil.buttonText(addSeriesButton, "AddSeries");
        LangUtil.buttonText(removeSeriesButton, "RemoveSeries");
        LangUtil.buttonText(resetChartButton, "ResetChart");
        LangUtil.buttonText(favoriteSplitButton, "Favorite");
        favoriteSplitButton.setToolTipText(LangUtil.getString("SaveCollectionToFavorites"));
    }

    private void initComponents() {
        bottomPanel.setBorder(new LineBorder(UIManager.getColor("Component.borderColor")));

        seriesTableModel = createSeriesTableModel();
        seriesTable.setModel(seriesTableModel);
        seriesTable.setRowHeight(25);
        seriesTable.setEditable(false);
        seriesTable.setSortable(false);
        seriesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        seriesTable.setColumnSelectionAllowed(false);
        seriesTable.setCellSelectionEnabled(false);
        seriesTable.setRowSelectionAllowed(true);

        seriesTable.setDefaultRenderer(Object.class, new DefaultTableRenderer(new SeriesTableRendererProvider()));

        if (UIManager.getBoolean("laf.dark")) {
            seriesTable.setShowHorizontalLines(true);
        }
        seriesTable.getSelectionModel().addListSelectionListener(this::tableSelectionChanged);
        seriesTableModel.addTableModelListener(l -> {
            pauseButton.setEnabled(!seriesTableModel.getSeries().isEmpty());
        });

        ListSelectionModel cellSelectionModel = seriesTable.getSelectionModel();
        cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cellSelectionModel.addListSelectionListener(e -> {
            int selectedRow = seriesTable.getSelectedRow();
            removeSeriesButton.setEnabled(selectedRow >= 0);
        });

        // Add series
        addSeriesButton.setIcon(Icons.ADD);
        addSeriesButton.addActionListener(this::addSeriesAction);
        // Double click to editing selected series
        seriesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    final int rowIndex = seriesTable.rowAtPoint(e.getPoint());
                    if (rowIndex >= 0) {
                        doubleClickOnTableRow(rowIndex);
                    }
                }
            }
        });
        // Remove selected series
        removeSeriesButton.setIcon(Icons.REMOVE);
        removeSeriesButton.setEnabled(false);
        removeSeriesButton.addActionListener(this::removeSeriesAction);

        // Reset chart
        resetChartButton.setIcon(Icons.RESET);
        resetChartButton.addActionListener(this::resetChartAction);

        pauseButton.setIcon(Icons.PAUSE);
        pauseButton.setText(LangUtil.getString("Pause"));
        pauseButton.addActionListener(e -> {
            paused = !paused;
            pauseButton.setIcon(paused ? Icons.EXECUTE : Icons.PAUSE);
            pauseButton.setText(paused ? LangUtil.getString("Resume") : LangUtil.getString("Pause"));
        });

        favoriteSplitButton.addActionListener(this::saveSeriesAction);
    }

    protected void saveSeriesAction(ActionEvent e) {
        if (seriesTableModel.getSeries().isEmpty()) {
            return;
        }
        Utils.Message.input(this, LangUtil.getString("EnterCollectionName"), (name) -> {
            List<FavoriteSeries<T>> favoriteSeries = getFavoriteSeries();
            if (favoriteSeries == null) {
                favoriteSeries = new ArrayList<>();
                saveSeriesToFavorite(favoriteSeries);
            }
            if (favoriteSeries.stream().anyMatch(t -> t.getName().equals(name))) {
                int opt = Utils.Message.confirm(this, LangUtil.format("OverwriteCollection", name));
                if (JOptionPane.YES_OPTION != opt) {
                    return;
                }
            }
            favoriteSeries.removeIf(t -> t.getName().equals(name));
            favoriteSeries.add(FavoriteSeries.of(name, seriesTableModel.getSeries()));
            Configuration.instance().changed();
            loadFavoriteSeries();
        });
    }

    private void tableSelectionChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (!e.getValueIsAdjusting()) {
            int selectedRow = lsm.getMaxSelectionIndex();
            removeSeriesButton.setEnabled(selectedRow >= 0);
        }
    }

    private void initMessageEvent() {
        eventAdapter = new InstanceEventAdapter() {
            @Override
            public void onMessage(MqttMessage message) {
                BaseChartFrame.this.onMessage(message);
            }
        };
        mqttInstance.addEventListener(eventAdapter);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mqttInstance.removeEventListener(eventAdapter);
                super.windowClosing(e);
            }
        });
    }

    void loadFavoriteSeries() {
        JPopupMenu menu = favoriteSplitButton.getPopupMenu();
        menu.removeAll();
        List<FavoriteSeries<T>> favoriteSeries = getFavoriteSeries();
        if (favoriteSeries != null && !favoriteSeries.isEmpty()) {
            favoriteSeries.forEach(item -> {
                SplitIconMenuItem menuItem = new SplitIconMenuItem(item.getName(), null, Icons.REMOVE);
                menu.add(menuItem);
                menuItem.addActionListener(e -> {
                    seriesTableModel.removeAll();
                    for (T series : item.getSeries()) {
                        seriesTableModel.addRow(series);
                    }
                    resetChartButton.doClick();
                });
                menuItem.addSplitActionListener(e -> {
                    int opt = Utils.Message.confirm(this, String.format(LangUtil.getString("RemoveFavoriteCollection"), item.getName()));
                    if (JOptionPane.YES_OPTION == opt) {
                        favoriteSeries.remove(item);
                        menu.remove(menuItem);
                        Configuration.instance().changed();
                    }
                });
            });
        }
    }

    protected boolean messageMatchesSeries(T series, MqttMessage message) {
        switch (series.getMatch()) {
            case TOPIC -> {
                switch (series.getMatchMode()) {
                    case WILDCARD -> {
                        return TopicUtil.match(series.getMatchExpression().getExpression(), message.getTopic());
                    }
                    case REGEXP -> {
                        return ReUtil.isMatch(series.getMatchExpression().getExpression(), message.getTopic());
                    }
                    default -> {
                        return false;
                    }
                }
            }
            case PAYLOAD -> {
                String payloadStr = message.payloadAsString(false);
                if (StrUtil.isEmpty(payloadStr)) {
                    return false;
                }
                switch (series.getMatchMode()) {
                    case REGEXP -> {
                        return ReUtil.isMatch(series.getMatchExpression().getExpression(), payloadStr);
                    }
                    case JSON_PATH -> {
                        MatchExpression expression = series.getMatchExpression();
                        ValueComparator comparator = expression.getComparator();
                        String expectedValue = expression.getValue();
                        String readValue = Utils.getByJsonPath(expression.getExpression(), payloadStr);
                        return ValueComparator.match(comparator, expectedValue, readValue);
                    }
                    case XPATH -> {
                        MatchExpression expression = series.getMatchExpression();
                        ValueComparator comparator = expression.getComparator();
                        String expectedValue = expression.getValue();
                        String readValue = Utils.getByXPath(expression.getExpression(), payloadStr);
                        return ValueComparator.match(comparator, expectedValue, readValue);
                    }
                    default -> {
                        return false;
                    }
                }
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
        splitPanel.setDividerLocation(160);
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
        pauseButton = new JButton();
        pauseButton.setEnabled(false);
        pauseButton.setText("");
        toolbar.add(pauseButton);
        final JToolBar.Separator toolBar$Separator3 = new JToolBar.Separator();
        toolbar.add(toolBar$Separator3);
        toolbar.add(favoriteSplitButton);
        tableScrollPanel = new JScrollPane();
        topPanel.add(tableScrollPanel, BorderLayout.CENTER);
        seriesTable = new JXTable();
        seriesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tableScrollPanel.setViewportView(seriesTable);
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(0, 0));
        splitPanel.setRightComponent(bottomPanel);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

    private void createUIComponents() {
        favoriteSplitButton = new SplitButton("Favorite");
        favoriteSplitButton.setIcon(Icons.FAVORITE);
    }
}
