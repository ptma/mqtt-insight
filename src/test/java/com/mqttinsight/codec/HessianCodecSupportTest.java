package com.mqttinsight.codec;

import com.mqttinsight.codec.impl.HessianCodecSupport;
import com.mqttinsight.util.Utils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class HessianCodecSupportTest {


    private HessianCodecSupport codecSupport;

    @BeforeEach
    void init() {
        codecSupport = new HessianCodecSupport();
    }

    @Test
    void test() throws IOException {
        /*
        HessianPojo pojo = new HessianPojo();
        pojo.setName("Ben");
        pojo.setFavoriteNumber(7);
        pojo.setFavoriteColor("red");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HessianOutput ho = new HessianOutput(bos);
        ho.writeObject(pojo);
        ho.flush();
        byte[] bytes = bos.toByteArray();
         */
        byte[] bytes = Hex.decode("4d740021636f6d2e6d717474696e73696768742e636f6465632e4865737369616e506f6a6f5300046e616d6553000342656e53000e6661766f726974654e756d626572490000000753000d6661766f72697465436f6c6f725300037265647a");

        String json = codecSupport.toString("testtopic", bytes);
        Map<String, Object> map = Utils.JSON.toObject(json, HashMap.class);
        Assertions.assertEquals(map.get("name"), "Ben");
        Assertions.assertEquals(map.get("favoriteNumber"), 7);
        Assertions.assertEquals(map.get("favoriteColor"), "red");

    }

}
