package de.greenrobot.event;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/** Preprocessed index: base class for generated "MyGeneratedSubscriberIndex" class by annotation processing. */
abstract class SubscriberIndex {
    private Map<Class<?>, SubscriberMethod[]> map = new HashMap<Class<?>, SubscriberMethod[]>();

    SubscriberMethod[] getSubscribersFor(Class<?> subscriberClass) {
        SubscriberMethod[] entries = map.get(subscriberClass);
        if (entries == null) {
            entries = createSubscribersFor(subscriberClass);
            if (entries != null) {
                map.put(subscriberClass, entries);
            }
        }
        return entries;
    }

    abstract SubscriberMethod[] createSubscribersFor(Class<?> subscriberClass);

    SubscriberMethod createSubscriberMethod(Class<?> subscriberClass, String methodName, Class<?> eventType,
                                            ThreadMode threadMode) {
        try {
            Method method = subscriberClass.getDeclaredMethod(methodName, eventType);
            return new SubscriberMethod(method, threadMode, eventType);
        } catch (NoSuchMethodException e) {
            throw new EventBusException("Could not find subscriber method in " + subscriberClass +
                    ". Maybe a missing ProGuard rule?", e);
        }
    }

}
