package de.greenrobot.eventperf.testsubject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import de.greenrobot.eventperf.Test;
import de.greenrobot.eventperf.TestEvent;
import de.greenrobot.eventperf.TestParams;

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
            long timeStart = System.currentTimeMillis();
            for (int i = 0; i < super.eventCount; i++) {
                super.eventBus.post(new TestEvent());
                if (canceled) {
                    break;
                }
            }
            long timeAfterPosting = System.currentTimeMillis();
            waitForReceivedEventCount(super.expectedEventCount);
            long timeAllReceived = System.currentTimeMillis();

            primaryResultMillis = timeAfterPosting - timeStart;
            primaryResultCount = super.expectedEventCount;
            long deliveredMillis = timeAllReceived - timeStart;
            int deliveryRate = (int) (primaryResultCount / (deliveredMillis / 1000d));
            otherTestResults = "Post and delivery time: " + deliveredMillis + " ms<br/>" + //
                    "Post and delivery rate: " + deliveryRate + "/s";
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
            long timeStart = System.currentTimeMillis();
            super.registerSubscribers();
            long timeEnd = System.currentTimeMillis();

            primaryResultMillis = timeEnd - timeStart;
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
            for (Object subscriber : super.subscribers) {
                if (cacheField != null) {
                    try {
                        cacheField.set(null, new HashMap());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                long beforeRegister = System.nanoTime();
                super.eventBus.register(subscriber);
                long timeRegister = System.nanoTime() - beforeRegister;
                time += timeRegister;
                super.eventBus.unregister(subscriber);
                if (canceled) {
                    return;
                }
            }

            primaryResultMillis = time / 1000000;
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

    private void registerSubscribers() {
        for (Object subscriber : subscribers) {
            eventBus.register(subscriber);
            if (canceled) {
                return;
            }
        }
    }

}
