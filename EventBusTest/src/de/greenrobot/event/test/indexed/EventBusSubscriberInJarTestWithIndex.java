package de.greenrobot.event.test.indexed;

import org.junit.Before;

import de.greenrobot.event.test.EventBusSubscriberInJarTest;

/** TODO */
public class EventBusSubscriberInJarTestWithIndex extends EventBusSubscriberInJarTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
