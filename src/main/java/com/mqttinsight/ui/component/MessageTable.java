package com.mqttinsight.ui.component;

import cn.hutool.core.img.ColorUtil;
import com.formdev.flatlaf.extras.components.FlatPopupMenu;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.ReceivedMqttMessage;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.ui.chart.MessageContentChartFrame;
import com.mqttinsight.ui.chart.MessageCountChartFrame;
import com.mqttinsight.ui.chart.MessageLoadChartFrame;
import com.mqttinsight.ui.component.model.MessageTableModel;
import com.mqttinsight.ui.component.model.MessageViewMode;
import com.mqttinsight.ui.component.renderer.DialogueViewRendererProvider;
import com.mqttinsight.ui.component.renderer.TableViewRendererProvider;
import com.mqttinsight.ui.event.InstanceEventListener;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.graphics.ColorUtilities;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.search.AbstractSearchable;
import org.jdesktop.swingx.table.TableColumnExt;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

/**
 * @author ptma
 */
@Slf4j
public class MessageTable extends JXTable {

    private final MqttInstance mqttInstance;
    @Getter
    private final MessageTableModel tableModel;
    private final DefaultTableRenderer tableRenderer;
    private FlatPopupMenu popupMenu;
    private JMenuItem menuCopyTopic;
    private JMenuItem menuCopy;
    private JMenuItem menuDelete;
    private JMenuItem menuClear;
    private JMenuItem menuClearVisible;
    private JMenuItem menuExport;
    private JMenu colorMenu;
    @Setter
    @Getter
    private boolean autoScroll;
    private final SubscriptionFilter subscriptionFilter = new SubscriptionFilter();
    private final TopicSegmentFilter topicSegmentFilter = new TopicSegmentFilter();

