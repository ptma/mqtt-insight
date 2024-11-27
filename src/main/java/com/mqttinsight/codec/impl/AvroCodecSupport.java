package com.mqttinsight.codec.impl;

import com.mqttinsight.codec.DynamicCodecSupport;
import com.mqttinsight.codec.proto.MappingField;
import com.mqttinsight.exception.CodecException;
import com.mqttinsight.exception.SchemaLoadException;
import com.mqttinsight.util.TopicUtil;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author ptma
 */
@Slf4j
public class AvroCodecSupport extends JsonCodecSupport implements DynamicCodecSupport {

    private final static String[] SCHEMA_FILE_EXTENSIONS = new String[]{"avsc"};
    private final static List<MappingField> MAPPING_FIELDS = List.of(
        MappingField.of("topic", "MappingFieldTopic", 50),
        MappingField.of("namespace", "AvroNamespace", 35),
        MappingField.of("name", "AvroName", 15)
    );

    private final String name;
    private final boolean instantiated;
    private final List<Map<String, String>> mappings;
    private String schemaFile = null;
    private Schema schema;

    public AvroCodecSupport() {
        this.name = "Avro";
        this.mappings = Collections.emptyList();
        this.instantiated = false;
    }

    AvroCodecSupport(String name, String schemaFile, List<Map<String, String>> mappings) throws SchemaLoadException {
        this.name = name;
        this.schemaFile = schemaFile;
        this.mappings = mappings;
        try {
            schema = new Schema.Parser().parse(new File(schemaFile));
            this.instantiated = true;
        } catch (Exception e) {
            throw new SchemaLoadException(e.getMessage(), e);
        }
    }

    @Override
    public AvroCodecSupport newDynamicInstance(String name, String schemaFile, List<Map<String, String>> mappings) throws SchemaLoadException {
        return new AvroCodecSupport(name, schemaFile, mappings);
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
    public boolean mappable() {
        return true;
    }

    @Override
    public List<MappingField> getMappings() {
        return MAPPING_FIELDS;
    }

    @Override
    public String toString(String topic, byte[] payload) {
        try {
            if (schema.getType().equals(Schema.Type.UNION)) {
                if (mappings != null && !mappings.isEmpty()) {
                    Map<String, String> matchedMapping = mappings.stream()
                        .filter(map -> TopicUtil.match(map.get("topic"), topic))
                        .findFirst()
                        .orElse(null);
                    if (matchedMapping != null) {
                        Schema matchedSchema = schema.getTypes()
                            .stream()
                            .filter(s -> s.getNamespace().equals(matchedMapping.get("namespace")) && s.getName().equals(matchedMapping.get("name")))
                            .findFirst()
                            .orElse(null);
                        if (matchedSchema != null) {
                            try {
                                return tryDecodeWithSchema(payload, matchedSchema);
                            } catch (Exception ignore) {

                            }
                        }
                    }
                }
                for (Schema s : schema.getTypes()) {
                    try {
                        return tryDecodeWithSchema(payload, s);
                    } catch (Exception ignore) {

                    }
                }
                throw new CodecException("Can not decode the message.");
            } else {
                return tryDecodeWithSchema(payload, schema);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    private String tryDecodeWithSchema(byte[] payload, Schema s) throws IOException {
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(s);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(payload, null);
        GenericRecord record = datumReader.read(null, decoder);
        if (!decoder.isEnd()) {
            throw new IOException("There is remaining data that has not been decoded.");
        }
        return record.toString();
    }

    @Override
    public byte[] toPayload(String topic, String json) throws CodecException {
        try {
            Map<String, Object> objectMap = Utils.JSON.toObject(json, HashMap.class);
            if (schema.getType().equals(Schema.Type.UNION)) {
                if (mappings != null && !mappings.isEmpty()) {
                    Map<String, String> matchedMapping = mappings.stream()
                        .filter(map -> TopicUtil.match(map.get("topic"), topic))
                        .findFirst()
                        .orElse(null);
                    if (matchedMapping != null) {
                        Schema matchedSchema = schema.getTypes()
                            .stream()
                            .filter(s -> s.getNamespace().equals(matchedMapping.get("namespace")) && s.getName().equals(matchedMapping.get("name")))
                            .findFirst()
                            .orElse(null);
                        if (matchedSchema != null) {
                            try {
                                return tryEncodeWithSchema(objectMap, matchedSchema);
                            } catch (Exception ignore) {

                            }
                        }
                    }
                }
                for (Schema s : schema.getTypes()) {
                    try {
                        return tryEncodeWithSchema(objectMap, s);
                    } catch (Exception ignore) {

                    }
                }
                throw new CodecException("Can not encode the message.");
            } else {
                return tryEncodeWithSchema(objectMap, schema);
            }
        } catch (Exception e) {
            throw new CodecException("An error occurred while encoding the message.", e);
        }
    }

    private byte[] tryEncodeWithSchema(Map<String, Object> objectMap, Schema s) throws IOException {
        Set<String> assignedFields = objectMap.keySet();
        long unknownFieldCount = s.getFields().stream().filter(field -> !assignedFields.contains(field.name()) && !field.hasDefaultValue())
            .count();
        if (unknownFieldCount > 0) {
            throw new IOException("There are unassigned fields present in the schema.");
        }
        GenericRecord record = new GenericData.Record(s);
        objectMap.entrySet()
            .stream()
            .filter(entry -> s.getField(entry.getKey()) != null)
            .forEach(entry -> record.put(entry.getKey(), entry.getValue()));

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(s);
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            writer.write(record, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        }
    }
}
