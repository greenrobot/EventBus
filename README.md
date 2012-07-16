EventBus
========
EventBus is an Android optimized publish/subscribe event bus. A typical use case for Android apps is gluing Activities, Fragments, and background threads together. Conventional wiring of those elements often introduces complex and error-prone dependencies and life cycle issues. Propagating listeners through all participants (e.g. background service -> activity -> multiple fragments or helper classes) becomes deprecated. EventBus decouples event senders and receivers and thus simplifies communication between app components. And you don't need to implement a single interface!

General Usage and API
---------------------
In EventBus, subscribers implement event handling methods and register themselves to the bus. Posted events are delivered to matching event handling methods based on their event type (the Java class/interfaces implemented by the event).

Using EventBus takes four simple steps:
1. Implement any number of event handling methods in the subscriber: public void onEvent(AnyEventType event) {}
2. Register subscribers: eventBus.register(this);
3. Post events to the bus: eventBus.post(event);
4. Unregister subscriber: eventBus.unregister(this);

The API is very close to (Guava's event bus)[http://code.google.com/p/guava-libraries/wiki/EventBusExplained] and its Android port [otto](http://square.github.com/otto/) ([announcement](http://corner.squareup.com/2012/07/otto.html)). Have a look at their documentation for publishers and posting events (skip producers, EventBus does not support producers because we haven't figured out valid use cases for those).

Additional Features and Notes
-----------------------------
While the API is partly identical to Guava/otto, EventBus is written from scratch with Android in mind and brings some unique features.
* *Simple to use thread transitions:* Events can be received in Android's main thread, even if they were posted in a background thread. Subscribers that want to be called on the main thread call registerForMainThread() instead of register(). For example, UI updates become trivial for state changes caused by background workers. And because of the decoupling, 
* *NOT based on annotations:* Querying annotations are slow on Android, especially before Android 4.0. Have a look at this (Android bug report)[http://code.google.com/p/android/issues/detail?id=7811]
* *Based on conventions:* Event handling methods are called "onEvent" (you could supply different names, but this is not encouraged).
* *Performanced optimized:* Who wants to be the first whos benchmarks it against otto? :D
* *Tiny:* The jar is less than 20 KBytes.
* *Convenience singleton:* You can get a process wide event bus instance by calling EventBus.getDefault(). You can still call new EventBus() to create any number of local busses.
* *Subscriber and event inheritance:* Event handler methods may be defined in super classes, and events are posted to handlers of the event's super classes including any implemented interfaces. For example, subscriber may register to events of the type Object to receive all events posted on the event bus.
* *Selective registration:* It's possible to register only for specific event types. This also allows subscribers to register only some of their event handling methods for main thread event delivery.

Example
-------
TODO

FAQ
---
Q: How's EventBus different to Android's BroadcastReceiver/Intent system?
A: Unlike Android's BroadcastReceiver/Intent system, EventBus uses standard Java classes as events and offers a more convenient API. EventBus is intended for a lot more uses cases where you wouldn't want to go through the hassle of setting up Intents, preparing Intent extras, implementing broadcast receivers, and extracting Intent extras again. Also, EventBus comes with a much lower overhead. 

Release History
---------------
### V1.0.0 (2012-07-16): First public release

License
-------
Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