    public MessageTable(MqttInstance mqttInstance, MessageTableModel tableModel) {
        super(tableModel);
        this.mqttInstance = mqttInstance;
        this.tableModel = tableModel;
        setIgnoreRepaint(true);
        setSortsOnUpdates(false);
        setSortable(false);
        setRolloverEnabled(false);
        setEditable(false);
        setDoubleBuffered(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(false);
        putClientProperty(AbstractSearchable.MATCH_HIGHLIGHTER, Boolean.TRUE);
        boolean darkLaf = UIManager.getBoolean("laf.dark");
        if (tableModel.getViewMode() == MessageViewMode.TABLE) {
            tableRenderer = new DefaultTableRenderer(new TableViewRendererProvider(tableModel));
            setRowHeight(28);
            initTableViewColumns();
            setColumnControlVisible(true);
            setShowHorizontalLines(darkLaf);
        } else {
            tableRenderer = new DefaultTableRenderer(new DialogueViewRendererProvider(tableModel));
            initDialogueViewColumns();
            getTableHeader().setVisible(false);
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            renderer.setPreferredSize(new Dimension(0, 0));
            getTableHeader().setDefaultRenderer(renderer);
        }
        this.createDefaultRenderers();
        AbstractSearchable searchable = (AbstractSearchable) this.getSearchable();
        Color highlighterBg = Color.YELLOW.brighter();
        Color highlighterFg = Utils.getReverseForegroundColor(highlighterBg, darkLaf);
        searchable.setMatchHighlighter(new ColorHighlighter(HighlightPredicate.NEVER, highlighterBg, highlighterFg, highlighterBg, highlighterFg));

        this.getActionMap().remove("find");
        if (this.getActionMap().getParent() != null) {
            this.getActionMap().getParent().remove("copy");
        }
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.isControlDown()) {
                    if (event.getKeyCode() == KeyEvent.VK_C) { // Copy
                        copyPayload();
                    }
                } else if (event.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteSelectedRow();
                }
            }
        });
        initPopupMenu(darkLaf);
        setRowFilter(null);
    }

    private void initPopupMenu(boolean darkLaf) {
        popupMenu = new FlatPopupMenu();
        menuCopyTopic = Utils.UI.createMenuItem(LangUtil.getString("Copy&Topic"), (e) -> copyTopic());
        popupMenu.add(menuCopyTopic);
        menuCopy = Utils.UI.createMenuItem(LangUtil.getString("Copy&Payload") + " (Ctrl + C)", (e) -> copyPayload());
        popupMenu.add(menuCopy);
        menuDelete = Utils.UI.createMenuItem(LangUtil.getString("&Delete"), (e) -> deleteSelectedRow());
        menuDelete.setIcon(Icons.REMOVE);
        popupMenu.add(menuDelete);

        popupMenu.addSeparator();

        {
            JMenu chartMenu = new JMenu(LangUtil.getString("Chart"));
            chartMenu.setIcon(Icons.CHART_BAR);
            JMenuItem countChartMenu = Utils.UI.createMenuItem(LangUtil.getString("MessageCountStatisticsChart"), e -> {
                MessageCountChartFrame.open(mqttInstance);
            });
            countChartMenu.setIcon(Icons.CHART_PIE);
            chartMenu.add(countChartMenu);

            JMenuItem loadChartMenu = Utils.UI.createMenuItem(LangUtil.getString("MessageLoadStatisticsChart"), e -> {
                MessageLoadChartFrame.open(mqttInstance);
            });
            loadChartMenu.setIcon(Icons.CHART_LINE);
            chartMenu.add(loadChartMenu);

            JMenuItem contentChartMenu = Utils.UI.createMenuItem(LangUtil.getString("MessageContentStatisticsChart"), e -> {
                MessageContentChartFrame.open(mqttInstance);
            });
            contentChartMenu.setIcon(Icons.CHART_LINE);
            chartMenu.add(contentChartMenu);

            popupMenu.add(chartMenu);
        }

        popupMenu.addSeparator();

        {
            colorMenu = new JMenu(LangUtil.getString("Color"));
            colorMenu.setIcon(Icons.PALETTE);

            float saturation = 1.0f;
            float lightness = darkLaf ? 0.7f : 0.3f;
            int colorCols = 16;
            for (int col = 0; col < colorCols; col++) {
                float hue = col * 1.0f / colorCols;
                Color color = ColorUtilities.HSLtoRGB(hue, saturation, lightness);
                JMenuItem colorItem = new JMenuItem();
                colorItem.setText(ColorUtil.toHex(color));
                colorItem.setOpaque(true);
                colorItem.setBackground(color);
                colorItem.setForeground(Utils.getReverseForegroundColor(color, darkLaf));
                colorItem.addActionListener(e -> {
                    changeRowColor(color);
                });
                colorMenu.add(colorItem);
            }
            colorMenu.addSeparator();
            JMenuItem moreItem = Utils.UI.createMenuItem(LangUtil.getString("MoreColor"), e -> {
                chooseRowColor();
            });
            colorMenu.add(moreItem);

            popupMenu.add(colorMenu);
        }

        popupMenu.addSeparator();

        menuClear = Utils.UI.createMenuItem(LangUtil.getString("ClearAllMessages"), (e) -> mqttInstance.applyEvent(InstanceEventListener::clearAllMessages));
        menuClear.setIcon(Icons.CLEAR);
        popupMenu.add(menuClear);
        menuClearVisible = Utils.UI.createMenuItem(LangUtil.getString("ClearVisibleMessages"), (e) -> clearVisibleMessages());
        menuClearVisible.setIcon(Icons.CLEAR);
        popupMenu.add(menuClearVisible);
        menuExport = Utils.UI.createMenuItem(LangUtil.getString("ExportAllMessages"), (e) -> mqttInstance.applyEvent(InstanceEventListener::exportAllMessages));
        menuExport.setIcon(Icons.EXPORT);
        popupMenu.add(menuExport);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    final int rowIndex = MessageTable.this.rowAtPoint(e.getPoint());
                    boolean onRow = rowIndex >= 0;
                    menuCopyTopic.setEnabled(onRow);
                    menuCopy.setEnabled(onRow);
                    menuDelete.setEnabled(onRow);
                    colorMenu.setEnabled(onRow);
                    if (onRow) {
                        MessageTable.this.setRowSelectionInterval(rowIndex, rowIndex);
                    }
                    menuClear.setEnabled(MessageTable.this.getRowCount() > 0);
                    menuExport.setEnabled(MessageTable.this.getRowCount() > 0);
                    menuClearVisible.setEnabled(MessageTable.this.getRowCount() > 0);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    public void rowHeightChanged(int row) {
        if (autoScroll && getSelectedRow() == row) {
            scrollRowToVisible(row);
        }
    }

    @Override
    protected void createDefaultRenderers() {
        super.createDefaultRenderers();
        setDefaultRenderer(Object.class, tableRenderer);
        setDefaultRenderer(Boolean.class, tableRenderer);
    }

    private void columnVisibleChange(TableColumnExt column) {
        int columnIndex = (Integer) column.getClientProperty("columnIndex");
        Configuration.instance().set(String.format("column%dVisible", columnIndex), column.isVisible());
        Configuration.instance().changed();
    }

    private void columnWidthChange(TableColumnExt column) {
        int columnIndex = (Integer) column.getClientProperty("columnIndex");
        Configuration.instance().set(String.format("column%dWidth", columnIndex), column.getPreferredWidth());
        Configuration.instance().changed();
    }

    private void loadColumnConfigsAndBindingListener(TableColumnExt column) {
        int columnIndex = (Integer) column.getClientProperty("columnIndex");
        Integer width = Configuration.instance().getInt(String.format("column%dWidth", columnIndex));
        if (width != null) {
            column.setWidth(width);
            column.setPreferredWidth(width);
        }
        Boolean visible = Configuration.instance().getBoolean(String.format("column%dVisible", columnIndex));
        if (visible != null) {
            column.setVisible(visible);
        }

        column.addPropertyChangeListener(e -> {
            if ("width".equals(e.getPropertyName())) {
                columnWidthChange((TableColumnExt) e.getSource());
            } else if ("visible".equals(e.getPropertyName())) {
                columnVisibleChange((TableColumnExt) e.getSource());
            }
        });
    }

    private void initTableViewColumns() {
        // Icon column
        TableColumnExt colDirection = this.getColumnExt(0);
        colDirection.setWidth(25);
        colDirection.setPreferredWidth(25);
        colDirection.setMinWidth(25);
        colDirection.setMaxWidth(25);
        colDirection.setResizable(false);
        colDirection.setHideable(false);
        colDirection.putClientProperty("columnIndex", MessageTableModel.COLUMN_TYPE);

        // Topic column
        TableColumnExt colTopic = this.getColumnExt(1);
        colTopic.setWidth(300);
        colTopic.setPreferredWidth(300);
        colTopic.setMinWidth(150);
        colTopic.putClientProperty("columnIndex", MessageTableModel.COLUMN_TOPIC);

        // Payload column
        TableColumnExt colPayload = this.getColumnExt(2);
        colPayload.setWidth(600);
        colPayload.setPreferredWidth(600);
        colPayload.setMinWidth(400);
        colPayload.setHideable(false);
        colPayload.putClientProperty("columnIndex", MessageTableModel.COLUMN_PAYLOAD);

        // QoS column
        TableColumnExt colQos = this.getColumnExt(3);
        colQos.setWidth(45);
        colQos.setPreferredWidth(45);
        colQos.setMinWidth(20);
        colQos.setMaxWidth(50);
        colQos.putClientProperty("columnIndex", MessageTableModel.COLUMN_QOS);

        // Retained column
        TableColumnExt colRetained = this.getColumnExt(4);
        colRetained.setWidth(65);
        colRetained.setPreferredWidth(65);
        colRetained.setMinWidth(20);
        colRetained.setMaxWidth(65);
        colRetained.setCellRenderer(tableRenderer);
        colRetained.putClientProperty("columnIndex", MessageTableModel.COLUMN_RETAINED);

        // Size column
        TableColumnExt colSize = this.getColumnExt(6);
        colSize.setWidth(80);
        colSize.setPreferredWidth(80);
        colSize.setMinWidth(50);
        colSize.setMaxWidth(120);
        colSize.putClientProperty("columnIndex", MessageTableModel.COLUMN_SIZE);

        // Time column
        TableColumnExt colTime = this.getColumnExt(5);
        colTime.setWidth(160);
        colTime.setPreferredWidth(160);
        colTime.setMinWidth(50);
        colTime.setMaxWidth(160);
        colTime.putClientProperty("columnIndex", MessageTableModel.COLUMN_TIME);

        loadColumnConfigsAndBindingListener(colDirection);
        loadColumnConfigsAndBindingListener(colTopic);
        loadColumnConfigsAndBindingListener(colPayload);
        loadColumnConfigsAndBindingListener(colQos);
        loadColumnConfigsAndBindingListener(colRetained);
        loadColumnConfigsAndBindingListener(colTime);
        loadColumnConfigsAndBindingListener(colSize);

        this.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
    }

    private void initDialogueViewColumns() {
        // Icon column
        TableColumnExt singleCol = this.getColumnExt(0);
        singleCol.setResizable(false);
        singleCol.setHideable(false);
        singleCol.putClientProperty("columnIndex", MessageTableModel.COLUMN_TYPE);
    }

    public void deleteSelectedRow() {
        int selRow = getSelectedRow();
        if (selRow >= 0 && selRow < getRowCount()) {
            int modelIndex = convertRowIndexToModel(selRow);
            tableModel.remove(modelIndex);
            if (getRowCount() > selRow) {
                setRowSelectionInterval(selRow, selRow);
            } else if (selRow > 0) {
                selRow--;
                setRowSelectionInterval(selRow, selRow);
            }
        }
    }

    public void clearVisibleMessages() {
        for (int row = getRowCount() - 1; row >= 0; row--) {
            int modelIndex = convertRowIndexToModel(row);
            tableModel.remove(modelIndex);
        }
        System.gc();
    }

    public void copyTopic() {
        int selRow = getSelectedRow();
        if (selRow >= 0 && selRow < getRowCount()) {
            int modelIndex = convertRowIndexToModel(selRow);
            MqttMessage message = tableModel.get(modelIndex);
            StringSelection selec = new StringSelection(message.getTopic());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selec, selec);
        }
    }

    public void copyPayload() {
        int selRow = getSelectedRow();
        if (selRow >= 0 && selRow < getRowCount()) {
            int modelIndex = convertRowIndexToModel(selRow);
            MqttMessage message = tableModel.get(modelIndex);
            StringSelection selec = new StringSelection(message.getPayload());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selec, selec);
        }
    }

    private void changeRowColor(Color color) {
        int selRow = getSelectedRow();
        if (selRow >= 0 && selRow < getRowCount()) {
            int modelIndex = convertRowIndexToModel(selRow);
            MqttMessage message = tableModel.get(modelIndex);
            message.setColor(color);
        }
    }

    private void chooseRowColor() {
        int selRow = getSelectedRow();
        if (selRow >= 0 && selRow < getRowCount()) {
            int modelIndex = convertRowIndexToModel(selRow);
            MqttMessage message = tableModel.get(modelIndex);
            JColorChooser colorChooser = new JColorChooser();
            if (message.getColor() != null) {
                colorChooser.setColor(message.getColor());
            }
            JDialog dialog = JColorChooser.createDialog(MqttInsightApplication.frame,
                LangUtil.getString("ChooseColor"),
                true,
                colorChooser,
                e1 -> {
                    message.setColor(colorChooser.getColor());
                },
                null
            );
            dialog.setVisible(true);
        }
    }

    public void goAndSelectRow(int row) {
        if (this.getRowCount() == 0) {
            this.clearSelection();
            return;
        }
        if (row < 0) {
            this.clearSelection();
        } else {
            if (row >= this.getRowCount()) {
                row = this.getRowCount() - 1;
            }
            this.scrollRowToVisible(row);
            this.setRowSelectionInterval(row, row);
        }
    }

    public List<MqttMessage> getMessage() {
        return tableModel.getMessages();
    }

    public Optional<MqttMessage> getSelectedMessage() {
        int selectedRow = this.getSelectedRow();
        if (selectedRow >= 0) {
            return Optional.of(tableModel.get(convertRowIndexToModel(selectedRow)));
        } else {
            return Optional.empty();
        }
    }

    public void doApplyFilterTopics(Set<String> topics) {
        boolean changed = topicSegmentFilter.applyTopics(topics);
        if (changed) {
            getTableModel().fireTableDataChanged();
        }
    }

    @Override
    public <R extends TableModel> void setRowFilter(RowFilter<? super R, ? super Integer> filter) {
        if (filter == null) {
            super.setRowFilter(RowFilter.andFilter(Arrays.asList(subscriptionFilter, topicSegmentFilter)));
        } else {
            super.setRowFilter(RowFilter.andFilter(Arrays.asList(subscriptionFilter, topicSegmentFilter, filter)));
        }
    }

    static class SubscriptionFilter extends RowFilter<TableModel, Integer> {

        protected SubscriptionFilter() {
        }

        @Override
        public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
            MessageTableModel tableModel = (MessageTableModel) entry.getModel();
            MqttMessage message = tableModel.get(entry.getIdentifier());
            if (message instanceof ReceivedMqttMessage) {
                return isVisible(((ReceivedMqttMessage) message).getSubscription());
            } else {
                return true;
            }
        }

        boolean isVisible(Subscription subscription) {
            if (subscription == null) {
                return true;
            } else {
                return subscription.isVisible();
            }
        }
    }

    static class TopicSegmentFilter extends RowFilter<TableModel, Integer> {

        private final Set<String> topics = new HashSet<>();

        protected TopicSegmentFilter() {
        }

        public boolean applyTopics(Set<String> topics) {
            boolean changed = this.topics.size() != topics.size() || !this.topics.containsAll(topics);
            if (changed) {
                this.topics.clear();
                this.topics.addAll(topics);
            }
            return changed;
        }

        @Override
        public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
            MessageTableModel tableModel = (MessageTableModel) entry.getModel();
            MqttMessage message = tableModel.get(entry.getIdentifier());
            String messageTopic = message.getTopic();
            return topics.isEmpty() || topics.stream().noneMatch(filterTopic -> messageTopic.equals(filterTopic) || isStartsWith(filterTopic, messageTopic));
        }

        private boolean isStartsWith(String filterTopic, String messageTopic) {
            if (filterTopic.equals("/")) {
                return messageTopic.startsWith(filterTopic);
            } else {
                return messageTopic.startsWith(filterTopic + "/");
            }
        }
    }
}
