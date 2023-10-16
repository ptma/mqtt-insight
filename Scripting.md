MqttInsight Scripting
--
MqttInsight 仅提供了一些基本的 MQTT 功能，用户可以通过编写脚本来实现自己需要的扩展功能，
例如：

* 对订阅的消息进行自定义的解码
* 将消息通过各种网络协议转发到其它的目标地址

脚本功能通过 Javet 框架实现， 支持 Node.js(v18.17.1) 中的大多数 API (fs, events, crypto, ...)。

## 脚本加载

在打开的 MQTT 连接标签页右侧的工具栏选择 "更多... -> 脚本 -> 加载脚本..."。

加载的脚本的作用域为当前 MQTT 连接的标签页。

脚本中如果使用了第三方的 npm 包, 需要在脚本所在目录执行 npm install xxx, 目前还**不支持 Node.js 的全局包(global)**

### 示例脚本1

将订阅的 testtopic/# 主题下的消息通过 mqtt.js 转发到 MQTT Broker:

```javascript
const mqttJS = require("mqtt");

const mqttClient = mqttJS.connect("mqtt://127.0.0.1:1883");
mqttClient.on("connect", () => {
    logger.debug("已连接: mqtt://127.0.0.1:1883");
});
codec.decode("testtopic/#", (message) => {
    // 将 testtopic/# 主题下的消息转发到 mqtt://127.0.0.1:1883
    mqttClient.publish(message.getTopic(), Buffer.from(message.getPayload()));
});

// 订阅相应的主题， 也可以通过 UI 手动添加订阅
mqtt.subscribe("testtopic/#");
```

### 示例脚本2

将订阅的 test/sample 主题下的 ProtoBuf 消息转换为 json 字符串:

```javascript
const fs = require("fs");
const protobuf = require('protocol-buffers');

var messages = protobuf(fs.readFileSync('SampleMessages.proto'))
codec.decode("test/sample", (message) => {
    let buffer = Buffer.from(message.getPayload());
    var obj = messages.SampleMessage.decode(buffer)
    return {
        payload: JSON.stringify(obj),
        format: 'json'
    };
});

// 订阅相应的主题， 也可以通过 UI 手动添加订阅
mqtt.subscribe("test/sample", 1);
```

## 内置对象

* mqtt - MqttInsight 中当前标签的实例，用于操作MQTT(订阅、发布)
* codec - 脚本解码器
* toast - 提示框工具
* logger - 日志工具

### 1. mqtt

#### mqtt.subscribe(String topic, [int qos])

添加 MQTT 订阅

#### mqtt.publish(String topic, String payload, [int qos], [boolean retained])

发布 MQTT 消息

#### mqtt.publish(String topic, byte[] payload, [int qos], [boolean retained])

发布 MQTT 消息

### 2. codec

#### codec.decode([String topic], callback)

* `topic` - string, 匹配的主题，可选
* `callback` - function (message), 消息处理回调方法，返回结果可选。
    - `message` - 收到的 MQTT 消息, 具有的方法如下:
        - `getTopic()` - string, 消息的主题
        - `getQos()` - int, 消息的 QoS
        - `isRetained()` - boolean, 是否为保留消息
        - `getPayload()` - Int8Array, 消息的载荷
        - `payloadAsString()` - string, 消息的字符串形式的载荷
    - `return` - 返回的类型可以是:
        - `string` - 消息体的文本
        - `json` - 消息体及消息格式对象, 例如: `{payload: "payload as json text ...", format: "json"}`

### 3. toast

toast 工具可以在 UI 上弹出各种提示消息, 格式化文本 `format` 中使用 `{}` 表示占位符

#### toast.info(String format, [Object... arguments])

#### toast.success(String format, [Object... arguments])

#### toast.warn(String format, [Object... arguments])

#### toast.error(String format, [Object... arguments])

### 4. logger

日志工具, 格式化文本 `format` 中使用 `{}` 表示占位符

#### logger.trace(String format, [Object... arguments])

#### logger.debug(String format, [Object... arguments])

#### logger.info(String format, [Object... arguments])

#### logger.warn(String format, [Object... arguments])

#### logger.error(String format, [Object... arguments])
