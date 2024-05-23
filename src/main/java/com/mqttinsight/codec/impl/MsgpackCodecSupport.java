package com.mqttinsight.codec.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mqttinsight.exception.CodecException;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.msgpack.jackson.dataformat.MessagePackMapper;

import java.nio.charset.StandardCharsets;

/**
 * @author ptma
 */
@Slf4j
public class MsgpackCodecSupport extends JsonCodecSupport {

    private final static ObjectMapper MSGPACK_MAPPER = new MessagePackMapper();

    public MsgpackCodecSupport() {
    }

    @Override
    public String getName() {
        return "MessagePack";
    }

    @Override
    public String toString(String topic, byte[] payload) {
        try {
            ObjectNode object = MSGPACK_MAPPER.readValue(payload, ObjectNode.class);
            return Utils.JSON.toString(object);
        } catch (Exception e) {
            log.warn(e.getMessage());
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] toPayload(String topic, String text) throws CodecException {
        try {
            ObjectNode object = Utils.JSON.toObject(text);
            return MSGPACK_MAPPER.writeValueAsBytes(object);
        } catch (Exception e) {
            throw new CodecException("An error occurred while encoding the message.", e);
        }
    }
}
