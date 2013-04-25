/*
 * Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.greenrobot.event;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Looper;
import android.util.Log;

/**
 * EventBus is a central publish/subscribe event system for Android. Events are posted ({@link #post(Object)} to the
 * bus, which delivers it to subscribers that have matching handler methods for the event type. To receive events,
 * subscribers must register themselves to the bus using the {@link #register(Object)} method. Once registered,
 * subscribers receive events until the call of {@link #unregister(Object)}. By default, subscribers will handle events
 * in methods named "onEvent".
 * 
 * @author Markus Junginger, greenrobot
 */
public class EventBus {
    static ExecutorService executorService = Executors.newCachedThreadPool();

    /** Log tag, apps may override it. */
    public static String TAG = "Event";

    private static volatile EventBus defaultInstance;

    private static final Map<Class<?>, List<Class<?>>> eventTypesCache = new HashMap<Class<?>, List<Class<?>>>();

    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;
    private final Map<Object, List<Class<?>>> typesBySubscriber;
    private final Map<Class<?>, Object> stickyEvents;

    private final ThreadLocal<List<Object>> currentThreadEventQueue = new ThreadLocal<List<Object>>() {
        @Override
        protected List<Object> initialValue() {
            return new ArrayList<Object>();
        }
    };

    private final ThreadLocal<BooleanWrapper> currentThreadIsPosting = new ThreadLocal<BooleanWrapper>() {
        @Override
        protected BooleanWrapper initialValue() {
            return new BooleanWrapper();
        }
    };

    private String defaultMethodName = "onEvent";

    private final HandlerPoster mainThreadPoster;
    private final BackgroundPoster backgroundPoster;
    private final AsyncPoster asyncPoster;
    private final SubscriberMethodFinder subscriberMethodFinder;

    private boolean subscribed;
    private boolean logSubscriberExceptions;

