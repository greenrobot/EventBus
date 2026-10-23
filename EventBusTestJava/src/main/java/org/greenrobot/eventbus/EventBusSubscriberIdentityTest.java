/*
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class EventBusSubscriberIdentityTest {
    @Parameterized.Parameters(name = "index={0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {{false}, {true}});
    }

    private final boolean useIndex;
    private EventBus eventBus;

    public EventBusSubscriberIdentityTest(boolean useIndex) {
        this.useIndex = useIndex;
    }

    @Before
    public void setUp() {
        EventBus.clearCaches();
        EventBusJavaTestsIndex index = new EventBusJavaTestsIndex();
        if (useIndex) {
            assertNotNull(index.getSubscriberInfo(ValueSubscriber.class));
        }
        eventBus = EventBus.builder().addIndex(index).ignoreGeneratedIndex(!useIndex).build();
    }

    @Test
    public void testIsRegisteredUsesIdentity() {
        ValueSubscriber registered = new ValueSubscriber(1);
        ValueSubscriber equal = new ValueSubscriber(1);
        eventBus.register(registered);

        assertTrue(eventBus.isRegistered(registered));
        assertFalse(eventBus.isRegistered(equal));
    }

    @Test
    public void testUnregisterEqualUnregisteredSubscriber() {
        ValueSubscriber registered = new ValueSubscriber(1);
        ValueSubscriber equal = new ValueSubscriber(1);
        eventBus.register(registered);
        eventBus.unregister(equal);

        assertTrue(eventBus.isRegistered(registered));
        eventBus.unregister(registered);
        eventBus.post("after unregister");
        assertEquals(0, registered.count);
    }

    @Test
    public void testEqualSubscribersCanBothUnregister() {
        ValueSubscriber first = new ValueSubscriber(1);
        ValueSubscriber second = new ValueSubscriber(1);
        eventBus.register(first);
        eventBus.register(second);
        eventBus.post("both registered");
        assertEquals(1, first.count);
        assertEquals(1, second.count);

        eventBus.unregister(first);
        eventBus.post("only second registered");
        assertEquals(1, first.count);
        assertEquals(2, second.count);

        eventBus.unregister(second);
        eventBus.post("both unregistered");
        assertEquals(1, first.count);
        assertEquals(2, second.count);
        assertFalse(eventBus.hasSubscriberForEvent(String.class));
    }

    @Test
    public void testChangedHashCodeDoesNotPreventUnregister() {
        ValueSubscriber subscriber = new ValueSubscriber(1);
        eventBus.register(subscriber);
        subscriber.key = 2;
        eventBus.unregister(subscriber);
        eventBus.post("after unregister");

        assertEquals(0, subscriber.count);
        assertFalse(eventBus.hasSubscriberForEvent(String.class));
    }

    @Test(expected = EventBusException.class)
    public void testSameSubscriberCannotRegisterTwice() {
        ValueSubscriber subscriber = new ValueSubscriber(1);
        eventBus.register(subscriber);
        eventBus.register(subscriber);
    }

    public static class ValueSubscriber {
        int key;
        int count;

        ValueSubscriber(int key) {
            this.key = key;
        }

        @Subscribe
        public void onEvent(String event) {
            count++;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ValueSubscriber && key == ((ValueSubscriber) other).key;
        }

        @Override
        public int hashCode() {
            return key;
        }
    }
}
