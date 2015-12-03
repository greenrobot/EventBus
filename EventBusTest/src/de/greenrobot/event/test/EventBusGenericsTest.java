package de.greenrobot.event.test;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

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

    public void testGenericEventAndSubscriber() {
        GenericSubscriber<IntTestEvent> genericSubscriber = new GenericSubscriber<IntTestEvent>();
        eventBus.register(genericSubscriber);
        eventBus.post(new GenericEvent<Integer>());
        eventBus.unregister(genericSubscriber);

        assertEventCount(1);
    }
}
