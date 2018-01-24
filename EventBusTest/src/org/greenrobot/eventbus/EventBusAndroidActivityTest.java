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
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

/**
 * @author Markus Junginger, greenrobot
 */
// Do not extend from AbstractAndroidEventBusTest, because it asserts test may not be in main thread
public class EventBusAndroidActivityTest extends AbstractEventBusTest {

    public static class WithIndex extends EventBusBasicTest {
        @Test
        public void dummy() {
        }

    }

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

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
