package de.greenrobot.event.test.indexed;

import org.junit.Before;

import de.greenrobot.event.test.EventBusSubscriberExceptionTest;

/** TODO */
public class EventBusSubscriberExceptionTestWithIndex extends EventBusSubscriberExceptionTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
