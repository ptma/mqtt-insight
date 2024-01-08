package com.mqttinsight.scripting.modules;

import com.caoccao.javet.values.reference.V8ValueTypedArray;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.codec.ScriptingCodecOption;
import com.mqttinsight.codec.ScriptingCodecSupport;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * @author ptma
 */
@Slf4j
public class CodecWrapper {

    /**
     * 注册脚本解码器
     *
     * @param name    编解码器名称
     * @param decoder 解码方法
     */
    public void register(String name, Function<byte[], String> decoder) {
        this.register(name, decoder, null, null, Collections.emptyMap());
    }

    /**
     * 注册脚本编解码器
     *
     * @param name    编解码器名称
     * @param decoder 解码方法
     * @param encoder 编码方法
     */
    public void register(String name,
                         Function<byte[], String> decoder,
                         Function<String, V8ValueTypedArray> encoder) {
        this.register(name, decoder, encoder, null, Collections.emptyMap());
    }

    /**
     * 注册脚本编解码器
     *
     * @param name    编解码器名称
     * @param decoder 解码方法
     * @param encoder 编码方法
     * @param options 选项， {@link ScriptingCodecOption}
     */
    public void register(String name,
                         Function<byte[], String> decoder,
                         Function<String, V8ValueTypedArray> encoder,
                         Map<String, Object> options) {
        this.register(name, decoder, encoder, null, options);
    }

    /**
     * 注册脚本编解码器和
     *
     * @param name       编解码器名称
     * @param decoder    解码方法
     * @param encoder    编码方法
     * @param loadSchema 模式文件载入方法
     * @param options    选项， {@link ScriptingCodecOption}
     */
    public void register(String name,
                         Function<byte[], String> decoder,
                         Function<String, V8ValueTypedArray> encoder,
                         Function<String, Void> loadSchema,
                         Map<String, Object> options) {
        ScriptingCodecOption codecOption = ScriptingCodecOption.of(decoder, encoder, loadSchema, options);
        ScriptingCodecSupport support = new ScriptingCodecSupport(name, codecOption);
        if (!codecOption.isDynamic()) {
            support.setInstantiated(true);
        }
        CodecSupports.instance().register(support);
    }
}
