EventBus
========
[English](https://github.com/tomridder/EventBus/blob/personal/tomridder/issue_515/README.md)

[EventBus](https://greenrobot.org/eventbus/) 是Android和Java的发布/订阅事件总线。<br/>
<img src="EventBus-Publish-Subscribe.png" width="500" height="187"/>

[![Build Status](https://github.com/greenrobot/EventBus/actions/workflows/gradle.yml/badge.svg)](https://github.com/greenrobot/EventBus/actions)
[![Follow greenrobot on Twitter](https://img.shields.io/twitter/follow/greenrobot_de.svg?style=flat-square&logo=twitter)](https://twitter.com/greenrobot_de)

EventBus...

* 简化组件之间的通信
    * 解耦事件发送者和接收者
    * 与Activities、Fragments和后台线程协同工作良好
    * 避免复杂和容易出错的依赖和生命周期问题
* 使您的代码更简单
* 运行速度快
* 很小（大约 60k jar）
* 在1,000,000,000+应用的安装中经得起实践考验
* 具有线程间投递、订阅者优先级等高级功能

使用EventBus的三个步骤：
-------------------
1. 定义事件：

    ```java  
    public static class MessageEvent { /* Additional fields if needed */ }
    ```

2. 准备订阅者:
   声明和注解您的订阅方法, 可以选择指定 [线程模式](https://greenrobot.org/eventbus/documentation/delivery-threads-threadmode/):

    ```java
    @Subscribe(threadMode = ThreadMode.MAIN)  
    public void onMessageEvent(MessageEvent event) {
        // Do something
    }
    ```
   注册和解注册您的订阅者。 例如，在Android上，Activities和Fragments通常应该根据它们的生命周期进行注册:

   ```java
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }
 
    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
    ```

3. 发布事件:

   ```java
    EventBus.getDefault().post(new MessageEvent());
    ```

阅读完整的 [入门指南](https://greenrobot.org/eventbus/documentation/how-to-get-started/).

这里还有一些 [示例](https://github.com/greenrobot-team/greenrobot-examples).

**注意:** 我们强烈推荐使用 [EventBus注释处理器及其订阅者索引](https://greenrobot.org/eventbus/documentation/subscriber-index/).
这将避免一些在实践中遇到的反射相关问题。

将EventBus添加到您的项目中