package de.greenrobot.eventperf;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import android.content.Context;
import de.greenrobot.event.EventBus;

public abstract class TestEventBus extends Test {

    private final EventBus eventBus;
    private final ArrayList<Object> subscribers;
    Class<?> subscriberClass;
    private final int iterations;
    private int expectedEventCount;

    public TestEventBus(Context context, TestParams params) {
        super(context, params);
        eventBus = new EventBus();
        subscribers = new ArrayList<Object>();
        iterations = params.getIterations();
        expectedEventCount = iterations * params.getSubscriberCount();
    }

    @Override
    public void prepareTest() {
        subscriberClass = getSubscriberClassForThreadMode();
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
            long timeBeforePosting = System.currentTimeMillis();
            for (int i = 0; i < super.iterations; i++) {
                super.eventBus.post(new TestEvent());
                if (canceled) {
                    break;
                }
            }
            long timeAfterPosting = System.currentTimeMillis();
            waitForReceivedEventCount(super.expectedEventCount);
            long timeAllReceived = System.currentTimeMillis();

            long t1 = timeAfterPosting - timeBeforePosting;
            long t2 = timeAllReceived - timeBeforePosting;
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
        public void onEventAsyncThread(TestEvent event) {
            eventsReceivedCount.incrementAndGet();
        }
    }

    @Override
    public String getDisplayName() {
        return "EventBus";
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
