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

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.greenrobot.eventbusperf.MyEventBusIndex;
import org.greenrobot.eventbusperf.Test;
import org.greenrobot.eventbusperf.TestEvent;
import org.greenrobot.eventbusperf.TestParams;

public abstract class PerfTestEventBus extends Test {

    private final EventBus eventBus;
    private final ArrayList<Object> subscribers;
    private final Class<?> subscriberClass;
    private final int eventCount;
    private final int expectedEventCount;

    public PerfTestEventBus(Context context, TestParams params) {
        super(context, params);
        eventBus = EventBus.builder().eventInheritance(params.isEventInheritance()).addIndex(new MyEventBusIndex())
                .ignoreGeneratedIndex(params.isIgnoreGeneratedIndex()).build();
        subscribers = new ArrayList<Object>();
        eventCount = params.getEventCount();
        expectedEventCount = eventCount * params.getSubscriberCount();
        subscriberClass = getSubscriberClassForThreadMode();
    }

    @Override
    public void prepareTest() {
        try {
            Constructor<?> constructor = subscriberClass.getConstructor(PerfTestEventBus.class);
            for (int i = 0; i < params.getSubscriberCount(); i++) {
                Object subscriber = constructor.newInstance(this);
                subscribers.add(subscriber);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> getSubscriberClassForThreadMode() {
        switch (params.getThreadMode()) {
            case MAIN:
                return SubscribeClassEventBusMain.class;
            case BACKGROUND:
                return SubscribeClassEventBusBackground.class;
            case ASYNC:
                return SubscriberClassEventBusAsync.class;
            case POSTING:
                return SubscribeClassEventBusDefault.class;
            default:
                throw new RuntimeException("Unknown: " + params.getThreadMode());
        }
    }

    private static String getDisplayModifier(TestParams params) {
        String inheritance = params.isEventInheritance() ? "" : ", no event inheritance";
        String ignoreIndex = params.isIgnoreGeneratedIndex() ? ", ignore index" : "";
        return inheritance + ignoreIndex;
    }


    public static class Post extends PerfTestEventBus {
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
            long timeAllReceived = System.nanoTime();

            primaryResultMicros = (timeAfterPosting - timeStart) / 1000;
            primaryResultCount = super.expectedEventCount;
            long deliveredMicros = (timeAllReceived - timeStart) / 1000;
            int deliveryRate = (int) (primaryResultCount / (deliveredMicros / 1000000d));
            otherTestResults = "Post and delivery time: " + deliveredMicros + " micros<br/>" + //
                    "Post and delivery rate: " + deliveryRate + "/s";
        }

        @Override
        public String getDisplayName() {
            return "EventBus Post Events, " + params.getThreadMode() + getDisplayModifier(params);
        }

    }

    public static class RegisterAll extends PerfTestEventBus {
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
            return "EventBus Register, no unregister" + getDisplayModifier(params);
        }
    }

    public static class RegisterOneByOne extends PerfTestEventBus {
        protected Method clearCachesMethod;

        public RegisterOneByOne(Context context, TestParams params) {
            super(context, params);
        }

        public void runTest() {
            long time = 0;
            if (clearCachesMethod == null) {
                // Skip first registration unless just the first registration is tested
                super.registerUnregisterOneSubscribers();
            }
            for (Object subscriber : super.subscribers) {
                if (clearCachesMethod != null) {
                    try {
                        clearCachesMethod.invoke(null);
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
            return "EventBus Register" + getDisplayModifier(params);
        }
    }

    public static class RegisterFirstTime extends RegisterOneByOne {

        public RegisterFirstTime(Context context, TestParams params) {
            super(context, params);
            try {
                Class<?> clazz = Class.forName("org.greenrobot.eventbus.SubscriberMethodFinder");
                clearCachesMethod = clazz.getDeclaredMethod("clearCaches");
                clearCachesMethod.setAccessible(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getDisplayName() {
            return "EventBus Register, first time"+ getDisplayModifier(params);
        }

    }

    public class SubscribeClassEventBusMain {
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(TestEvent event) {
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

    public class SubscribeClassEventBusBackground {
        @Subscribe(threadMode = ThreadMode.BACKGROUND)
        public void onEventBackgroundThread(TestEvent event) {
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

    public class SubscriberClassEventBusAsync {
        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void onEventAsync(TestEvent event) {
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
