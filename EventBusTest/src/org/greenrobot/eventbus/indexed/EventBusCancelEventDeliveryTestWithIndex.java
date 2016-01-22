package org.greenrobot.eventbus.indexed;

import org.greenrobot.eventbus.EventBusCancelEventDeliveryTest;
import org.junit.Before;

public class EventBusCancelEventDeliveryTestWithIndex extends EventBusCancelEventDeliveryTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
