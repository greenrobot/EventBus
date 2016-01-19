package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusSubscriberInJarTest;
import org.junit.Before;

/** TODO */
public class EventBusSubscriberInJarTestWithIndex extends EventBusSubscriberInJarTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
