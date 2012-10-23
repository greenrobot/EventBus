EventBus
========
EventBus is an Android optimized publish/subscribe event bus. A typical use case for Android apps is gluing Activities, Fragments, and background threads together. Conventional wiring of those elements often introduces complex and error-prone dependencies and life cycle issues. With EventBus propagating listeners through all participants (e.g. background service -> activity -> multiple fragments or helper classes) becomes deprecated. EventBus decouples event senders and receivers and thus simplifies communication between app components. Less code, better quality. And you don't need to implement a single interface!

General Usage and API
---------------------
In EventBus, subscribers implement event handling methods and register themselves to the bus. Posted events are delivered to matching event handling methods based on their event type (the Java class/interfaces implemented by the event).

Using EventBus takes four simple steps:

1. Implement any number of event handling methods in the subscriber:<br/>
<code>public void onEvent(AnyEventType event) {}</code>
2. Register subscribers:<br/>
<code>eventBus.register(this);</code>
3. Post events to the bus:<br/>
<code>eventBus.post(event);</code>
4. Unregister subscriber:<br/>
<code>eventBus.unregister(this);</code>

Delivery Threads
----------------
EventBus can deliver events in other threads independently from the posting thread. Threading is crucial to all Android apps, and EventBus will make threading easier. In Android development, UI changes must be done in the UI thread, while networking is forbidden here. If you want to do both networking and UI using standard Android API, you will need to take care of thread transistions, e.g. by using AsyncTask. If you use an event-based approach using EventBus, this gets simpler.

In EventBus, each event handling method is associated with a thread mode (have a look at the ThreadMode enum). The thread mode defines in which thread the event handling mehtod is called:
* **PostThread:** Subscriber will be called in the same thread, which is posting the event. This is the default. Event delivery implies the least overhead because it avoids thread switching completely. Thus this is the recommended mode for simple tasks that are known to complete is a very short time without requiring the main thread. Event handlers using this mode must return quickly to avoid blocking the posting thread, which may be the main thread.
* **MainThread:** Subscriber will be called in Android's main thread (sometimes referred to as UI thread). If the posting thread is the main thread, event handler methods will be called directly. Event handlers using this mode must return quickly to avoid blocking the main thread.
* **BackgroundThread:** Subscriber will be called in a background thread. If posting thread is not the main thread, event handler methods will be called directly in the posting thread. If the posting thread is the main thread, EventBus uses a single background thread that will deliver all its events sequentially. Event handlers using this mode should try to return quickly to avoid blocking the background thread.
* **Async:** Event handler methods are called in a separate thread. This is always independent from the posting thread and the main thread. Posting events never wait for event handler methods using this mode. Event handler methods should use this mode if their execution might take some time, e.g. for network access. Avoid triggering a large number of long running asynchronous handler methods at the same time to limit the number of concurrent threads. EventBus uses a thread pool to efficiently reuse threads from completed asynchronous event handler notifications.

*Example:* Consider your subscriber updates the UI, but the triggering event is posted by a background thread (using eventBus.post(event)). The name of the event handling method should be onEventMainThread. EventBus takes care of calling the method in the main thread without any further code required

Sticky Events
-------------
Some events carry information that is of interest after the event is posted. For example, this could be an event signalizing that some initialization is complete. Or if you have some sensor or location data and you want to hold on the most recent values. Instead of implementing your own caching, you can use sticky events. EventBus keeps the last sticky event of a certain type in memory. The sticky event can be delivered to subscribers or queried explicitly. Thus, you don't need any special logic to consider already available data.

API-wise events are made sticky by using postSticky(Object event) instead of post(Object event). Subscribers that want to get previously posted sticky events, use registerSticky(...) instead of register(...). Alternatively, the last sticky event of a certain event type can be queried by using getStickyEvent(Class<?> eventType).

Additional Features and Notes
-----------------------------

