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

import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusInheritanceTest extends TestCase {

    protected EventBus eventBus;

    protected int countMyEventExtended;
    protected int countMyEvent;
    protected int countObjectEvent;
    private int countMyEventInterface;
    private int countMyEventInterfaceExtended;

    protected void setUp() throws Exception {
        super.setUp();
        eventBus = new EventBus();
    }

    @Test
    public void testEventClassHierarchy() {
        eventBus.register(this);

        eventBus.post("Hello");
        assertEquals(1, countObjectEvent);

        eventBus.post(new MyEvent());
        assertEquals(2, countObjectEvent);
        assertEquals(1, countMyEvent);

        eventBus.post(new MyEventExtended());
        assertEquals(3, countObjectEvent);
        assertEquals(2, countMyEvent);
        assertEquals(1, countMyEventExtended);
    }

    @Test
    public void testEventClassHierarchySticky() {
        eventBus.postSticky("Hello");
        eventBus.postSticky(new MyEvent());
        eventBus.postSticky(new MyEventExtended());
        eventBus.register(new StickySubscriber());
        assertEquals(1, countMyEventExtended);
        assertEquals(2, countMyEvent);
        assertEquals(3, countObjectEvent);
    }

    @Test
    public void testEventInterfaceHierarchy() {
        eventBus.register(this);

        eventBus.post(new MyEvent());
        assertEquals(1, countMyEventInterface);

        eventBus.post(new MyEventExtended());
        assertEquals(2, countMyEventInterface);
        assertEquals(1, countMyEventInterfaceExtended);
    }

    @Test
    public void testEventSuperInterfaceHierarchy() {
        eventBus.register(this);

        eventBus.post(new MyEventInterfaceExtended() {
        });
        assertEquals(1, countMyEventInterface);
        assertEquals(1, countMyEventInterfaceExtended);
    }

    @Test
    public void testSubscriberClassHierarchy() {
        SubscriberExtended subscriber = new SubscriberExtended();
        eventBus.register(subscriber);

        eventBus.post("Hello");
        assertEquals(1, subscriber.countObjectEvent);

        eventBus.post(new MyEvent());
        assertEquals(2, subscriber.countObjectEvent);
        assertEquals(0, subscriber.countMyEvent);
        assertEquals(1, subscriber.countMyEventOverwritten);

        eventBus.post(new MyEventExtended());
        assertEquals(3, subscriber.countObjectEvent);
        assertEquals(0, subscriber.countMyEvent);
        assertEquals(1, subscriber.countMyEventExtended);
        assertEquals(2, subscriber.countMyEventOverwritten);
    }

    @Test
    public void testSubscriberClassHierarchyWithoutNewSubscriberMethod() {
        SubscriberExtendedWithoutNewSubscriberMethod subscriber = new SubscriberExtendedWithoutNewSubscriberMethod();
        eventBus.register(subscriber);

        eventBus.post("Hello");
        assertEquals(1, subscriber.countObjectEvent);

        eventBus.post(new MyEvent());
        assertEquals(2, subscriber.countObjectEvent);
        assertEquals(1, subscriber.countMyEvent);

        eventBus.post(new MyEventExtended());
        assertEquals(3, subscriber.countObjectEvent);
        assertEquals(2, subscriber.countMyEvent);
        assertEquals(1, subscriber.countMyEventExtended);
    }

    @Subscribe
    public void onEvent(Object event) {
        countObjectEvent++;
    }

    @Subscribe
    public void onEvent(MyEvent event) {
        countMyEvent++;
    }

    @Subscribe
    public void onEvent(MyEventExtended event) {
        countMyEventExtended++;
    }

    @Subscribe
    public void onEvent(MyEventInterface event) {
        countMyEventInterface++;
    }

    @Subscribe
    public void onEvent(MyEventInterfaceExtended event) {
        countMyEventInterfaceExtended++;
    }

    public static interface MyEventInterface {
    }

    public static class MyEvent implements MyEventInterface {
    }

    public static interface MyEventInterfaceExtended extends MyEventInterface {
    }

    public static class MyEventExtended extends MyEvent implements MyEventInterfaceExtended {
    }

    public static class SubscriberExtended extends EventBusInheritanceTest {
        private int countMyEventOverwritten;

        @Subscribe
        public void onEvent(MyEvent event) {
            countMyEventOverwritten++;
        }
    }

    static class SubscriberExtendedWithoutNewSubscriberMethod extends EventBusInheritanceTest {
    }

    public class StickySubscriber {
        @Subscribe(sticky = true)
        public void onEvent(Object event) {
            countObjectEvent++;
        }

        @Subscribe(sticky = true)
        public void onEvent(MyEvent event) {
            countMyEvent++;
        }

        @Subscribe(sticky = true)
        public void onEvent(MyEventExtended event) {
            countMyEventExtended++;
        }

        @Subscribe(sticky = true)
        public void onEvent(MyEventInterface event) {
            countMyEventInterface++;
        }

        @Subscribe(sticky = true)
        public void onEvent(MyEventInterfaceExtended event) {
            countMyEventInterfaceExtended++;
        }
    }

}
