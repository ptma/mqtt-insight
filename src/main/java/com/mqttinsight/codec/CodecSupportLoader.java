package com.mqttinsight.codec;

import com.mqttinsight.config.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

/**
 * @author ptma
 */
@Slf4j
public class CodecSupportLoader {

    public static void loadCodecs() {
        ServiceLoader<CodecSupport> loader = ServiceLoader.load(CodecSupport.class, CodecSupportLoader.class.getClassLoader());
        loader.iterator().forEachRemaining(provider -> {
            CodecSupports.instance().register(provider);
        });

        Configuration.instance().getDynamicCodecs().forEach(dynamicCodec -> {
            try {
                DynamicCodecSupport codecSupport = CodecSupports.instance().getDynamicByName(dynamicCodec.getType());
                if (codecSupport == null) {
                    log.error("Cannot find the codec support {}", dynamicCodec.getType());
                } else {
                    CodecSupports.instance().register(codecSupport.newDynamicInstance(dynamicCodec.getName(), dynamicCodec.getSchemaFile()));
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        });
    }
}
