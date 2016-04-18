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

import android.os.Looper;

import org.junit.Before;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Markus Junginger, greenrobot
 */
public abstract class AbstractEventBusTest {
    /** Activates long(er) running tests e.g. testing multi-threading more thoroughly.  */
    protected static final boolean LONG_TESTS = false;

    protected EventBus eventBus;

    protected final AtomicInteger eventCount = new AtomicInteger();
    protected final List<Object> eventsReceived;

    protected volatile Object lastEvent;
    protected volatile Thread lastThread;

    protected Thread mainThread;

    public AbstractEventBusTest() {
        this(false);
    }

    public AbstractEventBusTest(boolean collectEventsReceived) {
        if (collectEventsReceived) {
            eventsReceived = new CopyOnWriteArrayList<Object>();
        } else {
            eventsReceived = null;
        }
    }

    @Before
    public void setUpBase() throws Exception {
        EventBus.clearCaches();

        final EventBusBuilder builder = new EventBusBuilder();
        if (builder.mainThreadSupport == null) {
            final Executor mainExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    mainThread = new Thread(r);
                    return mainThread;
                }
            });
            // running on JVM
            builder.mainThreadSupport = new MainThreadSupport() {
                @Override
                public boolean isMainThread() {
                    return Thread.currentThread() == mainThread;
                }

                @Override
                public Poster createPoster(EventBus eventBus) {
                    return new AsyncPoster(eventBus, mainExecutor);
                }
            };
        } else {
            mainThread = Looper.getMainLooper().getThread();
            assertFalse(Looper.getMainLooper().getThread().equals(Thread.currentThread()));
        }
        eventBus = builder.build();
    }

    protected void waitForEventCount(int expectedCount, int maxMillis) {
        for (int i = 0; i < maxMillis; i++) {
            int currentCount = eventCount.get();
            if (currentCount == expectedCount) {
                break;
            } else if (currentCount > expectedCount) {
                fail("Current count (" + currentCount + ") is already higher than expected count (" + expectedCount
                        + ")");
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        assertEquals(expectedCount, eventCount.get());
    }

    protected void trackEvent(Object event) {
        lastEvent = event;
        lastThread = Thread.currentThread();
        if (eventsReceived != null) {
            eventsReceived.add(event);
        }
        // Must the the last one because we wait for this
        eventCount.incrementAndGet();
    }

    protected void assertEventCount(int expectedEventCount) {
        assertEquals(expectedEventCount, eventCount.intValue());
    }
    
    protected void countDownAndAwaitLatch(CountDownLatch latch, long seconds) {
        latch.countDown();
        awaitLatch(latch, seconds);
    }

    protected void awaitLatch(CountDownLatch latch, long seconds) {
        try {
            assertTrue(latch.await(seconds, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void log(String message) {
        eventBus.getLogger().log(Level.FINE, message);
    }
}
