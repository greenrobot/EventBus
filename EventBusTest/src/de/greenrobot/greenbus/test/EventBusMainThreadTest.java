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
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Application;
import android.os.Looper;
import android.test.ApplicationTestCase;
import de.greenrobot.event.EventBus;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusMainThreadTest extends ApplicationTestCase<Application> {

    private EventBus eventBus;

    private final AtomicInteger eventCount = new AtomicInteger();
    private volatile String lastEvent;
    private volatile Thread lastThread;

    private BackgroundPoster backgroundPoster;

    public EventBusMainThreadTest() {
        super(Application.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        eventBus = new EventBus();
        backgroundPoster = new BackgroundPoster();
        backgroundPoster.start();
    }

    @Override
    protected void tearDown() throws Exception {
        backgroundPoster.shutdown();
        backgroundPoster.join();
        super.tearDown();
    }

    public void testTestThreadIsNotMainThread() {
        assertFalse(Looper.getMainLooper().getThread().equals(Thread.currentThread()));
    }

    public void testPost_ThreadModePostThread() throws InterruptedException {
        eventBus.register(this);
        eventBus.post("Hello");
        assertEquals("Hello", lastEvent);
        assertEquals(Thread.currentThread(), lastThread);
    }

    public void testPost_ThreadModeMainThread() throws InterruptedException {
        eventBus.registerForMainThread(this);
        eventBus.post("Hello");
        waitForEventCount(1, 1000);

        assertEquals("Hello", lastEvent);
        assertEquals(Looper.getMainLooper().getThread(), lastThread);
    }

    public void testPostInBackgroundThread_ThreadModeMainThread() throws InterruptedException {
        eventBus.registerForMainThread(this);
        backgroundPoster.post("Hello");
        waitForEventCount(1, 1000);
        assertEquals("Hello", lastEvent);
        assertEquals(Looper.getMainLooper().getThread(), lastThread);
    }

    public void testPostInBackgroundThread_ThreadModePostThread() throws InterruptedException {
        eventBus.register(this);
        backgroundPoster.post("Hello");
        waitForEventCount(1, 1000);
        assertEquals("Hello", lastEvent);
        assertEquals(backgroundPoster, lastThread);
    }

    private void waitForEventCount(int count, int maxMillis) throws InterruptedException {
        for (int i = 0; i < maxMillis; i++) {
            if (eventCount.get() == count) {
                break;
            } else {
                Thread.sleep(1);
            }
        }
        assertEquals(count, eventCount.get());
    }

    public void onEvent(String event) {
        lastEvent = event;
        lastThread = Thread.currentThread();
        // Must the the last one because we wait for this
        eventCount.incrementAndGet();
    }

    class BackgroundPoster extends Thread {
        private boolean running = true;
        private List<Object> eventQ = new ArrayList<Object>();
        private List<Object> eventsDone = new ArrayList<Object>();
        
        public BackgroundPoster() {
            super("BackgroundPoster");
        }

        @Override
        public void run() {
            while (running) {
                Object event = pollEvent();
                if (event != null) {
                    eventBus.post(event);
                    synchronized (eventsDone) {
                        eventsDone.add(event);
                        eventsDone.notifyAll();
                    }
                }
            }
        }

        private synchronized Object pollEvent() {
            Object event = null;
            synchronized (eventQ) {
                if (eventQ.isEmpty()) {
                    try {
                        eventQ.wait();
                    } catch (InterruptedException e) {
                    }
                } else {
                    event = eventQ.remove(0);
                }
            }
            return event;
        }

        void shutdown() {
            running = false;
            synchronized (eventQ) {
                eventQ.notifyAll();
            }
        }

        void post(Object event) {
            synchronized (eventQ) {
                eventQ.add(event);
                eventQ.notifyAll();
            }
            synchronized (eventsDone) {
                while (eventsDone.remove(event)) {
                    try {
                        eventsDone.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

    }

}
