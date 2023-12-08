package com.mqttinsight.ui.component;

import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;

/**
 * @author ptma
 */
public class TextFieldPopupEventListener implements AWTEventListener {

    final JPopupMenu popupMenu;
    final JMenuItem copyMenu;
    final JMenuItem pasteMenu;
    final JMenuItem cutMenu;
    final JMenuItem selAllMenu;

    public TextFieldPopupEventListener() {
        super();
        popupMenu = new JPopupMenu();

        cutMenu = Utils.UI.createMenuItem(LangUtil.getString("Cut"), new DefaultEditorKit.CutAction());
        popupMenu.add(cutMenu);
        copyMenu = Utils.UI.createMenuItem(LangUtil.getString("Copy"), new DefaultEditorKit.CopyAction());
        popupMenu.add(copyMenu);
        pasteMenu = Utils.UI.createMenuItem(LangUtil.getString("Paste"), new DefaultEditorKit.PasteAction());
        popupMenu.add(pasteMenu);

        popupMenu.addSeparator();

        selAllMenu = Utils.UI.createMenuItem(LangUtil.getString("SelectAll"), new SelectAllAction());
        popupMenu.add(selAllMenu);
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            MouseEvent mev = (MouseEvent) event;
            if (mev.getComponent() instanceof JTextField && mev.getButton() == MouseEvent.BUTTON3) {
                JTextField textField = (JTextField) mev.getComponent();
                cutMenu.setEnabled(canCut(textField));
                copyMenu.setEnabled(canCopy(textField));
                pasteMenu.setEnabled(canPaste(textField));
                popupMenu.show(textField, mev.getX(), mev.getY());
            }
        }
    }

    public boolean canCut(JTextField textField) {
        if (!textField.isEditable()) {
            return false;
        }
        int start = textField.getSelectionStart();
        int end = textField.getSelectionEnd();
        return start != end;
    }

    public boolean canCopy(JTextField textField) {
        int start = textField.getSelectionStart();
        int end = textField.getSelectionEnd();
        return start != end;
    }

    public boolean canPaste(JTextField textField) {
        if (!textField.isEditable()) {
            return false;
        }
        boolean b = false;
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable content = clipboard.getContents(this);
        try {
            if (content.getTransferData(DataFlavor.stringFlavor) instanceof String) {
                b = true;
            }
        } catch (Exception e) {
        }
        return b;
    }

    static class SelectAllAction extends TextAction {

        SelectAllAction() {
            super("select-all");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                Document doc = target.getDocument();
                target.setCaretPosition(0);
                target.moveCaretPosition(doc.getLength());
            }
        }

    }
}
