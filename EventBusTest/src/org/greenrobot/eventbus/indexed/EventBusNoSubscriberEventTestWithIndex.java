package org.greenrobot.eventbus.indexed;

import org.greenrobot.eventbus.EventBusNoSubscriberEventTest;
import org.junit.Before;

/** TODO */
public class EventBusNoSubscriberEventTestWithIndex extends EventBusNoSubscriberEventTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
