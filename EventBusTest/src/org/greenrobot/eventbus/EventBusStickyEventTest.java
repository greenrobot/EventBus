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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusStickyEventTest extends AbstractEventBusTest {

    @Test
    public void testPostSticky() throws InterruptedException {
        eventBus.postSticky("Sticky");
        eventBus.register(this);
        assertEquals("Sticky", lastEvent);
        assertEquals(Thread.currentThread(), lastThread);
    }

    @Test
    public void testPostStickyTwoEvents() throws InterruptedException {
        eventBus.postSticky("Sticky");
        eventBus.postSticky(new IntTestEvent(7));
        eventBus.register(this);
        assertEquals(2, eventCount.intValue());
    }

    @Test
    public void testPostStickyTwoSubscribers() throws InterruptedException {
        eventBus.postSticky("Sticky");
        eventBus.postSticky(new IntTestEvent(7));
        eventBus.register(this);
        StickyIntTestSubscriber subscriber2 = new StickyIntTestSubscriber();
        eventBus.register(subscriber2);
        assertEquals(3, eventCount.intValue());

        eventBus.postSticky("Sticky");
        assertEquals(4, eventCount.intValue());

        eventBus.postSticky(new IntTestEvent(8));
        assertEquals(6, eventCount.intValue());
    }

    @Test
    public void testPostStickyRegisterNonSticky() throws InterruptedException {
        eventBus.postSticky("Sticky");
        eventBus.register(new NonStickySubscriber());
        assertNull(lastEvent);
        assertEquals(0, eventCount.intValue());
    }

    @Test
    public void testPostNonStickyRegisterSticky() throws InterruptedException {
        eventBus.post("NonSticky");
        eventBus.register(this);
        assertNull(lastEvent);
        assertEquals(0, eventCount.intValue());
    }

    @Test
    public void testPostStickyTwice() throws InterruptedException {
        eventBus.postSticky("Sticky");
        eventBus.postSticky("NewSticky");
        eventBus.register(this);
        assertEquals("NewSticky", lastEvent);
    }

    @Test
    public void testPostStickyThenPostNormal() throws InterruptedException {
        eventBus.postSticky("Sticky");
        eventBus.post("NonSticky");
        eventBus.register(this);
        assertEquals("Sticky", lastEvent);
    }

    @Test
    public void testPostStickyWithRegisterAndUnregister() throws InterruptedException {
        eventBus.register(this);
        eventBus.postSticky("Sticky");
        assertEquals("Sticky", lastEvent);

        eventBus.unregister(this);
        eventBus.register(this);
        assertEquals("Sticky", lastEvent);
        assertEquals(2, eventCount.intValue());

        eventBus.postSticky("NewSticky");
        assertEquals(3, eventCount.intValue());
        assertEquals("NewSticky", lastEvent);

        eventBus.unregister(this);
        eventBus.register(this);
        assertEquals(4, eventCount.intValue());
        assertEquals("NewSticky", lastEvent);
    }

    @Test
    public void testPostStickyAndGet() throws InterruptedException {
        eventBus.postSticky("Sticky");
        assertEquals("Sticky", eventBus.getStickyEvent(String.class));
    }

    @Test
    public void testPostStickyRemoveClass() throws InterruptedException {
        eventBus.postSticky("Sticky");
        eventBus.removeStickyEvent(String.class);
        assertNull(eventBus.getStickyEvent(String.class));
        eventBus.register(this);
        assertNull(lastEvent);
        assertEquals(0, eventCount.intValue());
    }

    @Test
    public void testPostStickyRemoveEvent() throws InterruptedException {
        eventBus.postSticky("Sticky");
        assertTrue(eventBus.removeStickyEvent("Sticky"));
        assertNull(eventBus.getStickyEvent(String.class));
        eventBus.register(this);
        assertNull(lastEvent);
        assertEquals(0, eventCount.intValue());
    }

    @Test
    public void testPostStickyRemoveAll() throws InterruptedException {
        eventBus.postSticky("Sticky");
        eventBus.postSticky(new IntTestEvent(77));
        eventBus.removeAllStickyEvents();
        assertNull(eventBus.getStickyEvent(String.class));
        assertNull(eventBus.getStickyEvent(IntTestEvent.class));
        eventBus.register(this);
        assertNull(lastEvent);
        assertEquals(0, eventCount.intValue());
    }

    @Test
    public void testRemoveStickyEventInSubscriber() throws InterruptedException {
        eventBus.register(new RemoveStickySubscriber());
        eventBus.postSticky("Sticky");
        eventBus.register(this);
        assertNull(lastEvent);
        assertEquals(0, eventCount.intValue());
        assertNull(eventBus.getStickyEvent(String.class));
    }

    @Subscribe(sticky = true)
    public void onEvent(String event) {
        trackEvent(event);
    }

    @Subscribe(sticky = true)
    public void onEvent(IntTestEvent event) {
        trackEvent(event);
    }

    public class RemoveStickySubscriber {
        @SuppressWarnings("unused")
        @Subscribe(sticky = true)
        public void onEvent(String event) {
            eventBus.removeStickyEvent(event);
        }
    }

    public class NonStickySubscriber {
        @Subscribe
        public void onEvent(String event) {
            trackEvent(event);
        }

        @Subscribe
        public void onEvent(IntTestEvent event) {
            trackEvent(event);
        }
    }

    public class StickyIntTestSubscriber {
        @Subscribe(sticky = true)
        public void onEvent(IntTestEvent event) {
            trackEvent(event);
        }
    }
}
