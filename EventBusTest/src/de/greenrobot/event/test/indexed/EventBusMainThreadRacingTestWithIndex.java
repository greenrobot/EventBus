package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusMainThreadRacingTest;
import org.junit.Before;

public class EventBusMainThreadRacingTestWithIndex extends EventBusMainThreadRacingTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
