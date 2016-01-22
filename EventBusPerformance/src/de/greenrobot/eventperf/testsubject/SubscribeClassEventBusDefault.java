package de.greenrobot.eventperf.testsubject;

import org.greenrobot.eventbus.Subscribe;

import de.greenrobot.eventperf.TestEvent;

public class SubscribeClassEventBusDefault {
    private PerfTestEventBus perfTestEventBus;

    public SubscribeClassEventBusDefault(PerfTestEventBus perfTestEventBus) {
        this.perfTestEventBus = perfTestEventBus;
    }

    @Subscribe
    public void onEvent(TestEvent event) {
        perfTestEventBus.eventsReceivedCount.incrementAndGet();
    }

    public void dummy() {
    }

    public void dummy2() {
    }

    public void dummy3() {
    }

    public void dummy4() {
    }

    public void dummy5() {
    }
}
