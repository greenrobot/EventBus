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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class EventBusMultithreadedTest extends AbstractEventBusTest {

    static final int COUNT = LONG_TESTS ? 100000 : 1000;

    final AtomicInteger countStringEvent = new AtomicInteger();
    final AtomicInteger countIntegerEvent = new AtomicInteger();
    final AtomicInteger countObjectEvent = new AtomicInteger();
    final AtomicInteger countIntTestEvent = new AtomicInteger();

    String lastStringEvent;
    Integer lastIntegerEvent;

    IntTestEvent lastIntTestEvent;

    @Test
    public void testPost01Thread() throws InterruptedException {
        runThreadsSingleEventType(1);
    }

    @Test
    public void testPost04Threads() throws InterruptedException {
        runThreadsSingleEventType(4);
    }

    @Test
    public void testPost40Threads() throws InterruptedException {
        runThreadsSingleEventType(40);
    }

    @Test
    public void testPostMixedEventType01Thread() throws InterruptedException {
        runThreadsMixedEventType(1);
    }

    @Test
    public void testPostMixedEventType04Threads() throws InterruptedException {
        runThreadsMixedEventType(4);
    }

    @Test
    public void testPostMixedEventType40Threads() throws InterruptedException {
        runThreadsMixedEventType(40);
    }

    private void runThreadsSingleEventType(int threadCount) throws InterruptedException {
        int iterations = COUNT / threadCount;
        eventBus.register(this);

        CountDownLatch latch = new CountDownLatch(threadCount + 1);
        List<PosterThread> threads = startThreads(latch, threadCount, iterations, "Hello");
        long time = triggerAndWaitForThreads(threads, latch);

        log(threadCount + " threads posted " + iterations + " events each in " + time + "ms");

        waitForEventCount(COUNT * 2, 5000);

        assertEquals("Hello", lastStringEvent);
        int expectedCount = threadCount * iterations;
        assertEquals(expectedCount, countStringEvent.intValue());
        assertEquals(expectedCount, countObjectEvent.intValue());
    }

    private void runThreadsMixedEventType(int threadCount) throws InterruptedException {
        runThreadsMixedEventType(COUNT, threadCount);
    }

    void runThreadsMixedEventType(int count, int threadCount) throws InterruptedException {
        eventBus.register(this);
        int eventTypeCount = 3;
        int iterations = count / threadCount / eventTypeCount;

        CountDownLatch latch = new CountDownLatch(eventTypeCount * threadCount + 1);
        List<PosterThread> threadsString = startThreads(latch, threadCount, iterations, "Hello");
        List<PosterThread> threadsInteger = startThreads(latch, threadCount, iterations, 42);
        List<PosterThread> threadsIntTestEvent = startThreads(latch, threadCount, iterations, new IntTestEvent(7));

        List<PosterThread> threads = new ArrayList<PosterThread>();
        threads.addAll(threadsString);
        threads.addAll(threadsInteger);
        threads.addAll(threadsIntTestEvent);
        long time = triggerAndWaitForThreads(threads, latch);

        log(threadCount * eventTypeCount + " mixed threads posted " + iterations + " events each in "
                + time + "ms");

        int expectedCountEach = threadCount * iterations;
        int expectedCountTotal = expectedCountEach * eventTypeCount * 2;
        waitForEventCount(expectedCountTotal, 5000);

        assertEquals("Hello", lastStringEvent);
        assertEquals(42, lastIntegerEvent.intValue());
        assertEquals(7, lastIntTestEvent.value);

        assertEquals(expectedCountEach, countStringEvent.intValue());
        assertEquals(expectedCountEach, countIntegerEvent.intValue());
        assertEquals(expectedCountEach, countIntTestEvent.intValue());

        assertEquals(expectedCountEach * eventTypeCount, countObjectEvent.intValue());
    }

    private long triggerAndWaitForThreads(List<PosterThread> threads, CountDownLatch latch) throws InterruptedException {
        while (latch.getCount() != 1) {
            // Let all other threads prepare and ensure this one is the last 
            Thread.sleep(1);
        }
        long start = System.currentTimeMillis();
        latch.countDown();
        for (PosterThread thread : threads) {
            thread.join();
        }
        return System.currentTimeMillis() - start;
    }

    private List<PosterThread> startThreads(CountDownLatch latch, int threadCount, int iterations, Object eventToPost) {
        List<PosterThread> threads = new ArrayList<PosterThread>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            PosterThread thread = new PosterThread(latch, iterations, eventToPost);
            thread.start();
            threads.add(thread);
        }
        return threads;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventBackgroundThread(String event) {
        lastStringEvent = event;
        countStringEvent.incrementAndGet();
        trackEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Integer event) {
        lastIntegerEvent = event;
        countIntegerEvent.incrementAndGet();
        trackEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventAsync(IntTestEvent event) {
        countIntTestEvent.incrementAndGet();
        lastIntTestEvent = event;
        trackEvent(event);
    }

    @Subscribe
    public void onEvent(Object event) {
        countObjectEvent.incrementAndGet();
        trackEvent(event);
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
                log("Unexpected interrupt", e);
            }

            for (int i = 0; i < iterations; i++) {
                eventBus.post(eventToPost);
            }
        }
    }

}
