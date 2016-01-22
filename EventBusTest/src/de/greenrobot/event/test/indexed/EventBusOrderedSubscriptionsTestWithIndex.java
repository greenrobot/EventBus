package de.greenrobot.event.test.indexed;

import org.junit.Before;

import de.greenrobot.event.test.EventBusOrderedSubscriptionsTest;

/** TODO */
public class EventBusOrderedSubscriptionsTestWithIndex extends EventBusOrderedSubscriptionsTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
