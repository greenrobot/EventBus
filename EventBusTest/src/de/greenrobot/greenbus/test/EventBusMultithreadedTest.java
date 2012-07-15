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

    public static final int COUNT = 2000;

    private EventBus eventBus;
    private String lastStringEvent;
    private final AtomicInteger countStringEvent = new AtomicInteger();
    private final AtomicInteger countIntegerEvent = new AtomicInteger();
    private final AtomicInteger countObjectEvent = new AtomicInteger();
    private Integer lastIntegerEvent;
    private Object lastObjectEvent;

    protected void setUp() throws Exception {
        super.setUp();
        eventBus = new EventBus();
    }

    public void test01PosterThreads() throws InterruptedException {
        runThreadsSingleEventType(1);
    }

    public void test02PosterThreads() throws InterruptedException {
        runThreadsSingleEventType(2);
    }

    public void test04PosterThreads() throws InterruptedException {
        runThreadsSingleEventType(4);
    }

    public void test40PosterThreads() throws InterruptedException {
        runThreadsSingleEventType(40);
    }

    private void runThreadsSingleEventType(int threadCount) throws InterruptedException {
        runThreadsSingleEventType(threadCount, COUNT / threadCount, "Hello");
    }

    private void runThreadsSingleEventType(int threadCount, int iterations, String eventToPost)
            throws InterruptedException {
        eventBus.register(this);

        CountDownLatch latch = new CountDownLatch(threadCount + 1);
        List<PosterThread> threads = startThreads(latch, threadCount, iterations, eventToPost);
        long time = triggerAndWaitForThreads(threads, latch);

        Log.d(EventBus.TAG, threadCount + " threads posted " + iterations + " events each in " + time + "ms");

        assertEquals(lastStringEvent, eventToPost);
        int expectedCount = threadCount * iterations;
        assertEquals(expectedCount, countStringEvent.intValue());
        assertEquals(expectedCount, countObjectEvent.intValue());
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
        long time = System.currentTimeMillis() - start;
        return time;
    }

    private List<PosterThread> startThreads(CountDownLatch latch, int threadCount, int iterations, String eventToPost)
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
        lastObjectEvent = event;
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

}
