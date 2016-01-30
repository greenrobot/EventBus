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

import android.app.Activity;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author Markus Junginger, greenrobot
 */
@RunWith(AndroidJUnit4.class)
public class EventBusBasicTest {

    public static class WithIndex extends EventBusBasicTest {
        @Test
        public void dummy() {}

    }

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    protected EventBus eventBus;
    private String lastStringEvent;
    private int countStringEvent;
    private int countIntEvent;
    private int lastIntEvent;
    private int countMyEventExtended;
    private int countMyEvent;
    private int countMyEvent2;

    @Before
    public void setUp() throws Exception {
        eventBus = new EventBus();
    }

    @Test
    @UiThreadTest
    public void testRegisterAndPost() {
        // Use an activity to test real life performance
        TestActivity testActivity = new TestActivity();
        String event = "Hello";

        long start = System.currentTimeMillis();
        eventBus.register(testActivity);
        long time = System.currentTimeMillis() - start;
        Log.d(EventBus.TAG, "Registered in " + time + "ms");

        eventBus.post(event);

        assertEquals(event, testActivity.lastStringEvent);
    }

    @Test
    public void testPostWithoutSubscriber() {
        eventBus.post("Hello");
    }

    @Test
    public void testUnregisterWithoutRegister() {
        // Results in a warning without throwing
        eventBus.unregister(this);
    }

    // This will throw "out of memory" if subscribers are leaked
    @Test
    public void testUnregisterNotLeaking() {
        int heapMBytes = (int) (Runtime.getRuntime().maxMemory() / (1024L * 1024L));
        for (int i = 0; i < heapMBytes * 2; i++) {
            EventBusBasicTest subscriber = new EventBusBasicTest() {
                byte[] expensiveObject = new byte[1024 * 1024];
            };
            eventBus.register(subscriber);
            eventBus.unregister(subscriber);
            Log.d("Test", "Iteration " + i + " / max heap: " + heapMBytes);
        }
    }

    @Test
    public void testRegisterTwice() {
        eventBus.register(this);
        try {
            eventBus.register(this);
            fail("Did not throw");
        } catch (RuntimeException expected) {
            // OK
        }
    }

    @Test
    public void testIsRegistered() {
        assertFalse(eventBus.isRegistered(this));
        eventBus.register(this);
        assertTrue(eventBus.isRegistered(this));
        eventBus.unregister(this);
        assertFalse(eventBus.isRegistered(this));
    }

    @Test
    public void testPostWithTwoSubscriber() {
        EventBusBasicTest test2 = new EventBusBasicTest();
        eventBus.register(this);
        eventBus.register(test2);
        String event = "Hello";
        eventBus.post(event);
        assertEquals(event, lastStringEvent);
        assertEquals(event, test2.lastStringEvent);
    }

    @Test
    public void testPostMultipleTimes() {
        eventBus.register(this);
        MyEvent event = new MyEvent();
        int count = 1000;
        long start = System.currentTimeMillis();
        // Debug.startMethodTracing("testPostMultipleTimes" + count);
        for (int i = 0; i < count; i++) {
            eventBus.post(event);
        }
        // Debug.stopMethodTracing();
        long time = System.currentTimeMillis() - start;
        Log.d(EventBus.TAG, "Posted " + count + " events in " + time + "ms");
        assertEquals(count, countMyEvent);
    }

    @Test
    public void testMultipleSubscribeMethodsForEvent() {
        eventBus.register(this);
        MyEvent event = new MyEvent();
        eventBus.post(event);
        assertEquals(1, countMyEvent);
        assertEquals(1, countMyEvent2);
    }

    @Test
    public void testPostAfterUnregister() {
        eventBus.register(this);
        eventBus.unregister(this);
        eventBus.post("Hello");
        assertNull(lastStringEvent);
    }

