package com.mqttinsight.codec.impl;

import cn.hutool.core.util.XmlUtil;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.exception.CodecException;

/**
 * @author ptma
 */
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
        return XmlUtil.format(payload);
    }

    @Override
    public byte[] toPayload(String text) throws CodecException {
        return super.toPayload(text);
    }

}
