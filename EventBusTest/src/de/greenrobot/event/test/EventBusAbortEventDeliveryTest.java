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
public class EventBusAbortEventDeliveryTest extends AbstractEventBusTest {

    private Throwable failed;

    public void testAbort() {
        Subscriber aborter = new Subscriber(true);
        eventBus.register(new Subscriber(false));
        eventBus.register(aborter, 1);
        eventBus.register(new Subscriber(false));
        eventBus.post("42");
        assertEquals(1, eventCount.intValue());

        eventBus.unregister(aborter);
        eventBus.post("42");
        assertEquals(1 + 2, eventCount.intValue());
    }

    public void testAbortInBetween() {
        Subscriber aborter = new Subscriber(true);
        eventBus.register(aborter, 2);
        eventBus.register(new Subscriber(false), 1);
        eventBus.register(new Subscriber(false), 3);
        eventBus.post("42");
        assertEquals(2, eventCount.intValue());
    }

    public void testAbortOutsideEventHandler() {
        try {
            eventBus.abortEventDelivery(this);
            fail("Should have thrown");
        } catch (EventBusException e) {
            // Expected
        }
    }

    public void testAbortWrongEvent() {
        eventBus.register(new SubscriberAbortOtherEvent());
        eventBus.post("42");
        assertEquals(0, eventCount.intValue());
        assertNotNull(failed);
    }

    @UiThreadTest
    public void testAbortInMainThread() {
        SubscriberMainThread subscriber = new SubscriberMainThread();
        eventBus.register(subscriber);
        eventBus.post("42");
        awaitLatch(subscriber.done, 10);
        assertEquals(0, eventCount.intValue());
        assertNotNull(failed);
    }

    class Subscriber {
        private boolean abort;

        public Subscriber(boolean abort) {
            this.abort = abort;
        }

        public void onEvent(String event) {
            trackEvent(event);
            if (abort) {
                eventBus.abortEventDelivery(event);
            }
        }
    }

    class SubscriberAbortOtherEvent {
        public void onEvent(String event) {
            try {
                eventBus.abortEventDelivery(this);
            } catch (EventBusException e) {
                failed = e;
            }
        }
    }

    class SubscriberMainThread {
        CountDownLatch done = new CountDownLatch(1);
        public void onEventMainThread(String event) {
            try {
                eventBus.abortEventDelivery(event);
            } catch (EventBusException e) {
                failed = e;
            }
            done.countDown();
        }
    }

}
