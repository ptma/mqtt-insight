package com.mqttinsight.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.avro.AvroFactory;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.mqttinsight.exception.SchemaLoadException;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author ptma
 */
@Slf4j
public class AvroCodecSupport extends JsonCodecSupport implements DynamicCodecSupport {

    private static final ObjectMapper AVRO_MAPPER = new ObjectMapper(new AvroFactory());
    private final static String[] SCHEMA_FILE_EXTENSIONS = new String[]{"avsc"};

    private final String name;
    private final boolean instantiated;
    private String schemaFile = null;
    private Schema schema;

    public AvroCodecSupport() {
        this.name = "Avro";
        this.instantiated = false;
    }

    public AvroCodecSupport(String name, String schemaFile) throws SchemaLoadException {
        this.name = name;
        this.schemaFile = schemaFile;
        try {
            schema = new Schema.Parser().parse(new File(schemaFile));
            this.instantiated = true;
        } catch (Exception e) {
            throw new SchemaLoadException(e.getMessage(), e);
        }
    }

    @Override
    public AvroCodecSupport newDynamicInstance(String name, String schemaFile) throws SchemaLoadException {
        return new AvroCodecSupport(name, schemaFile);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isInstantiated() {
        return instantiated;
    }

    @Override
    public String getSchemaFile() {
        return schemaFile;
    }

    @Override
    public String[] getSchemaFileExtensions() {
        return SCHEMA_FILE_EXTENSIONS;
    }

    @Override
    public boolean encodable() {
        return true;
    }

    @Override
    public String toString(byte[] payload) {
        try {
            GenericRecord record = new GenericData.Record(schema);
            ObjectNode objectNode = AVRO_MAPPER.readerFor(ObjectNode.class)
                .with(new AvroSchema(record.getSchema()))
                .readValue(payload);
            return objectNode.toString();
        } catch (Exception e) {
            log.error(e.getMessage());
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] toPayload(String json) {
        GenericRecord record = new GenericData.Record(schema);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Map<String, Object> objectMap = Utils.JSON_MAPPER.readValue(json, Map.class);
            objectMap.entrySet()
                .stream()
                .filter(entry -> schema.getField(entry.getKey()) != null)
                .forEach(entry -> record.put(entry.getKey(), entry.getValue()));

            DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(record.getSchema());
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            writer.write(record, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage());
            return super.toPayload(json);
        }
    }
}
