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
import static org.junit.Assert.assertSame;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusNoSubscriberEventTest extends AbstractEventBusTest {

    @Test
    public void testNoSubscriberEvent() {
        eventBus.register(this);
        eventBus.post("Foo");
        assertEventCount(1);
        assertEquals(NoSubscriberEvent.class, lastEvent.getClass());
        NoSubscriberEvent noSub = (NoSubscriberEvent) lastEvent;
        assertEquals("Foo", noSub.originalEvent);
        assertSame(eventBus, noSub.eventBus);
    }

    @Test
    public void testNoSubscriberEventAfterUnregister() {
        Object subscriber = new DummySubscriber();
        eventBus.register(subscriber);
        eventBus.unregister(subscriber);
        testNoSubscriberEvent();
    }

    @Test
    public void testBadNoSubscriberSubscriber() {
        eventBus = EventBus.builder().logNoSubscriberMessages(false).build();
        eventBus.register(this);
        eventBus.register(new BadNoSubscriberSubscriber());
        eventBus.post("Foo");
        assertEventCount(2);

        assertEquals(SubscriberExceptionEvent.class, lastEvent.getClass());
        NoSubscriberEvent noSub = (NoSubscriberEvent) ((SubscriberExceptionEvent) lastEvent).causingEvent;
        assertEquals("Foo", noSub.originalEvent);
    }

    @Subscribe
    public void onEvent(NoSubscriberEvent event) {
        trackEvent(event);
    }

    @Subscribe
    public void onEvent(SubscriberExceptionEvent event) {
        trackEvent(event);
    }

    public static class DummySubscriber {
        @SuppressWarnings("unused")
        @Subscribe
        public void onEvent(String dummy) {
        }
    }

    public class BadNoSubscriberSubscriber {
        @Subscribe
        public void onEvent(NoSubscriberEvent event) {
            throw new RuntimeException("I'm bad");
        }
    }

}
