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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Looper;
import android.util.Log;

/**
 * Class based event bus, optimized for Android. By default, subscribers will handle events in methods named "onEvent".
 * 
 * @author Markus Junginger, greenrobot
 */
public final class EventBus {
    static ExecutorService executorService = Executors.newCachedThreadPool();
    
    /** Log tag, apps may override it. */
    public static String TAG = "Event";

    private static final EventBus defaultInstance = new EventBus();

    private static final Map<String, List<SubscriberMethod>> methodCache = new HashMap<String, List<SubscriberMethod>>();
    private static final Map<Class<?>, List<Class<?>>> eventTypesCache = new HashMap<Class<?>, List<Class<?>>>();

    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;
    private final Map<Object, List<Class<?>>> typesBySubscriber;

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

    public static EventBus getDefault() {
        return defaultInstance;
    }

    public EventBus() {
        subscriptionsByEventType = new HashMap<Class<?>, CopyOnWriteArrayList<Subscription>>();
        typesBySubscriber = new HashMap<Object, List<Class<?>>>();
        mainThreadPoster = new HandlerPoster(Looper.getMainLooper(), 10);
        backgroundPoster = new BackgroundPoster(this);
        asyncPoster = new AsyncPoster(this);
    }

    public void register(Object subscriber) {
        register(subscriber, defaultMethodName);
    }

    public void register(Object subscriber, String methodName) {
        List<SubscriberMethod> subscriberMethods = findSubscriberMethods(subscriber.getClass(), methodName);
        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            subscribe(subscriber, subscriberMethod);
        }
    }

    private List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass, String eventMethodName) {
        String key = subscriberClass.getName() + '.' + eventMethodName;
        List<SubscriberMethod> subscriberMethods;
        synchronized (methodCache) {
            subscriberMethods = methodCache.get(key);
        }
        if (subscriberMethods != null) {
            return subscriberMethods;
        }
        subscriberMethods = new ArrayList<SubscriberMethod>();
        Class<?> clazz = subscriberClass;
        HashSet<String> eventTypesFound = new HashSet<String>();
        while (clazz != null) {
            String name = clazz.getName();
            if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
                // Skip system classes, this just degrades performance
                break;
            }

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                if (methodName.startsWith(eventMethodName)) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1) {
                        String modifierString = methodName.substring(eventMethodName.length());
                        ThreadMode threadMode;
                        if (modifierString.length() == 0) {
                            threadMode = ThreadMode.PostThread;
                        } else if (modifierString.equals("MainThread")) {
                            threadMode = ThreadMode.MainThread;
                        } else if (modifierString.equals("BackgroundThread")) {
                            threadMode = ThreadMode.BackgroundThread;
                        } else if (modifierString.equals("Async")) {
                            threadMode = ThreadMode.Async;
                        } else {
                            throw new EventBusException("Illegal onEvent method, check for typos: " + method);
                        }
                        Class<?> eventType = parameterTypes[0];
                        String methodKey = methodName + ">" + eventType.getName();
                        if (eventTypesFound.add(methodKey)) {
                            // Only add if not already found in a sub class
                            subscriberMethods.add(new SubscriberMethod(method, threadMode, eventType));
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        if (subscriberMethods.isEmpty()) {
            throw new RuntimeException("Subscriber " + subscriberClass + " has no methods called " + eventMethodName);
        } else {
            synchronized (methodCache) {
                methodCache.put(key, subscriberMethods);
            }
            return subscriberMethods;
        }
    }

    public void register(Object subscriber, Class<?> eventType, Class<?>... moreEventTypes) {
        register(subscriber, defaultMethodName, eventType, moreEventTypes);
    }

    public synchronized void register(Object subscriber, String methodName, Class<?> eventType,
            Class<?>... moreEventTypes) {
        Class<?> subscriberClass = subscriber.getClass();
        List<SubscriberMethod> subscriberMethods = findSubscriberMethods(subscriberClass, methodName);
        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            if (eventType == subscriberMethod.eventType) {
                subscribe(subscriber, subscriberMethod);
            }
        }
    }

    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        Class<?> eventType = subscriberMethod.eventType;
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
        if (subscriptions == null) {
            subscriptions = new CopyOnWriteArrayList<Subscription>();
            subscriptionsByEventType.put(eventType, subscriptions);
        } else {
            for (Subscription subscription : subscriptions) {
                if (subscription.equals(newSubscription)) {
                    throw new RuntimeException("Subscriber " + subscriber.getClass() + " already registered to event "
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

    private void postSingleEvent(Object event, boolean isMainThread) throws Error {
        List<Class<?>> eventTypes = findEventTypes(event.getClass());
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
            Log.d(TAG, "No subscripers registered for event " + event.getClass());
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

    static void invokeSubscriber(PendingPost pendingPost) {
        Object event = pendingPost.event;
        Subscription subscription = pendingPost.subscription;
        PendingPost.releasePendingPost(pendingPost);
        EventBus.invokeSubscriber(subscription, event);
    }

    static void invokeSubscriber(Subscription subscription, Object event) throws Error {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            Log.e(TAG, "Could not dispatch event: " + event.getClass() + " to subscribing class "
                    + subscription.subscriber.getClass(), cause);
            if (cause instanceof Error) {
                throw (Error) cause;
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    /** For ThreadLocal, much faster to set than storing a new Boolean. */
    final static class BooleanWrapper {
        boolean value;
    }

}
