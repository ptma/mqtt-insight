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

    private Map<String, CodecSupport> supports = new LinkedHashMap<>();

    private PlainCodecSupport plainCodec = new PlainCodecSupport();

    private CodecSupports() {
        register(plainCodec);
        register(new HexCodecSupport());
        register(new JsonCodecSupport());
    }

    public void register(CodecSupport support) {
        supports.put(support.getName().toLowerCase(), support);
    }

    public Collection<CodecSupport> getCodes() {
        return supports.values();
    }

    public CodecSupport getByName(String name) {
        if (name == null) {
            return plainCodec;
        }
        return supports.getOrDefault(name.toLowerCase(), plainCodec);
    }

    public PlainCodecSupport getDefaultCodec() {
        return plainCodec;
    }
}
