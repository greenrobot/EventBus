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

import static org.junit.Assert.assertEquals;

/**
 * @author Markus Junginger, greenrobot
 */
public class EventBusSubscriberLegalTest extends AbstractEventBusTest {

    @Test
    public void testSubscriberLegal() {
        eventBus.register(this);
        eventBus.post("42");
        eventBus.unregister(this);
        assertEquals(1, eventCount.intValue());
    }

    // With build time verification, some of these tests are obsolete (and cause problems during build)
//    public void testSubscriberNotPublic() {
//        try {
//            eventBus.register(new NotPublic());
//            fail("Registration of ilegal subscriber successful");
//        } catch (EventBusException e) {
//            // Expected
//        }
//    }

//    public void testSubscriberStatic() {
//        try {
//            eventBus.register(new Static());
//            fail("Registration of ilegal subscriber successful");
//        } catch (EventBusException e) {
//            // Expected
//        }
//    }

    public void testSubscriberLegalAbstract() {
        eventBus.register(new AbstractImpl());

        eventBus.post("42");
        assertEquals(1, eventCount.intValue());
    }

    @Subscribe
    public void onEvent(String event) {
        trackEvent(event);
    }

//    public static class NotPublic {
//        @Subscribe
//        void onEvent(String event) {
//        }
//    }

    public static abstract class Abstract {
        @Subscribe
        public abstract void onEvent(String event);
    }

    public class AbstractImpl extends Abstract {

        @Override
        @Subscribe
        public void onEvent(String event) {
            trackEvent(event);
        }

    }

//    public static class Static {
//        @Subscribe
//        public static void onEvent(String event) {
//        }
//    }

}
