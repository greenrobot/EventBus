package org.greenrobot.eventbus;

import org.junit.Test;

/**
 * Created by t54 on 2019/4/28.
 */

public class EventBusFilterTest extends AbstractEventBusTest {
    @Test
    public void test() {
        eventBus.register(this);
        eventBus.post("hello", filter);
        eventBus.post("no filter hello");

        eventBus.postSticky("hello Sticky", filter);
        eventBus.postSticky("no filter hello Sticky");
        eventBus.unregister(this);
    }

    @Subscribe
    public void goodDay(String hello) {
        System.out.println(EventBus.TAG + ":goodDay:" + hello);
    }

    @Subscribe
    public void badDay(String hello) {
        System.out.println(EventBus.TAG + ":badDay:" + hello);
    }

    Filter filter = new Filter() {
        @Override
        public boolean allow(Subscription subscription) {
            return subscription.getSubscriberMethod().method.getName().equals("goodDay");
        }
    };
}
