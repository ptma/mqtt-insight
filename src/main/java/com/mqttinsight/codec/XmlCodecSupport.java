package com.mqttinsight.codec;

import cn.hutool.core.util.XmlUtil;

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
    public byte[] toPayload(String text) {
        return super.toPayload(text);
    }

}
