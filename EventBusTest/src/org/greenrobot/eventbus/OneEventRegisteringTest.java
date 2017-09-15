package org.greenrobot.eventbus;

import org.junit.Test;

/**
 * Created by driesgo on 15/09/17.
 */

public class OneEventRegisteringTest extends AbstractEventBusTest {

    @Test
    public void testRegisterForOneEvent() {
        eventBus.register(this, String.class);
        eventBus.post("Foo");
        eventBus.post(new IntTestEvent(1));
        assertEventCount(1);
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
