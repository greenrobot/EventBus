package org.greenrobot.eventbus;

public interface ISubscription {

    public Object getSubscriber();

    public SubscriberMethod getSubscriberMethod();

    public boolean isActive();

    public void setIsActive(boolean isActive);
}
