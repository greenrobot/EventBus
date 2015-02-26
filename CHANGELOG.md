### V2.x.x (2015-0x-xx) Future release
* Registering for sticky events now considers sticky events of subclasses, not just the exact same event type. This makes the semantic consistent to posting events. Note, that this may lead to subscribers being called more than once if matching sticky events of event type subclasses are available.

### V2.4.0 (2014-11-11) Clean up release
* Removed deprecated APIs: A year ago in Version 2.2.0, a couple of EventBus methods were deprecated and flagged to be removed in a future release. Well, version 2.4.0 is that release. Clean ups like this one keep the API concise and simple.

**Note:** No new feature were added since 2.3.0. Use this release if you do not rely on deprecated APIs.

### V2.3.0 (2014-11-11) Feature release: EventBusBuilder and performance fix
* New EventBusBuilder to configure EventBus instances (including the getDefault() instance, #124)
* Added configuration to disable "No subscribers registered for event" logs (EventBusBuilder, #107, #117)
* Added configuration to disable sending SubscriberExceptionEvent and NoSubscriberEvent (EventBusBuilder)
* Added configuration to fail when subscribers throw exceptions (EventBusBuilder, #55)
* Added configuration to use an existing thread pool (EventBusBuilder, #115)
* Added configuration to disable event inheritance improving performance for apps with high event rates (EventBusBuilder)
* Fixed performance regression sneaked into V2.2.x affecting (first time) registration of subscribers
* Updated to Gradle 2.1, using wrapper
* EventBusTest and EventBusPerformance use Gradle to build
* Added hasSubscriberForEvent to check if currently subscribers exist registered to a given event type
* Improved README.md and extracted an extended HOWTO.md and CHANGELOG.md from it
* Ignore compiler generated methods (#76)
* Various small code improvements (#120 among many others)

**Note:** This is your last chance to use APIs that were deprecated in V2.2.0. It's recommended to switch to Version 2.4.0 (or above) at your earliest convenience.

### V2.2.1 (2014-05-21) Bug fix release
* Fixed an issue with AsyncExecutor and execution scope

### V2.2.0 (2013-11-18) Feature release, subscriber priority
* Register subscribers with a priority to to influence the order of event delivery (per delivery thread)
* Event delivery can be canceled by subscribers so subsequent subscribers will not receive the event
* Added "isRegistered" and "removeAllStickyEvents" methods
* Deprecated registration methods with custom method names and event class filters
* Starting with EventBus 2.2 we enforced methods to be public
* Fixed a race conditions with subscriber registration
* Fixed NoSubscriberEvent delivery after unregister

### V2.1.0 (2013-11-15) Bug fix release, experimental util package
* Experimental: AsyncExecutor executes RunnableEx and wraps exceptions into FailureEvents
* Experimental: exception to UI mapping (for now based on dialogs)
* Fixed race condition with queued events that were delivered after subscription was unregistered. This is important for main thread events tied to application life cycle.
* Fixed typos and improved readme (#17, #22, #37, #39)
* Make getStickyEvent and removeStickyEvent generic (#45)
* Fixed bug in SubscriberMethod.equals() (#38)

### V2.0.2 (2013-03-02) Bug fix release
* Fixed build dependencies, are "provided" now

### V2.0.1 (2013-02-25) Bug fix release, Gradle and Maven Central
* Fixed #15: removeStickyEvent(...) does not remove event the first time
* Introduced Gradle build scripts for main project
* Maven artifacts are pushed to Maven Central starting with this version
* Added Travis CI

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
