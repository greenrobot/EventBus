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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.greenrobot.eventbus.EventBus;
import org.junit.Test;

import org.greenrobot.eventbus.SubscriberInJar;

public class EventBusSubscriberInJarTest extends TestCase {
    protected EventBus eventBus = EventBus.builder().build();

    @Test
    public void testSubscriberInJar() {
        SubscriberInJar subscriber = new SubscriberInJar();
        eventBus.register(subscriber);
        eventBus.post("Hi Jar");
        eventBus.post(42);
        Assert.assertEquals(1, subscriber.getCollectedStrings().size());
        Assert.assertEquals("Hi Jar", subscriber.getCollectedStrings().get(0));
    }
}
