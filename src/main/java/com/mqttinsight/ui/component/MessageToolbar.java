package com.mqttinsight.ui.component;

import cn.hutool.core.util.StrUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatTextField;
import com.formdev.flatlaf.icons.FlatSearchWithHistoryIcon;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.ui.component.model.MessageViewMode;
import com.mqttinsight.ui.event.InstanceEventAdapter;
import com.mqttinsight.ui.event.InstanceEventListener;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import org.jdesktop.swingx.search.PatternModel;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author ptma
 */
public class MessageToolbar extends JToolBar {

    protected static final Color NOTFOUND_TEXT_COLOR = Color.decode("#FF6666");
    protected static final Color FOUND_TEXT_COLOR = UIManager.getColor("TextField.foreground");
    protected static final int SEARCH_HISTORY_ENTRIES = 15;

    private final MqttInstance mqttInstance;
    private final RegexSearchCreator regexCreator;
    private PatternModel searchPatternModel;
    private JLabel messageIndicatorLabel;
    private FlatTextField searchField;
    private JToggleButton matchCaseButton;
    private JToggleButton wordsButton;
    private JToggleButton regexButton;
    private JButton searchPrevButton;
    private JButton searchNextButton;
    private JToggleButton filterButton;

    private JButton firstButton;
    private JButton previousButton;
    private JButton nextButton;
    private JButton lastButton;

    private JToggleButton tableViewButton;
    private JToggleButton dialogueViewButton;

    private JToggleButton autoScrollButton;
    private PopupMenuButton moreMenuButton;
    private JMenu scriptMenu;
    private JMenu formatMenu;


