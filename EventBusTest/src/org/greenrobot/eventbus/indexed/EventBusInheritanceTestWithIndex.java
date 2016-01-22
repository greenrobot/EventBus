package org.greenrobot.eventbus.indexed;

import org.greenrobot.eventbus.EventBusInheritanceTest;
import org.junit.Before;

public class EventBusInheritanceTestWithIndex extends EventBusInheritanceTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
