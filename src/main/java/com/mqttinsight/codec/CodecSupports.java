package com.mqttinsight.codec;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ptma
 */
public class CodecSupports {

    private static class CodecSupportsHolder {
        private final static CodecSupports INSTANCE = new CodecSupports();
    }

    public static CodecSupports instance() {
        return CodecSupportsHolder.INSTANCE;
    }

    private final Map<String, DynamicCodecSupport> dynamicSupports = new LinkedHashMap<>();

    private final Map<String, CodecSupport> supports = new LinkedHashMap<>();

    private final PlainCodecSupport plainCodec = new PlainCodecSupport();

    private CodecSupports() {
        register(plainCodec);
        register(new HexCodecSupport());
        register(new JsonCodecSupport());
    }

    public void register(CodecSupport support) {
        if (support instanceof DynamicCodecSupport) {
            DynamicCodecSupport dynamicSupport = (DynamicCodecSupport) support;
            if (dynamicSupport.isInstantiated()) {
                supports.put(support.getName(), support);
            } else {
                dynamicSupports.put(support.getName(), dynamicSupport);
            }
        } else {
            supports.put(support.getName(), support);
        }
    }

    public void remove(String name) {
        supports.remove(name);
    }

    public Collection<CodecSupport> getCodecs() {
        return supports.values();
    }

    public Collection<String> getDynamicCodecNames() {
        return dynamicSupports.keySet();
    }

    public CodecSupport getByName(String name) {
        if (name == null) {
            return plainCodec;
        }
        return supports.getOrDefault(name, plainCodec);
    }

    public DynamicCodecSupport getDynamicByName(String name) {
        return dynamicSupports.get(name);
    }
}
