package com.mqttinsight.ui.log;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author ptma
 */
public class StdStream extends ByteArrayOutputStream {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private final LogTab logTab;
    private final StringBuilder buffer;
    private final PrintStream printStream;

    public StdStream(LogTab logTab, PrintStream printStream) {
        this.logTab = logTab;
        this.printStream = printStream;
        this.buffer = new StringBuilder();
    }

    @Override
    public void flush() {
        String text = this.toString();
        if (text.length() != 0) {
            this.append(text);
            this.reset();
        }
    }

    private void append(String text) {
        if (logTab.getTextArea().getText() != null && logTab.getTextArea().getText().length() == 0) {
            this.buffer.setLength(0);
        }

        if (LINE_SEPARATOR.equals(text)) {
            this.buffer.append(text);
        } else {
            this.buffer.append(text);
            this.clearBuffer();
        }
        if (logTab.isScrollToEnd()) {
            logTab.getTextArea().setCaretPosition(logTab.getTextArea().getDocument().getLength());
        }
    }

    private void clearBuffer() {
        String line = this.buffer.toString();
        SwingUtilities.invokeLater(() -> {
            logTab.getTextArea().append(line);
        });
        if (this.printStream != null) {
            this.printStream.print(line);
        }
        this.buffer.setLength(0);
    }
}
