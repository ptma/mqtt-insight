package com.mqttinsight.codec;

import com.caucho.hessian.io.Hessian2Input;
import com.mqttinsight.util.Utils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class Hessian2CodecSupportTest {


    private Hessian2CodecSupport codecSupport;

    @BeforeEach
    void init() {
        codecSupport = new Hessian2CodecSupport();
    }

    @Test
    void test() throws IOException {
        /*
        HessianPojo pojo = new HessianPojo();
        pojo.setName("Ben");
        pojo.setFavoriteNumber(7);
        pojo.setFavoriteColor("red");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output h2o = new Hessian2Output(bos);
        h2o.writeObject(pojo);
        h2o.flush();
        byte[] bytes = bos.toByteArray();
         */
        byte[] bytes = Hex.decode("433021636f6d2e6d717474696e73696768742e636f6465632e4865737369616e506f6a6f93046e616d650e6661766f726974654e756d6265720d6661766f72697465436f6c6f72600342656e9703726564");
        String json = codecSupport.toString(bytes);
        Map<String, Object> map = Utils.toJsonObject(json, HashMap.class);
        Assertions.assertEquals(map.get("name"), "Ben");
        Assertions.assertEquals(map.get("favoriteNumber"), 7);
        Assertions.assertEquals(map.get("favoriteColor"), "red");
    }

}
