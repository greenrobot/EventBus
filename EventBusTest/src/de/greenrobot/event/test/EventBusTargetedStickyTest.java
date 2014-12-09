package de.greenrobot.event.test;

import de.greenrobot.event.util.TargetedEvent;

/**
 * Author: landerlyoung@gmail.com
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
        //no effect
        assertEquals(0, eventResult);
        eventBus.register(s2);
        //still no effect
        assertEquals(0, eventResult);
        //got the sticky event
        eventBus.registerSticky(s1);
        assertEquals(SubscribeActivity1.RES, eventResult);
        assertEquals(TargetedEvent.obtain(event, SubscribeActivity1.class.getName()),
                eventBus.getTargetedStickyEvent(event.getClass()));
        eventBus.removeStickyEvent(event);

        eventBus.unregister(s1);
        eventBus.unregister(s2);
    }

    public void testTargetedStickyPostConstantRegister() {
        eventResult = 0;
        eventBus.postSticky(event);
        eventBus.registerSticky(s2);
        assertEquals(SubscribeActivity2.RES, eventResult);
        eventBus.registerSticky(s3);
        assertEquals(SubscribeActivity2.RES | SubscribeActivity3.RES, eventResult);
        assertEquals(TargetedEvent.obtain(event, null), eventBus.
                        getTargetedStickyEvent(event.getClass())
        );

        eventBus.unregister(s2);
        eventBus.unregister(s3);
    }

    public void testOverrideStickyEvent() {
        eventBus.postSticky(event, SubscribeActivity1.class);
        eventBus.postSticky("Hello", SubscribeActivity2.class);
        eventBus.postSticky("world", SubscribeActivity3.class);
        eventResult = 0;
        eventBus.registerSticky(s4);
        //no effect
        assertEquals(0, eventResult);
        eventBus.registerSticky(s1);
        assertEquals(SubscribeActivity1.RES, eventResult);
        //no effect because
        //"hello" was overridden by "world"
        assertFalse(eventBus.getTargetedStickyEvent(String.class).target.equals(
                SubscribeActivity2.class.getName()));
        assertFalse(eventBus.getTargetedStickyEvent(String.class).event.equals(
                "Hello"));
        eventBus.registerSticky(s2);
        assertEquals(SubscribeActivity1.RES,
                eventResult);
        eventBus.registerSticky(s3);
        assertEquals(SubscribeActivity1.RES | SubscribeActivity3.RES,
                eventResult);
        eventBus.unregister(s1);
        eventBus.unregister(s2);
        eventBus.unregister(s3);
        eventBus.unregister(s4);

        assertEquals(eventBus.getTargetedStickyEvent(String.class),
                TargetedEvent.obtain("world", SubscribeActivity3.class.getName()));

        assertEquals(eventBus.getTargetedStickyEvent(IntTestEvent.class),
                TargetedEvent.obtain(event, SubscribeActivity1.class.getName()));
    }


}
