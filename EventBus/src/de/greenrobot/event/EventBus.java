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
import java.util.List;
import java.util.Map;

import android.util.Log;

/**
 * Class based event bus, optimized for Android. By default, subscribers will handle events in methods named "onEvent".
 * 
 * @author Markus Junginger, greenrobot
 */
public class EventBus {
    /** Log tag, apps may override it. */
    public static String TAG = "Event";

    private static EventBus defaultInstance;

    private static Map<String, List<Method>> methodCache = new HashMap<String, List<Method>>();

    private final Map<Class<?>, List<Subscription>> subscriptionsByEventType;
    private final Map<Object, List<Class<?>>> typesBySubscriber;
    private final ThreadLocal<List<Subscription>> postQueue = new ThreadLocal<List<Subscription>>() {
        protected java.util.List<Subscription> initialValue() {
            return new ArrayList<Subscription>();
        };
    };

    private String defaultMethodName = "onEvent";

    public static synchronized EventBus getDefault() {
        if (null == defaultInstance) {
            defaultInstance = new EventBus();
        }
        return defaultInstance;
    }

    public EventBus() {
        subscriptionsByEventType = new HashMap<Class<?>, List<Subscription>>();
        typesBySubscriber = new HashMap<Object, List<Class<?>>>();
    }

    public void register(Object subscriber) {
        register(subscriber, defaultMethodName);
    }

    public void register(Object subscriber, String methodName) {
        List<Method> subscriberMethods = findSubscriberMethods(subscriber.getClass(), methodName);
        for (Method method : subscriberMethods) {
            Class<?> eventType = method.getParameterTypes()[0];
            subscribe(subscriber, method, eventType);
        }
    }

    private List<Method> findSubscriberMethods(Class<?> subscriberClass, String methodName) {
        String key = subscriberClass.getName() + '.' + methodName;
        List<Method> subscriberMethods;
        synchronized (methodCache) {
            subscriberMethods = methodCache.get(key);
        }
        if (subscriberMethods != null) {
            return subscriberMethods;
        }
        subscriberMethods = new ArrayList<Method>();
        Class<?> clazz = subscriberClass;
        while (clazz != null) {
            String name = clazz.getName();
            if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
                // Skip system classes, this just degrades performance
                break;
            }

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1) {
                        subscriberMethods.add(method);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        if (subscriberMethods.isEmpty()) {
            throw new RuntimeException("Subscriber " + subscriberClass + " has no methods called " + methodName);
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
        Method method = findSubscriberMethod(subscriberClass, methodName, eventType);
        subscribe(subscriber, method, eventType);

        for (Class<?> anothereventType : moreEventTypes) {
            method = findSubscriberMethod(subscriberClass, methodName, anothereventType);
            subscribe(subscriber, method, anothereventType);
        }
    }

    private void subscribe(Object subscriber, Method subscriberMethod, Class<?> eventType) {
        List<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions == null) {
            subscriptions = new ArrayList<Subscription>();
            subscriptionsByEventType.put(eventType, subscriptions);
        } else {
            for (Subscription subscription : subscriptions) {
                if (subscription.subscriber == subscriber) {
                    throw new RuntimeException("Subscriber " + subscriber.getClass() + " already registered to event "
                            + eventType);
                }
            }
        }

        subscriberMethod.setAccessible(true);
        Subscription subscription = new Subscription(subscriber, subscriberMethod);
        subscriptions.add(subscription);

        List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
        if (subscribedEvents == null) {
            subscribedEvents = new ArrayList<Class<?>>();
            typesBySubscriber.put(subscriber, subscribedEvents);
        }
        subscribedEvents.add(eventType);
    }

    /**
     * Class.getMethod is slow on Android 2.3 (and probably other versions), so use getDeclaredMethod and go up in the
     * class hierarchy if neccessary.
     */
    private Method findSubscriberMethod(Class<?> subscriberClass, String methodName, Class<?> eventType) {
        Class<?> clazz = subscriberClass;
        while (clazz != null) {
            try {
                return clazz.getDeclaredMethod(methodName, eventType);
            } catch (NoSuchMethodException ex) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Method " + methodName + " not found  in " + subscriberClass
                + " (must have single parameter of event type " + eventType + ")");
    }

    /** Unregisters the given subscriber for the given event classes. */
    public synchronized void unregister(Object subscriber, Class<?>... eventTypes) {
        if (eventTypes.length == 0) {
            throw new IllegalArgumentException("Provide at least one event class");
        }
        List<Class<?>> subscribedClasses = typesBySubscriber.get(subscriber);
        if (subscribedClasses != null && !subscribedClasses.isEmpty()) {
            for (Class<?> eventType : eventTypes) {
                unubscribeByEventType(subscriber, eventType);
                subscribedClasses.remove(eventType);
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
        if (subscribedTypes != null && !subscribedTypes.isEmpty()) {
            for (Class<?> eventType : subscribedTypes) {
                unubscribeByEventType(subscriber, eventType);
            }
            subscribedTypes.clear();
        } else {
            Log.w(TAG, "Subscriber to unregister was not registered before: " + subscriber.getClass());
        }
    }

    /** Posts the given event to the event bus. */
    public void post(Object event) {
        List<Subscription> subscriptions = postQueue.get();
        if (!subscriptions.isEmpty()) {
            Log.e(TAG, "Post queue not empty prior to posting");
            subscriptions.clear();
        }

        Class<? extends Object> clazz = event.getClass();
        // Don't block other threads during event handling, just grab the subscriptions to call
        synchronized (this) {
            List<Subscription> list = subscriptionsByEventType.get(clazz);
            if (list != null && !list.isEmpty()) {
                subscriptions.addAll(list);
            }
        }
        if (subscriptions.isEmpty()) {
            Log.d(TAG, "No subscriptions registered for event " + event.getClass());
        } else {
            int size = subscriptions.size();
            for (int i = 0; i < size; i++) {
                Subscription subscription = subscriptions.get(i);
                postToSubscribtion(subscription, event);
            }
            subscriptions.clear();
        }
    }

    private void postToSubscribtion(Subscription subscription, Object event) throws Error {
        try {
            subscription.method.invoke(subscription.subscriber, event);
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

    static class Subscription {
        final Object subscriber;
        final Method method;

        Subscription(Object subscriber, Method method) {
            this.subscriber = subscriber;
            this.method = method;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Subscription) {
                Subscription otherSubscription = (Subscription) other;
                // Super slow (improve once used): http://code.google.com/p/android/issues/detail?id=7811
                return subscriber == otherSubscription.subscriber && method.equals(otherSubscription.method);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            // Check performance once used
            return subscriber.hashCode() + method.hashCode();
        }
    }

}
