EventBus How-To
===============
In the [README file](README.md), you got to know EventBus, and some of its basic principles. You also saw how to add EventBus to your project using Maven Central. Great, now let's dive deeper!

General usage and API
---------------------
Here we pick up on the 3 steps of the README and expand a bit on the code.
### 1: Define events ###
Events are POJO (plain old Java object) without any specific requirements.

```java
public class MessageEvent {
    public final String message;

    public MessageEvent(String message) {
        this.message = message;
    }
}
```
### 2: Prepare subscribers ###

Subscribers implement event handling `onEvent` methods that will be called when an event is received. They also need to register and unregister themselves to the bus.

```java
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    
    // This method will be called when a MessageEvent is posted
    public void onEvent(MessageEvent event){
        Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
    }
    
    // This method will be called when a SomeOtherEvent is posted
    public void onEvent(SomeOtherEvent event){
        doSomethingWith(event);
    }
    
```
### 3: Post events ###
Post an event from any part of your code. All subscribers matching the event type will receive it.

```java
    EventBus.getDefault().post(new MessageEvent("Hello everyone!"));
```

Delivery threads and ThreadModes
--------------------------------
EventBus can handle threading for you: events can be posted in threads different from the posting thread. 

A common use case is dealing with UI changes. In Android, UI changes must be done in the UI (main) thread. On the other hand, networking, or any time consuming task, must not run on the main thread. EventBus helps you to deal with those tasks and synchronize with the UI thread (without having to delve into thread transitions, using AsyncTask, etc).

In EventBus, you may define the thread that will call the event handling method `onEvent` by using a **ThreadMode**:
* **PostThread:** Subscriber will be called in the same thread, which is posting the event. This is the default. Event delivery implies the least overhead because it avoids thread switching completely. Thus this is the recommended mode for simple tasks that are known to complete is a very short time without requiring the main thread. Event handlers using this mode should return quickly to avoid blocking the posting thread, which may be the main thread.
Example:
```java
    // Called in the same thread (default)
    public void onEvent(MessageEvent event) {
        log(event.message);
    }
```
* **MainThread:** Subscriber will be called in Android's main thread (sometimes referred to as UI thread). If the posting thread is the main thread, event handler methods will be called directly. Event handlers using this mode must return quickly to avoid blocking the main thread.
Example:
```java
    // Called in Android UI's main thread
    public void onEventMainThread(MessageEvent event) {
        textField.setText(event.message);
    }
```
* **BackgroundThread:** Subscriber will be called in a background thread. If posting thread is not the main thread, event handler methods will be called directly in the posting thread. If the posting thread is the main thread, EventBus uses a single background thread that will deliver all its events sequentially. Event handlers using this mode should try to return quickly to avoid blocking the background thread.
```java
    // Called in the background thread
    public void onEventBackgroundThread(MessageEvent event){
        saveToDisk(event.message);
    }
```
* **Async:** Event handler methods are called in a separate thread. This is always independent from the posting thread and the main thread. Posting events never wait for event handler methods using this mode. Event handler methods should use this mode if their execution might take some time, e.g. for network access. Avoid triggering a large number of long running asynchronous handler methods at the same time to limit the number of concurrent threads. EventBus uses a thread pool to efficiently reuse threads from completed asynchronous event handler notifications.
```java
    // Called in a separate thread
    public void onEventAsync(MessageEvent event){
        backend.send(event.message);
    }
```

*Note:* EventBus takes care of calling the `onEvent` method in the proper thread depending on its name (onEvent, onEventAsync, etc.).

Subscriber priorities and ordered event delivery
------------------------------------------------
You may change the order of event delivery by providing a priority to the subscriber during registration.

```java
    int priority = 1;
    EventBus.getDefault().register(this, priority);
```

Within the same delivery thread (ThreadMode), higher priority subscribers will receive events before others with a lower priority. The default priority is 0. 

*Note*: the priority does *NOT* affect the order of delivery among subscribers with different [ThreadModes](#delivery-threads-and-threadmodes)!

Configure EventBus using EventBusBuilder
----------------------------------------
EventBus 2.3 added EventBusBuilder to configure various aspects of EventBus. For example, here's how to build an EventBus that keeps quiet in case a posted event has no subscribers:

```java
    EventBus eventBus = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).build();
```

Another example is to fail when a subscriber throws an exception. Note: by default, EventBus catches exceptions thrown from onEvent methods and sends an SubscriberExceptionEvent that may but do not have to be handled.

```java
    EventBus eventBus = EventBus.builder().throwSubscriberException(true).build();
```

Check the EventBusBuilder class and its JavaDoc for all possible configuration possibilities.

### Configure the default EventBus instance ###
Using EventBus.getDefault() is a simple way to get an shared EventBus instance. EventBusBuilder also allows to configure this default instance using the method <code>installDefaultEventBus()</code>.

For example, it's possible to configure the default EventBus instance to rethrow exceptions, which occurred in onEvent methods. But let's to this only for DEBUG builds, because this will likely crash the app on exceptions:

```java
EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus();
```

Note: this can be done only once before the the default EventBus instance is used the first time. This ensures consistent behavior in your app. Your Application class is a good place to configure the default EventBus instance before its used.

Cancelling event delivery
-------------------------
You may cancel the event delivery process by calling `cancelEventDelivery(Object event)` from a subscriber's event handling method. 
Any further event delivery will be cancelled: subsequent subscribers won't receive the event.
```java
    // Called in the same thread (default)
    public void onEvent(MessageEvent event){
    	// Process the event 
    	...
    	
    	EventBus.getDefault().cancelEventDelivery(event) ;
    }
```

Events are usually cancelled by higher priority subscribers. Cancelling is restricted to event handling methods running in posting thread [ThreadMode.PostThread](#delivery-threads-and-threadmodes).

Sticky Events
-------------
Some events carry information that is of interest after the event is posted. For example, this could be an event signalizing that some initialization is complete. Or if you have some sensor or location data and you want to hold on the most recent values. Instead of implementing your own caching, you can use sticky events. EventBus keeps the last sticky event of a certain type in memory. The sticky event can be delivered to subscribers or queried explicitly. Thus, you don't need any special logic to consider already available data.

Let's say, an sticky event was posted some time ago:
```java
    EventBus.getDefault().postSticky(new MessageEvent("Hello everyone!"));
```

After that, a new Activity gets started. During registration using registerSticky, it will immediately get the previously posted sticky event:
```java
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    public void onEventMainThread(MessageEvent event) {
        textField.setText(event.message);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
```

You may also get the last sticky event of a certain type with:
```java
    EventBus.getDefault().getStickyEvent(Class<?> eventType)
```

ProGuard configuration
----------------------
ProGuard obfuscates method names. However, the onEvent methods must not renamed because they are accessed using reflection. Use the following snip in your ProGuard configuration file (proguard.cfg):
<pre><code>-keepclassmembers class ** {
    public void onEvent*(**);
}
</code></pre>


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
