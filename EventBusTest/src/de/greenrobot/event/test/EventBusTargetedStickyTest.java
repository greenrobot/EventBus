package de.greenrobot.event.test;

/**
 * Author: taylorcyang
 * Date:   2014-12-09
 * Time:   15:05
 * Life with passion. Code with creativity!
 */
public class EventBusTargetedStickyTest extends EventBusTargetedBasicTest {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testTargetedStickyPostNormal() {
        eventResult = 0;
        eventBus.postSticky(event, SubscribeActivity1.class);
        assertEquals(eventResult, 0);
        eventBus.registerSticky(s1);
        assertEquals(eventResult, SubscribeActivity1.RES);
        eventBus.unregister(s1);
    }
}
