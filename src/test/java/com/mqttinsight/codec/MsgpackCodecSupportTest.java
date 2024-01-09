package com.mqttinsight.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttinsight.codec.impl.MsgpackCodecSupport;
import com.mqttinsight.exception.CodecException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.msgpack.jackson.dataformat.MessagePackMapper;

class MsgpackCodecSupportTest {

    private MsgpackCodecSupport codecSupport;

    @BeforeEach
    void init() {
        codecSupport = new MsgpackCodecSupport();
    }

    @Test
    void test() throws JsonProcessingException, CodecException {
        MsgpackPojo pojo = new MsgpackPojo();
        pojo.setName("Ben");
        pojo.setFavoriteNumber(7);
        pojo.setFavoriteColor("red");

        ObjectMapper objectMapper = new MessagePackMapper();
        byte[] encoded = objectMapper.writeValueAsBytes(pojo);
        String json = codecSupport.toString(encoded);
        byte[] encoded2 = codecSupport.toPayload(json);
        Assertions.assertArrayEquals(encoded, encoded2);
    }

}
