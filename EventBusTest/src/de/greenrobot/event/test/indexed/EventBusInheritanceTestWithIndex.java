package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusInheritanceTest;
import org.junit.Before;

public class EventBusInheritanceTestWithIndex extends EventBusInheritanceTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
