/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.greenrobot.eventbus;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

    public class PublicClassWithPublicAndPrivateEvent {
        @Subscribe
        public void onEvent(String any) {
            trackEvent(any);
        }

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
    public void testSubscriberClassWithPublicAndPrivateEvent() {
        eventBus.register(new PublicClassWithPublicAndPrivateEvent());

        eventBus.post("Hello");
        assertEquals("Hello", lastEvent);
        assertEquals(1, eventsReceived.size());

        PrivateEvent privateEvent = new PrivateEvent();
        eventBus.post(privateEvent);
        assertEquals(privateEvent, lastEvent);
        assertEquals(2, eventsReceived.size());
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
