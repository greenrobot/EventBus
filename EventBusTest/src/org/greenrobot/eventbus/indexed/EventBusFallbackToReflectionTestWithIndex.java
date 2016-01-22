package org.greenrobot.eventbus.indexed;

import org.junit.Before;

import org.greenrobot.eventbus.EventBusFallbackToReflectionTest;

public class EventBusFallbackToReflectionTestWithIndex extends EventBusFallbackToReflectionTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }

}
