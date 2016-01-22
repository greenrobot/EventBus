package org.greenrobot.eventbus.indexed;

import org.junit.Before;

import org.greenrobot.eventbus.EventBusMainThreadRacingTest;

public class EventBusMainThreadRacingTestWithIndex extends EventBusMainThreadRacingTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
