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

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class EventBusActivityPerformanceTest {
    protected EventBus eventBus;

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

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

    public static class TestActivity extends Activity {
        public String lastStringEvent;

        @Subscribe
        public void onEvent(String event) {
            lastStringEvent = event;
        }
    }
}
