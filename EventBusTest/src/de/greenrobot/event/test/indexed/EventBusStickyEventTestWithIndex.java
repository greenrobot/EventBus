package de.greenrobot.event.test.indexed;

import org.junit.Before;

import de.greenrobot.event.test.EventBusStickyEventTest;

/** TODO */
public class EventBusStickyEventTestWithIndex extends EventBusStickyEventTest {
    @Before
    public void overwriteEventBus() throws Exception {
        eventBus = Indexed.build();
    }
}
