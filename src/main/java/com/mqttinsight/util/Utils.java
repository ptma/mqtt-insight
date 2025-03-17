package com.mqttinsight.util;

import cn.hutool.core.img.ColorUtil;
import cn.hutool.core.lang.PatternPool;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.mqttinsight.MqttInsightApplication;
import com.mqttinsight.ui.component.ExceptionDialog;
import com.mqttinsight.ui.component.NormalMenuItem;
import com.mqttinsight.ui.form.InputDialog;
import org.jdesktop.swingx.graphics.ColorUtilities;
import org.xml.sax.InputSource;
import raven.toast.Notifications;

import javax.swing.*;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ptma
 */
public class Utils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ObjectMapper JSON_MAPPER = JsonMapper.builder()
        .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
        .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
        .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
        .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
        .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
        .enable(JsonReadFeature.ALLOW_MISSING_VALUES)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();

    private static final Color DARKER_TEXT_COLOR = Color.BLACK;
    private static final Color LIGHTER_TEXT_COLOR = ColorUtil.hexToColor("#ABB2BF");

    private static final Map<String, XPathExpression> XPATH_CACHE = new ConcurrentHashMap<>();

    static {
        // JsonPath default JsonProviders
        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

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
            info(MqttInsightApplication.frame, message);
        }

        public static void info(Component parent, String message) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(MqttInsightApplication.frame),
                message,
                LangUtil.getString("Information"), JOptionPane.INFORMATION_MESSAGE);
        }

        public static void info(String message, Throwable ex) {
            info(MqttInsightApplication.frame, message, ex);
        }

        public static void info(Component parent, String message, Throwable ex) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(parent),
                message + "\n\n" + ex.getMessage(),
                LangUtil.getString("Information"), JOptionPane.INFORMATION_MESSAGE);
        }

        public static void warning(String message, Throwable ex) {
            warning(MqttInsightApplication.frame, message, ex);
        }

        public static void warning(Component parent, String message, Throwable ex) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(parent),
                message + "\n\n" + ex.getMessage(),
                LangUtil.getString("Warning"), JOptionPane.WARNING_MESSAGE);
        }

        public static void warning(String message) {
            warning(MqttInsightApplication.frame, message);
        }

        public static void warning(Component parent, String message) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(parent),
                message,
                LangUtil.getString("Warning"), JOptionPane.WARNING_MESSAGE);
        }

        public static void error(String message, Throwable ex) {
            error(MqttInsightApplication.frame, message, ex);
        }

        public static void error(Component parent, String message, Throwable ex) {
            if (ex == null) {
                error(parent, message);
            } else {
                ExceptionDialog.open(SwingUtilities.windowForComponent(parent), message, ex);
            }
        }

        public static void error(String message) {
            error(MqttInsightApplication.frame, message);
        }

        public static void error(Component parent, String message) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(parent),
                message,
                LangUtil.getString("Error"), JOptionPane.ERROR_MESSAGE);
        }

        /**
         * @param message
         * @return JOptionPane.YES_OPTION, JOptionPane.NO_OPTION
         */
        public static int confirm(String message) {
            return confirm(MqttInsightApplication.frame, message);
        }

        /**
         * @param message
         * @return JOptionPane.YES_OPTION, JOptionPane.NO_OPTION
         */
        public static int confirm(Component parent, String message) {
            return JOptionPane.showConfirmDialog(parent, message,
                LangUtil.getString("Confirm"), JOptionPane.YES_NO_OPTION);
        }

        public static void input(String message, Consumer<String> inputConsumer) {
            input(MqttInsightApplication.frame, message, null, null, inputConsumer);
        }

        public static void input(String message, Set<String> options, Consumer<String> inputConsumer) {
            input(MqttInsightApplication.frame, message, null, options, inputConsumer);
        }

        public static void input(String message, String defaultValue, Consumer<String> inputConsumer) {
            input(MqttInsightApplication.frame, message, defaultValue, inputConsumer);
        }

        public static void input(Frame parent, String message, Consumer<String> inputConsumer) {
            input(parent, message, null, null, inputConsumer);
        }

        public static void input(Frame parent, String message, Set<String> options, Consumer<String> inputConsumer) {
            input(parent, message, null, options, inputConsumer);
        }

        public static void input(Frame parent, String message, String defaultValue, Consumer<String> inputConsumer) {
            InputDialog.open(parent, message, defaultValue, null, inputConsumer);
        }

        public static void input(Frame parent, String message, String defaultValue, Set<String> options, Consumer<String> inputConsumer) {
            InputDialog.open(parent, message, defaultValue, options, inputConsumer);
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

        public static JMenuItem createMenuItem(String menuText) {
            return createMenuItem(menuText, null);
        }

        public static JMenuItem createMenuItem(String menuText, ActionListener action) {
            JMenuItem menuItem = new NormalMenuItem();
            Utils.UI.buttonText(menuItem, menuText);

            if (action != null) {
                menuItem.addActionListener(action);
            }
            return menuItem;
        }

        public static JMenuItem createCheckBoxMenuItem(String menuText, ActionListener action) {
            JMenuItem menuItem = new JCheckBoxMenuItem();
            Utils.UI.buttonText(menuItem, menuText);

            if (action != null) {
                menuItem.addActionListener(action);
            }
            return menuItem;
        }

        public static JMenuItem createMenuItem(String menuText, Action action) {
            JMenuItem menuItem = new NormalMenuItem();
            if (action != null) {
                menuItem.setAction(action);
            }
            Utils.UI.buttonText(menuItem, menuText);
            return menuItem;
        }
    }

    public static class JSON {

        public static <T> T readObject(File jsonFile, Class<T> valueType) throws IOException {
            return JSON_MAPPER.readValue(jsonFile, valueType);
        }

        public static String toString(Object object) throws JsonProcessingException {
            return JSON_MAPPER.writeValueAsString(object);
        }

        public static ObjectNode toObject(String jsonString) throws JsonProcessingException {
            return JSON_MAPPER.readValue(jsonString, ObjectNode.class);
        }

        public static <T> T toObject(String jsonString, Class<T> valueType) throws JsonProcessingException {
            return JSON_MAPPER.readValue(jsonString, valueType);
        }
    }

    /**
     * 获取比给定的颜色更亮的颜色
     *
     * @param color  指定的颜色。
     * @param factor 调节因子 (0, 1)，值越小颜色越亮。
     * @return 变亮后的新颜色。
     */
    public static Color brighter(Color color, float factor) {
        if (factor <= 0 || factor >= 1) {
            throw new IllegalArgumentException("The factor must be within the range of (0, 1)");
        }
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();

        float threshold = 1.0f - factor;

        if (r == 0 && g == 0 && b == 0) {
            int c = Math.min((int) (threshold * 255), 255);
            return new Color(c, c, c, alpha);
        }

        r = Math.min((int) (r / factor), 255);
        g = Math.min((int) (g / factor), 255);
        b = Math.min((int) (b / factor), 255);

        return new Color(r, g, b, alpha);
    }

    /**
     * 获取比给定的颜色更暗的颜色。
     *
     * @param color  指定的颜色。
     * @param factor 调节因子，[0, 1]，因子越小颜色越暗。
     * @return 变暗后的新颜色。
     */
    public static Color darker(Color color, float factor) {
        if (factor < 0 || factor > 1) {
            throw new IllegalArgumentException("The factor must be within the range of [0, 1]");
        }
        return new Color(Math.max((int) (color.getRed() * factor), 0),
            Math.max((int) (color.getGreen() * factor), 0),
            Math.max((int) (color.getBlue() * factor), 0),
            color.getAlpha());
    }

    public static Color generateRandomColor() {
        float lightness = (RANDOM.nextInt(40) + 30) / 100f;//0.3--0.7
        float randomHue = RANDOM.nextInt(360) / 360f;
        Color color = ColorUtilities.HSLtoRGB(randomHue, 1.0f, lightness);
        int randomAlpha = RANDOM.nextInt(30) + 10; // 10--40
        return Utils.mixColorsWithAlpha(UIManager.getColor("Table.background"), color, randomAlpha);
    }

    public static Color mixColorsWithAlpha(Color color1, Color color2, int alpha) {
        float factor = alpha / 255f;
        int red = (int) (color1.getRed() * (1 - factor) + color2.getRed() * factor);
        int green = (int) (color1.getGreen() * (1 - factor) + color2.getGreen() * factor);
        int blue = (int) (color1.getBlue() * (1 - factor) + color2.getBlue() * factor);
        return new Color(red, green, blue);
    }

    /**
     * 根据给定的颜色和界面主题，获取对应背景色的前景颜色（文本颜色）。
     * 该方法通过计算给定颜色的灰度级别来决定返回的文本颜色应该是深色还是浅色。
     *
     * @param bgColor 需要反转其前景色的颜色对象。
     * @param darkLaf 指示当前界面主题是否为暗色主题的布尔值。
     * @return 与给定颜色相反的文本颜色，基于界面主题的黑暗程度。
     */
    public static Color getReverseForegroundColor(Color bgColor, boolean darkLaf) {
        float grayLevel = (bgColor.getRed() * 299 + bgColor.getGreen() * 587 + bgColor.getBlue() * 114) / 1000f / 255;
        return grayLevel >= 0.45 ? DARKER_TEXT_COLOR : (
            darkLaf ? LIGHTER_TEXT_COLOR : Color.WHITE
        );
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

    public static String findRegexMatchGroup(String regex, String content) {
        Pattern pattern = PatternPool.get(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.matches()) {
            return matcher.group(matcher.groupCount() >= 1 ? 1 : 0);
        } else {
            return null;
        }
    }

    public static boolean verifyJsonPath(String jsonPath) {
        try {
            JsonPath.compile(jsonPath);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    public static String getSingleValueByJsonPath(String jsonPath, String source) {
        try {
            Object value = JsonPath.read(source, jsonPath);
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                return list.isEmpty() ? "" : list.get(0).toString();
            } else {
                return value.toString();
            }
        } catch (Exception ignore) {
            return null;
        }
    }

    public static boolean verifyXPath(String xpath) {
        try {
            XPATH_CACHE.computeIfAbsent(xpath, (key) -> {
                try {
                    return XPathFactory.newInstance().newXPath().compile(key);
                } catch (XPathExpressionException e) {
                    throw new RuntimeException(e);
                }
            });
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    public static String getByXPath(String xpath, String source) {
        try (StringReader reader = new StringReader(source)) {
            XPathExpression exp = XPATH_CACHE.computeIfAbsent(xpath, (key) -> {
                try {
                    return XPathFactory.newInstance().newXPath().compile(key);
                } catch (XPathExpressionException e) {
                    throw new RuntimeException(e);
                }
            });
            return (String) exp.evaluate(new InputSource(reader), XPathConstants.STRING);
        } catch (Exception ignore) {
            return null;
        }
    }

    public static Throwable getRootThrowable(Throwable throwable) {
        Throwable result = throwable;
        while (result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }
}
