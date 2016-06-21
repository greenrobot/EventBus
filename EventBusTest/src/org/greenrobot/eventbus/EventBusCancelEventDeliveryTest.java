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

import android.test.UiThreadTest;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusCancelEventDeliveryTest extends AbstractEventBusTest {

    private Throwable failed;

    @Test
    public void testCancel() {
        Subscriber canceler = new Subscriber(1, true);
        eventBus.register(new Subscriber(0, false));
        eventBus.register(canceler);
        eventBus.register(new Subscriber(0, false));
        eventBus.post("42");
        assertEquals(1, eventCount.intValue());

        eventBus.unregister(canceler);
        eventBus.post("42");
        assertEquals(1 + 2, eventCount.intValue());
    }

    @Test
    public void testCancelInBetween() {
        eventBus.register(new Subscriber(2, true));
        eventBus.register(new Subscriber(1, false));
        eventBus.register(new Subscriber(3, false));
        eventBus.post("42");
        assertEquals(2, eventCount.intValue());
    }

    @Test
    public void testCancelOutsideEventHandler() {
        try {
            eventBus.cancelEventDelivery(this);
            fail("Should have thrown");
        } catch (EventBusException e) {
            // Expected
        }
    }

    @Test
    public void testCancelWrongEvent() {
        eventBus.register(new SubscriberCancelOtherEvent());
        eventBus.post("42");
        assertEquals(0, eventCount.intValue());
        assertNotNull(failed);
    }

    @UiThreadTest
    @Test
    public void testCancelInMainThread() {
        SubscriberMainThread subscriber = new SubscriberMainThread();
        eventBus.register(subscriber);
        eventBus.post("42");
        awaitLatch(subscriber.done, 10);
        assertEquals(0, eventCount.intValue());
        assertNotNull(failed);
    }

    public class Subscriber {
        private final int prio;
        private final boolean cancel;

        public Subscriber(int prio, boolean cancel) {
            this.prio = prio;
            this.cancel = cancel;
        }

        @Subscribe
        public void onEvent(String event) {
            handleEvent(event, 0);
        }

        @Subscribe(priority = 1)
        public void onEvent1(String event) {
            handleEvent(event, 1);
        }

        @Subscribe(priority = 2)
        public void onEvent2(String event) {
            handleEvent(event, 2);
        }

        @Subscribe(priority = 3)
        public void onEvent3(String event) {
            handleEvent(event, 3);
        }

        private void handleEvent(String event, int prio) {
            if(this.prio == prio) {
                trackEvent(event);
                if (cancel) {
                    eventBus.cancelEventDelivery(event);
                }
            }
        }
    }

    public class SubscriberCancelOtherEvent {
        @Subscribe
        public void onEvent(String event) {
            try {
                eventBus.cancelEventDelivery(this);
            } catch (EventBusException e) {
                failed = e;
            }
        }
    }

    public class SubscriberMainThread {
        final CountDownLatch done = new CountDownLatch(1);

        @Subscribe(threadMode = ThreadMode.MAIN)
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
