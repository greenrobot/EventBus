package org.greenrobot.eventbus.indexed;

import org.greenrobot.eventbus.EventBusMethodModifiersTest;
import org.junit.Before;

public class EventBusMethodModifiersTestWithIndex extends EventBusMethodModifiersTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
