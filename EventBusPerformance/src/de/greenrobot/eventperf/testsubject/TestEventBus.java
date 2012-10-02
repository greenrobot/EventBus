package de.greenrobot.eventperf.testsubject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.content.Context;
import android.os.SystemClock;
import de.greenrobot.event.EventBus;
import de.greenrobot.eventperf.Test;
import de.greenrobot.eventperf.TestEvent;
import de.greenrobot.eventperf.TestParams;

public abstract class TestEventBus extends Test {

    private final EventBus eventBus;
    private final ArrayList<Object> subscribers;
    private final Class<?> subscriberClass;
    private final int eventCount;
    private final int expectedEventCount;

    public TestEventBus(Context context, TestParams params) {
        super(context, params);
        eventBus = new EventBus();
        subscribers = new ArrayList<Object>();
        eventCount = params.getEventCount();
        expectedEventCount = eventCount * params.getSubscriberCount();
        subscriberClass = getSubscriberClassForThreadMode();
    }

    @Override
    public void prepareTest() {
        try {
            Constructor<?> constructor = subscriberClass.getConstructor(TestEventBus.class);
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
        case MainThread:
            return SubscribeClassEventBusMain.class;
        case BackgroundThread:
            return SubscribeClassEventBusBackground.class;
        case Async:
            return SubscriberClassEventBusAsync.class;
        case PostThread:
            return SubscribeClassEventBusDefault.class;
        default:
            throw new RuntimeException("Unknown: " + params.getThreadMode());
        }
    }

    public static class Post extends TestEventBus {
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
            return "EventBus Post Events, " + params.getThreadMode();
        }
    }

    public static class RegisterAll extends TestEventBus {
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
            return "EventBus Register, no unregister";
        }
    }

    public static class RegisterOneByOne extends TestEventBus {
        protected Method clearCachesMethod;

        public RegisterOneByOne(Context context, TestParams params) {
            super(context, params);
        }

        public void runTest() {
            long time = 0;
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
            return "EventBus Register";
        }
    }

    public static class RegisterFirstTime extends RegisterOneByOne {

        public RegisterFirstTime(Context context, TestParams params) {
            super(context, params);
            try {
                Class<?> clazz = Class.forName("de.greenrobot.event.SubscriberMethodFinder");
                clearCachesMethod = clazz.getDeclaredMethod("clearCaches");
                clearCachesMethod.setAccessible(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getDisplayName() {
            return "EventBus Register, first time";
        }

    }

    public class SubscribeClassEventBusDefault {
        public void onEvent(TestEvent event) {
            eventsReceivedCount.incrementAndGet();
        }
    }

    public class SubscribeClassEventBusMain {
        public void onEventMainThread(TestEvent event) {
            eventsReceivedCount.incrementAndGet();
        }
    }

    public class SubscribeClassEventBusBackground {
        public void onEventBackgroundThread(TestEvent event) {
            eventsReceivedCount.incrementAndGet();
        }
    }

    public class SubscriberClassEventBusAsync {
        public void onEventAsync(TestEvent event) {
            eventsReceivedCount.incrementAndGet();
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
