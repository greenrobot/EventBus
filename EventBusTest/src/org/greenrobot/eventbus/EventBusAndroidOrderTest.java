package org.greenrobot.eventbus;

import android.os.Handler;
import android.os.Looper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class EventBusAndroidOrderTest extends AbstractAndroidEventBusTest {

    private TestBackgroundPoster backgroundPoster;
    private Handler handler;

    @Before
    public void setUp() throws Exception {
        handler = new Handler(Looper.getMainLooper());
        backgroundPoster = new TestBackgroundPoster(eventBus);
        backgroundPoster.start();
    }

    @After
    public void tearDown() throws Exception {
        backgroundPoster.shutdown();
        backgroundPoster.join();
    }

    @Test
    public void backgroundAndMainUnordered() {
        eventBus.register(this);

        handler.post(new Runnable() {
            @Override
            public void run() {
                // post from non-main thread
                backgroundPoster.post("non-main");
                // post from main thread
                eventBus.post("main");
            }
        });

        waitForEventCount(2, 1000);

        // observe that event from *main* thread is posted FIRST
        // NOT in posting order
        assertEquals("non-main", lastEvent);
    }

    @Test
    public void backgroundAndMainOrdered() {
        eventBus.register(this);

        handler.post(new Runnable() {
            @Override
            public void run() {
                // post from non-main thread
                backgroundPoster.post(new OrderedEvent("non-main"));
                // post from main thread
                eventBus.post(new OrderedEvent("main"));
            }
        });

        waitForEventCount(2, 1000);

        // observe that event from *main* thread is posted LAST
        // IN posting order
        assertEquals("main", ((OrderedEvent) lastEvent).thread);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String event) {
        trackEvent(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEvent(OrderedEvent event) {
        trackEvent(event);
    }

    static class OrderedEvent {
        String thread;

        OrderedEvent(String thread) {
            this.thread = thread;
        }
    }

}
