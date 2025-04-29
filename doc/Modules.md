脚本内置模块
--

* mqtt - MQTT 操作(订阅、发布、解码)工具
* codec - 编解码器注册工具
* toast - 消息提示工具
* logger - 日志工具

### 1. mqtt

MQTT 操作(订阅、发布、解码)工具, 本模块不能用于编解码器脚本中。

#### 1.1 mqtt.subscribe(topic[, qos])

订阅主题

* `topic` string, 订阅的主题
* `qos` integer, 可选, 消息的 QoS, 默认为 0

```js
mqtt.subscribe("test/#");

mqtt.subscribe("test/#", 1);
````

#### 1.2 mqtt.publish(String topic, payload[, qos][, retained])

发布消息

* `topic` string, 发布的主题
* `payload` string | Int8Array | Uint8Array | Buffer , 消息的载荷
* `qos` integer, 可选, 消息的 QoS, 默认为 0
* `retained` boolean, 可选, 是否为保留消息, 默认为 false

```js
let payload = new Uint8Array([0x49, 0x6e, 0x74, 0x3a]);
mqtt.publish("test/binary", payload);

mqtt.publish("test/binary", Buffer.from("496E743A", "hex"), 1);
```

#### 1.3 mqtt.decode([topic, ]callback)

消息解码

* `topic` - string, 可选, 匹配的主题, 支持MQTT 主题通配符 `+` `#`, 无此参数时表示针对所有已订阅的主题
* `callback` - function (message), 消息处理回调方法。
    - `message` - 收到的 MQTT 消息, 具有的方法如下:
        - `getTopic()` - string, 消息的主题
        - `getQos()` - int, 消息的 QoS
        - `isRetained()` - boolean, 是否为保留消息
        - `isDuplicate` - boolean, 是否为副本消息
        - `getPayload()` - Int8Array, 消息的载荷
        - `payloadAsString()` - string, 消息的字符串形式的载荷
        - `setPayload(payload)` - 直接设置消息的载荷，参数 payload 允许的类型有 string | Int8Array | Uint8Array | Buffer
* `return` - string | json | 无返回值
    - `string` - 消息体的文本
    - `json` - 消息对象, 例如: ```{payload: "payload as json text ...", format: "json"}```, 消息对象支持的属性有:
        * `payload` - string|Object, 消息体
        * `format` - string, 可选, 值可以是 `plain`|`json`|`hex`|`xml`
        * `color` - string, 可选, Hex 颜色代码, 例如```#FF0000```

```js
mqtt.decode("test/sample1", (message) => {
    let buffer = Buffer.from(message.getPayload());
    let obj = messages.SampleMessage1.decode(buffer);
    // 直接返回消息文本
    return JSON.stringify(obj);
});

mqtt.decode("test/sample2", (message) => {
    let buffer = Buffer.from(message.getPayload());
    let obj = messages.SampleMessage2.decode(buffer);
    // 返回消息 JSON 对象
    return {
        payload: JSON.stringify(obj),
        format: "json",
        color: "#00FF00"
    };
});

mqtt.decode("test/sample3", (message) => {
    let buffer = Buffer.from(message.getPayload());
    let obj = messages.SampleMessage3.decode(buffer);
    // 直接修改原始消息的载荷
    message.setPayload(JSON.stringify(obj));
});
```

#### 1.4 mqtt.topicVariables(template, topic)

从 Topic 提取变量集

* `template` string , 变量模版, 使用 `{VariableName}` 表示变量
* `topic` string, 要提取的主题
* `return` Object, 提取到的变量集

```js
mqtt.topicVariables("/device/{product}", "/device/test123");
/*
返回
{
    "product", "test123"
}
*/
```

#### 1.5 mqtt.topicMatch(pattern, topic)

判断 Topic 是否匹配模式

* `pattern` string , 主题匹配模式, 可使用 `#`、`+`、`*`、`?` 通配符以及正则表达式
* `topic` string, 要匹配的主题
* `return` boolean, 是否匹配

