package de.greenrobot.eventperf.testsubject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import de.greenrobot.eventperf.Test;
import de.greenrobot.eventperf.TestEvent;
import de.greenrobot.eventperf.TestParams;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class PerfTestLocalBroadcast extends Test {


    private final ArrayList<SubscriberWrapper> subscribers;
    private final Class<?> subscriberClass;
    private final int eventCount;
    private final int expectedEventCount;
    public static final String action = "de.greenrobot.eventperf.LOCAL_BROADCAST";
    public static final String key_value = "key_value";

    private class SubscriberWrapper {
        public    Subscriber receiver;
        public IntentFilter filter;
    }

    public PerfTestLocalBroadcast(Context context, TestParams params) {
        super(context, params);
        subscribers = new ArrayList<SubscriberWrapper>();
        eventCount = params.getEventCount();
        expectedEventCount = eventCount * params.getSubscriberCount();
        subscriberClass = Subscriber.class;
    }

    @Override
    public void prepareTest() {
//        Looper.prepare();

        try {
            for (int i = 0; i < params.getSubscriberCount(); i++) {
                SubscriberWrapper subscriberWrap = new SubscriberWrapper();
                subscriberWrap.receiver = new Subscriber();
                subscriberWrap.filter = new IntentFilter(action);
                subscribers.add(subscriberWrap);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Post extends PerfTestLocalBroadcast {
        public Post(Context context, TestParams params) {
            super(context, params);
        }

        @Override
        public void prepareTest() {
            super.prepareTest();
            super.registerSubscribers();
        }

        public void runTest() {
            long timeStart = System.nanoTime();
            Intent intent;
            for (int i = 0; i < super.eventCount; i++) {
                intent = new Intent(action);
                intent.putExtra(key_value, 1);
                LocalBroadcastManager.getInstance(super.context).sendBroadcast(intent);
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
            return "Local Broadcast Post Events";
        }
    }

    public static class RegisterAll extends PerfTestLocalBroadcast {
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
            return "Local Broadcast Register, no unregister";
        }
    }

    public static class RegisterOneByOne extends PerfTestLocalBroadcast {
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
            for (SubscriberWrapper subscriber : super.subscribers) {
                if (cacheField != null) {
                    try {
                        cacheField.set(null, new HashMap());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                long beforeRegister = System.nanoTime();
                LocalBroadcastManager.getInstance(super.context).registerReceiver(subscriber.receiver, subscriber.filter);
                long timeRegister = System.nanoTime() - beforeRegister;
                time += timeRegister;
                LocalBroadcastManager.getInstance(super.context).unregisterReceiver(subscriber.receiver);
                if (canceled) {
                    return;
                }
            }

            primaryResultMicros = time / 1000;
            primaryResultCount = params.getSubscriberCount();
        }

        @Override
        public String getDisplayName() {
            return "Local Broadcast Register";
        }
    }

    public static class RegisterFirstTime extends RegisterOneByOne {

        public RegisterFirstTime(Context context, TestParams params) {
            super(context, params);
        }

        @Override
        public String getDisplayName() {
            return "Local Broadcast, first time";
        }

    }

    public class Subscriber extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(action.equals(intent.getAction())) {
                TestEvent event = new TestEvent();
                event.value = intent.getIntExtra(key_value, 0);
                onEvent(event);
            }
        }

        public void onEvent(TestEvent event) {
            eventsReceivedCount.addAndGet(event.value);
        }
    }

    private long registerSubscribers() {
        long time = 0;
        for (SubscriberWrapper subscriber : subscribers) {
            long timeStart = System.nanoTime();
            LocalBroadcastManager.getInstance(super.context).registerReceiver(subscriber.receiver, subscriber.filter);
//            eventBus.register(subscriber);
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
            SubscriberWrapper subscriberWrapper = subscribers.get(0);
            LocalBroadcastManager.getInstance(context).registerReceiver(subscriberWrapper.receiver, subscriberWrapper.filter);
            LocalBroadcastManager.getInstance(context).unregisterReceiver(subscriberWrapper.receiver);
        }
    }

}
