package com.mqttinsight.util;

import com.mqttinsight.MqttInsightApplication;
import org.jdesktop.swingx.graphics.ColorUtilities;
import raven.toast.Notifications;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * @author ptma
 */
public class Utils {

    public static class Toast {

        public static void info(String message) {
            Notifications.getInstance().show(Notifications.Type.INFO,
                Notifications.Location.TOP_RIGHT,
                5000,
                message
            );
        }

        public static void success(String message) {
            Notifications.getInstance().show(Notifications.Type.SUCCESS,
                Notifications.Location.TOP_RIGHT,
                5000,
                message
            );
        }

        public static void warn(String message) {
            Notifications.getInstance().show(Notifications.Type.WARNING,
                Notifications.Location.TOP_RIGHT,
                5000,
                message
            );
        }

        public static void error(String message) {
            Notifications.getInstance().show(Notifications.Type.ERROR,
                Notifications.Location.TOP_RIGHT,
                5000,
                message
            );
        }
    }

    public static class Message {

        public static void info(String message) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(MqttInsightApplication.frame),
                message,
                LangUtil.getString("Information"), JOptionPane.INFORMATION_MESSAGE);
        }

        public static void info(String message, Throwable ex) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(MqttInsightApplication.frame),
                message + "\n\n" + ex.getMessage(),
                LangUtil.getString("Information"), JOptionPane.INFORMATION_MESSAGE);
        }

        public static void warning(String message, Throwable ex) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(MqttInsightApplication.frame),
                message + "\n\n" + ex.getMessage(),
                LangUtil.getString("Warning"), JOptionPane.WARNING_MESSAGE);
        }

        public static void warning(String message) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(MqttInsightApplication.frame),
                message,
                LangUtil.getString("Warning"), JOptionPane.WARNING_MESSAGE);
        }

        public static void error(String message, Throwable ex) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(MqttInsightApplication.frame),
                message + "\n\n" + ex.getMessage(),
                LangUtil.getString("Error"), JOptionPane.ERROR_MESSAGE);
        }

        public static void error(String message) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(MqttInsightApplication.frame),
                message,
                LangUtil.getString("Error"), JOptionPane.ERROR_MESSAGE);
        }

        public static int confirm(String message) {
            return JOptionPane.showConfirmDialog(MqttInsightApplication.frame, message,
                LangUtil.getString("Confirm"), JOptionPane.YES_NO_OPTION);
        }
    }

    public static class UI {

        public static void buttonText(AbstractButton component, String text) {
            StringBuilder result = new StringBuilder();
            boolean haveMnemonic = false;
            char mnemonic = '\0';
            int mnemonicIndex = -1;
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '&') {
                    i++;
                    if (i == text.length()) {
                        break;
                    }
                    if (!haveMnemonic && text.charAt(i) != '&') {
                        haveMnemonic = true;
                        mnemonic = text.charAt(i);
                        mnemonicIndex = result.length();
                    }
                }
                result.append(text.charAt(i));
            }
            component.setText(result.toString());
            if (haveMnemonic) {
                component.setMnemonic(mnemonic);
                component.setDisplayedMnemonicIndex(mnemonicIndex);
            }
        }

        public static JMenu createMenu(String menuText) {
            return createMenu(menuText, null);
        }

        public static JMenu createMenu(String menuText, ActionListener action) {
            JMenu menu = new JMenu();
            Utils.UI.buttonText(menu, menuText);

            if (action != null) {
                menu.addActionListener(action);
            }
            return menu;
        }

        public static JMenuItem createMenuItem(String menuText, ActionListener action) {
            JMenuItem menuItem = new JMenuItem();
            Utils.UI.buttonText(menuItem, menuText);

            if (action != null) {
                menuItem.addActionListener(action);
            }
            return menuItem;
        }
    }

    public static Color brighter(Color color, float factor) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();
        int i = (int) (1.0 / (1.0 - factor));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;

        return new Color(Math.min((int) (r / factor), 255),
            Math.min((int) (g / factor), 255),
            Math.min((int) (b / factor), 255),
            alpha);
    }

    public static Color darker(Color color, float factor) {
        return new Color(Math.max((int) (color.getRed() * factor), 0),
            Math.max((int) (color.getGreen() * factor), 0),
            Math.max((int) (color.getBlue() * factor), 0),
            color.getAlpha());
    }

    public static Color generateRandomColor() {
        Random random = new Random(System.currentTimeMillis());
        float lightness = 0.5f;
        float randomHue = random.nextInt(360) / 360f;
        Color color = ColorUtilities.HSLtoRGB(randomHue, 1.0f, lightness);
        int randomAlpha = random.nextInt(15) + 25; // 25 - 40
        return Utils.mixColorsWithAlpha(UIManager.getColor("Table.background"), color, randomAlpha);
    }

    public static Color mixColorsWithAlpha(Color color1, Color color2, int alpha) {
        float factor = alpha / 255f;
        int red = (int) (color1.getRed() * (1 - factor) + color2.getRed() * factor);
        int green = (int) (color1.getGreen() * (1 - factor) + color2.getGreen() * factor);
        int blue = (int) (color1.getBlue() * (1 - factor) + color2.getBlue() * factor);
        return new Color(red, green, blue);
    }

    public static Color getReverseForegroundColor(Color color) {
        float grayLevel = (color.getRed() * 299 + color.getGreen() * 587 + color.getBlue() * 114) / 1000f / 255;
        return grayLevel >= 0.45 ? Color.BLACK : Color.WHITE;
    }

    public static String md5(String content) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] buffer = content.getBytes();
        messageDigest.update(buffer, 0, buffer.length);
        byte[] digest = messageDigest.digest();
        StringBuilder builder = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

}
