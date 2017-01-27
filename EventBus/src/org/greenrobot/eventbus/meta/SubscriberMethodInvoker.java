package org.greenrobot.eventbus.meta;



public interface SubscriberMethodInvoker {
    void invoke(Object subscriber, Object event);
}
