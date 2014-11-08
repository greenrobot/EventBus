/*
 * Copyright (C) 2013 Markus Junginger, greenrobot (http://greenrobot.de)
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

import java.util.concurrent.CountDownLatch;

import android.test.UiThreadTest;
import de.greenrobot.event.EventBusException;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusCancelEventDeliveryTest extends AbstractEventBusTest {

    private Throwable failed;

    public void testCancel() {
        Subscriber canceler = new Subscriber(true);
        eventBus.register(new Subscriber(false));
        eventBus.register(canceler, 1);
        eventBus.register(new Subscriber(false));
        eventBus.post("42");
        assertEquals(1, eventCount.intValue());

        eventBus.unregister(canceler);
        eventBus.post("42");
        assertEquals(1 + 2, eventCount.intValue());
    }

    public void testCancelInBetween() {
        Subscriber canceler = new Subscriber(true);
        eventBus.register(canceler, 2);
        eventBus.register(new Subscriber(false), 1);
        eventBus.register(new Subscriber(false), 3);
        eventBus.post("42");
        assertEquals(2, eventCount.intValue());
    }

    public void testCancelOutsideEventHandler() {
        try {
            eventBus.cancelEventDelivery(this);
            fail("Should have thrown");
        } catch (EventBusException e) {
            // Expected
        }
    }

    public void testCancelWrongEvent() {
        eventBus.register(new SubscriberCancelOtherEvent());
        eventBus.post("42");
        assertEquals(0, eventCount.intValue());
        assertNotNull(failed);
    }

    @UiThreadTest
    public void testCancelInMainThread() {
        SubscriberMainThread subscriber = new SubscriberMainThread();
        eventBus.register(subscriber);
        eventBus.post("42");
        awaitLatch(subscriber.done, 10);
        assertEquals(0, eventCount.intValue());
        assertNotNull(failed);
    }

    class Subscriber {
        private final boolean cancel;

        public Subscriber(boolean cancel) {
            this.cancel = cancel;
        }

        public void onEvent(String event) {
            trackEvent(event);
            if (cancel) {
                eventBus.cancelEventDelivery(event);
            }
        }
    }

    class SubscriberCancelOtherEvent {
        public void onEvent(String event) {
            try {
                eventBus.cancelEventDelivery(this);
            } catch (EventBusException e) {
                failed = e;
            }
        }
    }

    class SubscriberMainThread {
        final CountDownLatch done = new CountDownLatch(1);

        public void onEventMainThread(String event) {
            try {
                eventBus.cancelEventDelivery(event);
            } catch (EventBusException e) {
                failed = e;
            }
            done.countDown();
        }
    }

}
