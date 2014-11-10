/*
 * Copyright (C) 2014 Markus Junginger, greenrobot (http://greenrobot.de)
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
package de.greenrobot.event.test;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusBuilder;
import de.greenrobot.event.EventBusException;
import de.greenrobot.event.NoSubscriberEvent;
import de.greenrobot.event.SubscriberExceptionEvent;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusBuilderTest extends AbstractEventBusTest {

    public void testThrowSubscriberException() {
        eventBus = EventBus.builder().throwSubscriberException(true).build();
        eventBus.register(new SubscriberExceptionEventTracker());
        eventBus.register(new ThrowingSubscriber());
        try {
            eventBus.post("Foo");
            fail("Should have thrown");
        } catch (EventBusException e) {
            // Expected
        }
    }

    public void testDoNotSendSubscriberExceptionEvent() {
        eventBus = EventBus.builder().logSubscriberExceptions(false).sendSubscriberExceptionEvent(false).build();
        eventBus.register(new SubscriberExceptionEventTracker());
        eventBus.register(new ThrowingSubscriber());
        eventBus.post("Foo");
        assertEventCount(0);
    }

    public void testDoNotSendNoSubscriberEvent() {
        eventBus = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).build();
        eventBus.register(new NoSubscriberEventTracker());
        eventBus.post("Foo");
        assertEventCount(0);
    }

    public void testInstallDefaultEventBus() {
        EventBusBuilder builder = EventBus.builder();
        try {
            // Either this should throw when another unit test got the default event bus...
            eventBus = builder.installDefaultEventBus();
            assertEquals(eventBus, EventBus.getDefault());

            // ...or this should throw
            eventBus = builder.installDefaultEventBus();
            fail("Should have thrown");
        } catch (EventBusException e) {
            // Expected
        }
    }

    public void testEventInheritance() {
        eventBus = EventBus.builder().eventInheritance(false).build();
        eventBus.register(new ThrowingSubscriber());
        eventBus.post("Foo");
    }

    class SubscriberExceptionEventTracker {
        public void onEvent(SubscriberExceptionEvent event) {
            trackEvent(event);
        }
    }

    class NoSubscriberEventTracker {
        public void onEvent(NoSubscriberEvent event) {
            trackEvent(event);
        }
    }

    class ThrowingSubscriber {
        public void onEvent(Object event) {
            throw new RuntimeException();
        }
    }

}
