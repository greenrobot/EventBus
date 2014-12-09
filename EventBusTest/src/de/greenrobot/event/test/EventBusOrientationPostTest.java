package de.greenrobot.event.test;

/**
 * Author: taylorcyang
 * Date:   2014-12-08
 * Time:   17:41
 * Life with passion. Code with creativity!
 */
public class EventBusOrientationPostTest extends EventBusOrientationBasicTest {

    public void testNormalOrientationPostClassName() throws InterruptedException {
        eventResult = 0;
        eventBus.post(event, "de.greenrobot.event.test.EventBusOrientationBasicTest.SubscribeActivity1");
        eventBus.post(new Object());
        assertEquals(eventResult, SubscribeActivity1.RES);
    }

    public void testNormalOrientationPostCanonicalClassName() throws InterruptedException {
        eventResult = 0;
        eventBus.post(event, "de.greenrobot.event.test.EventBusOrientationBasicTest$SubscribeActivity1");
        eventBus.post(new Object());
        assertEquals(eventResult, SubscribeActivity1.RES);
    }

    public void testNormalOrientationPostClazz1() throws InterruptedException {
        eventResult = 0;
        eventBus.post(event, SubscribeActivity1.class);
        eventBus.post(new Object());
        assertEquals(eventResult, SubscribeActivity1.RES);
    }

    public void testNormalOrientationPostClazz2() throws InterruptedException {
        eventResult = 0;
        eventBus.post(event, SubscribeActivity2.class);
        assertEquals(eventResult, SubscribeActivity2.RES);
    }

    public void testNormalOrientationPostClazz3() throws InterruptedException {
        eventResult = 0;
        eventBus.post(event, SubscribeActivity3.class.getName());
        assertEquals(eventResult, SubscribeActivity3.RES);
    }

    public void testNormalOrientationPostClazz4() throws InterruptedException {

        eventResult = 0;
        eventBus.post(event, SubscribeActivity4.class.getCanonicalName());
        assertEquals(eventResult, SubscribeActivity4.RES);
    }

    public void testNormalOrientationPostClazz3and4() throws InterruptedException {

        eventResult = 0;
        eventBus.post(event, SubscribeActivity4.class.getCanonicalName());
        eventBus.post(event, SubscribeActivity3.class);
        assertEquals(eventResult, SubscribeActivity3.RES | SubscribeActivity4.RES);
    }

    public void testNormalOrientationPostNoTarget() throws InterruptedException {
        eventResult = 0;
        eventBus.post(event);
        assertEquals(eventResult, SubscribeActivity1.RES | SubscribeActivity2.RES |
                SubscribeActivity3.RES | SubscribeActivity4.RES);
    }

}
