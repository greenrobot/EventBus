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
package de.greenrobot.greenbus.test;

import junit.framework.TestCase;
import de.greenrobot.event.EventBus;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusInheritanceTest extends TestCase {

    private EventBus eventBus;

    protected int countMyEventExtended;
    protected int countMyEvent;
    protected int countObjectEvent;

    protected void setUp() throws Exception {
        super.setUp();
        eventBus = new EventBus();
    }

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

    public void onEvent(Object event) {
        countObjectEvent++;
    }

    public void onEvent(MyEvent event) {
        countMyEvent++;
    }

    public void onEvent(MyEventExtended event) {
        countMyEventExtended++;
    }

    static class MyEvent {
    }

    static class MyEventExtended extends MyEvent {
    }
    
    static class SubscriberExtended extends EventBusInheritanceTest {
        private int countMyEventOverwritten;

        public void onEvent(MyEvent event) {
            countMyEventOverwritten++;
        }
    }

}
