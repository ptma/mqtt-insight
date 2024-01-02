package com.mqttinsight.codec;

import cn.hutool.json.JSONObject;
import com.mqttinsight.codec.proto.DynamicProtoSchema;
import com.mqttinsight.exception.SchemaLoadException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author ptma
 */
@Slf4j
public class ProtobufCodecSupport extends JsonCodecSupport implements DynamicCodecSupport {
    private final static String[] SCHEMA_FILE_EXTENSIONS = new String[]{ "proto"};
    private final String name;
    private final boolean instantiated;
    private String protoFile = null;
    private DynamicProtoSchema dynamicProtoSchema;

    protected ProtobufCodecSupport() {
        this.name = "Protobuf";
        this.instantiated = false;
    }

    private ProtobufCodecSupport(String name, String protoFile) throws SchemaLoadException {
        this.name = name;
        this.protoFile = protoFile;
        this.dynamicProtoSchema = new DynamicProtoSchema(this.protoFile);
        this.instantiated = true;
    }

    @Override
    public DynamicCodecSupport newDynamicInstance(String name, String schemaFile) throws SchemaLoadException {
        return new ProtobufCodecSupport(name, schemaFile);
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
    public String toString(byte[] payload) {
        try {
            JSONObject msg = dynamicProtoSchema.parse(payload);
            return msg.toString();
        } catch (Exception e) {
            log.error(e.getMessage());
            return new String(payload, StandardCharsets.UTF_8);
        }
    }
}
