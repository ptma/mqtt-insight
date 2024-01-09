package com.mqttinsight.codec;

import com.mqttinsight.codec.impl.AvroCodecSupport;
import com.mqttinsight.exception.CodecException;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class AvroCodecSupportTest {

    private AvroCodecSupport codecSupport;

    @BeforeEach
    void init() {
        String filename = getClass().getResource("/test.avsc").getFile();
        codecSupport = new AvroCodecSupport("Test", filename);
    }

    @Test
    void test() throws IOException, CodecException {
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

            String json = codecSupport.toString(encoded);
            byte[] encoded2 = codecSupport.toPayload(json);

            Assertions.assertArrayEquals(encoded, encoded2);

        }
    }

}
