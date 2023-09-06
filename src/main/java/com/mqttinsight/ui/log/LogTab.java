package com.mqttinsight.ui.log;

import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.ui.component.SingleLineBorder;
import com.mqttinsight.util.Const;
import com.mqttinsight.util.Icons;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;

import javax.swing.*;
import javax.swing.text.StyleContext;
import java.awt.*;

/**
 * @author ptma
 */
public class LogTab extends JPanel {

    private static final KeyStroke FIND_HOT_KEY = KeyStroke.getKeyStroke("ctrl F");
    private static final KeyStroke ESC_KEY = KeyStroke.getKeyStroke("ESCAPE");

    private JPanel toolbarPanel;
    private LogToolbar searchToolbar;
    private JScrollPane textScrollPanel;
    private JTextArea textArea;

    private boolean scrollToEnd = true;

    public LogTab() {
        super();
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        textScrollPanel = new JScrollPane();
        add(textScrollPanel, BorderLayout.CENTER);
        textArea = new JTextArea();
        textArea.requestFocus();
        textArea.setCaretColor(Color.decode("#EEEEEE"));
        textArea.setForeground(Color.decode("#EEEEEE"));
        textArea.setBackground(Color.decode("#282C34"));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuClear = Utils.UI.createMenuItem(LangUtil.getString("ClearAll"), (e) -> clearAll());
        menuClear.setIcon(Icons.REMOVE);
        popupMenu.add(menuClear);
        textArea.setComponentPopupMenu(popupMenu);

        String fontName = Configuration.instance().getString(ConfKeys.FONT_NAME, Const.EDITOR_FONT_NAME);
        Integer fontSize = Configuration.instance().getInt(ConfKeys.FONT_SIZE, Const.EDITOR_FONT_SIZE);
        if (fontName != null && fontSize != null) {
            Font font = StyleContext.getDefaultStyleContext().getFont(fontName, Font.PLAIN, fontSize);
            textArea.setFont(font);
        }
        textScrollPanel.setViewportView(textArea);
        textArea.registerKeyboardAction(e -> {
            activeFindToolbar();
        }, FIND_HOT_KEY, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        textArea.registerKeyboardAction(e -> {
            closeFindToolbar();
        }, ESC_KEY, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        toolbarPanel = new JPanel();
        toolbarPanel.setVisible(false);
        toolbarPanel.setLayout(new BorderLayout(0, 0));
        toolbarPanel.setBorder(new SingleLineBorder(UIManager.getColor("Component.borderColor"), true, true, false, true));
        searchToolbar = new LogToolbar(this, textArea);
        toolbarPanel.add(searchToolbar, BorderLayout.CENTER);
        add(toolbarPanel, BorderLayout.NORTH);

        StdHook.hook(this);
    }

    public void clearAll() {
        textArea.setText("");
    }

    public void toggleScrollToEnd(boolean scrollToEnd) {
        this.scrollToEnd = scrollToEnd;
    }

    public void activeFindToolbar() {
        toolbarPanel.setVisible(true);
        searchToolbar.focusSearch();
    }

    public void closeFindToolbar() {
        toolbarPanel.setVisible(false);
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public boolean isScrollToEnd() {
        return scrollToEnd;
    }
}
