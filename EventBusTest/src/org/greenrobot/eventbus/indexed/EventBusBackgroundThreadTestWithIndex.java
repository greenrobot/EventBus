package org.greenrobot.eventbus.indexed;

import org.greenrobot.eventbus.EventBusBackgroundThreadTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class EventBusBackgroundThreadTestWithIndex extends EventBusBackgroundThreadTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }

    @Test
    public void testIndex() {
        assertTrue(eventBus.toString().contains("indexCount=1"));
    }
}
