package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusRegistrationRacingTest;
import org.junit.Before;

/** TODO */
public class EventBusRegistrationRacingTestWithIndex extends EventBusRegistrationRacingTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
