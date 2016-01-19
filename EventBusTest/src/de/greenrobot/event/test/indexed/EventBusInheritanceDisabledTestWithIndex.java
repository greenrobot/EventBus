package de.greenrobot.event.test.indexed;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.test.EventBusInheritanceDisabledTest;
import org.greenrobot.eventbus.EventBusTestsIndex;
import org.junit.Before;

public class EventBusInheritanceDisabledTestWithIndex extends EventBusInheritanceDisabledTest {
    @Before
    public void setUp() throws Exception {
        eventBus = EventBus.builder().eventInheritance(false).addIndex(new EventBusTestsIndex()).build();
    }
}
