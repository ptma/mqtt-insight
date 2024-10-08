package com.mqttinsight.codec;

import com.mqttinsight.exception.CodecException;

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
     * <p>
     * {@link org.fife.ui.rsyntaxtextarea.RSyntaxTextArea#getSyntaxEditingStyle()}
     * {@link org.fife.ui.rsyntaxtextarea.SyntaxConstants}
     */
    String getSyntax();

    default boolean encodable() {
        return true;
    }

    /**
     * Decode payload into text
     *
     * @param topic   message topic
     * @param payload Payload buffer bytes
     * @return Decoded text
     */
    String toString(String topic, byte[] payload);

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
     * Encode inputted text as payload
     *
     * @param text Inputted text
     * @return Payload buffer bytes
     */
    byte[] toPayload(String topic, String text) throws CodecException;
}
