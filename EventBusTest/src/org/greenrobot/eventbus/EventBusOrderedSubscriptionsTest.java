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

import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusOrderedSubscriptionsTest extends AbstractEventBusTest {

    int lastPrio = Integer.MAX_VALUE;
    final List<PrioSubscriber> registered = new ArrayList<PrioSubscriber>();
    private String fail;

    @Test
    public void testOrdered() {
        runTestOrdered("42", false, 5);
    }

    @Test
    public void testOrderedMainThread() {
        runTestOrdered(new IntTestEvent(42), false, 3);
    }

    @Test
    public void testOrderedBackgroundThread() {
        runTestOrdered(Integer.valueOf(42), false, 3);
    }

    @Test
    public void testOrderedSticky() {
        runTestOrdered("42", true, 5);
    }

    @Test
    public void testOrderedMainThreadSticky() {
        runTestOrdered(new IntTestEvent(42), true, 3);
    }

    @Test
    public void testOrderedBackgroundThreadSticky() {
        runTestOrdered(Integer.valueOf(42), true, 3);
    }

    protected void runTestOrdered(Object event, boolean sticky, int expectedEventCount) {
        Object subscriber = sticky ? new PrioSubscriberSticky() : new PrioSubscriber();
        eventBus.register(subscriber);
        eventBus.post(event);

        waitForEventCount(expectedEventCount, 10000);
        assertEquals(null, fail);

        eventBus.unregister(subscriber);
    }

    public final class PrioSubscriber {
        @Subscribe(priority = 1)
        public void onEventP1(String event) {
            handleEvent(1, event);
        }

        @Subscribe(priority = -1)
        public void onEventM1(String event) {
            handleEvent(-1, event);
        }

        @Subscribe(priority = 0)
        public void onEventP0(String event) {
            handleEvent(0, event);
        }

        @Subscribe(priority = 10)
        public void onEventP10(String event) {
            handleEvent(10, event);
        }

        @Subscribe(priority = -100)
        public void onEventM100(String event) {
            handleEvent(-100, event);
        }


        @Subscribe(threadMode = ThreadMode.MAIN, priority = -1)
        public void onEventMainThreadM1(IntTestEvent event) {
            handleEvent(-1, event);
        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThreadP0(IntTestEvent event) {
            handleEvent(0, event);
        }

        @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
        public void onEventMainThreadP1(IntTestEvent event) {
            handleEvent(1, event);
        }

        @Subscribe(threadMode = ThreadMode.BACKGROUND, priority = 1)
        public void onEventBackgroundThreadP1(Integer event) {
            handleEvent(1, event);
        }

        @Subscribe(threadMode = ThreadMode.BACKGROUND)
        public void onEventBackgroundThreadP0(Integer event) {
            handleEvent(0, event);
        }

        @Subscribe(threadMode = ThreadMode.BACKGROUND, priority = -1)
        public void onEventBackgroundThreadM1(Integer event) {
            handleEvent(-1, event);
        }

        protected void handleEvent(int prio, Object event) {
            if (prio > lastPrio) {
                fail = "Called prio " + prio + " after " + lastPrio;
            }
            lastPrio = prio;

            Log.d(EventBus.TAG, "Subscriber " + prio + " got: " + event);
            trackEvent(event);
        }

    }

    public final class PrioSubscriberSticky {
        @Subscribe(priority = 1, sticky = true)
        public void onEventP1(String event) {
            handleEvent(1, event);
        }


        @Subscribe(priority = -1, sticky = true)
        public void onEventM1(String event) {
            handleEvent(-1, event);
        }

        @Subscribe(priority = 0, sticky = true)
        public void onEventP0(String event) {
            handleEvent(0, event);
        }

        @Subscribe(priority = 10, sticky = true)
        public void onEventP10(String event) {
            handleEvent(10, event);
        }

        @Subscribe(priority = -100, sticky = true)
        public void onEventM100(String event) {
            handleEvent(-100, event);
        }

        @Subscribe(threadMode = ThreadMode.MAIN, priority = -1, sticky = true)
        public void onEventMainThreadM1(IntTestEvent event) {
            handleEvent(-1, event);
        }

        @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
        public void onEventMainThreadP0(IntTestEvent event) {
            handleEvent(0, event);
        }

        @Subscribe(threadMode = ThreadMode.MAIN, priority = 1, sticky = true)
        public void onEventMainThreadP1(IntTestEvent event) {
            handleEvent(1, event);
        }

        @Subscribe(threadMode = ThreadMode.BACKGROUND, priority = 1, sticky = true)
        public void onEventBackgroundThreadP1(Integer event) {
            handleEvent(1, event);
        }

        @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
        public void onEventBackgroundThreadP0(Integer event) {
            handleEvent(0, event);
        }

        @Subscribe(threadMode = ThreadMode.BACKGROUND, priority = -1, sticky = true)
        public void onEventBackgroundThreadM1(Integer event) {
            handleEvent(-1, event);
        }

        protected void handleEvent(int prio, Object event) {
            if (prio > lastPrio) {
                fail = "Called prio " + prio + " after " + lastPrio;
            }
            lastPrio = prio;

            Log.d(EventBus.TAG, "Subscriber " + prio + " got: " + event);
            trackEvent(event);
        }

    }

}
