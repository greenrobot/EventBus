package de.greenrobot.event.test.indexed;

import org.junit.Before;

import de.greenrobot.event.test.EventBusNoSubscriberEventTest;

/** TODO */
public class EventBusNoSubscriberEventTestWithIndex extends EventBusNoSubscriberEventTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
