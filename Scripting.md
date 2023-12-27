MqttInsight 脚本使用说明
--
MqttInsight 仅提供了一些基本的 MQTT 功能，用户可以通过编写脚本来实现自己需要的扩展功能，
例如：

* 订阅消息并自定义解码
* 订阅消息并发布回复
* 定时发布消息
* 转发消息到其它的目标地址或接口

脚本功能通过 Javet 框架实现， 支持 Node.js(v18.17.1) 中的大多数 API (fs, events, crypto, ...)。

## 脚本加载

在打开的 MQTT 连接标签页右侧的工具栏选择 "更多... -> 脚本 -> 加载脚本..."。脚本成功加载后会在脚本菜单中附加脚本文件的菜单项，点击该菜单项可选择重新载入或移除脚本。

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
mqtt.decode("testtopic/#", (message) => {
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
const protobuf = require("protocol-buffers");

var messages = protobuf(fs.readFileSync("SampleMessages.proto"))
mqtt.decode("test/sample", (message) => {
    let buffer = Buffer.from(message.getPayload());
    let obj = messages.SampleMessage.decode(buffer);

    // 直接返回消息文本
    // return JSON.stringify(obj);

    // 返回消息 JSON 对象
    return {
        payload: JSON.stringify(obj),
        format: "json",
        color: "#00FF00"
    };
});

// 订阅相应的主题， 也可以通过 UI 手动添加订阅
mqtt.subscribe("test/sample", 1);
```

## 内置对象

* mqtt - MqttInsight 中当前标签的实例，用于操作MQTT(订阅、发布、解码)
* toast - 提示框工具
* logger - 日志工具

### 1. mqtt

#### mqtt.subscribe(topic[, qos])

订阅主题

* `topic` string, 订阅的主题
* `qos` integer, 可选, 消息的 QoS, 默认为 0

```js
mqtt.subscribe("test/#");

mqtt.subscribe("test/#", 1);
````

#### mqtt.publish(String topic, payload[, qos][, retained])

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

#### mqtt.decode([topic, ]callback)

消息解码

* `topic` - string, 可选, 匹配的主题
* `callback` - function (message), 消息处理回调方法。
    - `message` - 收到的 MQTT 消息, 具有的方法如下:
        - `getTopic()` - string, 消息的主题
        - `getQos()` - int, 消息的 QoS
        - `isRetained()` - boolean, 是否为保留消息
        - `getPayload()` - Int8Array, 消息的载荷
        - `payloadAsString()` - string, 消息的字符串形式的载荷
* `return` - string | json | 无返回值
    - `string` - 消息体的文本
    - `json` - 消息对象, 例如: ```{payload: "payload as json text ...", format: "json"}```, 消息对象支持的属性有:
        * `payload` - string|Object, 消息体
        * `format` - string, 可选, 值可以是 `plain`|`json`|`hex`|`xml`
        * `color` - string, 可选, Hex 颜色代码, 例如```#FF0000```

```js
mqtt.decode("test/sample", (message) => {
    let buffer = Buffer.from(message.getPayload());
    let obj = messages.SampleMessage.decode(buffer);
    // 直接返回消息文本
    return JSON.stringify(obj);
});

mqtt.decode("test/sample", (message) => {
    let buffer = Buffer.from(message.getPayload());
    let obj = messages.SampleMessage.decode(buffer);
    // 返回消息 JSON 对象
    return {
        payload: JSON.stringify(obj),
        format: "json",
        color: "#00FF00"
    };
});
```

#### mqtt.topicVariables(template, topic)

从 Topic 提取变量集

* `template` string , 变量模版, 使用 `{VariableName}` 表示变量
* `topic` string, 要提取的主题
* `return` Object, 提取到的变量集

```js
mqtt.topicVariables("/device/{product}", "/device/test123");
/*
返回
{
    "product", "test1234"
}
*/
```

### 2. toast

toast 工具可以在 UI 上弹出各种提示消息, 格式化模板 `format` 中使用 `{}` 表示占位符

```js
toast.success("Hello {}", "MqttInsight!");
// 显示 Toast: "Hello MqttInsight!"

toast.info("{} {}", "Hello", "World!");
// 显示 Toast: "Hello World!"
```

#### toast.info(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### toast.success(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### toast.warn(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### toast.error(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

### 3. logger

日志工具, 格式化模板 `format` 中使用 `{}` 表示占位符

```js
logger.debug("Hello {}", "MqttInsight!");
// 输出: "Hello MqttInsight!"

logger.info("{} {}", "Hello", "World!");
// 输出: "Hello World!"
```

#### logger.trace(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### logger.debug(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### logger.info(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### logger.warn(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数

#### logger.error(format[, ...args])

* `format` string, 消息模板
* `...args` any, 参数
