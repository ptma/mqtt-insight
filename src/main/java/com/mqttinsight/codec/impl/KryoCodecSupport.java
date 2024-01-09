package com.mqttinsight.codec.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.mqttinsight.codec.CodecClassLoader;
import com.mqttinsight.codec.DynamicCodecSupport;
import com.mqttinsight.exception.CodecException;
import com.mqttinsight.exception.SchemaLoadException;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author ptma
 */
@Slf4j
public class KryoCodecSupport extends JsonCodecSupport implements DynamicCodecSupport {

    private final static String[] SCHEMA_FILE_EXTENSIONS = new String[]{"jar"};

    private static final ThreadLocal<Kryo> KRYOS = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setReferences(true);
        return kryo;
    });

    private final String name;
    private final boolean instantiated;
    private String jarFile = null;
    private CodecClassLoader classLoader;

    public KryoCodecSupport() {
        this.name = "Kryo";
        this.instantiated = false;
    }

    private KryoCodecSupport(String name, String jarFile) throws SchemaLoadException, MalformedURLException {
        this.name = name;
        this.jarFile = jarFile;
        this.classLoader = new CodecClassLoader(new URL[]{new File(jarFile).toURI().toURL()}, this.getClass().getClassLoader());
        this.instantiated = true;
    }

    @Override
    public KryoCodecSupport newDynamicInstance(String name, String schemaFile) throws SchemaLoadException {
        try {
            return new KryoCodecSupport(name, schemaFile);
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
    public boolean encodable() {
        return false;
    }

    @Override
    public String toString(byte[] payload) {
        try {
            Kryo kryo = KRYOS.get();
            kryo.setClassLoader(classLoader);
            Input input = new Input(payload);
            Object obj = kryo.readClassAndObject(input);
            return Utils.JSON.toString(obj);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    @Override
    public byte[] toPayload(String json) throws CodecException {
        throw new CodecException("Kryo encoding is not supported.");
    }
}
