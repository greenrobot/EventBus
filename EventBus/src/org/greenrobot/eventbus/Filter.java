package org.greenrobot.eventbus;

/**
 * Filter those subscription events to pass
 *
 * @author miqt:https://github.com/miqt
 */
public interface Filter {
    /**
     * @param subscription
     * @return true is can invoke Subscriber false are not
     */
    public boolean allow(Subscription subscription);
}
