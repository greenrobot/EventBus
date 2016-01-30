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

package org.greenrobot.eventbusperf.testsubject;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.greenrobot.eventbusperf.Test;
import org.greenrobot.eventbusperf.TestEvent;
import org.greenrobot.eventbusperf.TestParams;

public abstract class PerfTestOtto extends Test {

    private final Bus eventBus;
    private final ArrayList<Object> subscribers;
    private final Class<?> subscriberClass;
    private final int eventCount;
    private final int expectedEventCount;

    public PerfTestOtto(Context context, TestParams params) {
        super(context, params);
        eventBus = new Bus(ThreadEnforcer.ANY);
        subscribers = new ArrayList<Object>();
        eventCount = params.getEventCount();
        expectedEventCount = eventCount * params.getSubscriberCount();
        subscriberClass = Subscriber.class;
    }

    @Override
    public void prepareTest() {
        Looper.prepare();

        try {
            Constructor<?> constructor = subscriberClass.getConstructor(PerfTestOtto.class);
            for (int i = 0; i < params.getSubscriberCount(); i++) {
                Object subscriber = constructor.newInstance(this);
                subscribers.add(subscriber);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Post extends PerfTestOtto {
        public Post(Context context, TestParams params) {
            super(context, params);
        }

        @Override
        public void prepareTest() {
            super.prepareTest();
            super.registerSubscribers();
        }

        public void runTest() {
            TestEvent event = new TestEvent();
            long timeStart = System.nanoTime();
            for (int i = 0; i < super.eventCount; i++) {
                super.eventBus.post(event);
                if (canceled) {
                    break;
                }
            }
            long timeAfterPosting = System.nanoTime();
            waitForReceivedEventCount(super.expectedEventCount);

            primaryResultMicros = (timeAfterPosting - timeStart) / 1000;
            primaryResultCount = super.expectedEventCount;
        }

        @Override
        public String getDisplayName() {
            return "Otto Post Events";
        }
    }

    public static class RegisterAll extends PerfTestOtto {
        public RegisterAll(Context context, TestParams params) {
            super(context, params);
        }

        public void runTest() {
            super.registerUnregisterOneSubscribers();
            long timeNanos = super.registerSubscribers();
            primaryResultMicros = timeNanos / 1000;
            primaryResultCount = params.getSubscriberCount();
        }

        @Override
        public String getDisplayName() {
            return "Otto Register, no unregister";
        }
    }

    public static class RegisterOneByOne extends PerfTestOtto {
        protected Field cacheField;

        public RegisterOneByOne(Context context, TestParams params) {
            super(context, params);
        }

        @SuppressWarnings("rawtypes")
        public void runTest() {
            long time = 0;
            if (cacheField == null) {
                // Skip first registration unless just the first registration is tested
                super.registerUnregisterOneSubscribers();
            }
            for (Object subscriber : super.subscribers) {
                if (cacheField != null) {
                    try {
                        cacheField.set(null, new ConcurrentHashMap());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                long beforeRegister = System.nanoTime();
                super.eventBus.register(subscriber);

                long afterRegister = System.nanoTime();
                long end = System.nanoTime();
                long timeMeasureOverhead = (end - afterRegister) * 2;
                long timeRegister = end - beforeRegister - timeMeasureOverhead;
                time += timeRegister;
                super.eventBus.unregister(subscriber);
                if (canceled) {
                    return;
                }
            }

            primaryResultMicros = time / 1000;
            primaryResultCount = params.getSubscriberCount();
        }

        @Override
        public String getDisplayName() {
            return "Otto Register";
        }
    }

    public static class RegisterFirstTime extends RegisterOneByOne {

        public RegisterFirstTime(Context context, TestParams params) {
            super(context, params);
            try {
                Class<?> clazz = Class.forName("com.squareup.otto.AnnotatedHandlerFinder");
                cacheField = clazz.getDeclaredField("SUBSCRIBERS_CACHE");
                cacheField.setAccessible(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getDisplayName() {
            return "Otto Register, first time";
        }

    }

    public class Subscriber extends Activity {
        public Subscriber() {
        }

        @Subscribe
        public void onEvent(TestEvent event) {
            eventsReceivedCount.incrementAndGet();
        }

        public void dummy() {
        }

        public void dummy2() {
        }

        public void dummy3() {
        }

        public void dummy4() {
        }

        public void dummy5() {
        }

    }

    private long registerSubscribers() {
        long time = 0;
        for (Object subscriber : subscribers) {
            long timeStart = System.nanoTime();
            eventBus.register(subscriber);
            long timeEnd = System.nanoTime();
            time += timeEnd - timeStart;
            if (canceled) {
                return 0;
            }
        }
        return time;
    }

    private void registerUnregisterOneSubscribers() {
        if (!subscribers.isEmpty()) {
            Object subscriber = subscribers.get(0);
            eventBus.register(subscriber);
            eventBus.unregister(subscriber);
        }
    }

}
