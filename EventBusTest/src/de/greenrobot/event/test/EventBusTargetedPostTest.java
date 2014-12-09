package de.greenrobot.event.test;

/**
 * Author: landerlyoung@gmail.com
 * Date:   2014-12-08
 * Time:   17:41
 * Life with passion. Code with creativity!
 */
public class EventBusTargetedPostTest extends EventBusTargetedBasicTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        eventBus.register(s1);
        eventBus.register(s2);
        eventBus.register(s3);
        eventBus.register(s4);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        eventBus.unregister(s1);
        eventBus.unregister(s2);
        eventBus.unregister(s3);
        eventBus.unregister(s4);
    }

    public void testNormalTargetedPostClassName() throws InterruptedException {
        eventResult = 0;
        eventBus.post(event, "de.greenrobot.event.test.EventBusTargetedBasicTest.SubscribeActivity1");
        eventBus.post(new Object());
        assertEquals(SubscribeActivity1.RES, eventResult);
    }

    public void testNormalTargetedPostCanonicalClassName() throws InterruptedException {
        eventResult = 0;
        eventBus.post(event, "de.greenrobot.event.test.EventBusTargetedBasicTest$SubscribeActivity1");
        eventBus.post(new Object());
        assertEquals(SubscribeActivity1.RES, eventResult);
    }

    public void testNormalTargetedPostClazz1() throws InterruptedException {
        eventResult = 0;
        eventBus.post(event, SubscribeActivity1.class);
        eventBus.post(new Object());
        assertEquals(SubscribeActivity1.RES, eventResult);
    }

    public void testNormalTargetedPostClazz2() throws InterruptedException {
        eventResult = 0;
        eventBus.post(event, SubscribeActivity2.class);
        assertEquals(SubscribeActivity2.RES, eventResult);
    }

    public void testNormalTargetedPostClazz3() throws InterruptedException {
        eventResult = 0;
        eventBus.post(event, SubscribeActivity3.class.getName());
        assertEquals(SubscribeActivity3.RES, eventResult);
    }

    public void testNormalTargetedPostClazz4() throws InterruptedException {

        eventResult = 0;
        eventBus.post(event, SubscribeActivity4.class.getCanonicalName());
        assertEquals(SubscribeActivity4.RES, eventResult);
    }

    public void testNormalTargetedPostClazz3and4() throws InterruptedException {

        eventResult = 0;
        eventBus.post(event, SubscribeActivity4.class.getCanonicalName());
        eventBus.post(event, SubscribeActivity3.class);
        assertEquals(SubscribeActivity3.RES | SubscribeActivity4.RES, eventResult);
    }

    public void testNormalTargetedPostNoTarget() throws InterruptedException {
        eventResult = 0;
        eventBus.post(event);
        assertEquals(SubscribeActivity1.RES | SubscribeActivity2.RES |
                        SubscribeActivity3.RES | SubscribeActivity4.RES,
                eventResult);
    }

}
