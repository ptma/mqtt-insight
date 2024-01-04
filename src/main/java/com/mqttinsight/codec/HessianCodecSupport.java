package com.mqttinsight.codec;

import com.caucho.hessian.io.HessianInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.msgpack.jackson.dataformat.MessagePackMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ptma
 */
@Slf4j
public class HessianCodecSupport extends JsonCodecSupport {

    public HessianCodecSupport() {
    }


    @Override
    public boolean encodable() {
        return false;
    }

    @Override
    public String getName() {
        return "Hessian";
    }

    @Override
    public String toString(byte[] payload) {
        if (payload == null) {
            return "";
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(payload)) {
            HessianInput hInput = new HessianInput(bis);
            return Utils.toJsonString(hInput.readObject());
        } catch (IOException e) {
            log.error(e.getMessage());
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] toPayload(String json) {
        throw new UnsupportedOperationException("Hessian encoding is not supported.");
    }
}
