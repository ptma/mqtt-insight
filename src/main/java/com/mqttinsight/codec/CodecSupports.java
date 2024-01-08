package com.mqttinsight.codec;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
                supports.put(support.getName().toLowerCase(), support);
            } else {
                dynamicSupports.put(support.getName().toLowerCase(), dynamicSupport);
            }
        } else {
            supports.put(support.getName().toLowerCase(), support);
        }
    }

    public void remove(String name) {
        supports.remove(name.toLowerCase());
    }

    public Collection<CodecSupport> getCodecs() {
        return supports.values();
    }

    public Collection<String> getDynamicCodecNames() {
        return dynamicSupports.values()
            .stream()
            .map(CodecSupport::getName)
            .collect(Collectors.toList());
    }

    public CodecSupport getByName(String name) {
        if (name == null) {
            return plainCodec;
        }
        return supports.getOrDefault(name.toLowerCase(), plainCodec);
    }

    public boolean nameExists(String name) {
        return supports.containsKey(name.toLowerCase());
    }

    public DynamicCodecSupport getDynamicByName(String name) {
        return dynamicSupports.get(name.toLowerCase());
    }
}