### 1.6 mqtt.onClose(callback)

关闭回调方法

```js
mqtt.onClose(() => {
    // do something
});
```

### 2. codec

编解码器注册工具

#### 2.1 codec.register(name, decoder, encoder, schemaLoader, options)

注册编解码器, 全参数, 该方法的部分参数是可选的

* `name` string, 编解码器的名称
* `decoder` Function(topic, payload), 解码的方法
    - `topic` string, 消息的主题
    - `payload` Uint8Array, 消息的载荷
    - `retrun` string, 经过解码的文本
* `encoder` Function(topic, text), 编码的方法, 不支持编码的设置为 null
    - `topic` string, 消息的主题
    - `text` string, 发布消息时用户输入的文本
    - `retrun` Int8Array | Uint8Array | Buffer, 经过编码的载荷
* `schemaLoader` Function(file), **动态编解码器**传入模式文件的回调, 无返回值
    - `file` string, 模式文件路径
* `options` Object, 配置项
    - `format` string, 可选, 消息的格式, 默认值为 `plain`, 可选值有: plain|json|hex|xml
    - `dynamic` boolean, 是否为**动态编解码器**, 默认值为 false, 如果为 true, 则需要提供 `schemaExts` 参数,
      并且需要实现 `schemaLoader` 方法
    - `schemaExts` string, 可选, **动态编解码器**模式文件的扩展名, 使用 `,`分隔, 例如 `txt,xml`

> **动态编解码器**  
> 有些序列化框架需要指定模式(Schema、IDL)文件才能正确进行序列化(编码)和反序列化(解码), 例如
> Protobuf。这一类的编解码器注册以后不会立即出现在格式下拉框中， 需要用户在`文件->编解码设置`中设置具体的名称和模式文件。

#### 2.2 codec.register(name, decoder)

注册编解码器, 仅支持解码, 参数说明见 2.1

#### 2.3 codec.register(name, decoder, encoder)

注册编解码器, 支持解码和编码, 参数说明见 2.1

#### 2.4 codec.register(name, decoder, encoder, options)

注册编解码器, 支持解码和编码, 并设置参数, 参数说明见 2.1

#### 2.5 codec.topicVariables(template, topic)

从 Topic 提取变量集

* `template` string , 变量模版, 使用 `{VariableName}` 表示变量
* `topic` string, 要提取的主题
* `return` Object, 提取到的变量集

```js
mqtt.topicVariables("/device/{product}", "/device/test123");
/*
返回
{
    "product", "test123"
}
*/
```

#### 2.6 codec.topicMatch(pattern, topic)

判断 Topic 是否匹配模式

* `pattern` string , 主题匹配模式, 可使用 `#`、`+`、`*`、`?` 通配符以及正则表达式
* `topic` string, 要匹配的主题
* `return` boolean, 是否匹配

### 2.7 codec.onClose(callback)

关闭回调方法

```js
codec.onClose(() => {
    // do something
});
```

### 3. toast

消息提示工具

toast 工具可以在 UI 上弹出各种提示消息, 格式化模板`format`中使用`{}`表示占位符

```js
toast.success("Hello {}", "MqttInsight!");
// 显示 Toast: "Hello MqttInsight!"

toast.info("{} {}", "Hello", "World!");
// 显示 Toast: "Hello World!"
```

#### 3.1 toast.info(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### 3.2 toast.success(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### 3.3 toast.warn(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### 3.4 toast.error(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

### 4. logger 日志工具

日志工具, 格式化模板 `format` 中使用 `{}` 表示占位符

```js
logger.debug("Hello {}", "MqttInsight!");
// 输出: "Hello MqttInsight!"

logger.info("{} {}", "Hello", "World!");
// 输出: "Hello World!"
```

#### 4.1 logger.trace(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### 4.2 logger.debug(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### 4.3 logger.info(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### 4.4 logger.warn(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### 4.5 logger.error(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数
