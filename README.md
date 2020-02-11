EventBus
========
[EventBus](https://greenrobot.org/eventbus/) is a publish/subscribe event bus for Android and Java.<br/>
<img src="EventBus-Publish-Subscribe.png" width="500" height="187"/>

[![Build Status](https://travis-ci.org/greenrobot/EventBus.svg?branch=master)](https://travis-ci.org/greenrobot/EventBus)
[![Follow greenrobot on Twitter](https://img.shields.io/twitter/follow/greenrobot_de.svg?style=flat-square&logo=twitter)](https://twitter.com/greenrobot_de)

EventBus...

 * simplifies the communication between components
    * decouples event senders and receivers
    * performs well with Activities, Fragments, and background threads
    * avoids complex and error-prone dependencies and life cycle issues
 * makes your code simpler
 * is fast
 * is tiny (~60k jar)
 * is proven in practice by apps with 1,000,000,000+ installs
 * has advanced features like delivery threads, subscriber priorities, etc.

EventBus in 3 steps
-------------------
1. Define events:

    ```java  
    public static class MessageEvent { /* Additional fields if needed */ }
    ```

2. Prepare subscribers:
    Declare and annotate your subscribing method, optionally specify a [thread mode](https://greenrobot.org/eventbus/documentation/delivery-threads-threadmode/):  

    ```java
    @Subscribe(threadMode = ThreadMode.MAIN)  
    public void onMessageEvent(MessageEvent event) {/* Do something */};
    ```
    Register and unregister your subscriber. For example on Android, activities and fragments should usually register according to their life cycle:

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

3. Post events:

   ```java
    EventBus.getDefault().post(new MessageEvent());
    ```

Read the full [getting started guide](https://greenrobot.org/eventbus/documentation/how-to-get-started/).

There are also some [examples](https://github.com/greenrobot-team/greenrobot-examples).

**Note:** we highly recommend the [EventBus annotation processor with its subscriber index](https://greenrobot.org/eventbus/documentation/subscriber-index/).
This will avoid some reflection related problems seen in the wild.  

Add EventBus to your project
----------------------------
<a href="https://search.maven.org/search?q=g:org.greenrobot%20AND%20a:eventbus"><img src="https://img.shields.io/maven-central/v/org.greenrobot/eventbus.svg"></a>

Available on <a href="https://search.maven.org/search?q=g:org.greenrobot%20AND%20a:eventbus">Maven Central</a>.

Via Gradle:
```gradle
implementation 'org.greenrobot:eventbus:3.2.0'
```

Via Maven:
```xml
<dependency>
    <groupId>org.greenrobot</groupId>
    <artifactId>eventbus</artifactId>
    <version>3.2.0</version>
</dependency>
```

R8, ProGuard
------------

If your project uses R8 or ProGuard add the following rules:

```bash
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
 
# And if you use AsyncExecutor:
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
```

Homepage, Documentation, Links
------------------------------
For more details please check the [EventBus website](https://greenrobot.org/eventbus). Here are some direct links you may find useful:

[Features](https://greenrobot.org/eventbus/features/)

[Documentation](https://greenrobot.org/eventbus/documentation/)

[Changelog](https://greenrobot.org/eventbus/changelog/)

[FAQ](https://greenrobot.org/eventbus/documentation/faq/)

How does EventBus compare to other solutions, like Otto from Square? Check this [comparison](COMPARISON.md).

License
-------
Copyright (C) 2012-2020 Markus Junginger, greenrobot (https://greenrobot.org)

EventBus binaries and source code can be used according to the [Apache License, Version 2.0](LICENSE).

Other projects by greenrobot
============================
[__ObjectBox__](https://objectbox.io/) ([GitHub](https://github.com/objectbox/objectbox-java)) is a new superfast object-oriented database.

[__Essentials__](https://github.com/greenrobot/essentials) is a set of utility classes and hash functions for Android & Java projects.

[__greenDAO__](https://github.com/greenrobot/greenDAO) is an ORM optimized for Android: it maps database tables to Java objects and uses code generation for optimal speed.
