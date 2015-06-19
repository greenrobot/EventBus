package de.greenrobot.event;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/** Preprocessed index: base class for generated "MyGeneratedSubscriberIndex" class by annotation processing. */
public abstract class SubscriberInfo {
    public static class Data {
        public final Class<?> subscriberClass;
        public final SubscriberMethod[] subscriberMethods;
        public final Class<?> nextInfo;

        public Data(Class<?> subscriberClass, SubscriberMethod[] subscriberMethods, Class<?> nextInfo) {
            this.subscriberClass = subscriberClass;
            this.subscriberMethods = subscriberMethods;
            this.nextInfo = nextInfo;
        }
    }

    private Map<Class<?>, Data> map = new HashMap<Class<?>, Data>();

    Data getSubscriberData(Class<?> subscriberClass) {
        Data data = map.get(subscriberClass);
        if (data == null) {
            data = createSubscriberData();
            if (data != null) {
                map.put(subscriberClass, data);
            }
        }
        return data;
    }

    abstract protected Data createSubscriberData();

    protected SubscriberMethod createSubscriberMethod(Class<?> subscriberClass, String methodName, Class<?> eventType,
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
