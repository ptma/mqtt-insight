编解码器脚本
--
MqttInsight 允许使用 JavaScript 来编写编解码器。

## 脚本加载和注册

MqttInsight 会在启动时扫描**用户目录** `codecs` 文件夹下的 .js
文件，并自动加载到脚本引擎。在编解码器脚本中注册的编解码器的是全局可用的（在所有连接标签页中均可用）。
> **用户目录**:
> * Windows：MqttInsight.exe 所在目录；
> * Linux 和 MacOS：${user.home}/MqttInsight

## 可用内置模块

* codec - 编解码器注册器
* toast - 提示框工具
* logger - 日志工具  
  参考: [脚本内置模块](Modules.md)

## 示例

### 注册动态编解码器

动态编解码器由于需要用户额外通过 UI 界面来指定模式文件（Schema），因此需要实现 `schemaLoader`
方法来处理用户选择的模式文件。并且，动态编解码器注册成功以后，并不会直接出现消息格式的可选列表中， 需要在"文件->
编解码设置"中新建编解码器并绑定模式文件。

```js
const fs = require("fs");
const protobuf = require('protocol-buffers');
var messages;
// Register a dynamic codec
codec.register("ProtoDynamicSample",
    (topic, payload) => { // decoder function
        let buffer = Buffer.from(payload);
        var obj = messages.SampleMessage.decode(buffer);
        return JSON.stringify(obj);
    },
    (topic, text) => { // encoder function
        return messages.SampleMessage.encode(JSON.parse(text))
    },
    (file) => {// schema loader function
        // load the specified .proto file
        messages = protobuf(fs.readFileSync(file));
    },
    {
        dynamic: true, // 动态编解码器
        format: "json",
        schemaExts: "proto" // 模式文件扩展名
    }
);
```

### 注册静态解码器

```js
const fs = require("fs");
const protobuf = require('protocol-buffers');
var messages = protobuf(fs.readFileSync(file));
// Register a static decoder
codec.register("ProtoStaticSample",
    (topic, payload) => { // decoder function
        let buffer = Buffer.from(payload);
        var obj = messages.SampleMessage.decode(buffer);
        return JSON.stringify(obj);
    },
    null,
    {
        dynamic: false, // 静态解码器
        format: "json"
    }
);
```
