/*
 * Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de)
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
package de.greenrobot.greenbus.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;
import android.util.Log;
import de.greenrobot.event.EventBus;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusMultithreadedTest extends TestCase {

    /**
     * Iteration count per test (will be divided by number of threads); run with 1000 for quick testing, and with 10000
     * from time to time.
     */
    private static final int COUNT = 1000;

    private final AtomicInteger countStringEvent = new AtomicInteger();
    private final AtomicInteger countIntegerEvent = new AtomicInteger();
    private final AtomicInteger countObjectEvent = new AtomicInteger();

    private EventBus eventBus;
    private String lastStringEvent;
    private Integer lastIntegerEvent;

    protected void setUp() throws Exception {
        super.setUp();
        eventBus = new EventBus();
    }

    public void testPost01Thread() throws InterruptedException {
        runThreadsSingleEventType(1);
    }

    public void testPost04Threads() throws InterruptedException {
        runThreadsSingleEventType(4);
    }

    public void testPost40Threads() throws InterruptedException {
        runThreadsSingleEventType(40);
    }

    public void testPostMixedEventType01Thread() throws InterruptedException {
        runThreadsMixedEventType(1);
    }

    public void testPostMixedEventType04Threads() throws InterruptedException {
        runThreadsMixedEventType(4);
    }

    public void testPostMixedEventType40Threads() throws InterruptedException {
        runThreadsMixedEventType(40);
    }

    public void testSubscribeUnSubscribeAndPostMixedEventType() throws InterruptedException {
        List<SubscribeUnsubscribeThread> threads = new ArrayList<SubscribeUnsubscribeThread>();
        for (int i = 0; i < 10; i++) {
            SubscribeUnsubscribeThread thread = new SubscribeUnsubscribeThread();
            thread.start();
            threads.add(thread);
        }
        runThreadsMixedEventType(10);
        for (SubscribeUnsubscribeThread thread : threads) {
            thread.shutdown();
        }
        for (SubscribeUnsubscribeThread thread : threads) {
            thread.join();
        }
    }

    private void runThreadsSingleEventType(int threadCount) throws InterruptedException {
        int iterations = COUNT / threadCount;
        eventBus.register(this);

        CountDownLatch latch = new CountDownLatch(threadCount + 1);
        List<PosterThread> threads = startThreads(latch, threadCount, iterations, "Hello");
        long time = triggerAndWaitForThreads(threads, latch);

        Log.d(EventBus.TAG, threadCount + " threads posted " + iterations + " events each in " + time + "ms");

        assertEquals("Hello", lastStringEvent);
        int expectedCount = threadCount * iterations;
        assertEquals(expectedCount, countStringEvent.intValue());
        assertEquals(expectedCount, countObjectEvent.intValue());
    }

    private void runThreadsMixedEventType(int threadCount) throws InterruptedException {
        int iterations = COUNT / threadCount / 2;
        eventBus.register(this);

        CountDownLatch latch = new CountDownLatch(2 * threadCount + 1);
        List<PosterThread> threadsString = startThreads(latch, threadCount, iterations, "Hello");
        List<PosterThread> threadsInteger = startThreads(latch, threadCount, iterations, 42);
        List<PosterThread> threads = new ArrayList<PosterThread>();
        threads.addAll(threadsString);
        threads.addAll(threadsInteger);
        long time = triggerAndWaitForThreads(threads, latch);

        Log.d(EventBus.TAG, threadCount * 2 + " mixed threads posted " + iterations + " events each in " + time + "ms");

        assertEquals("Hello", lastStringEvent);
        assertEquals(42, lastIntegerEvent.intValue());

        int expectedCount = threadCount * iterations;
        assertEquals(expectedCount, countStringEvent.intValue());
        assertEquals(expectedCount, countIntegerEvent.intValue());
        assertEquals(expectedCount * 2, countObjectEvent.intValue());
    }

    private long triggerAndWaitForThreads(List<PosterThread> threads, CountDownLatch latch) throws InterruptedException {
        while (latch.getCount() != 1) {
            // Let all threads prepare
            Thread.sleep(1);
        }
        long start = System.currentTimeMillis();
        latch.countDown();
        for (PosterThread thread : threads) {
            thread.join();
        }
        return System.currentTimeMillis() - start;
    }

    private List<PosterThread> startThreads(CountDownLatch latch, int threadCount, int iterations, Object eventToPost)
            throws InterruptedException {
        List<PosterThread> threads = new ArrayList<PosterThread>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            PosterThread thread = new PosterThread(latch, iterations, eventToPost);
            thread.start();
            threads.add(thread);
        }
        return threads;
    }

    public void onEvent(String event) {
        lastStringEvent = event;
        countStringEvent.incrementAndGet();
    }

    public void onEvent(Integer event) {
        lastIntegerEvent = event;
        countIntegerEvent.incrementAndGet();
    }

    public void onEvent(Object event) {
        countObjectEvent.incrementAndGet();
    }

    class PosterThread extends Thread {

        private final CountDownLatch startLatch;
        private final int iterations;
        private final Object eventToPost;

        public PosterThread(CountDownLatch latch, int iterations, Object eventToPost) {
            this.startLatch = latch;
            this.iterations = iterations;
            this.eventToPost = eventToPost;
        }

        @Override
        public void run() {
            startLatch.countDown();
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                Log.w(EventBus.TAG, "Unexpeced interrupt", e);
            }

            for (int i = 0; i < iterations; i++) {
                eventBus.post(eventToPost);
            }
        }
    }

    class SubscribeUnsubscribeThread extends Thread {
        boolean running = true;

        public void shutdown() {
            running = false;
        }

        @Override
        public void run() {
            try {
                while (running) {
                    eventBus.register(this);
                    double random = Math.random();
                    if (random > 0.6d) {
                        Thread.sleep(1);
                    } else if (random > 0.3d) {
                        Thread.yield();
                    }
                    eventBus.unregister(this);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public void onEvent(String event) {
        }

        public void onEvent(Integer event) {
        }

        public void onEvent(Object event) {
        }
    }

}
