package org.greenrobot.eventbus.indexed;

import org.junit.Before;

import org.greenrobot.eventbus.EventBusGenericsTest;

public class EventBusGenericsTestWithIndex extends EventBusGenericsTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
