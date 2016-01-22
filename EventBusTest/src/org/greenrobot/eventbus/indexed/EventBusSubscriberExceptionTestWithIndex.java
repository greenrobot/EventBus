package org.greenrobot.eventbus.indexed;

import org.junit.Before;

import org.greenrobot.eventbus.EventBusSubscriberExceptionTest;

/** TODO */
public class EventBusSubscriberExceptionTestWithIndex extends EventBusSubscriberExceptionTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
