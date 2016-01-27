package org.greenrobot.eventbus.indexed;

import org.junit.Before;

import org.greenrobot.eventbus.EventBusRegistrationRacingTest;

public class EventBusRegistrationRacingTestWithIndex extends EventBusRegistrationRacingTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
