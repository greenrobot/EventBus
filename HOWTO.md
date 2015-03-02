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

Another example is to fail when a subscriber throws an exception. Note: by default, EventBus catches exceptions thrown from onEvent methods and sends a SubscriberExceptionEvent that may but do not have to be handled.

```java
    EventBus eventBus = EventBus.builder().throwSubscriberException(true).build();
```

Check the EventBusBuilder class and its JavaDoc for all possible configuration possibilities.

### Configure the default EventBus instance ###
Using EventBus.getDefault() is a simple way to get a shared EventBus instance. EventBusBuilder also allows to configure this default instance using the method <code>installDefaultEventBus()</code>.

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

Let's say, a sticky event was posted some time ago:
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

It's also possible to remove previously posted sticky events using one of the removeStickyEvent methods. They take either a concrete event object, or an event class. Like this it's possible to create consumable events. Keep in mind though that that only the last event of an event type is kept.

ProGuard configuration
----------------------
ProGuard obfuscates method names. However, the onEvent methods must not renamed because they are accessed using reflection. Use the following snip in your ProGuard configuration file (proguard.cfg):

```
-keepclassmembers class ** {
    public void onEvent*(**);
}

# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
```


AsyncExecutor
-------------
_Disclaimer:_ AsyncExecutor is a non-core utility class. It might save you some code with error handling in background threads, but it's not a core EventBus class.

AsyncExecutor is like a thread pool, but with failure handling. Failures are thrown exceptions, which get are wrapped inside an event, which is posted automatically by AsyncExecutor.

Usually, you call AsyncExecutor.create() to create an instance and keep it in Application scope. To execute something, implement the RunnableEx interface and pass it to the execute method of the AsyncExecutor. Unlike Runnable, RunnableEx may throw an Exception.

If the RunnableEx implementation throws an exception, it will be catched and wrapped into a ThrowableFailureEvent, which will be posted.

Code example for execution:

```java
AsyncExecutor.create().execute(
  new RunnableEx {
    public void run throws LoginException {
      remote.login();
      EventBus.getDefault().postSticky(new LoggedInEvent());
      // No need to catch Exception
    }
  }
}
```

Code example for the receiving part:

```java
public void onEventMainThread(LoggedInEvent event) {
  // Change some UI
}

public void onEventMainThread(ThrowableFailureEvent event) {
  // Show error in UI
}
```

AsyncExecutor Builder
---------------------
If you want to customize your AsyncExecutor instance, call the static method AsyncExecutor.builder(). It will return a builder which lets you customize the EventBus instance, the thread pool, and the class of the failure event.

Another customization options is the execution scope, which gives failure events context information. For example, a failure event may be relevant only to a specific Activity instance or class. If your custom failure event class implements the HasExecutionScope interface, AsyncExecutor will set the execution scope automatically. Like this, your subscriber can query the failure event for its execution scope and react depending on it.


Comparison with Square's Otto
-----------------------------
Check the [COMPARISON.md](COMPARISON.md)