    private List<String> searchHistory;
    private ActionListener searchHistoryMenuAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JMenuItem) {
                searchField.setText(((JMenuItem) e.getSource()).getText());
                find(true);
            }
        }
    };

    public MessageToolbar(final MqttInstance mqttInstance) {
        super();
        this.mqttInstance = mqttInstance;
        initComponents();

        searchPatternModel = new PatternModel();
        searchPatternModel.setIncremental(true);
        searchPatternModel.setWrapping(true);
        searchPatternModel.setMatchRule(PatternModel.MATCH_RULE_CONTAINS);
        regexCreator = new RegexSearchCreator();
        searchPatternModel.setRegexCreator(regexCreator);
        searchPatternModel.setRawText("");

        searchHistory = new ArrayList<>();
        List<String> savedHistory = mqttInstance.getProperties().getSearchHistory();
        if (savedHistory != null && !savedHistory.isEmpty()) {
            searchHistory.addAll(savedHistory);
        }

        initEventListeners();
        resetMessageTableActions();
    }

    private void initComponents() {
        this.addSeparator();

        messageIndicatorLabel = new JLabel();
        messageIndicatorLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        this.add(messageIndicatorLabel);
        // search input
        searchField = new FlatTextField();
        searchField.setMinimumWidth(250);
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JTextField.clearCallback",
            (Consumer<JTextComponent>) textField -> {
                searchField.setText("");
                cancelSearch();
            });
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    find(true);
                }
            }
        });
        this.add(searchField);

        // search history button
        JButton historyButton = new JButton(new FlatSearchWithHistoryIcon(true));
        historyButton.setToolTipText(LangUtil.getString("SearchHistory"));
        historyButton.addActionListener(e -> {
            JPopupMenu popupMenu = new JPopupMenu();
            if (searchHistory.isEmpty()) {
                popupMenu.add(LangUtil.getString("Empty"));
            } else {
                for (String searchText : searchHistory) {
                    popupMenu.add(searchText).addActionListener(searchHistoryMenuAction);
                }
            }
            popupMenu.show(historyButton, 0, historyButton.getHeight());
        });
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, historyButton);

        // match case button
        matchCaseButton = new JToggleButton(Icons.SEARCH_MATCHCASE);
        matchCaseButton.setRolloverIcon(Icons.SEARCH_MATCHCASE_HOVER);
        matchCaseButton.setSelectedIcon(Icons.SEARCH_MATCHCASE_SELECTED);
        matchCaseButton.setToolTipText(LangUtil.getString("MatchCase"));
        matchCaseButton.addActionListener(e -> find(true));

        // whole words button
        wordsButton = new JToggleButton(Icons.SEARCH_WORDS);
        wordsButton.setRolloverIcon(Icons.SEARCH_WORDS_HOVER);
        wordsButton.setSelectedIcon(Icons.SEARCH_WORDS_SELECTED);
        wordsButton.setToolTipText(LangUtil.getString("WholeWords"));
        wordsButton.addActionListener(e -> find(true));

        // regex button
        regexButton = new JToggleButton(Icons.SEARCH_REGEX);
        regexButton.setRolloverIcon(Icons.SEARCH_REGEX_HOVER);
        regexButton.setSelectedIcon(Icons.SEARCH_REGEX_SELECTED);
        regexButton.setToolTipText(LangUtil.getString("RegularExpression"));
        regexButton.addActionListener(e -> find(true));

        // search inpput trailing buttons
        JToolBar trailingToolbar = new JToolBar();
        trailingToolbar.add(matchCaseButton);
        trailingToolbar.add(wordsButton);
        trailingToolbar.add(regexButton);
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, trailingToolbar);

        // previous button
        searchPrevButton = new JButton(Icons.PREVIOUS_OCCURENCE);
        searchPrevButton.setEnabled(false);
        searchPrevButton.addActionListener(e -> find(false));
        searchPrevButton.setToolTipText(LangUtil.getString("PreviousOccurrence"));
        this.add(searchPrevButton);

        // next button
        searchNextButton = new JButton(Icons.NEXT_OCCURENCE);
        searchNextButton.setEnabled(false);
        searchNextButton.addActionListener(e -> find(true));
        searchNextButton.setToolTipText(LangUtil.getString("NextOccurrence"));
        this.add(searchNextButton);

        this.addSeparator();

        // filter button
        filterButton = new JToggleButton(Icons.FILTER);
        filterButton.setToolTipText(LangUtil.getString("FilterSearchResults"));
        filterButton.addActionListener(e -> filter());
        this.add(filterButton);

        // Register shotcut
        searchField.registerKeyboardAction(e -> {
            matchCaseButton.setSelected(!matchCaseButton.isSelected());
            find(true);
        }, KeyStroke.getKeyStroke("alt C"), JComponent.WHEN_FOCUSED);
        searchField.registerKeyboardAction(e -> {
            wordsButton.setSelected(!wordsButton.isSelected());
            find(true);
        }, KeyStroke.getKeyStroke("alt W"), JComponent.WHEN_FOCUSED);
        searchField.registerKeyboardAction(e -> {
            regexButton.setSelected(!regexButton.isSelected());
            find(true);
        }, KeyStroke.getKeyStroke("alt X"), JComponent.WHEN_FOCUSED);

        searchField.registerKeyboardAction(e -> find(true), KeyStroke.getKeyStroke("F3"), JComponent.WHEN_FOCUSED);
        searchField.registerKeyboardAction(e -> find(false), KeyStroke.getKeyStroke("shift F3"), JComponent.WHEN_FOCUSED);
        searchField.registerKeyboardAction(e -> filterButton.doClick(), KeyStroke.getKeyStroke("alt ctrl F"), JComponent.WHEN_FOCUSED);
        searchField.registerKeyboardAction(e -> cancelSearch(), KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_FOCUSED);

        //
        addSeparator();
        firstButton = new JButton(Icons.ARROW_FIRST);
        firstButton.setToolTipText(LangUtil.getString("GoFirst"));
        firstButton.setActionCommand("first");
        firstButton.setEnabled(false);
        firstButton.addActionListener(this::messageTableNavigation);
        add(firstButton);
        previousButton = new JButton(Icons.ARROW_BACK);
        previousButton.setToolTipText(LangUtil.getString("GoPrevious"));
        previousButton.setActionCommand("previous");
        previousButton.setEnabled(false);
        previousButton.addActionListener(this::messageTableNavigation);
        add(previousButton);
        nextButton = new JButton(Icons.ARROW_FORWARD);
        nextButton.setToolTipText(LangUtil.getString("GoNext"));
        nextButton.setActionCommand("next");
        nextButton.setEnabled(false);
        nextButton.addActionListener(this::messageTableNavigation);
        add(nextButton);
        lastButton = new JButton(Icons.ARROW_LAST);
        lastButton.setToolTipText(LangUtil.getString("GoLast"));
        lastButton.setActionCommand("last");
        lastButton.setEnabled(false);
        lastButton.addActionListener(this::messageTableNavigation);
        add(lastButton);
        addSeparator();

        // View mode toggle
        MessageViewMode viewMode = MessageViewMode.of(Configuration.instance().getString(ConfKeys.MESSAGE_VIEW, MessageViewMode.TABLE.toString()));
        tableViewButton = new JToggleButton(Icons.TABLE_VIEW);
        tableViewButton.setToolTipText(LangUtil.getString("TableView"));
        if (MessageViewMode.TABLE == viewMode) {
            tableViewButton.setSelected(true);
        }
        tableViewButton.addActionListener(this::viewModeChanged);
        add(tableViewButton);
        dialogueViewButton = new JToggleButton(Icons.DIALOGUE_VIEW);
        dialogueViewButton.setToolTipText(LangUtil.getString("DialogueView"));
        if (MessageViewMode.DIALOGUE == viewMode) {
            dialogueViewButton.setSelected(true);
        }
        dialogueViewButton.addActionListener(this::viewModeChanged);
        add(dialogueViewButton);
        ButtonGroup viewButtonGroup = new ButtonGroup();
        viewButtonGroup.add(tableViewButton);
        viewButtonGroup.add(dialogueViewButton);
        addSeparator();

        autoScrollButton = new JToggleButton(Icons.DOWN_ARRAW);
        LangUtil.buttonText(autoScrollButton, "Autoscroll");
        autoScrollButton.addActionListener(this::toggleAutoScroll);
        add(autoScrollButton);

        addSeparator();

        // More dropdown memnu
        moreMenuButton = new PopupMenuButton(Icons.MORE);
        LangUtil.buttonText(moreMenuButton, "More");

        {
            scriptMenu = new JMenu();
            LangUtil.buttonText(scriptMenu, "Script");
            JMenuItem loadScriptMenu = new JMenuItem();
            LangUtil.buttonText(loadScriptMenu, "LoadScript");
            loadScriptMenu.addActionListener(this::loadScript);
            scriptMenu.add(loadScriptMenu);
            scriptMenu.addSeparator();
            moreMenuButton.addMunuItem(scriptMenu);
        }

        {
            formatMenu = new JMenu();
            LangUtil.buttonText(formatMenu, "PayloadFormat");
            moreMenuButton.addMunuItem(formatMenu);
            loadFormatMenus();
        }

        moreMenuButton.addSeparator();
        JMenuItem clearMessageMenu = new JMenuItem();
        LangUtil.buttonText(clearMessageMenu, "ClearAllMessages");
        moreMenuButton.addMunuItem(clearMessageMenu).addActionListener(this::clearAllMessages);
        JMenuItem exportMenu = new JMenuItem();
        LangUtil.buttonText(exportMenu, "ExportAllMessages");
        moreMenuButton.addMunuItem(exportMenu).addActionListener(this::exportAllMessages);
        add(moreMenuButton);
    }

    private void initEventListeners() {
        mqttInstance.addEventListeners(new InstanceEventAdapter() {
            @Override
            public void viewInitializeCompleted() {
                resetMessageTableActions();
            }

            @Override
            public void onMessage(MqttMessage message) {
                updateMessageNavigation();
            }

            @Override
            public void clearAllMessages() {
                updateMessageNavigation();
            }

            @Override
            public void clearMessages(Subscription subscription) {
                updateMessageNavigation();
            }

            @Override
            public void tableSelectionChanged(MqttMessage message) {
                updateMessageNavigation();
            }

            @Override
            public void scriptLoaded(File scriptFile) {
                onScriptLoaded(scriptFile);
            }
        });
    }

    private void loadFormatMenus() {
        formatMenu.removeAll();
        ButtonGroup formatGroup = new ButtonGroup();
        for (CodecSupport codecSupport : CodecSupports.instance().getCodes()) {
            JCheckBoxMenuItem formatMenuItem = new JCheckBoxMenuItem(codecSupport.getName());
            formatMenuItem.addActionListener(this::payloadFormatChanged);
            if (codecSupport.getName().equals(mqttInstance.getPayloadFormat())) {
                formatMenuItem.setSelected(true);
            }
            formatMenu.add(formatMenuItem);
            formatGroup.add(formatMenuItem);
        }
    }

    private void loadScript(ActionEvent e) {
        mqttInstance.getEventListeners().forEach(InstanceEventListener::fireLoadScript);
    }

    private void onScriptLoaded(final File scriptFile) {
        String filePath = scriptFile.getAbsolutePath();
        for (int i = 2; i < scriptMenu.getMenuComponentCount(); i++) {
            Component subComponent = scriptMenu.getMenuComponent(i);
            if (subComponent instanceof JMenuItem) {
                JMenuItem subMenu = (JMenuItem) subComponent;
                if (filePath.equals(subMenu.getActionCommand())) {
                    int opt = Utils.Message.confirm(String.format(LangUtil.getString("ScriptReloadConfirm"), filePath));
                    if (JOptionPane.YES_OPTION == opt) {
                        mqttInstance.getEventListeners().forEach(l -> l.fireScriptReload(scriptFile));
                    }
                    return;
                }
            }
        }

        final JMenuItem menuItem = new JMenuItem(scriptFile.getName());
        menuItem.setActionCommand(scriptFile.getAbsolutePath());
        menuItem.addActionListener(e -> {
            Window window = SwingUtilities.windowForComponent(this);
            JButton reloadButton = new JButton();
            Utils.UI.buttonText(reloadButton, LangUtil.getString("Re&load"));
            reloadButton.addActionListener(e1 -> {
                JOptionPane pane = getOptionPane((JComponent) e1.getSource());
                pane.setValue(reloadButton);
            });
            JButton removeButton = new JButton();
            Utils.UI.buttonText(removeButton, LangUtil.getString("&Remove"));
            removeButton.addActionListener(e1 -> {
                JOptionPane pane = getOptionPane((JComponent) e1.getSource());
                pane.setValue(removeButton);
            });
            JButton cancelButton = new JButton();
            Utils.UI.buttonText(cancelButton, LangUtil.getString("&Cancel"));
            cancelButton.addActionListener(e1 -> {
                JOptionPane pane = getOptionPane((JComponent) e1.getSource());
                pane.setValue(cancelButton);
            });
            JButton[] options = new JButton[]{reloadButton, removeButton, cancelButton};
            int choice = JOptionPane.showOptionDialog(window,
                LangUtil.getString("ChooseScriptOperation"),
                LangUtil.getString("Question"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[2]);

            switch (choice) {
                case 0:
                    mqttInstance.getEventListeners().forEach(l -> l.fireScriptReload(scriptFile));
                    break;
                case 1:
                    mqttInstance.getEventListeners().forEach(l -> {
                        l.fireScriptRemove(scriptFile);
                        scriptMenu.remove(menuItem);
                    });
                    break;
                default:
            }
        });
        scriptMenu.add(menuItem);
    }

    private JOptionPane getOptionPane(JComponent parent) {
        JOptionPane pane = null;
        if (!(parent instanceof JOptionPane)) {
            pane = getOptionPane((JComponent) parent.getParent());
        } else {
            pane = (JOptionPane) parent;
        }
        return pane;
    }

    private void resetMessageTableActions() {
        mqttInstance.getMessageTable().resetKeyboardActions();
        mqttInstance.getMessageTable().registerKeyboardAction(e -> focusSearch(), KeyStroke.getKeyStroke("ctrl F"), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        mqttInstance.getMessageTable().registerKeyboardAction(e -> find(true), KeyStroke.getKeyStroke("F3"), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        mqttInstance.getMessageTable().registerKeyboardAction(e -> find(false), KeyStroke.getKeyStroke("shift F3"), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        mqttInstance.getMessageTable().registerKeyboardAction(e -> filter(), KeyStroke.getKeyStroke("alt ctrl F"), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        mqttInstance.getMessageTable().registerKeyboardAction(e -> cancelSearch(), KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public void focusSearch() {
        searchField.requestFocus();
    }

    /**
     * Search in message table
     *
     * @param forward Search direction, forward or backward
     */
    public void find(boolean forward) {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) {
            cancelSearch();
            return;
        }
        searchPatternModel.setCaseSensitive(matchCaseButton.isSelected());
        regexCreator.setWordsMatch(wordsButton.isSelected());
        regexCreator.setRegexMatch(regexButton.isSelected());
        if (searchPatternModel.getRawText() == null || !searchPatternModel.getRawText().equals(searchText)) {
            searchPatternModel.setRawText(searchText);
        }
        searchPatternModel.setBackwards(!forward);

        int foundIndex = doSearch();
        boolean found = (foundIndex != -1) || searchPatternModel.isEmpty();
        if (!found && searchPatternModel.isWrapping()) {
            foundIndex = doSearch();
            found = foundIndex != -1;
        }
        if (found) {
            mqttInstance.getMessageTable().goAndSelectRow(foundIndex);
        } else {
            searchField.setForeground(NOTFOUND_TEXT_COLOR);
        }
        searchPrevButton.setEnabled(found);
        searchNextButton.setEnabled(found);
        appendSearchHistory(searchPatternModel.getRawText());
        if (filterButton.isSelected()) {
            doFilter();
        }
    }

    protected int doSearch() {
        int foundIndex = mqttInstance.getMessageTable().getSearchable().search(
            searchPatternModel.getPattern(),
            searchPatternModel.getFoundIndex(),
            searchPatternModel.isBackwards()
        );
        searchPatternModel.setFoundIndex(foundIndex);
        return searchPatternModel.getFoundIndex();
    }

    public void cancelSearch() {
        searchPatternModel.setRawText("");
        doSearch();
        searchPrevButton.setEnabled(false);
        searchNextButton.setEnabled(false);
        clearFilter();
    }

    public void filter() {
        if (filterButton.isSelected() && !searchField.getText().isEmpty()) {
            doFilter();
        } else {
            clearFilter();
        }
    }

    public void doFilter() {
        RowFilter rowFilter = RowFilter.regexFilter(searchPatternModel.getPattern().pattern());
        mqttInstance.getMessageTable().setRowFilter(rowFilter);
    }

    public void clearFilter() {
        filterButton.setSelected(false);
        mqttInstance.getMessageTable().setRowFilter(null);
    }

    public void appendSearchHistory(String searchText) {
        if (StrUtil.isNotEmpty(searchText)) {
            searchHistory.remove(searchText);
            searchHistory.add(0, searchText);
            while (searchHistory.size() > SEARCH_HISTORY_ENTRIES) {
                searchHistory.remove(SEARCH_HISTORY_ENTRIES);
            }
            mqttInstance.getProperties().setSearchHistory(searchHistory);
            Configuration.instance().changed();
        }
    }

    private void viewModeChanged(ActionEvent e) {
        MessageViewMode viewMode;
        if (tableViewButton.isSelected()) {
            viewMode = MessageViewMode.TABLE;
        } else {
            viewMode = MessageViewMode.DIALOGUE;
        }
        mqttInstance.getEventListeners().forEach(l -> l.onViewModeChanged(viewMode));
    }

    private void updateMessageNavigation() {
        SwingUtilities.invokeLater(() -> {
            int selectedRow = mqttInstance.getMessageTable().getSelectedRow();
            int rowCount = mqttInstance.getMessageTable().getRowCount();
            messageIndicatorLabel.setText(String.format(LangUtil.getString("MessageIndicator"), selectedRow + 1, rowCount));

            firstButton.setEnabled(selectedRow > 0);
            previousButton.setEnabled(selectedRow > 0);
            nextButton.setEnabled(selectedRow != -1 && selectedRow < rowCount - 1);
            lastButton.setEnabled(selectedRow != -1 && selectedRow < rowCount - 1);
        });
    }

    private void messageTableNavigation(ActionEvent e) {
        String cmd = e.getActionCommand();
        if ("first".equals(cmd)) {
            mqttInstance.getMessageTable().goAndSelectRow(0);
        } else if ("previous".equals(cmd)) {
            mqttInstance.getMessageTable().goAndSelectRow(mqttInstance.getMessageTable().getSelectedRow() - 1);
        } else if ("next".equals(cmd)) {
            mqttInstance.getMessageTable().goAndSelectRow(mqttInstance.getMessageTable().getSelectedRow() + 1);
        } else if ("last".equals(cmd)) {
            mqttInstance.getMessageTable().goAndSelectRow(mqttInstance.getMessageTable().getRowCount() - 1);
        }
    }

    private void toggleAutoScroll(ActionEvent e) {
        mqttInstance.getEventListeners().forEach(l -> l.toggleAutoScroll(autoScrollButton.isSelected()));
    }

    private void payloadFormatChanged(ActionEvent e) {
        String format = e.getActionCommand();
        mqttInstance.setPayloadFormat(format);
    }

    private void clearAllMessages(ActionEvent e) {
        mqttInstance.getEventListeners().forEach(InstanceEventListener::clearAllMessages);
    }

    private void exportAllMessages(ActionEvent e) {
        mqttInstance.getEventListeners().forEach(InstanceEventListener::exportAllMessages);
    }

    public static class RegexSearchCreator extends PatternModel.RegexCreator {

        private boolean regexMatch;

        private boolean wordsMatch;

        @Override
        public String createRegEx(String searchExp) {
            if (regexMatch) {
                return createMatchRegEx(searchExp);
            }
            if (wordsMatch) {
                return createMatchWords(searchExp);
            }
            return super.createRegEx(searchExp);
        }

        protected String createMatchWords(String searchString) {
            StringBuffer buf = new StringBuffer(searchString.length() + 6);
            if (wordsMatch) {
                buf.append("\\b");
            }
            buf.append(Pattern.quote(searchString));
            if (wordsMatch) {
                buf.append("\\b");
            }
            return buf.toString();
        }

        private String createMatchRegEx(String searchString) {
            StringBuffer buf = new StringBuffer(searchString.length() + 6);
            if (wordsMatch) {
                buf.append("\\b");
            }
            buf.append(searchString);
            if (wordsMatch) {
                buf.append("\\b");
            }
            return buf.toString();
        }

        public void setRegexMatch(boolean regexMatch) {
            this.regexMatch = regexMatch;
        }

        public void setWordsMatch(boolean wordsMatch) {
            this.wordsMatch = wordsMatch;
        }

    }
}
