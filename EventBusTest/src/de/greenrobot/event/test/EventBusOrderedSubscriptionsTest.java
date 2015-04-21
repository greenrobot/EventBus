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

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import android.util.Log;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusOrderedSubscriptionsTest extends AbstractEventBusTest {

    int lastPrio = Integer.MAX_VALUE;
    final List<PrioSubscriber> registered = new ArrayList<PrioSubscriber>();
    private String fail;

    public void testOrdered() {
        runTestOrdered("42", false);
    }

    public void testOrderedMainThread() {
        runTestOrdered(new IntTestEvent(42), false);
    }

    public void testOrderedBackgroundThread() {
        runTestOrdered(Integer.valueOf(42), false);
    }
    
    public void testOrderedSticky() {
        runTestOrdered("42", true);
    }

    public void testOrderedMainThreadSticky() {
        runTestOrdered(new IntTestEvent(42), true);
    }

    public void testOrderedBackgroundThreadSticky() {
        runTestOrdered(Integer.valueOf(42), true);
    }

    protected void runTestOrdered(Object event, boolean sticky) {
        register(1, sticky);
        register(-1, sticky);
        register(10, sticky);
        register(0, sticky);
        register(-100, sticky);
        assertEquals(5, registered.size());

        eventBus.post(event);

        waitForEventCount(5, 10000);
        assertEquals(null, fail);

        unregisterAll();
    }

    private void unregisterAll() {
        for (PrioSubscriber subscriber : registered) {
            eventBus.unregister(subscriber);
        }
    }

    protected PrioSubscriber register(int priority, boolean sticky) {
        PrioSubscriber subscriber = new PrioSubscriber(priority);
        if (sticky) {
            eventBus.registerSticky(subscriber, priority);
        } else {
            eventBus.register(subscriber, priority);
        }
        registered.add(subscriber);
        return subscriber;
    }

    private final class PrioSubscriber {

        final int prio;

        public PrioSubscriber(int prio) {
            this.prio = prio;
            // TODO Auto-generated constructor stub
        }

        public void onEvent(String event) {
            handleEvent(event);
        }

        public void onEventMainThread(IntTestEvent event) {
            handleEvent(event);
        }

        public void onEventBackgroundThread(Integer event) {
            handleEvent(event);
        }

        protected void handleEvent(Object event) {
            if (prio > lastPrio) {
                fail = "Called prio " + prio + " after " + lastPrio;
            }
            lastPrio = prio;

            Log.d(EventBus.TAG, "Subscriber " + prio + " got: " + event);
            trackEvent(event);
        }

    }

}
