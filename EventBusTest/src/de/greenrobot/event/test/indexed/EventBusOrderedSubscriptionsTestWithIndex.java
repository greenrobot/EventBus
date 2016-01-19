package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusOrderedSubscriptionsTest;
import org.junit.Before;

/** TODO */
public class EventBusOrderedSubscriptionsTestWithIndex extends EventBusOrderedSubscriptionsTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
