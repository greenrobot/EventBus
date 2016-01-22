package de.greenrobot.event.test.indexed;

import org.junit.Before;

import de.greenrobot.event.test.EventBusCancelEventDeliveryTest;

public class EventBusCancelEventDeliveryTestWithIndex extends EventBusCancelEventDeliveryTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
