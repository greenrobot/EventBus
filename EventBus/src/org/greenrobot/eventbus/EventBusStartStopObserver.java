package org.greenrobot.eventbus;

import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;


public class EventBusStartStopObserver implements LifecycleObserver {

    private final LifecycleOwner lifecycleOwner;
    private final EventBus eventBus;

    public EventBusStartStopObserver(LifecycleOwner lifecycleOwner, EventBus eventBus) {
        this.lifecycleOwner = lifecycleOwner;
        this.eventBus = eventBus;
    }

    @OnLifecycleEvent(Event.ON_START)
    void register() {
        eventBus.register(lifecycleOwner);
    }

    @OnLifecycleEvent(Event.ON_STOP)
    void unregister() {
        eventBus.unregister(lifecycleOwner);
    }

}
