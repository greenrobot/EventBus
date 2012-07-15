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

    private int countMyEventExtended;
    private int countMyEvent;

    protected void setUp() throws Exception {
        super.setUp();
        eventBus = new EventBus();
    }

    public void testEventClassHierarchy() {
        eventBus.register(this);
        eventBus.post(new MyEvent());
        assertEquals(1, countMyEvent);
        eventBus.post(new MyEventExtended());
        assertEquals(2, countMyEvent);
        assertEquals(1, countMyEventExtended);
    }

    public void onEvent(MyEvent event) {
        countMyEvent++;
    }

    public void onEvent(MyEventExtended event) {
        countMyEventExtended++;
    }

    class MyEvent {
    }

    class MyEventExtended extends MyEvent {
    }

}
