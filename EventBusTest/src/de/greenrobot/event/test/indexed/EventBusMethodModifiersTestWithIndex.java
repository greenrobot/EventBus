package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusMethodModifiersTest;
import org.junit.Before;

public class EventBusMethodModifiersTestWithIndex extends EventBusMethodModifiersTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
