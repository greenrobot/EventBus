package de.greenrobot.event.test;

import org.greenrobot.eventbus.Subscribe;
import org.junit.Test;

public class EventBusGenericsTest extends AbstractEventBusTest {
    public static class GenericEvent<T> {
        T value;
    }

    public class GenericSubscriber<T> {
        @Subscribe
        public void onGenericEvent(GenericEvent<T> event) {
            trackEvent(event);
        }
    }

    @Test
    public void testGenericEventAndSubscriber() {
        GenericSubscriber<IntTestEvent> genericSubscriber = new GenericSubscriber<IntTestEvent>();
        eventBus.register(genericSubscriber);
        eventBus.post(new GenericEvent<Integer>());
        eventBus.unregister(genericSubscriber);

        assertEventCount(1);
    }
}
