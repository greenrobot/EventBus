package org.greenrobot.eventbus;

import org.junit.Test;

/**
 * Created by driesgo on 15/09/17.
 */

public class OneEventUnregisteringTest extends AbstractEventBusTest {

    @Test
    public void testUnregisterForOneEvent() {
        eventBus.register(this);
        eventBus.post("Foo");
        eventBus.post(new IntTestEvent(1));
        assertEventCount(2);
        eventBus.unregister(this, String.class);
        eventBus.post("Bar");
        assertEventCount(2);
        eventBus.post(new IntTestEvent(1));
        assertEventCount(3);
    }

    @Subscribe
    public void onEvent(String event) {
        trackEvent(event);
    }

    @Subscribe
    public void onEvent(IntTestEvent event) {
        trackEvent(event);
    }

}
