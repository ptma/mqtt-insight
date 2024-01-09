普通脚本
--
在某个连接标签页手动加载的脚本为普通脚本。普通脚本的作用域为当前 MQTT 连接标签页，并且在关闭连接标签页后, 脚本会被自动卸载。下一次重新启动 MqttInsight 并打开链接标签页后依旧需要手动加载。

## 脚本加载
在连接标签页右侧的工具栏选择 "更多... -> 脚本 -> 加载脚本..."。脚本成功加载后会在脚本菜单中附加脚本文件的菜单项，点击该菜单项可选择重新载入或移除脚本。



脚本中如果使用了第三方的 npm 包, 需要在脚本所在目录执行 npm install xxx, 目前还**不支持 Node.js 的全局包(global)**

## 可用内置模块
* mqtt - MQTT 操作(订阅、发布、解码)工具
* toast - 提示框工具
* logger - 日志工具  
参考: [脚本内置模块](Modules.md)

## 示例
### 示例1

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

### 示例2

将订阅的 test/sample 主题下的 ProtoBuf 消息转换为 json 字符串:

```javascript
const fs = require("fs");
const protobuf = require("protocol-buffers");

var messages = protobuf(fs.readFileSync("SampleMessages.proto"))
mqtt.decode("test/sample", (message) => {
    let buffer = Buffer.from(message.getPayload());
    let obj = messages.SampleMessage.decode(buffer);

    // 方式 1：直接返回消息文本
    // return JSON.stringify(obj);

    // 方式 2：返回消息 JSON 对象
    return {
        payload: JSON.stringify(obj),
        format: "json",
        color: "#00FF00"
    };

    // 方式 3：直接设置消息的载荷
    // message.setPayload(JSON.stringify(obj));
});

// 订阅相应的主题， 也可以通过 UI 手动添加订阅
mqtt.subscribe("test/sample", 1);
```


