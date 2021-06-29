package org.greenrobot.eventbus.android;

import android.os.Looper;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.HandlerPoster;
import org.greenrobot.eventbus.MainThreadSupport;
import org.greenrobot.eventbus.Poster;

public class DefaultAndroidMainThreadSupport implements MainThreadSupport {

    @Override
    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    @Override
    public Poster createPoster(EventBus eventBus) {
        return new HandlerPoster(eventBus, Looper.getMainLooper(), 10);
    }
}
