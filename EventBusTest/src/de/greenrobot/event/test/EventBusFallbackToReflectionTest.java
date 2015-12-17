package de.greenrobot.event.test;

import de.greenrobot.event.Subscribe;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** TODO */
public class EventBusFallbackToReflectionTest extends AbstractEventBusTest {
    private class PrivateEvent {
    }

    public class PublicClass {
        @Subscribe
        public void onEvent(Object any) {
            trackEvent(any);
        }
    }

    private class PrivateClass {
        @Subscribe
        public void onEvent(Object any) {
            trackEvent(any);
        }
    }

    public class PublicWithPrivateSuperClass extends PrivateClass {
        @Subscribe
        public void onEvent(String any) {
            trackEvent(any);
        }
    }

    public class PublicClassWithPrivateEvent {
        @Subscribe
        public void onEvent(PrivateEvent any) {
            trackEvent(any);
        }
    }

    public class PublicWithPrivateEventInSuperclass extends PublicClassWithPrivateEvent {
        @Subscribe
        public void onEvent(Object any) {
            trackEvent(any);
        }
    }

    public EventBusFallbackToReflectionTest() {
        super(true);
    }

    @Test
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

    @Test
    public void testAnonymousSubscriberClassWithPublicSuperclass() {
        Object subscriber = new PublicClass() {
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

    @Test
    public void testAnonymousSubscriberClassWithPrivateSuperclass() {
        eventBus.register(new PublicWithPrivateSuperClass());
        eventBus.post("Hello");
        assertEquals("Hello", lastEvent);
        assertEquals(2, eventsReceived.size());
    }

    @Test
    public void testSubscriberClassWithPrivateEvent() {
        eventBus.register(new PublicClassWithPrivateEvent());
        PrivateEvent privateEvent = new PrivateEvent();
        eventBus.post(privateEvent);
        assertEquals(privateEvent, lastEvent);
        assertEquals(1, eventsReceived.size());
    }

    @Test
    public void testSubscriberExtendingClassWithPrivateEvent() {
        eventBus.register(new PublicWithPrivateEventInSuperclass());
        PrivateEvent privateEvent = new PrivateEvent();
        eventBus.post(privateEvent);
        assertEquals(privateEvent, lastEvent);
        assertEquals(2, eventsReceived.size());
    }

}
