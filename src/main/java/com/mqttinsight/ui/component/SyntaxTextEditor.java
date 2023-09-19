package com.mqttinsight.ui.component;


import com.mqttinsight.config.ConfKeys;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.util.Const;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.CaretStyle;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.IOException;

/**
 * @author ptma
 */
public class SyntaxTextEditor extends RTextScrollPane {

    private final RSyntaxTextArea textArea;

    public SyntaxTextEditor() {
        super();
        this.textArea = new RSyntaxTextArea();
        this.textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        this.textArea.setCodeFoldingEnabled(true);
        this.textArea.setPaintTabLines(false);
        this.textArea.setTabSize(2);
        this.textArea.setShowMatchedBracketPopup(false);
        this.textArea.setBracketMatchingEnabled(false);
        this.textArea.setAutoIndentEnabled(true);
        this.textArea.setMargin(new Insets(5, 5, 5, 5));
        this.textArea.setCaretStyle(RSyntaxTextArea.INSERT_MODE, CaretStyle.VERTICAL_LINE_STYLE);

        String fontName = Configuration.instance().getString(ConfKeys.FONT_NAME, Const.EDITOR_FONT_NAME);
        Integer fontSize = Configuration.instance().getInt(ConfKeys.FONT_SIZE, Const.EDITOR_FONT_SIZE);
        if (fontName != null && fontSize != null) {
            Font font = StyleContext.getDefaultStyleContext().getFont(fontName, Font.PLAIN, fontSize);
            this.textArea.setFont(font);
        }
        this.textArea.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception e1) {
                    // ignore
                }
            }
        });
        this.setViewportView(textArea);
        this.setLineNumbersEnabled(true);
        this.setFoldIndicatorEnabled(true);
        this.getGutter().setBorder(new Gutter.GutterBorder(0, 5, 0, 2));
        try {
            boolean isDarkTheme = UIManager.getBoolean("laf.dark");
            Theme theme;
            if (isDarkTheme) {
                theme = Theme.load(getClass().getResourceAsStream("/com/mqttinsight/ui/rsyntaxtextarea/theme/dark.xml"));
            } else {
                theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default.xml"));
            }
            theme.apply(textArea);
        } catch (IOException ioe) {
            // ignore
        }
    }

    public RSyntaxTextArea textArea() {
        return textArea;
    }

    public void setText(String text) {
        this.textArea.setText(text);
        this.textArea.setCaretPosition(0);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.textArea.setEnabled(enabled);
    }

    public String getText() {
        return this.textArea.getText();
    }

    public void setSyntax(String syntax) {
        this.textArea.setSyntaxEditingStyle(syntax);
    }

    @Override
    public void updateUI() {
        super.updateUI();
    }

}
