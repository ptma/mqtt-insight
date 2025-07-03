package com.mqttinsight.scripting.modules;

import com.caoccao.javet.values.reference.V8ValueTypedArray;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.codec.ScriptingCodecOption;
import com.mqttinsight.codec.impl.ScriptingCodecSupport;
import com.mqttinsight.util.TopicUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author ptma
 */
@Slf4j
public class CodecWrapper implements CloseableModule {

    private Runnable closingCallback;

    /**
     * 注册脚本解码器
     *
     * @param name    编解码器名称
     * @param decoder 解码方法
     */
    public void register(String name, BiFunction<String, byte[], String> decoder) {
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
                         BiFunction<String, byte[], String> decoder,
                         BiFunction<String, String, V8ValueTypedArray> encoder) {
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
                         BiFunction<String, byte[], String> decoder,
                         BiFunction<String, String, V8ValueTypedArray> encoder,
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
                         BiFunction<String, byte[], String> decoder,
                         BiFunction<String, String, V8ValueTypedArray> encoder,
                         Function<String, Void> loadSchema,
                         Map<String, Object> options) {
        ScriptingCodecOption codecOption = ScriptingCodecOption.of(decoder, encoder, loadSchema, options);
        ScriptingCodecSupport support = new ScriptingCodecSupport(name, codecOption);
        if (!codecOption.isDynamic()) {
            support.setInstantiated(true);
        }
        CodecSupports.instance().register(support);
    }

    /**
     * 根据模版从 topic 上提取变量,如果提取出错则返回空Map
     *
     * <pre>
     *   topicVariables("/device/{product}","/device/test123");
     *   => {"product","test1234"}
     * </pre>
     *
     * @param template Topic模版
     * @param topic    要提取的 topic
     * @return 变量提取结果集
     */
    public Map<String, String> topicVariables(String template, String topic) {
        return TopicUtil.topicVariables(template, topic);
    }

    /**
     * 消息主题是否与订阅主题匹配
     *
     * @param pattern 订阅的主题，支持通配符
     * @param topic   发布的消息主题
     * @return 是否匹配
     */
    public boolean topicMatch(String pattern, String topic) {
        return TopicUtil.match(pattern, topic);
    }

    public void onClose(Runnable callback) {
        this.closingCallback = callback;
    }

    public void close() {
        if (closingCallback != null) {
            closingCallback.run();
        }
    }
}
