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

public class EventBusGenericsTest extends AbstractEventBusTest {
    public static class GenericEvent<T> {
        T value;
    }

    public class GenericEventSubscriber<T> {
        @Subscribe
        public void onGenericEvent(GenericEvent<T> event) {
            trackEvent(event);
        }
    }

    public class FullGenericEventSubscriber<T> {
        @Subscribe
        public void onGenericEvent(T event) {
            trackEvent(event);
        }
    }

    public class GenericNumberEventSubscriber<T extends Number> {
        @Subscribe
        public void onGenericEvent(T event) {
            trackEvent(event);
        }
    }

    public class GenericFloatEventSubscriber extends GenericNumberEventSubscriber<Float> {
    }

    @Test
    public void testGenericEventAndSubscriber() {
        GenericEventSubscriber<IntTestEvent> genericSubscriber = new GenericEventSubscriber<IntTestEvent>();
        eventBus.register(genericSubscriber);
        eventBus.post(new GenericEvent<Integer>());
        assertEventCount(1);
    }

    @Test
    public void testGenericEventAndSubscriber_TypeErasure() {
        FullGenericEventSubscriber<IntTestEvent> genericSubscriber = new FullGenericEventSubscriber<IntTestEvent>();
        eventBus.register(genericSubscriber);
        eventBus.post(new IntTestEvent(42));
        eventBus.post("Type erasure!");
        assertEventCount(2);
    }

    @Test
    public void testGenericEventAndSubscriber_BaseType() {
        GenericNumberEventSubscriber<Float> genericSubscriber = new GenericNumberEventSubscriber<>();
        eventBus.register(genericSubscriber);
        eventBus.post(new Float(42));
        eventBus.post(new Double(23));
        assertEventCount(2);
        eventBus.post("Not the same base type");
        assertEventCount(2);
    }

    @Test
    public void testGenericEventAndSubscriber_Subclass() {
        GenericFloatEventSubscriber genericSubscriber = new GenericFloatEventSubscriber();
        eventBus.register(genericSubscriber);
        eventBus.post(new Float(42));
        eventBus.post(new Double(77));
        assertEventCount(2);
        eventBus.post("Not the same base type");
        assertEventCount(2);
    }
}
