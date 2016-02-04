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

import android.os.Handler;
import android.os.Looper;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusMainThreadRacingTest extends AbstractEventBusTest {

    private static final int ITERATIONS = LONG_TESTS ? 100000 : 1000;

    protected boolean unregistered;
    private CountDownLatch startLatch;
    private volatile RuntimeException failed;

    @Test
    public void testRacingThreads() throws InterruptedException {
        Runnable register = new Runnable() {
            @Override
            public void run() {
                eventBus.register(EventBusMainThreadRacingTest.this);
                unregistered = false;
            }
        };

        Runnable unregister = new Runnable() {
            @Override
            public void run() {
                eventBus.unregister(EventBusMainThreadRacingTest.this);
                unregistered = true;
            }
        };

        startLatch = new CountDownLatch(2);
        BackgroundPoster backgroundPoster = new BackgroundPoster();
        backgroundPoster.start();
        try {
            Handler handler = new Handler(Looper.getMainLooper());
            Random random = new Random();
            countDownAndAwaitLatch(startLatch, 10);
            for (int i = 0; i < ITERATIONS; i++) {
                handler.post(register);
                Thread.sleep(0, random.nextInt(300)); // Sleep just some nanoseconds, timing is crucial here
                handler.post(unregister);
                if (failed != null) {
                    throw new RuntimeException("Failed in iteration " + i, failed);
                }
                // Don't let the queue grow to avoid out-of-memory scenarios
                waitForHandler(handler);
            }
        } finally {
            backgroundPoster.running = false;
            backgroundPoster.join();
        }
    }

    protected void waitForHandler(Handler handler) {
        final CountDownLatch doneLatch = new CountDownLatch(1);
        handler.post(new Runnable() {

            @Override
            public void run() {
                doneLatch.countDown();
            }
        });
        awaitLatch(doneLatch, 10);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(String event) {
        trackEvent(event);
        if (unregistered) {
            failed = new RuntimeException("Main thread event delivered while unregistered on received event #"
                    + eventCount);
        }
    }

    class BackgroundPoster extends Thread {
        volatile boolean running = true;

        public BackgroundPoster() {
            super("BackgroundPoster");
        }

        @Override
        public void run() {
            countDownAndAwaitLatch(startLatch, 10);
            while (running) {
                eventBus.post("Posted in background");
                if (Math.random() > 0.9f) {
                    // Single cores would take very long without yielding
                    Thread.yield();
                }
            }
        }

    }

}
