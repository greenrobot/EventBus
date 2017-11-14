package org.greenrobot.eventbus.android.lifecycle;

import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import org.greenrobot.eventbus.EventBus;


public class EventBusCreateDestroyObserver implements LifecycleObserver {

    private final LifecycleOwner lifecycleOwner;
    private final EventBus eventBus;

    public EventBusCreateDestroyObserver(LifecycleOwner lifecycleOwner, EventBus eventBus) {
        this.lifecycleOwner = lifecycleOwner;
        this.eventBus = eventBus;
    }

    @OnLifecycleEvent(Event.ON_CREATE)
    void register() {
        eventBus.register(lifecycleOwner);
    }

    @OnLifecycleEvent(Event.ON_DESTROY)
    void unregister() {
        eventBus.unregister(lifecycleOwner);
    }

}
