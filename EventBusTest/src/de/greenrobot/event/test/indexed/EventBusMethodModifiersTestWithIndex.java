package de.greenrobot.event.test.indexed;

import org.junit.Before;

import de.greenrobot.event.test.EventBusMethodModifiersTest;

public class EventBusMethodModifiersTestWithIndex extends EventBusMethodModifiersTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
