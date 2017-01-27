package org.greenrobot.eventbus.meta;


/**
 * Interface for generated method invocation to avoid using reflection
 */
public interface SubscriberMethodInvoker {
    void invoke(Object subscriber, Object event);
}
