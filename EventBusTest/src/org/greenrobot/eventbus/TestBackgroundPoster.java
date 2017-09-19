package org.greenrobot.eventbus;

import java.util.ArrayList;
import java.util.List;

public class TestBackgroundPoster extends Thread {
    private final EventBus eventBus;
    volatile boolean running = true;
    private final List<Object> eventQ = new ArrayList<>();
    private final List<Object> eventsDone = new ArrayList<>();

    TestBackgroundPoster(EventBus eventBus) {
        super("BackgroundPoster");
        this.eventBus = eventBus;
    }

    @Override
    public void run() {
        while (running) {
            Object event = pollEvent();
            if (event != null) {
                eventBus.post(event);
                synchronized (eventsDone) {
                    eventsDone.add(event);
                    eventsDone.notifyAll();
                }
            }
        }
    }

    private synchronized Object pollEvent() {
        Object event = null;
        synchronized (eventQ) {
            if (eventQ.isEmpty()) {
                try {
                    eventQ.wait(1000);
                } catch (InterruptedException ignored) {
                }
            }
            if(!eventQ.isEmpty()) {
                event = eventQ.remove(0);
            }
        }
        return event;
    }

    void shutdown() {
        running = false;
        synchronized (eventQ) {
            eventQ.notifyAll();
        }
    }

    void post(Object event) {
        synchronized (eventQ) {
            eventQ.add(event);
            eventQ.notifyAll();
        }
        synchronized (eventsDone) {
            while (!eventsDone.remove(event)) {
                try {
                    eventsDone.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
