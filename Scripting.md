MqttInsight Scripting
--
MqttInsight 仅提供了一些基本的 MQTT 功能，用户可以通过编写脚本来实现自己需要的扩展功能，
例如：

* 对订阅的消息进行自定义的解码
* 将消息通过各种网络协议转发到其它的目标地址

脚本功能通过 Javet 框架实现， 支持 Node.js(v18.17.1) 中的大多数 API (fs, events, crypto, ...)。

示例脚本:

```javascript
const mqtt = require("mqtt");

const mqttClient = mqtt.connect("mqtt://127.0.0.1:1883");
mqttClient.on("connect", () => {
    logger.debug("已连接: mqtt://127.0.0.1:1883");
});
codec.decode("testtopic/#", (message) => {
    // 将 testtopic/# 主题下的消息转发到 mqtt://127.0.0.1:1883
    mqttClient.publish(message.getTopic(), message.getPayload());
});
```

## 内置对象

* mqtt - MqttInsight 中当前标签的实例，用于操作MQTT(订阅、发布)
* codec - 脚本解码器
* toast - 提示框工具
* logger - 日志工具

### mqtt

#### mqtt.subscribe(String topic, [int qos])

#### mqtt.publish(String topic, String payload, [int qos], [boolean retained])

#### mqtt.publish(String topic, byte[] payload, [int qos], [boolean retained])

### codec

#### codec.decode([String topic], callback)

* `topic` - 匹配的主题，可选
* `callback` - function (message), 消息处理回调方法，可以返回消息处理结果。 返回的类型可以是:
    - `string` - 消息体的文本
    - `json` - 消息体及消息格式对象, 例如: `{payload: "payload as json text ...", format: "json"}`

### toast

#### toast.info(String format, [Object... arguments])

#### toast.success(String format, [Object... arguments])

#### toast.warn(String format, [Object... arguments])

#### toast.error(String format, [Object... arguments])

### logger

#### logger.trace(String format, [Object... arguments])

#### logger.debug(String format, [Object... arguments])

#### logger.info(String format, [Object... arguments])

#### logger.warn(String format, [Object... arguments])

#### logger.error(String format, [Object... arguments])
