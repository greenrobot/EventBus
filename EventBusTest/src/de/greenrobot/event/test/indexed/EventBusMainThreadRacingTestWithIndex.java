package de.greenrobot.event.test.indexed;

import org.junit.Before;

import de.greenrobot.event.test.EventBusMainThreadRacingTest;

public class EventBusMainThreadRacingTestWithIndex extends EventBusMainThreadRacingTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
