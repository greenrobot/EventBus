package de.greenrobot.event;

/** Preprocessed index as used with annotation-preprocessed code generation */
public class SubscriberIndexEntry {
    final Class<?> subscriberType;
    final String methodName;
    final Class<?> eventType;
    final ThreadMode threadMode;

    public SubscriberIndexEntry(Class<?> subscriberType, String methodName, Class<?> eventType,
                                ThreadMode threadMode) {
        this.subscriberType = subscriberType;
        this.methodName = methodName;
        this.eventType = eventType;
        this.threadMode = threadMode;
    }
}
