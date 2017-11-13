package org.greenrobot.eventbus;

import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;


public class EventBusLifecycleObserver implements LifecycleObserver {

    private final LifecycleOwner lifecycleOwner;
    private final EventBus eventBus;

    public EventBusLifecycleObserver(LifecycleOwner lifecycleOwner, EventBus eventBus) {
        this.lifecycleOwner = lifecycleOwner;
        this.eventBus = eventBus;
    }

    @OnLifecycleEvent(Event.ON_START)
    void start() {
        eventBus.register(lifecycleOwner);
    }

    @OnLifecycleEvent(Event.ON_STOP)
    void stop() {
        eventBus.unregister(lifecycleOwner);
    }

}
