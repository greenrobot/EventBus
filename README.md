EventBus
========
EventBus is publish/subscribe event bus optimized for Android.

EventBus...

 * simplifies the communication between components
    * decouples event senders and receivers
    * performs well with Activities, Fragments, and background threads
    * avoids complex and error-prone dependencies and life cycle issues
 * makes your code simpler
 * is fast
 * is tiny (<50k jar)
 * is proven in practice by apps with 100,000,000+ installs
 * has advanced features like delivery threads, subscriber priorities, etc.

EventBus in 3 steps
-------------------
1. Define event class:<br/>
<code>public class MessageEvent { /* Additional fields if needed */ }</code>
2. Prepare subscribers:<br/>
<code>eventBus.register(this);</code><br/>
<code>public void onEvent(AnyEventType event) {/* Do something */};</code>
3. Post events to the bus:<br/>
<code>eventBus.post(event);</code>

Add EventBus to your project
----------------------------
EventBus is available on Maven Central, so you simply include it as a dependency in your build. Please ensure that you are using the latest version by [checking here](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.greenrobot%22%20AND%20a%3A%22eventbus%22)

Gradle:
```
    compile 'de.greenrobot:eventbus:2.2.1'
```
Maven:
```
<dependency>
    <groupId>de.greenrobot</groupId>
    <artifactId>eventbus</artifactId>
    <version>2.2.1</version>
</dependency>
```
Ivy:
```
<dependency name="eventbus" org="de.greenrobot" rev="2.2.1" />
```
[Or download EventBus from Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.greenrobot%22%20AND%20a%3A%22eventbus%22)

How-to, Developer Documentation
-------------------------------
Details on how the EventBus API is available in the [HOWTO file](HOWTO.md).

Additional Features and Notes
-----------------------------

* **NOT based on annotations:** Querying annotations are slow on Android, especially before Android 4.0. Have a look at this [Android bug report](http://code.google.com/p/android/issues/detail?id=7811).
* **Based on conventions:** Event handling methods are called "onEvent".
* **Performance optimized:** It's probably the fastest event bus for Android.
* **Convenience singleton:** You can get a process wide event bus instance by calling EventBus.getDefault(). You can still call new EventBus() to create any number of local busses.
* **Subscriber and event inheritance:** Event handler methods may be defined in super classes, and events are posted to handlers of the event's super classes including any implemented interfaces. For example, subscriber may register to events of the type Object to receive all events posted on the event bus.

FAQ
---
**Q:** How's EventBus different to Android's BroadcastReceiver/Intent system?<br/>
**A:** Unlike Android's BroadcastReceiver/Intent system, EventBus uses standard Java classes as events and offers a more convenient API. EventBus is intended for a lot more uses cases where you wouldn't want to go through the hassle of setting up Intents, preparing Intent extras, implementing broadcast receivers, and extracting Intent extras again. Also, EventBus comes with a much lower overhead. 

**[Release History](CHANGELOG.md)**

License
-------
Copyright (C) 2012-2014 Markus Junginger, greenrobot (http://greenrobot.de)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
