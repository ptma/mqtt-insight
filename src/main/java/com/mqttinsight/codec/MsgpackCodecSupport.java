package com.mqttinsight.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    public String toString(byte[] payload) {
        try {
            ObjectNode object = MSGPACK_MAPPER.readValue(payload, ObjectNode.class);
            return Utils.toJsonString(object);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] toPayload(String text) {
        try {
            ObjectNode object = Utils.toJsonObject(text);
            return MSGPACK_MAPPER.writeValueAsBytes(object);
        } catch (Exception e) {
            log.error(e.getMessage());
            return super.toPayload(text);
        }
    }
}
