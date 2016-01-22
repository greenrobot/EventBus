package de.greenrobot.event.test.indexed;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusTestsIndex;
import org.junit.Before;

import de.greenrobot.event.test.EventBusInheritanceDisabledTest;

public class EventBusInheritanceDisabledTestWithIndex extends EventBusInheritanceDisabledTest {
    @Before
    public void setUp() throws Exception {
        eventBus = EventBus.builder().eventInheritance(false).addIndex(new EventBusTestsIndex()).build();
    }
}
