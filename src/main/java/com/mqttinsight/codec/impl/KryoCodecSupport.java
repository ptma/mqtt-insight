package com.mqttinsight.codec.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.mqttinsight.codec.CodecClassLoader;
import com.mqttinsight.codec.DynamicCodecSupport;
import com.mqttinsight.codec.proto.MappingField;
import com.mqttinsight.exception.CodecException;
import com.mqttinsight.exception.SchemaLoadException;
import com.mqttinsight.util.TopicUtil;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author ptma
 */
@Slf4j
public class KryoCodecSupport extends JsonCodecSupport implements DynamicCodecSupport {

    private final static String[] SCHEMA_FILE_EXTENSIONS = new String[]{"jar"};

    private final static List<MappingField> MAPPING_FIELDS = List.of(
        MappingField.of("topic", "MappingFieldTopic", 55),
        MappingField.of("class", "KryoRecordClass", 45)
    );

    private static final ThreadLocal<Kryo> KRYOS = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setReferences(true);
        return kryo;
    });

    private final String name;
    private final boolean instantiated;
    private final List<Map<String, String>> mappings;
    private String jarFile = null;
    private CodecClassLoader classLoader;

    public KryoCodecSupport() {
        this.name = "Kryo";
        this.mappings = Collections.emptyList();
        this.instantiated = false;
    }

    KryoCodecSupport(String name, String jarFile, List<Map<String, String>> mappings) throws SchemaLoadException, MalformedURLException {
        this.name = name;
        this.jarFile = jarFile;
        this.mappings = mappings;
        this.classLoader = new CodecClassLoader(new URL[]{new File(jarFile).toURI().toURL()}, this.getClass().getClassLoader());
        this.instantiated = true;
    }

    @Override
    public KryoCodecSupport newDynamicInstance(String name, String schemaFile, List<Map<String, String>> mappings) throws SchemaLoadException {
        try {
            return new KryoCodecSupport(name, schemaFile, mappings);
        } catch (MalformedURLException e) {
            throw new SchemaLoadException(e);
        }
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
        return jarFile;
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
            Kryo kryo = KRYOS.get();
            kryo.setClassLoader(classLoader);
            Input input = new Input(payload);

            Object obj;
            if (mappings != null && !mappings.isEmpty()) {
                String className = mappings.stream().filter(map -> TopicUtil.match(map.get("topic"), topic))
                    .map(map -> map.get("class"))
                    .findFirst()
                    .orElse(null);
                obj = tryReadObject(kryo, input, className);
            } else {
                obj = kryo.readClassAndObject(input);
            }
            return Utils.JSON.toString(obj);
        } catch (Exception e) {
            log.warn(e.getMessage());
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    private Object tryReadObject(Kryo kryo, Input input, String className) {
        try {
            if (className != null) {
                return kryo.readObject(input, Class.forName(className));
            }
        } catch (Exception ignore) {

        }
        return kryo.readClassAndObject(input);
    }

    @Override
    public byte[] toPayload(String topic, String json) throws CodecException {
        throw new CodecException("Kryo encoding is not supported.");
    }
}
