package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusGenericsTest;
import org.junit.Before;

public class EventBusGenericsTestWithIndex extends EventBusGenericsTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
