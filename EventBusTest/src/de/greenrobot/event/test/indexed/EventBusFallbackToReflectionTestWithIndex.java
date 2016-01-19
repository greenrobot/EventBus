package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusFallbackToReflectionTest;
import org.junit.Before;

public class EventBusFallbackToReflectionTestWithIndex extends EventBusFallbackToReflectionTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }

}
