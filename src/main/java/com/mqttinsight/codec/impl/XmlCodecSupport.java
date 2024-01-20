package com.mqttinsight.codec.impl;

import cn.hutool.core.util.XmlUtil;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.exception.CodecException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ptma
 */
@Slf4j
public class XmlCodecSupport extends PlainCodecSupport implements CodecSupport {

    @Override
    public String getName() {
        return "XML";
    }

    @Override
    public String getSyntax() {
        return "text/xml";
    }

    @Override
    public String toPrettyString(String payload) {
        try {
            return XmlUtil.format(payload);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return payload;
        }
    }

    @Override
    public byte[] toPayload(String text) throws CodecException {
        return super.toPayload(text);
    }

}