    /** Convenience singleton for apps using a process-wide EventBus instance. */
    public static EventBus getDefault() {
        if (defaultInstance == null) {
            synchronized (EventBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new EventBus();
                }
            }
        }
        return defaultInstance;
    }

    /** For unit test primarily. */
    public static void clearCaches() {
        SubscriberMethodFinder.clearCaches();
        eventTypesCache.clear();
    }

    /**
     * Method name verification is done for methods starting with onEvent to avoid typos; using this method you can
     * exclude subscriber classes from this check.
     */
    public static void skipMethodNameVerificationFor(Class<?> clazz) {
        SubscriberMethodFinder.skipMethodNameVerificationFor(clazz);
    }

    /** For unit test primarily. */
    public static void clearSkipMethodNameVerifications() {
        SubscriberMethodFinder.clearSkipMethodNameVerifications();
    }

    /**
     * Creates a new EventBus instance; each instance is a separate scope in which events are delivered. To use a
     * central bus, consider {@link #getDefault()}.
     */
    public EventBus() {
        subscriptionsByEventType = new HashMap<Class<?>, CopyOnWriteArrayList<Subscription>>();
        typesBySubscriber = new HashMap<Object, List<Class<?>>>();
        stickyEvents = new ConcurrentHashMap<Class<?>, Object>();
        mainThreadPoster = new HandlerPoster(this, Looper.getMainLooper(), 10);
        backgroundPoster = new BackgroundPoster(this);
        asyncPoster = new AsyncPoster(this);
        subscriberMethodFinder = new SubscriberMethodFinder();
        logSubscriberExceptions = true;
    }

    /**
     * Before registering any subscribers, use this method to configure if EventBus should log exceptions thrown by
     * subscribers (default: true).
     */
    public void configureLogSubscriberExceptions(boolean logSubscriberExceptions) {
        if (subscribed) {
            throw new EventBusException("This method must be called before any registration");
        }
        this.logSubscriberExceptions = logSubscriberExceptions;
    }

    /**
     * Registers the given subscriber to receive events. Subscribers must call {@link #unregister(Object)} once they are
     * no longer interested in receiving events.
     * 
     * Subscribers have event handling methods that are identified by their name, typically called "onEvent". Event
     * handling methods must have exactly one parameter, the event. If the event handling method is to be called in a
     * specific thread, a modifier is appended to the method name. Valid modifiers match one of the {@link ThreadMode}
     * enums. For example, if a method is to be called in the UI/main thread by EventBus, it would be called
     * "onEventMainThread".
     */
    public void register(Object subscriber) {
        register(subscriber, defaultMethodName, false);
    }

    /**
     * Like {@link #register(Object)}, but allows to define a custom method name for event handler methods.
     */
    public void register(Object subscriber, String methodName) {
        register(subscriber, methodName, false);
    }

    /**
     * Like {@link #register(Object)}, but also triggers delivery of the most recent sticky event (posted with
     * {@link #postSticky(Object)}) to the given subscriber.
     */
    public void registerSticky(Object subscriber) {
        register(subscriber, defaultMethodName, true);
    }

    /**
     * Like {@link #registerSticky(Object)}, but allows to define a custom method name for event handler methods.
     */
    public void registerSticky(Object subscriber, String methodName) {
        register(subscriber, methodName, true);
    }

    private void register(Object subscriber, String methodName, boolean sticky) {
        List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriber.getClass(),
                methodName);
        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            subscribe(subscriber, subscriberMethod, sticky);
        }
    }

    /**
     * Like {@link #register(Object)}, but only registers the subscriber for the given event types.
     */
    public void register(Object subscriber, Class<?> eventType, Class<?>... moreEventTypes) {
        register(subscriber, defaultMethodName, false, eventType, moreEventTypes);
    }

    /**
     * Like {@link #register(Object, String)}, but only registers the subscriber for the given event types.
     */
    public synchronized void register(Object subscriber, String methodName, Class<?> eventType,
            Class<?>... moreEventTypes) {
        register(subscriber, methodName, false, eventType, moreEventTypes);
    }

    /**
     * Like {@link #registerSticky(Object)}, but only registers the subscriber for the given event types.
     */
    public void registerSticky(Object subscriber, Class<?> eventType, Class<?>... moreEventTypes) {
        register(subscriber, defaultMethodName, true, eventType, moreEventTypes);
    }

    /**
     * Like {@link #registerSticky(Object, String)}, but only registers the subscriber for the given event types.
     */
    public synchronized void registerSticky(Object subscriber, String methodName, Class<?> eventType,
            Class<?>... moreEventTypes) {
        register(subscriber, methodName, true, eventType, moreEventTypes);
    }

    private synchronized void register(Object subscriber, String methodName, boolean sticky, Class<?> eventType,
            Class<?>... moreEventTypes) {
        Class<?> subscriberClass = subscriber.getClass();
        List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriberClass,
                methodName);
        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            if (eventType == subscriberMethod.eventType) {
                subscribe(subscriber, subscriberMethod, sticky);
            } else if (moreEventTypes != null) {
                for (Class<?> eventType2 : moreEventTypes) {
                    if (eventType2 == subscriberMethod.eventType) {
                        subscribe(subscriber, subscriberMethod, sticky);
                        break;
                    }
                }
            }
        }
    }

    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod, boolean sticky) {
        subscribed = true;
        Class<?> eventType = subscriberMethod.eventType;
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
        if (subscriptions == null) {
            subscriptions = new CopyOnWriteArrayList<Subscription>();
            subscriptionsByEventType.put(eventType, subscriptions);
        } else {
            for (Subscription subscription : subscriptions) {
                if (subscription.equals(newSubscription)) {
                    throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event "
                            + eventType);
                }
            }
        }

        subscriberMethod.method.setAccessible(true);
        subscriptions.add(newSubscription);

        List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
        if (subscribedEvents == null) {
            subscribedEvents = new ArrayList<Class<?>>();
            typesBySubscriber.put(subscriber, subscribedEvents);
        }
        subscribedEvents.add(eventType);

        if (sticky) {
            Object stickyEvent;
            synchronized (stickyEvents) {
                stickyEvent = stickyEvents.get(eventType);
            }
            if (stickyEvent != null) {
                postToSubscription(newSubscription, stickyEvent, Looper.getMainLooper() == Looper.myLooper());
            }
        }
    }

    /** Unregisters the given subscriber for the given event classes. */
    public synchronized void unregister(Object subscriber, Class<?>... eventTypes) {
        if (eventTypes.length == 0) {
            throw new IllegalArgumentException("Provide at least one event class");
        }
        List<Class<?>> subscribedClasses = typesBySubscriber.get(subscriber);
        if (subscribedClasses != null) {
            for (Class<?> eventType : eventTypes) {
                unubscribeByEventType(subscriber, eventType);
                subscribedClasses.remove(eventType);
            }
            if (subscribedClasses.isEmpty()) {
                typesBySubscriber.remove(subscriber);
            }
        } else {
            Log.w(TAG, "Subscriber to unregister was not registered before: " + subscriber.getClass());
        }
    }

    /** Only updates subscriptionsByEventType, not typesBySubscriber! Caller must update typesBySubscriber. */
    private void unubscribeByEventType(Object subscriber, Class<?> eventType) {
        List<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions != null) {
            int size = subscriptions.size();
            for (int i = 0; i < size; i++) {
                if (subscriptions.get(i).subscriber == subscriber) {
                    subscriptions.remove(i);
                    i--;
                    size--;
                }
            }
        }
    }

    /** Unregisters the given subscriber from all event classes. */
    public synchronized void unregister(Object subscriber) {
        List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber);
        if (subscribedTypes != null) {
            for (Class<?> eventType : subscribedTypes) {
                unubscribeByEventType(subscriber, eventType);
            }
            typesBySubscriber.remove(subscriber);
        } else {
            Log.w(TAG, "Subscriber to unregister was not registered before: " + subscriber.getClass());
        }
    }

    /** Posts the given event to the event bus. */
    public void post(Object event) {
        List<Object> eventQueue = currentThreadEventQueue.get();
        eventQueue.add(event);

        BooleanWrapper isPosting = currentThreadIsPosting.get();
        if (isPosting.value) {
            return;
        } else {
            boolean isMainThread = Looper.getMainLooper() == Looper.myLooper();
            isPosting.value = true;
            try {
                while (!eventQueue.isEmpty()) {
                    postSingleEvent(eventQueue.remove(0), isMainThread);
                }
            } finally {
                isPosting.value = false;
            }
        }
    }

    /**
     * Posts the given event to the event bus and holds on to the event (because it is sticky). The most recent sticky
     * event of an event's type is kept in memory for future access. This can be {@link #registerSticky(Object)} or
     * {@link #getStickyEvent(Class)}.
     */
    public void postSticky(Object event) {
        synchronized (stickyEvents) {
            stickyEvents.put(event.getClass(), event);
        }
        // Should be posted after it is putted, in case the subscriber wants to remove immediately
        post(event);
    }

    /**
     * Gets the most recent sticky event for the given type.
     * 
     * @see #postSticky(Object)
     */
    public Object getStickyEvent(Class<?> eventType) {
        synchronized (stickyEvents) {
            return stickyEvents.get(eventType);
        }
    }

    /**
     * Remove and gets the recent sticky event for the given type.
     * 
     * @see #postSticky(Object)
     */
    public Object removeStickyEvent(Class<?> eventType) {
        synchronized (stickyEvents) {
            return stickyEvents.remove(eventType);
        }
    }

    /**
     * Removes the sticky event if it equals to the given event.
     * 
     * @return true if the events matched and the sticky event was removed.
     */
    public boolean removeStickyEvent(Object event) {
        synchronized (stickyEvents) {
            Class<? extends Object> eventType = event.getClass();
            Object existingEvent = stickyEvents.get(eventType);
            if (event.equals(existingEvent)) {
                stickyEvents.remove(eventType);
                return true;
            } else {
                return false;
            }
        }
    }

    private void postSingleEvent(Object event, boolean isMainThread) throws Error {
        Class<? extends Object> eventClass = event.getClass();
        List<Class<?>> eventTypes = findEventTypes(eventClass);
        boolean subscriptionFound = false;
        int countTypes = eventTypes.size();
        for (int h = 0; h < countTypes; h++) {
            Class<?> clazz = eventTypes.get(h);
            CopyOnWriteArrayList<Subscription> subscriptions;
            synchronized (this) {
                subscriptions = subscriptionsByEventType.get(clazz);
            }
            if (subscriptions != null) {
                for (Subscription subscription : subscriptions) {
                    postToSubscription(subscription, event, isMainThread);
                }
                subscriptionFound = true;
            }
        }
        if (!subscriptionFound) {
            Log.d(TAG, "No subscripers registered for event " + eventClass);
            if (eventClass != NoSubscriberEvent.class && eventClass != SubscriberExceptionEvent.class) {
                post(new NoSubscriberEvent(this, event));
            }
        }
    }

    private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
        switch (subscription.subscriberMethod.threadMode) {
        case PostThread:
            invokeSubscriber(subscription, event);
            break;
        case MainThread:
            if (isMainThread) {
                invokeSubscriber(subscription, event);
            } else {
                mainThreadPoster.enqueue(subscription, event);
            }
            break;
        case BackgroundThread:
            if (isMainThread) {
                backgroundPoster.enqueue(subscription, event);
            } else {
                invokeSubscriber(subscription, event);
            }
            break;
        case Async:
            asyncPoster.enqueue(subscription, event);
            break;
        default:
            throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
        }
    }

    /** Finds all Class objects including super classes and interfaces. */
    private List<Class<?>> findEventTypes(Class<?> eventClass) {
        synchronized (eventTypesCache) {
            List<Class<?>> eventTypes = eventTypesCache.get(eventClass);
            if (eventTypes == null) {
                eventTypes = new ArrayList<Class<?>>();
                Class<?> clazz = eventClass;
                while (clazz != null) {
                    eventTypes.add(clazz);
                    addInterfaces(eventTypes, clazz.getInterfaces());
                    clazz = clazz.getSuperclass();
                }
                eventTypesCache.put(eventClass, eventTypes);
            }
            return eventTypes;
        }
    }

    /** Recurses through super interfaces. */
    static void addInterfaces(List<Class<?>> eventTypes, Class<?>[] interfaces) {
        for (Class<?> interfaceClass : interfaces) {
            if (!eventTypes.contains(interfaceClass)) {
                eventTypes.add(interfaceClass);
                addInterfaces(eventTypes, interfaceClass.getInterfaces());
            }
        }
    }

    void invokeSubscriber(PendingPost pendingPost) {
        Object event = pendingPost.event;
        Subscription subscription = pendingPost.subscription;
        PendingPost.releasePendingPost(pendingPost);
        invokeSubscriber(subscription, event);
    }

    void invokeSubscriber(Subscription subscription, Object event) throws Error {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (event instanceof SubscriberExceptionEvent) {
                // Don't send another SubscriberExceptionEvent to avoid infinite event recursion, just log
                Log.e(TAG, "SubscriberExceptionEvent subscriber " + subscription.subscriber.getClass()
                        + " threw an exception", cause);
                SubscriberExceptionEvent exEvent = (SubscriberExceptionEvent) event;
                Log.e(TAG, "Initial event " + exEvent.causingEvent + " caused exception in "
                        + exEvent.causingSubscriber, exEvent.throwable);
            } else {
                if (logSubscriberExceptions) {
                    Log.e(TAG, "Could not dispatch event: " + event.getClass() + " to subscribing class "
                            + subscription.subscriber.getClass(), cause);
                }
                SubscriberExceptionEvent exEvent = new SubscriberExceptionEvent(this, cause, event,
                        subscription.subscriber);
                post(exEvent);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    /** For ThreadLocal, much faster to set than storing a new Boolean. */
    final static class BooleanWrapper {
        boolean value;
    }

    // Just an idea: we could provide a callback to post() to be notified, an alternative would be events, of course...
    /* public */interface PostCallback {
        void onPostCompleted(List<SubscriberExceptionEvent> exceptionEvents);
    }

}
