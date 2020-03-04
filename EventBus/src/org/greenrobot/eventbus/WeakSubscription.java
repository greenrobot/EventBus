package org.greenrobot.eventbus;

import java.lang.ref.WeakReference;

public class WeakSubscription extends WeakReference<Object> implements ISubscription {

    private final SubscriberMethod subscriberMethod;

    /**
     * Becomes false as soon as {@link EventBus#unregister(Object)} is called, which is checked by queued event delivery
     * {@link EventBus#invokeSubscriber(PendingPost)} to prevent race conditions.
     */
    private volatile boolean active;

    WeakSubscription(Object subscriber, SubscriberMethod subscriberMethod) {
        super(subscriber);
        this.subscriberMethod = subscriberMethod;
        active = true;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ISubscription) {
            ISubscription otherSubscription = (ISubscription) other;
            return get() == otherSubscription.getSubscriber()
                    && subscriberMethod.equals(otherSubscription.getSubscriberMethod());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // I have to admit that this kind of implementation could not guarantee thread safe.
        // If the reference is being claimed when hashCode() executing, and the equals()
        // might not behave in accordance with it. But it seems there is no better way.
        Object referent = get();
        if (referent != null) {
            return referent.hashCode() + subscriberMethod.methodString.hashCode();
        } else {
            return System.identityHashCode(this) + subscriberMethod.methodString.hashCode();
        }
    }

    @Override
    public Object getSubscriber() {
        return get();
    }

    @Override
    public SubscriberMethod getSubscriberMethod() {
        return subscriberMethod;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setIsActive(boolean isActive) {
        this.active = isActive;
    }
}
