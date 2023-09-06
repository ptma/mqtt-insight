package com.mqttinsight.codec;

import java.util.ServiceLoader;

/**
 * @author ptma
 */
public class CodecSupportLoader {

    public static void loadCodecs() {
        ServiceLoader<CodecSupport> loader = ServiceLoader.load(CodecSupport.class, CodecSupportLoader.class.getClassLoader());
        loader.iterator().forEachRemaining(provider -> {
            CodecSupports.instance().register(provider);
        });
    }
}