    @Test
    public void testRegisterAndPostTwoTypes() {
        eventBus.register(this);
        eventBus.post(42);
        eventBus.post("Hello");
        assertEquals(1, countIntEvent);
        assertEquals(1, countStringEvent);
        assertEquals(42, lastIntEvent);
        assertEquals("Hello", lastStringEvent);
    }

    @Test
    public void testRegisterUnregisterAndPostTwoTypes() {
        eventBus.register(this);
        eventBus.unregister(this);
        eventBus.post(42);
        eventBus.post("Hello");
        assertEquals(0, countIntEvent);
        assertEquals(0, lastIntEvent);
        assertEquals(0, countStringEvent);
    }

    @Test
    public void testPostOnDifferentEventBus() {
        eventBus.register(this);
        new EventBus().post("Hello");
        assertEquals(0, countStringEvent);
    }

    @Test
    public void testPostInEventHandler() {
        RepostInteger reposter = new RepostInteger();
        eventBus.register(reposter);
        eventBus.register(this);
        eventBus.post(1);
        assertEquals(10, countIntEvent);
        assertEquals(10, lastIntEvent);
        assertEquals(10, reposter.countEvent);
        assertEquals(10, reposter.lastEvent);
    }

    @Test
    public void testHasSubscriberForEvent() {
        assertFalse(eventBus.hasSubscriberForEvent(String.class));

        eventBus.register(this);
        assertTrue(eventBus.hasSubscriberForEvent(String.class));

        eventBus.unregister(this);
        assertFalse(eventBus.hasSubscriberForEvent(String.class));
    }

    @Test
    public void testHasSubscriberForEventSuperclass() {
        assertFalse(eventBus.hasSubscriberForEvent(String.class));

        Object subscriber = new ObjectSubscriber();
        eventBus.register(subscriber);
        assertTrue(eventBus.hasSubscriberForEvent(String.class));

        eventBus.unregister(subscriber);
        assertFalse(eventBus.hasSubscriberForEvent(String.class));
    }

    @Test
    public void testHasSubscriberForEventImplementedInterface() {
        assertFalse(eventBus.hasSubscriberForEvent(String.class));

        Object subscriber = new CharSequenceSubscriber();
        eventBus.register(subscriber);
        assertTrue(eventBus.hasSubscriberForEvent(CharSequence.class));
        assertTrue(eventBus.hasSubscriberForEvent(String.class));

        eventBus.unregister(subscriber);
        assertFalse(eventBus.hasSubscriberForEvent(CharSequence.class));
        assertFalse(eventBus.hasSubscriberForEvent(String.class));
    }

    @Subscribe
    public void onEvent(String event) {
        lastStringEvent = event;
        countStringEvent++;
    }

    @Subscribe
    public void onEvent(Integer event) {
        lastIntEvent = event;
        countIntEvent++;
    }

    @Subscribe
    public void onEvent(MyEvent event) {
        countMyEvent++;
    }

    @Subscribe
    public void onEvent2(MyEvent event) {
        countMyEvent2++;
    }

    @Subscribe
    public void onEvent(MyEventExtended event) {
        countMyEventExtended++;
    }

    public static class TestActivity extends Activity {
        public String lastStringEvent;

        @Subscribe
        public void onEvent(String event) {
            lastStringEvent = event;
        }
    }

    public static class CharSequenceSubscriber {
        @Subscribe
        public void onEvent(CharSequence event) {
        }
    }

    public static class ObjectSubscriber {
        @Subscribe
        public void onEvent(Object event) {
        }
    }

    public class MyEvent {
    }

    public class MyEventExtended extends MyEvent {
    }

    public class RepostInteger {
        public int lastEvent;
        public int countEvent;

        @Subscribe
        public void onEvent(Integer event) {
            lastEvent = event;
            countEvent++;
            assertEquals(countEvent, event.intValue());

            if (event < 10) {
                int countIntEventBefore = countEvent;
                eventBus.post(event + 1);
                // All our post calls will just enqueue the event, so check count is unchanged
                assertEquals(countIntEventBefore, countIntEventBefore);
            }
        }
    }

}
