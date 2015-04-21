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
import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusMethodModifiersTest extends AbstractEventBusTest {

    public void testRegisterForEventTypeAndPost() throws InterruptedException {
        eventBus.register(this);
        String event = "Hello";
        eventBus.post(event);
        waitForEventCount(4, 1000);
    }

    public void testIllegalMethodNameThrow() {
        try {
            eventBus.register(new IllegalEventMethodName());
            fail("Illegal name registered");
        } catch (EventBusException ex) {
            // OK, expected
        }
    }

    public void testIllegalMethodNameSkip() {
        eventBus=EventBus.builder().skipMethodVerificationFor(IllegalEventMethodName.class).build();
        eventBus.register(new IllegalEventMethodName());
        eventBus.post(new Object());
    }

    public void onEvent(String event) {
        trackEvent(event);
        assertNotSame(Looper.getMainLooper(), Looper.myLooper());
    }

    public void onEventMainThread(String event) {
        trackEvent(event);
        assertSame(Looper.getMainLooper(), Looper.myLooper());
    }

    public void onEventBackgroundThread(String event) {
        trackEvent(event);
        assertNotSame(Looper.getMainLooper(), Looper.myLooper());
    }

    public void onEventAsync(String event) {
        trackEvent(event);
        assertNotSame(Looper.getMainLooper(), Looper.myLooper());
    }

    public static class IllegalEventMethodName {
        public void onEventIllegalName(Object event) {
            fail("onEventIllegalName got called");
        }

        public void onEvent(IntTestEvent event) {
        }
    }
}
