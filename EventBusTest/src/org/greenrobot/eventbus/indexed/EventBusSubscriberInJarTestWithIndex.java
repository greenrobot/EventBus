package org.greenrobot.eventbus.indexed;

import org.greenrobot.eventbus.EventBusSubscriberInJarTest;
import org.junit.Before;

/** TODO */
public class EventBusSubscriberInJarTestWithIndex extends EventBusSubscriberInJarTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
