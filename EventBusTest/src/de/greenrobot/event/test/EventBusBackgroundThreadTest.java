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
package de.greenrobot.event.test;

import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusBackgroundThreadTest extends AbstractEventBusTest {

    @Test
    public void testPostInCurrentThread() throws InterruptedException {
        eventBus.register(this);
        eventBus.post("Hello");
        waitForEventCount(1, 1000);

        assertEquals("Hello", lastEvent);
        assertEquals(Thread.currentThread(), lastThread);
    }

    @Test
    public void testPostFromMain() throws InterruptedException {
        eventBus.register(this);
        postInMainThread("Hello");
        waitForEventCount(1, 1000);
        assertEquals("Hello", lastEvent);
        assertFalse(lastThread.equals(Thread.currentThread()));
        assertFalse(lastThread.equals(Looper.getMainLooper().getThread()));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventBackgroundThread(String event) {
        trackEvent(event);
    }

}
