package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusMultithreadedTest;
import org.junit.Before;

public class EventBusMultithreadedTestWithIndex extends EventBusMultithreadedTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
