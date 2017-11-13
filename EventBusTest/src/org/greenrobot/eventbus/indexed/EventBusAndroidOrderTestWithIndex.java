package org.greenrobot.eventbus.indexed;

import org.greenrobot.eventbus.EventBusAndroidOrderTest;

public class EventBusAndroidOrderTestWithIndex extends EventBusAndroidOrderTest {

    @Override
    public void setUp() throws Exception {
        eventBus = Indexed.build();
        super.setUp();
    }

}
