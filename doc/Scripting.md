MqttInsight 脚本使用说明
--
MqttInsight 提供了两种脚本类型的支持，用户可以根据自己的需要来通过脚本实现一些扩展功能。

1. [编解码器脚本](CodecScript.md)  
   通过脚本实现编解码器
2. [普通脚本](NormalScript.md)  
   可以一些相对较为灵活扩展功能，例如：
    - 订阅消息并自定义解码
    - 订阅消息并发布回复
    - 定时发布消息
    - 转发消息到其它的目标地址或接口

脚本功能通过 Javet 框架实现， 支持 Node.js(v18.17.1) 中的大多数 API (fs, events, crypto, ...)。

> 脚本中如果使用了第三方的 npm 包, 需要在脚本所在目录执行 npm install xxx, 目前还**不支持 Node.js 的全局包(global)**
