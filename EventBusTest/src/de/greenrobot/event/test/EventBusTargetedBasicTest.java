package de.greenrobot.event.test;

/**
 * Author: landerlyoung@gmail.com
 * Date:   2014-12-09
 * Time:   14:42
 * Life with passion. Code with creativity!
 */
public class EventBusTargetedBasicTest extends AbstractEventBusTest {
    protected int eventResult;
    protected Object s1 = new SubscribeActivity1();
    protected Object s2 = new SubscribeActivity2();
    protected Object s3 = new SubscribeActivity3();
    protected Object s4 = new SubscribeActivity4();
    protected IntTestEvent event = new IntTestEvent(0);

    protected class SubscribeActivity1 {
        public static final int RES = 1;

        public void onEvent(IntTestEvent e) {
            eventResult |= RES;
        }
    }

    protected class SubscribeActivity2 {
        public static final int RES = 1 << 1;

        public void onEvent(IntTestEvent e) {
            eventResult |= RES;
        }

        public void onEvent(String s) {
            eventResult |= RES;
        }
    }

    protected class SubscribeActivity3 {
        public static final int RES = 1 << 2;

        public void onEvent(IntTestEvent e) {
            eventResult |= RES;
        }

        public void onEvent(String s) {
            eventResult |= RES;
        }
    }

    protected class SubscribeActivity4 {
        public static final int RES = 1 << 3;

        public void onEvent(IntTestEvent e) {
            eventResult |= RES;
        }
    }
}
