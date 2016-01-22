package de.greenrobot.event.test.indexed;

import org.junit.Before;

import de.greenrobot.event.test.EventBusMultithreadedTest;

public class EventBusMultithreadedTestWithIndex extends EventBusMultithreadedTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
