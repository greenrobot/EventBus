package de.greenrobot.event;

import java.lang.reflect.Method;

/** Base class for generated index classes created by annotation processing. */
public abstract class SubscriberInfo {
    final Class subscriberClass;
    final Class superSubscriberInfoClass;
    final Class nextSubscriberInfoClass;

    protected SubscriberInfo(Class subscriberClass, Class superSubscriberInfoClass, Class nextSubscriberInfoClass) {
        this.subscriberClass = subscriberClass;
        this.superSubscriberInfoClass = superSubscriberInfoClass;
        this.nextSubscriberInfoClass = nextSubscriberInfoClass;
    }

    abstract protected SubscriberMethod[] createSubscriberMethods();

    protected SubscriberMethod createSubscriberMethod(String methodName, Class<?> eventType) {
        return createSubscriberMethod(methodName, eventType, ThreadMode.POSTING, 0, false);

    }

    protected SubscriberMethod createSubscriberMethod(String methodName, Class<?> eventType, ThreadMode threadMode) {
        return createSubscriberMethod(methodName, eventType, threadMode, 0, false);
    }

    protected SubscriberMethod createSubscriberMethod(String methodName, Class<?> eventType, ThreadMode threadMode,
                                                      int priority, boolean sticky) {
        try {
            Method method = subscriberClass.getDeclaredMethod(methodName, eventType);
            return new SubscriberMethod(method, eventType, threadMode, priority, sticky);
        } catch (NoSuchMethodException e) {
            throw new EventBusException("Could not find subscriber method in " + subscriberClass +
                    ". Maybe a missing ProGuard rule?", e);
        }
    }

}
