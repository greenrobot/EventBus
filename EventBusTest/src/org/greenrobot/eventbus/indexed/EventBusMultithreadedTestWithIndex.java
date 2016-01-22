package org.greenrobot.eventbus.indexed;

import org.greenrobot.eventbus.EventBusMultithreadedTest;
import org.junit.Before;

public class EventBusMultithreadedTestWithIndex extends EventBusMultithreadedTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
