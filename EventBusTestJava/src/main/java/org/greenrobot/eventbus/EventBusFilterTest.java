package org.greenrobot.eventbus;

import org.junit.Test;

/**
 * @author miqt
 */

public class EventBusFilterTest extends AbstractEventBusTest {
    @Test
    public void testFilter() {
        eventBus.register(this);

        eventBus.post("hello", filter);
        eventBus.post("no filter hello");

        eventBus.postSticky("hello Sticky", filter);
        eventBus.postSticky("no filter hello Sticky");

        eventBus.unregister(this);
    }

    @Subscribe
    public void goodDay(String str) {
        System.out.println(EventBus.TAG + ":goodDay:" + str);
    }

    @Subscribe
    public void badDay(String str) {
        System.out.println(EventBus.TAG + ":badDay:" + str);
    }

    private Filter filter = new Filter() {
        @Override
        public boolean allow(Subscription subscription) {
            return subscription.getSubscriberMethod().method.getName().equals("goodDay");
        }
    };
}
