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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Emir Rahman Muhammadzadeh
 */
public class EventBusPostByIdTest extends AbstractAndroidEventBusTest {
    @Test
    public void testPostById() throws InterruptedException {

        Object obj1 = new Object() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            public void onEvent(String event) {
                assertEquals("hello First Object", event);
            }
        };

        Object obj2 = new Object() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            public void onEvent(String event) {
                assertEquals("hello Second Object", event);
            }
        };

        Object obj3 = new Object() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            public void onEvent(String event) {
                assertEquals("hello Third Object", event);
            }
        };

        eventBus.register(obj1, 1);
        eventBus.register(obj2, 2);
        eventBus.register(obj3, 3);


        eventBus.post("hello First Object", 1);
        eventBus.post("hello Second Object", 2);
        eventBus.post("hello Third Object", 3);
    }

    @Test
    public void testPostPublic() throws InterruptedException {

        Object obj1 = new Object() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            public void onEvent(String event) {
                assertEquals("hello everybody", event);
            }
        };

        Object obj2 = new Object() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            public void onEvent(String event) {
                assertEquals("hello everybody", event);
            }
        };

        Object obj3 = new Object() {
            @Subscribe(threadMode = ThreadMode.MAIN)
            public void onEvent(String event) {
                assertEquals("hello everybody", event);
            }
        };

        eventBus.register(obj1, 1);
        eventBus.register(obj2, 2);
        eventBus.register(obj3, 3);

        eventBus.post("hello everybody");

    }

}
