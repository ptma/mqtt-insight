package com.mqttinsight.codec;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.HessianInput;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ptma
 */
@Slf4j
public class Hessian2CodecSupport extends JsonCodecSupport {

    public Hessian2CodecSupport() {
    }


    @Override
    public boolean encodable() {
        return false;
    }

    @Override
    public String getName() {
        return "Hessian2";
    }

    @Override
    public String toString(byte[] payload) {
        if (payload == null) {
            return "";
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(payload)) {
            Hessian2Input h2Input = new Hessian2Input(bis);
            return Utils.toJsonString(h2Input.readObject());
        } catch (IOException e) {
            log.error(e.getMessage());
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] toPayload(String json) {
        throw new UnsupportedOperationException("Hessian2 encoding is not supported.");
    }
}
