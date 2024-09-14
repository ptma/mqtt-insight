package com.mqttinsight.ui.component;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatTextField;
import com.mqttinsight.ui.form.panel.BasePreviewPanel;
import com.mqttinsight.ui.form.panel.MessagePreviewPanel;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import net.miginfocom.swing.MigLayout;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * @author ptma
 */
public class TextSearchToolbar extends JPanel {

    protected static final Color NOTFOUND_TEXT_COLOR = Color.decode("#FF6666");
    protected static final Color FOUND_TEXT_COLOR = UIManager.getColor("TextField.foreground");

    private final BasePreviewPanel parent;
    private final JTextArea textArea;
    private FlatTextField searchField;
    private JToggleButton matchCaseButton;
    private JToggleButton wholeWordsButton;
    private JToggleButton regexButton;
    private JButton previousButton;
    private JButton nextButton;
    private JButton closeButton;
    private SearchContext context;
    private boolean searching = false;

    public TextSearchToolbar(final BasePreviewPanel parent, final JTextArea textArea) {
        super();
        this.parent = parent;
        this.textArea = textArea;
        initComponents();
    }

    private void initComponents() {
        this.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.setLayout(new MigLayout(
            "insets 0,gap 0",
            "[grow][][][][60]",
            "[]"
        ));
        // search input
        searchField = new FlatTextField();
        searchField.setMinimumWidth(220);
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
        this.add(searchField, "growx");

        // match case button
        matchCaseButton = new JToggleButton(Icons.SEARCH_MATCHCASE);
        matchCaseButton.setRolloverIcon(Icons.SEARCH_MATCHCASE_HOVER);
        matchCaseButton.setSelectedIcon(Icons.SEARCH_MATCHCASE_SELECTED);
        matchCaseButton.setToolTipText(LangUtil.getString("MatchCase") + " (Alt + C)");
        matchCaseButton.addActionListener(e -> {
            find(true);
        });

        // whole words button
        wholeWordsButton = new JToggleButton(Icons.SEARCH_WORDS);
        wholeWordsButton.setRolloverIcon(Icons.SEARCH_WORDS_HOVER);
        wholeWordsButton.setSelectedIcon(Icons.SEARCH_WORDS_SELECTED);
        wholeWordsButton.setToolTipText(LangUtil.getString("WholeWords") + " (Alt + W)");
        wholeWordsButton.addActionListener(e -> {
            find(true);
        });

        // regex button
        regexButton = new JToggleButton(Icons.SEARCH_REGEX);
        regexButton.setRolloverIcon(Icons.SEARCH_REGEX_HOVER);
        regexButton.setSelectedIcon(Icons.SEARCH_REGEX_SELECTED);
        regexButton.setToolTipText(LangUtil.getString("RegularExpression") + " (Alt + X)");
        regexButton.addActionListener(e -> {
            find(true);
        });

        // search inpput trailing buttons
        JToolBar trailingToolbar = new JToolBar();
        trailingToolbar.add(matchCaseButton);
        trailingToolbar.add(wholeWordsButton);
        trailingToolbar.add(regexButton);
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, trailingToolbar);

        // previous button
        previousButton = new JButton(Icons.PREVIOUS_OCCURENCE);
        previousButton.setEnabled(false);
        previousButton.addActionListener(e -> {
            find(false);
        });
        previousButton.setToolTipText(LangUtil.getString("PreviousOccurrence") + " (Shift + F3)");
        previousButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        this.add(previousButton, "");

        // next button
        nextButton = new JButton(Icons.NEXT_OCCURENCE);
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> {
            find(true);
        });
        nextButton.setToolTipText(LangUtil.getString("NextOccurrence") + " (F3)");
        nextButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        this.add(nextButton, "");

        this.add(new JToolBar.Separator(), "");

        closeButton = new JButton();
        closeButton.setIcon(Icons.CANCEL);
        closeButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
        closeButton.addActionListener(e -> {
            parent.closeFindToolbar();
        });

        this.add(closeButton, "right");

        // Register shotcut
        searchField.registerKeyboardAction(e -> {
            matchCaseButton.setSelected(!matchCaseButton.isSelected());
            find(true);
        }, KeyStroke.getKeyStroke("alt C"), JComponent.WHEN_FOCUSED);
        searchField.registerKeyboardAction(e -> {
            wholeWordsButton.setSelected(!wholeWordsButton.isSelected());
            find(true);
        }, KeyStroke.getKeyStroke("alt W"), JComponent.WHEN_FOCUSED);
        searchField.registerKeyboardAction(e -> {
            regexButton.setSelected(!regexButton.isSelected());
            find(true);
        }, KeyStroke.getKeyStroke("alt X"), JComponent.WHEN_FOCUSED);
        searchField.registerKeyboardAction(e -> find(true), KeyStroke.getKeyStroke("F3"), JComponent.WHEN_FOCUSED);
        searchField.registerKeyboardAction(e -> find(false), KeyStroke.getKeyStroke("shift F3"), JComponent.WHEN_FOCUSED);
        searchField.registerKeyboardAction(e -> {
            cancelSearch();
            parent.closeFindToolbar();
        }, KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_FOCUSED);
        textArea.registerKeyboardAction(e -> find(true), KeyStroke.getKeyStroke("F3"), JComponent.WHEN_FOCUSED);
        textArea.registerKeyboardAction(e -> find(false), KeyStroke.getKeyStroke("shift F3"), JComponent.WHEN_FOCUSED);
        textArea.registerKeyboardAction(e -> {
            cancelSearch();
            parent.closeFindToolbar();
        }, KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_FOCUSED);
    }

    public void focusSearch(String text) {
        if (text != null && !text.isEmpty()) {
            searchField.setText(text);
        }
        searchField.requestFocus();
        searchField.selectAll();
    }

    public void find(boolean forward) {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) {
            cancelSearch();
            return;
        }
        searching = true;
        boolean found = doSearch(searchText, forward);
        if (found) {
            searchField.setForeground(FOUND_TEXT_COLOR);
        } else {
            searchField.setForeground(NOTFOUND_TEXT_COLOR);
        }
        previousButton.setEnabled(found);
        nextButton.setEnabled(found);
    }

    protected boolean doSearch(String searchText, boolean forward) {
        if (context == null) {
            context = new SearchContext();
        }
        context.setSearchFor(searchText);
        context.setMatchCase(matchCaseButton.isSelected());
        context.setWholeWord(wholeWordsButton.isSelected());
        context.setRegularExpression(regexButton.isSelected());
        context.setSearchForward(forward);
        context.setSearchWrap(true);
        context.setMarkAll(false);

        return SearchEngine.find(textArea, context).wasFound();
    }

    public void cancelSearch() {
        if (searching) {
            doSearch(null, true);
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
            searching = false;
        }
    }

}
