package org.greenrobot.eventbus.indexed;

import org.junit.Before;

import org.greenrobot.eventbus.EventBusMainThreadTest;

public class EventBusMainThreadTestWithIndex extends EventBusMainThreadTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
