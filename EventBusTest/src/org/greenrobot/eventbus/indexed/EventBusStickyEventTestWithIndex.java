package org.greenrobot.eventbus.indexed;

import org.greenrobot.eventbus.EventBusStickyEventTest;
import org.junit.Before;

public class EventBusStickyEventTestWithIndex extends EventBusStickyEventTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
