package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusCancelEventDeliveryTest;
import org.junit.Before;

public class EventBusCancelEventDeliveryTestWithIndex extends EventBusCancelEventDeliveryTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
