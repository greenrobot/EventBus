package de.greenrobot.event.test;

import de.greenrobot.event.Subscribe;

/** TODO */
public class EventBusFallbackToReflectionTest extends AbstractEventBusTest {
    public class PublicSuperClass {
        @Subscribe
        public void onEvent(Object any) {
            trackEvent(any);
        }
    }

    private class PrivateSuperClass {
        @Subscribe
        public void onEvent(Object any) {
            trackEvent(any);
        }
    }

    public class PublicWithPrivateSuperClass extends PrivateSuperClass {
        @Subscribe
        public void onEvent(String any) {
            trackEvent(any);
        }
    }

    public EventBusFallbackToReflectionTest() {
        super(true);
    }

    public void testAnonymousSubscriberClass() {
        Object subscriber = new Object() {
            @Subscribe
            public void onEvent(String event) {
                trackEvent(event);
            }
        };
        eventBus.register(subscriber);

        eventBus.post("Hello");
        assertEquals("Hello", lastEvent);
        assertEquals(1, eventsReceived.size());
    }

    public void testAnonymousSubscriberClassWithPublicSuperclass() {
        Object subscriber = new PublicSuperClass() {
            @Subscribe
            public void onEvent(String event) {
                trackEvent(event);
            }
        };
        eventBus.register(subscriber);

        eventBus.post("Hello");
        assertEquals("Hello", lastEvent);
        assertEquals(2, eventsReceived.size());
    }

    public void testAnonymousSubscriberClassWithPrivateSuperclass() {
        eventBus.register(new PublicWithPrivateSuperClass());
        eventBus.post("Hello");
        assertEquals("Hello", lastEvent);
        assertEquals(2, eventsReceived.size());
    }

}
