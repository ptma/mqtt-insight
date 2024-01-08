package com.mqttinsight.codec;

import com.caoccao.javet.values.reference.V8ValueTypedArray;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.function.Function;

@Getter
@Setter
public class ScriptingCodecOption {

    private String format;
    private boolean dynamic;
    private String[] schemaExts;


    private Function<byte[], String> decoder;
    private Function<String, V8ValueTypedArray> encoder;
    private Function<String, Void> loadSchema;

    private ScriptingCodecOption() {}

    public static ScriptingCodecOption of(Function<byte[], String> decoder,
                                          Function<String, V8ValueTypedArray> encoder,
                                          Function<String, Void> loadSchema,
                                          Map<String, Object> optionMap) {
        ScriptingCodecOption option = new ScriptingCodecOption();
        option.decoder = decoder;
        option.encoder = encoder;
        option.loadSchema = loadSchema;

        option.format = (String) optionMap.getOrDefault("format", "plain");
        option.dynamic = (Boolean) optionMap.getOrDefault("dynamic", false);
        option.schemaExts = ((String) optionMap.getOrDefault("schemaExts", "*")).split(",");
        return option;
    }

}
