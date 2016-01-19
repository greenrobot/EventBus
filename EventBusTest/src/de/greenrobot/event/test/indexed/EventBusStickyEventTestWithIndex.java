package de.greenrobot.event.test.indexed;

import de.greenrobot.event.test.EventBusStickyEventTest;
import org.junit.Before;

/** TODO */
public class EventBusStickyEventTestWithIndex extends EventBusStickyEventTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