* **NOT based on annotations:** Querying annotations are slow on Android, especially before Android 4.0. Have a look at this [Android bug report](http://code.google.com/p/android/issues/detail?id=7811)
* **Based on conventions:** Event handling methods are called "onEvent" (you could supply different names, but this is not encouraged).
* **Performanced optimized:** It's probably the fastest event bus for Android.
* **Tiny:** The jar is less than 30 KBytes.
* **Convenience singleton:** You can get a process wide event bus instance by calling EventBus.getDefault(). You can still call new EventBus() to create any number of local busses.
* **Subscriber and event inheritance:** Event handler methods may be defined in super classes, and events are posted to handlers of the event's super classes including any implemented interfaces. For example, subscriber may register to events of the type Object to receive all events posted on the event bus.
* **Selective registration:** It's possible to register only for specific event types. This also allows subscribers to register only some of their event handling methods for main thread event delivery.

Comparison with Square's Otto
-----------------------------
Otto is another event bus library for Android; actually it's a fork of Guava's EventBus. greenrobot's EventBus and Otto share some basic semantics (register, post, unregister, ...), but there are differences which the following table summarizes:
<table>
    <tr>
        <th></th>
        <th>EventBus</th>
        <th>Otto</th>
    </tr>
    <tr>
        <th>Declare event handling methods</th>
        <td>Name conventions</td>
        <td>Annotations</td>
    </tr>	
    <tr>
        <th>Event inheritance</th>
        <td>Yes</td>
        <td>Yes</td>
    </tr>	
    <tr>
        <th>Subscriber inheritance</th>
        <td>Yes</td>
        <td>No</td>
    </tr>
    <tr>
        <th>Cache most recent events</th>
        <td>Yes, sticky events</td>
        <td>No</td>
    </tr>
    <tr>
        <th>Event producers (e.g. for coding cached events)</th>
        <td>No</td>
        <td>Yes</td>
    </tr>
    <tr>
        <th>Event delivery in posting thread</th>
        <td>Yes (Default)</td>
        <td>Yes</td>
    </tr>	
    <tr>
        <th>Event delivery in main thread</th>
        <td>Yes</td>
        <td>No</td>
    </tr>	
    <tr>
        <th>Event delivery in background thread</th>
        <td>Yes</td>
        <td>No</td>
    </tr>	
    <tr>
        <th>Aynchronous event delivery</th>
        <td>Yes</td>
        <td>No</td>
    </tr>
</table>

Besides features, performance is another differentiator. To compare performance, we created an Android application, which is also part of this repository (EventBusPerformance). You can also run the app on your phone to benchmark different scenarios.

Benchmark results indicate that EventBus is significantly faster in almost every scenario:
<table>
    <tr>
        <th></th>
        <th>EventBus</th>
        <th>Otto</th>
    </tr>
    <tr>
        <th>Posting 1000 events, Android 2.3 emulator</th>
        <td>~70% faster</td>
        <td></td>
    </tr>
	<tr>
        <th>Posting 1000 events, S3 Android 4.0</th>
        <td>~110% faster</td>
        <td></td>
    </tr>
    <tr>
        <th>Register 1000 subscribers, Android 2.3 emulator</th>
        <td>~10% faster</td>
        <td></td>
    </tr>
    <tr>
        <th>Register 1000 subscribers, S3 Android 4.0</th>
        <td>~70% faster</td>
        <td></td>
    </tr>
    <tr>
        <th>Register subscribers cold start, Android 2.3 emulator</th>
        <td>~350% faster</td>
        <td></td>
    </tr>	
    <tr>
        <th>Register subscribers cold start, S3 Android 4.0</th>
        <td colspan="2">About the same</td>
    </tr>	
</table>

Example
-------
TODO

FAQ
---
**Q:** How's EventBus different to Android's BroadcastReceiver/Intent system?<br/>
**A:** Unlike Android's BroadcastReceiver/Intent system, EventBus uses standard Java classes as events and offers a more convenient API. EventBus is intended for a lot more uses cases where you wouldn't want to go through the hassle of setting up Intents, preparing Intent extras, implementing broadcast receivers, and extracting Intent extras again. Also, EventBus comes with a much lower overhead. 

Release History
---------------
### V2.0.0 (2012-10-23) Major feature release
* Event methods define for themselves in which thread they get called. This is done by providing "modifiers" to the method name, e.g. onEventMainThread is called by the main thread without further configuration. Have a look at the JavaDoc of the enum ThreadMode for all available thread modes.
* The event method modifiers replace registerForMainThread methods. Moving this information to the method itself should make things clearer.
* Using event method modifiers, subscribers can receive the same event type in different threads if they choose to.
* New "BackgroundThread" modifier for onEvent handler methods ensures that event handler methods are called in a background thread. If an event is posted from a non-main thread, handler methods will be called directly. If posted from the main thread, EventBus will use a background thread to call the handler methods.
* New "Async" modifier for onEvent handler methods ensures that each event handler method is called completely asynchronously.
* Better performance: Delivery of multiple events in the main thread got significantly faster.
* Added sticky events, which are inspired by sticky broadcasts of the Android system. EventBus keeps the most recent sticky events in memory. Subscribers registering with the new method registerSticky, will receive sticky events right away. You can also query and remove sticky events (methods getStickyEvent and removeStickyEvent).
* By listening to SubscriberExceptionEvent, it is possible to react to Exceptions occuring in subscribers.
* Bug fixes, and internal refactorings

### V1.0.1 (2012-07-31): Important bug fix release
Please update! Now, EventBus.unregister releases all internal references to the subscriber.

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
