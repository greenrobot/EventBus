package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusNoSubscriberEventTest;
import org.junit.Before;

/** TODO */
public class EventBusNoSubscriberEventTestWithIndex extends EventBusNoSubscriberEventTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
