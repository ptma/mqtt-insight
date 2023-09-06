package com.mqttinsight.util;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttinsight.exception.VerificationException;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.function.Supplier;

/**
 * @author ptma
 */
public class Validator {


    public static void notEmpty(JTextComponent textComponent, Supplier<String> errorSupplier) throws VerificationException {
        if (textComponent.getText() == null || textComponent.getText().isEmpty()) {
            focusComponent(textComponent);
            throw new VerificationException(errorSupplier.get());
        }
    }

    public static void notEmpty(JComboBox comboBox, Supplier<String> errorSupplier) throws VerificationException {
        if (comboBox.getSelectedItem() == null || StrUtil.isEmpty(comboBox.getSelectedItem().toString())) {
            focusComponent(comboBox);
            throw new VerificationException(errorSupplier.get());
        }
    }

    public static void range(JTextComponent textComponent, int min, int max, Supplier<String> errorSupplier) throws VerificationException {
        if (textComponent.getText() == null || textComponent.getText().isEmpty()) {
            return;
        }
        if (!NumberUtil.isInteger(textComponent.getText())
            || Integer.parseInt(textComponent.getText()) < min
            || Integer.parseInt(textComponent.getText()) > max
        ) {
            focusComponent(textComponent);
            throw new VerificationException(errorSupplier.get());
        }
    }

    public static void range(JTextComponent textComponent, long min, long max, Supplier<String> errorSupplier) throws VerificationException {
        if (textComponent.getText() == null || textComponent.getText().isEmpty()) {
            return;
        }
        if (!NumberUtil.isLong(textComponent.getText())
            || Long.parseLong(textComponent.getText()) < min
            || Long.parseLong(textComponent.getText()) > max
        ) {
            focusComponent(textComponent);
            throw new VerificationException(errorSupplier.get());
        }
    }

    public static void maxLength(JTextComponent textComponent, int maxLength, Supplier<String> errorSupplier) throws VerificationException {
        if (textComponent.getText() == null || textComponent.getText().isEmpty()) {
            return;
        }
        if (textComponent.getText().length() > maxLength) {
            focusComponent(textComponent);
            throw new VerificationException(errorSupplier.get());
        }
    }

    public static void regex(JTextComponent textComponent, String regex, Supplier<String> errorSupplier) throws VerificationException {
        if (textComponent.getText() == null || textComponent.getText().isEmpty()) {
            return;
        }
        if (!ReUtil.isMatch(regex, textComponent.getText())) {
            focusComponent(textComponent);
            throw new VerificationException(errorSupplier.get());
        }
    }

    private static void focusComponent(JComponent component) {
        Container container = component;
        while (!(container.getParent() instanceof Window)) {
            // Active tabPanel index
            if (container.getParent() instanceof JTabbedPane) {
                JTabbedPane tabbedPanel = (JTabbedPane) container.getParent();
                tabbedPanel.setSelectedComponent(container);
            }
            container = container.getParent();
        }
        component.requestFocusInWindow();
    }
}
