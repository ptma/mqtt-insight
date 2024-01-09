package com.mqttinsight.codec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mqttinsight.exception.CodecException;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

class KryoCodecSupportTest {

    private Kryo kryo;

    @BeforeEach
    void init() {
        kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setReferences(true);
    }

    @Test
    void test() throws CodecException {
        KryoPojo pojo = new KryoPojo();
        pojo.setName("Ben");
        pojo.setFavoriteNumber(7);
        pojo.setFavoriteColor("red");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);
        kryo.writeClassAndObject(output, pojo);
        byte[] encoded = output.toBytes();
        System.out.println(Hex.toHexString(encoded));

        // MsgpackPojo 0100636f6d2e6d717474696e73696768742e636f6465632e4d73677061636b506f6aef01017265e4010e014265ee
        // KryoPojo 0100636f6d2e6d717474696e73696768742e636f6465632e4b72796f506f6aef01017265e4010e014265ee
        byte[] bytes = Hex.decode("0100636f6d2e6d717474696e73696768742e636f6465632e4b72796f506f6aef01017265e4010e014265ee");
        Object obj = kryo.readClassAndObject(new Input(bytes));
        Assertions.assertInstanceOf(KryoPojo.class, obj);
        Assertions.assertEquals(((KryoPojo)obj).getName(), "Ben");
        Assertions.assertEquals(((KryoPojo)obj).getFavoriteNumber(), 7);
        Assertions.assertEquals(((KryoPojo)obj).getFavoriteColor(), "red");
    }

}
