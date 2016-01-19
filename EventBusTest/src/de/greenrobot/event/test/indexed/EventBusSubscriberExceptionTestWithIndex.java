package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusSubscriberExceptionTest;
import org.junit.Before;

/** TODO */
public class EventBusSubscriberExceptionTestWithIndex extends EventBusSubscriberExceptionTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
