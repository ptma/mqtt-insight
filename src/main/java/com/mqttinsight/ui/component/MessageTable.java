package com.mqttinsight.ui.component;

import com.formdev.flatlaf.extras.components.FlatPopupMenu;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.ui.component.model.MessageTableModel;
import com.mqttinsight.ui.component.model.MessageViewMode;
import com.mqttinsight.ui.component.renderer.DialogueViewRendererProvider;
import com.mqttinsight.ui.component.renderer.TableViewRendererProvider;
import com.mqttinsight.ui.event.InstanceEventListener;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.search.AbstractSearchable;
import org.jdesktop.swingx.table.TableColumnExt;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author ptma
 */
@Slf4j
public class MessageTable extends JXTable {

    private final MqttInstance mqttInstance;
    private final MessageTableModel tableModel;
    private final DefaultTableRenderer tableRenderer;
    private FlatPopupMenu popupMenu;
    private JMenuItem menuCopyTopic;
    private JMenuItem menuCopy;
    private JMenuItem menuDelete;
    private boolean autoScroll;

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
        if (tableModel.getViewMode() == MessageViewMode.TABLE) {
            tableRenderer = new DefaultTableRenderer(new TableViewRendererProvider(tableModel));
            setRowHeight(28);
            initTableViewColumns();
            setColumnControlVisible(true);
            if (UIManager.getBoolean("laf.dark")) {
                setShowHorizontalLines(true);
            }
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
        Color highlighterFg = Utils.getReverseForegroundColor(highlighterBg);
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
                }
            }
        });
        initPopupMenu();
    }

    private void initPopupMenu() {
        popupMenu = new FlatPopupMenu();
        menuCopyTopic = Utils.UI.createMenuItem(LangUtil.getString("Copy&Topic"), (e) -> copyTopic());
        popupMenu.add(menuCopyTopic);
        menuCopy = Utils.UI.createMenuItem(LangUtil.getString("Copy&Payload"), (e) -> copyPayload());
        popupMenu.add(menuCopy);
        menuDelete = Utils.UI.createMenuItem(LangUtil.getString("&Delete"), (e) -> deleteSelectedRow());
        menuDelete.setIcon(Icons.REMOVE);
        popupMenu.add(menuDelete);
        popupMenu.add(new JSeparator());

        JMenuItem menuClear = Utils.UI.createMenuItem(LangUtil.getString("ClearAllMessages"), (e) -> mqttInstance.getEventListeners().forEach(InstanceEventListener::clearAllMessages));
        menuClear.setIcon(Icons.CLEAR);
        popupMenu.add(menuClear);
        JMenuItem menuExport = Utils.UI.createMenuItem(LangUtil.getString("ExportAllMessages"), (e) -> mqttInstance.getEventListeners().forEach(InstanceEventListener::exportAllMessages));
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
                    if (onRow) {
                        MessageTable.this.setRowSelectionInterval(rowIndex, rowIndex);
                    }
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
        TableColumnExt column;
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
    }

    private void initDialogueViewColumns() {
        TableColumnExt column;
        // Icon column
        TableColumnExt singleCol = this.getColumnExt(0);
        singleCol.setResizable(false);
        singleCol.setHideable(false);
        singleCol.putClientProperty("columnIndex", MessageTableModel.COLUMN_TYPE);
    }

    public void deleteSelectedRow() {
        int modelIndex = convertRowIndexToModel(getSelectedRow());
        tableModel.remove(modelIndex);
    }

    public void copyTopic() {
        int selectedRow = getSelectedRow();
        int modelIndex = convertRowIndexToModel(getSelectedRow());
        MqttMessage message = tableModel.get(modelIndex);
        StringSelection selec = new StringSelection(message.getTopic());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selec, selec);
    }

    public void copyPayload() {
        int selectedRow = getSelectedRow();
        int modelIndex = convertRowIndexToModel(getSelectedRow());
        MqttMessage message = tableModel.get(modelIndex);
        StringSelection selec = new StringSelection(message.getPayload());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selec, selec);
    }

    public void goAndSelectRow(int row) {
        if (this.getRowCount() == 0) {
            return;
        }
        if (row < 0) {
            row = 0;
        }
        if (row >= this.getRowCount()) {
            row = this.getRowCount() - 1;
        }
        this.scrollRowToVisible(row);
        this.setRowSelectionInterval(row, row);
    }

    public boolean isAutoScroll() {
        return autoScroll;
    }

    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }
}
