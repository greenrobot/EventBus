package de.greenrobot.event;

import java.lang.reflect.Method;

/** Preprocessed index: base class for generated "MyGeneratedSubscriberIndex" class by annotation processing. */
public abstract class SubscriberInfo {
    final Class subscriberClass;
    final Class superSubscriberInfoClass;
    final Class nextSubscriberInfoClass;

    protected SubscriberMethod[] subscriberMethods;

    protected SubscriberInfo(Class subscriberClass, Class superSubscriberInfoClass, Class nextSubscriberInfoClass) {
        this.subscriberClass = subscriberClass;
        this.superSubscriberInfoClass = superSubscriberInfoClass;
        this.nextSubscriberInfoClass = nextSubscriberInfoClass;
    }

    abstract protected  SubscriberMethod[] createSubscriberMethods();

    protected SubscriberMethod createSubscriberMethod(String methodName, Class<?> eventType,
                                            ThreadMode threadMode, int priority, boolean sticky) {
        try {
            Method method = subscriberClass.getDeclaredMethod(methodName, eventType);
            return new SubscriberMethod(method, eventType, threadMode, priority, sticky);
        } catch (NoSuchMethodException e) {
            throw new EventBusException("Could not find subscriber method in " + subscriberClass +
                    ". Maybe a missing ProGuard rule?", e);
        }
    }

}
