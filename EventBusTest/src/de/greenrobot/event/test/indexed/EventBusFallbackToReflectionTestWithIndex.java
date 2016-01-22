package de.greenrobot.event.test.indexed;

import org.junit.Before;

import de.greenrobot.event.test.EventBusFallbackToReflectionTest;

public class EventBusFallbackToReflectionTestWithIndex extends EventBusFallbackToReflectionTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }

}
