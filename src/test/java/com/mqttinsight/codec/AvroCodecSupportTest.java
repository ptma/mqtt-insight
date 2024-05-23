package com.mqttinsight.codec;

import com.mqttinsight.codec.impl.AvroCodecSupport;
import com.mqttinsight.exception.CodecException;
import com.mqttinsight.util.Utils;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class AvroCodecSupportTest {

    @Test
    void testPojo() throws IOException, CodecException {
        String filename = getClass().getResource("/test.avsc").getFile();
        AvroCodecSupport codecSupport = new AvroCodecSupport("Test", filename);

        AvroPojo pojo = new AvroPojo();
        pojo.setName("Ben");
        pojo.setFavoriteNumber(7);
        pojo.setFavoriteColor("red");

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            DatumWriter<AvroPojo> writer = new SpecificDatumWriter<AvroPojo>(AvroPojo.class);
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            writer.write(pojo, encoder);
            encoder.flush();
            byte[] encoded = outputStream.toByteArray();

            String json = codecSupport.toString("testtopic", encoded);
            byte[] encoded2 = codecSupport.toPayload("testtopic", json);

            Assertions.assertArrayEquals(encoded, encoded2);
        }
    }

    @Test
    void testUnionPojo() throws IOException, CodecException {
        String filename = getClass().getResource("/test_union.avsc").getFile();
        AvroCodecSupport codecSupport = new AvroCodecSupport("Test", filename);

        Map<String, Object> pojo = new HashMap<>();
        pojo.put("name", "Jack");
        pojo.put("favorite_number", 9);
        pojo.put("favorite_color", "red");

        String pojoJson = Utils.JSON.toString(pojo);
        byte[] encoded = codecSupport.toPayload("testtopic", pojoJson);
        String decodedJson = codecSupport.toString("testtopic", encoded);

        Map<String, Object> decodedMap = Utils.JSON.toObject(decodedJson, HashMap.class);
        Assertions.assertEquals(decodedMap.get("name"), "Jack");
        Assertions.assertEquals(decodedMap.get("favorite_number"), 9);
        Assertions.assertEquals(decodedMap.get("favorite_color"), "red");
    }

    @Test
    void testUnionPojo2() throws IOException, CodecException {
        String filename = getClass().getResource("/test_union.avsc").getFile();
        AvroCodecSupport codecSupport = new AvroCodecSupport("Test", filename);

        Map<String, Object> pojo = new HashMap<>();
        pojo.put("name", "John");
        pojo.put("age", 30);

        String pojoJson = Utils.JSON.toString(pojo);
        byte[] encoded = codecSupport.toPayload("testtopic", pojoJson);
        String decodedJson = codecSupport.toString("testtopic", encoded);

        Map<String, Object> decodedMap = Utils.JSON.toObject(decodedJson, HashMap.class);
        Assertions.assertEquals(decodedMap.get("name"), "John");
        Assertions.assertEquals(decodedMap.get("age"), 30);
    }
}
