package de.greenrobot.event;

import java.lang.reflect.Method;

final class Subscription {
    final Object subscriber;
    final Method method;
    final ThreadMode threadMode;

    Subscription(Object subscriber, Method method, ThreadMode threadMode) {
        this.subscriber = subscriber;
        this.method = method;
        this.threadMode = threadMode;
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