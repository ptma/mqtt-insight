编解码器脚本
--
MqttInsight 现已允许使用 JavaScript 来编写编解码器。  

## 脚本加载  
MqttInsight 会在启动时扫描**用户目录** `codecs` 文件夹下的 .js 文件，并自动加载到脚本引擎。在编解码器脚本中注册的编解码器的是全局可用的（在所有连接标签页中均可用），因此解码方法仅传入的 Mqtt 消息的 payload 参数。
> **用户目录**:  
> * Windows：MqttInsight.exe 所在目录；  
> * Linux 和 MacOS：${user.home}/MqttInsight

## 可用内置模块
* codec - 编解码器注册器
* toast - 提示框工具
* logger - 日志工具  
参考: [脚本内置模块](Modules.md)

## 示例
### 示例1
注册动态编解码器
```js
const fs = require("fs");
const protobuf = require('protocol-buffers');
var messages;
// Register a dynamic codec
codec.register("ProtoDynamicSample",
    (payload) => { // decoder function
        let buffer = Buffer.from(payload);
        var obj = messages.SampleMessage.decode(buffer);
        return JSON.stringify(obj);
    },
    (text) => { // encoder function
        return messages.SampleMessage.encode(JSON.parse(text))
    },
    (file) => {// schema loader function
        // load the specified .proto file
        messages = protobuf(fs.readFileSync(file));
    },
    {
        dynamic: true,
        format: "json",
        schemaExts: "proto"
    }
);
```

### 示例2
注册静态解码器
```js
const fs = require("fs");
const protobuf = require('protocol-buffers');
var messages = protobuf(fs.readFileSync(file));
// Register a static decoder
codec.register("ProtoStaticSample",
    (payload) => { // decoder function
        let buffer = Buffer.from(payload);
        var obj = messages.SampleMessage.decode(buffer);
        return JSON.stringify(obj);
    },
    null,
    {
        dynamic: false,
        format: "json"
    }
);
```
