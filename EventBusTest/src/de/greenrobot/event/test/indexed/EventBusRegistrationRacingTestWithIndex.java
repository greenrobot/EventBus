package de.greenrobot.event.test.indexed;

import org.junit.Before;

import de.greenrobot.event.test.EventBusRegistrationRacingTest;

/** TODO */
public class EventBusRegistrationRacingTestWithIndex extends EventBusRegistrationRacingTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
