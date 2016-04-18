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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusInheritanceTest {

    protected EventBus eventBus;
    protected final Subscriber subscriber = new Subscriber();

    @Before
    public void setUp() throws Exception {
        eventBus = new EventBus();
    }

    @Test
    public void testEventClassHierarchy() {
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

    @Test
    public void testEventClassHierarchySticky() {
        eventBus.postSticky("Hello");
        eventBus.postSticky(new MyEvent());
        eventBus.postSticky(new MyEventExtended());
        eventBus.register(new StickySubscriber());
        assertEquals(1, subscriber.countMyEventExtended);
        assertEquals(2, subscriber.countMyEvent);
        assertEquals(3, subscriber.countObjectEvent);
    }

    @Test
    public void testEventInterfaceHierarchy() {
        eventBus.register(subscriber);

        eventBus.post(new MyEvent());
        assertEquals(1, subscriber.countMyEventInterface);

        eventBus.post(new MyEventExtended());
        assertEquals(2, subscriber.countMyEventInterface);
        assertEquals(1, subscriber.countMyEventInterfaceExtended);
    }

    @Test
    public void testEventSuperInterfaceHierarchy() {
        eventBus.register(subscriber);

        eventBus.post(new MyEventInterfaceExtended() {
        });
        assertEquals(1, subscriber.countMyEventInterface);
        assertEquals(1, subscriber.countMyEventInterfaceExtended);
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

    public interface MyEventInterface {
    }

    public static class MyEvent implements MyEventInterface {
    }

    public interface MyEventInterfaceExtended extends MyEventInterface {
    }

    public static class MyEventExtended extends MyEvent implements MyEventInterfaceExtended {
    }
    
    public static class Subscriber {
        int countMyEventExtended;
        int countMyEvent;
        int countObjectEvent;
        int countMyEventInterface;
        int countMyEventInterfaceExtended;
        
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
    }

    public static class SubscriberExtended extends Subscriber {
        private int countMyEventOverwritten;

        @Subscribe
        public void onEvent(MyEvent event) {
            countMyEventOverwritten++;
        }
    }

    static class SubscriberExtendedWithoutNewSubscriberMethod extends Subscriber {
    }

    public class StickySubscriber {
        @Subscribe(sticky = true)
        public void onEvent(Object event) {
            subscriber.countObjectEvent++;
        }

        @Subscribe(sticky = true)
        public void onEvent(MyEvent event) {
            subscriber.countMyEvent++;
        }

        @Subscribe(sticky = true)
        public void onEvent(MyEventExtended event) {
            subscriber.countMyEventExtended++;
        }

        @Subscribe(sticky = true)
        public void onEvent(MyEventInterface event) {
            subscriber.countMyEventInterface++;
        }

        @Subscribe(sticky = true)
        public void onEvent(MyEventInterfaceExtended event) {
            subscriber.countMyEventInterfaceExtended++;
        }
    }

}
