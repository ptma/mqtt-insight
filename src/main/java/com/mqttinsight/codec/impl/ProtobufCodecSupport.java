package com.mqttinsight.codec.impl;

import com.mqttinsight.codec.DynamicCodecSupport;
import com.mqttinsight.codec.proto.DynamicProtoSchema;
import com.mqttinsight.codec.proto.MappingField;
import com.mqttinsight.exception.CodecException;
import com.mqttinsight.exception.SchemaLoadException;
import com.mqttinsight.util.TopicUtil;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author ptma
 */
@Slf4j
public class ProtobufCodecSupport extends JsonCodecSupport implements DynamicCodecSupport {

    private final static String[] SCHEMA_FILE_EXTENSIONS = new String[]{"proto"};
    private final static List<MappingField> MAPPING_FIELDS = List.of(
        MappingField.of("topic", "MappingFieldTopic", 70),
        MappingField.of("name", "ProtobufMessageName", 30)
    );

    private final String name;
    private final boolean instantiated;
    private final List<Map<String, String>> mappings;
    private String protoFile = null;
    private DynamicProtoSchema dynamicProtoSchema;

    public ProtobufCodecSupport() {
        this.name = "Protobuf";
        this.mappings = Collections.emptyList();
        this.instantiated = false;
    }

    ProtobufCodecSupport(String name, String protoFile, List<Map<String, String>> mappings) throws SchemaLoadException {
        this.name = name;
        this.protoFile = protoFile;
        this.mappings = mappings;
        this.dynamicProtoSchema = new DynamicProtoSchema(this.protoFile);
        this.instantiated = true;
    }

    @Override
    public ProtobufCodecSupport newDynamicInstance(String name, String schemaFile, List<Map<String, String>> mappings) throws SchemaLoadException {
        return new ProtobufCodecSupport(name, schemaFile, mappings);
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
        return protoFile;
    }

    @Override
    public String[] getSchemaFileExtensions() {
        return SCHEMA_FILE_EXTENSIONS;
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
            Map<String, Object> msg;
            if (mappings != null && !mappings.isEmpty()) {
                String mappingMessage = mappings.stream().filter(map -> TopicUtil.match(map.get("topic"), topic))
                    .map(map -> map.get("name"))
                    .findFirst()
                    .orElse(null);
                msg = dynamicProtoSchema.parse(payload, mappingMessage);
            } else {
                msg = dynamicProtoSchema.parse(payload, null);
            }
            return Utils.JSON.toString(msg);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] toPayload(String topic, String json) throws CodecException {
        throw new CodecException("Protobuf encoding is not supported.");
    }
}
