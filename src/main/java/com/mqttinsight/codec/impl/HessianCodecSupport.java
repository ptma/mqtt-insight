package com.mqttinsight.codec.impl;

import com.caucho.hessian.io.HessianInput;
import com.mqttinsight.exception.CodecException;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;

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
            return Utils.JSON.toString(hInput.readObject());
        } catch (IOException e) {
            log.error(e.getMessage());
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] toPayload(String json) throws CodecException {
        throw new CodecException("Hessian encoding is not supported.");
    }
}
