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


/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusStickyEventTest extends AbstractEventBusTest {

    public void testPostSticky() throws InterruptedException {
        eventBus.postSticky("Sticky");
        eventBus.registerSticky(this);
        assertEquals("Sticky", lastEvent);
        assertEquals(Thread.currentThread(), lastThread);
    }

    public void testPostStickyTwice() throws InterruptedException {
        eventBus.postSticky("Sticky");
        eventBus.postSticky("NewSticky");
        eventBus.registerSticky(this);
        assertEquals("NewSticky", lastEvent);
        assertEquals(Thread.currentThread(), lastThread);
    }

    public void testPostStickyWithRegisterAndUnregister() throws InterruptedException {
        eventBus.registerSticky(this);
        eventBus.postSticky("Sticky");
        assertEquals("Sticky", lastEvent);

        eventBus.unregister(this);
        eventBus.registerSticky(this);
        assertEquals("Sticky", lastEvent);
        assertEquals(2, eventCount.intValue());
        
        eventBus.postSticky("NewSticky");
        assertEquals(3, eventCount.intValue());
        assertEquals("NewSticky", lastEvent);
        
        eventBus.unregister(this);
        eventBus.registerSticky(this);
        assertEquals(4, eventCount.intValue());
        assertEquals("NewSticky", lastEvent);
    }

    public void onEvent(String event) {
        trackEvent(event);
    }

}
