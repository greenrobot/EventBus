package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusMainThreadTest;
import org.junit.Before;

public class EventBusMainThreadTestWithIndex extends EventBusMainThreadTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
