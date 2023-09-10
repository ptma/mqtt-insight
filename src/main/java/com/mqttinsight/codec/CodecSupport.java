package com.mqttinsight.codec;

/**
 * Encoder and decoder support
 *
 * @author ptma
 */
public interface CodecSupport {

    String DEFAULT = "Default";
    String PLAIN = "Plain";
    String JSON = "JSON";

    /**
     * Codec name
     */
    String getName();

    /**
     * SyntaxEditingStyle for RSyntaxTextArea
     *
     * @link <a href="https://javadoc.fifesoft.com/rsyntaxtextarea/org/fife/ui/rsyntaxtextarea/RSyntaxTextArea.html#setSyntaxEditingStyle(java.lang.String)">RSyntaxTextArea.setSyntaxEditingStyle</a>
     * @see org.fife.ui.rsyntaxtextarea.RSyntaxTextArea#setSyntaxEditingStyle(String)
     */
    String getSyntax();

    /**
     * Decode payload into text
     *
     * @param payload Payload buffer bytes
     * @return Decoded text
     */
    String toString(byte[] payload);

    /**
     * Format payload
     *
     * @param payload Decoded payload
     * @return Formatted payload
     */
    default String toPrettyString(String payload) {
        return payload;
    }

    /**
     * Encode inputted text  as payload
     *
     * @param text Inputted text
     * @return Payload buffer bytes
     */
    byte[] toPayload(String text);
}
