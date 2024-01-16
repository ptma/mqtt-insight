package com.mqttinsight.codec.impl;

import cn.hutool.core.util.XmlUtil;
import com.caoccao.javet.enums.V8ValueReferenceType;
import com.caoccao.javet.values.reference.V8ValueTypedArray;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mqttinsight.codec.DynamicCodecSupport;
import com.mqttinsight.codec.ScriptingCodecOption;
import com.mqttinsight.exception.CodecException;
import com.mqttinsight.exception.SchemaLoadException;
import com.mqttinsight.util.Utils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ptma
 */
@Slf4j
public class ScriptingCodecSupport extends JsonCodecSupport implements DynamicCodecSupport {

    private final String name;
    private final ScriptingCodecOption options;

    private String schemaFile;
    @Setter
    private boolean instantiated;

    public ScriptingCodecSupport(String name, ScriptingCodecOption options) {
        this.name = name;
        this.options = options;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSyntax() {
        return "text/" + options.getFormat();
    }

    @Override
    public ScriptingCodecSupport newDynamicInstance(String name, String schemaFile) throws SchemaLoadException, CodecException {
        ScriptingCodecSupport newInstance = new ScriptingCodecSupport(name, this.options);
        newInstance.schemaFile = schemaFile;
        if (newInstance.options.getLoadSchema() != null) {
            newInstance.options.getLoadSchema().apply(schemaFile);
        } else if (newInstance.options.isDynamic()) {
            throw new CodecException("Cannot initialize a dynamic codec without \"schemaLoader\" function.");
        }
        newInstance.instantiated = true;
        return newInstance;
    }

    @Override
    public String getSchemaFile() {
        return schemaFile;
    }

    @Override
    public String[] getSchemaFileExtensions() {
        return options.getSchemaExts();
    }

    @Override
    public boolean isInstantiated() {
        return instantiated;
    }

    @Override
    public boolean encodable() {
        return options.getEncoder() != null;
    }

    @Override
    public String toPrettyString(String payload) {
        if ("json".equals(options.getFormat())) {
            return prettyPrint(payload);
        } else if ("xml".equals(options.getFormat())) {
            return XmlUtil.format(payload);
        } else {
            return payload;
        }
    }

    @Override
    public String toString(byte[] payload) {
        return options.getDecoder().apply(payload);
    }

    @Override
    public byte[] toPayload(String text) throws CodecException {
        return options.getEncoder() != null ? convert(options.getEncoder().apply(text)) : new byte[0];
    }

    private byte[] convert(V8ValueTypedArray payload) throws CodecException {
        byte[] bytes;
        try {
            if (payload.getType() == V8ValueReferenceType.Uint8Array) {
                ObjectNode json = Utils.JSON.toObject(payload.toJsonString(), ObjectNode.class);
                if (json.get("type") != null && "Buffer".equals(json.get("type").asText())) {
                    bytes = json.get("data").binaryValue();
                } else {
                    bytes = payload.toBytes();
                }
            } else if (payload.getType() == V8ValueReferenceType.Int8Array) {
                bytes = payload.toBytes();
            } else {
                throw new CodecException(String.format("The type of the payload parameter \"%s\" is not supported.", payload.getType()));
            }
        } catch (Exception e) {
            throw new CodecException(e.getMessage(), e);
        }
        return bytes;
    }
}
