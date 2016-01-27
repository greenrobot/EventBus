package org.greenrobot.eventbus.indexed;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusSubscriberInJarTest;
import org.greenrobot.eventbus.InJarIndex;
import org.junit.Before;

public class EventBusSubscriberInJarTestWithIndex extends EventBusSubscriberInJarTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = EventBus.builder().addIndex(new InJarIndex()).build();
    }
}
